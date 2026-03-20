package ua.vk.ucu.ddb.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/counter/neo4j")
@RequiredArgsConstructor
public class Neo4jCounterController {

    private final Driver driver;

    @PostConstruct
    public void init() {
        log.info("Verifying Neo4j connectivity...");
        driver.verifyConnectivity(); // blocks until the driver confirms a live connection, throws if it cannot
        log.info("Neo4j is ready. Initializing counter node...");

        try (Session session = driver.session()) {
            session.run("""
                    MERGE (c:Counter {name: $name})
                    ON CREATE SET c.value = 0
                    """,
                    Values.parameters("name", "global"));
        }

        log.info("Counter node ready.");
    }

    /**
     * Atomically increments the counter using Neo4j's single-writer lock on the node.
     * SET on a single node is serialized by Neo4j — no lost updates under concurrent load.
     */
    @PostMapping
    public void increment() {
        try (Session session = driver.session()) {
            session.run("""
                    MATCH (c:Counter {name: $name})
                    SET c.value = c.value + 1
                    """,
                    Values.parameters("name", "global"));
        }
    }

    @GetMapping
    public long count() {
        try (Session session = driver.session()) {
            return session.run("""
                            MATCH (c:Counter {name: $name})
                            RETURN c.value AS value
                            """,
                            Values.parameters("name", "global"))
                    .single()
                    .get("value")
                    .asLong();
        }
    }

    @DeleteMapping
    public void reset() {
        try (Session session = driver.session()) {
            session.run("""
                    MATCH (c:Counter {name: $name})
                    SET c.value = 0
                    """,
                    Values.parameters("name", "global"));
        }
    }
}