package ua.vk.ucu.ddb.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.time.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {

    private static final String COUNTER_URL_TEMPLATE = "http://localhost:8080/counter/%s";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        log.info("Client start");
        String counterType = String.valueOf(args[0]);
        int parallelClients = Integer.parseInt(args[1]);
        int requestsPerClient = Integer.parseInt(args[2]);

        URI counterUri = getCounterUri(counterType);
        resetCounter(counterUri);

        log.info("==============================");
        log.info("Counter client execution start");
        log.info("Counter type: {}", counterType);
        log.info("Number of parallel clients: {}", parallelClients);
        log.info("Requests per client: {}", requestsPerClient);

        StopWatch watch = StopWatch.createStarted();
        
        runLoadTest(parallelClients, requestsPerClient, counterUri);

        watch.stop();
        Duration duration = watch.getDuration();
        double elapsedSeconds = duration.toNanos() / 1_000_000_000.0;

        int totalRequests = parallelClients * requestsPerClient;

        Long counterValue = getCounterValue(counterUri);
        assert counterValue == totalRequests;

        String elapsedSecondsStr = String.format("%.3f", elapsedSeconds);
        String throughputStr = String.format("%.2f", totalRequests / elapsedSeconds);
        log.info("Total requests: {}", totalRequests);
        log.info("Resulting counter value: {}", counterValue);
        log.info("Elapsed time: {} sec", elapsedSecondsStr);
        log.info("Throughput: {} req/sec", throughputStr);
        log.info("| {}             | {}                  | {}                         | {}             | {}                   | {}                             | ",
            counterType, parallelClients, requestsPerClient, counterValue, elapsedSecondsStr, throughputStr
        );

    }
    
    private static void runLoadTest(int parallelClients, int requestsPerClient, URI counterUri) 
            throws InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
        .uri(counterUri)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
        
        try (ExecutorService executor =
            Executors.newVirtualThreadPerTaskExecutor()) {
                
                List<Callable<Void>> tasks = new ArrayList<>();
                
                for (int i = 0; i < parallelClients; i++) {
                    tasks.add(() -> {
                        for (int j = 0; j < requestsPerClient; j++) {
                            client.send(request, BodyHandlers.discarding());
                        }
                        return null;
                    });
                }
                
                executor.invokeAll(tasks);
            }
    }

    private static Long getCounterValue(URI counterUri) throws IOException, InterruptedException {
        HttpRequest countRequest = HttpRequest.newBuilder()
                .uri(counterUri)
                .GET()
                .build();
        HttpResponse<String> countResponse = client.send(
                countRequest,
                BodyHandlers.ofString()
        );
        Long counterValue = Long.valueOf(countResponse.body());
        return counterValue;
    }

    private static void resetCounter(URI counterUri) throws IOException, InterruptedException {
        log.info("Resetting counter");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(counterUri)
            .DELETE()
            .build();
        client.send(request, BodyHandlers.discarding());
    }
    
    private static URI getCounterUri(String counterType) {
        String counterUrl = COUNTER_URL_TEMPLATE.formatted(counterType);
        return URI.create(counterUrl);
    }

}