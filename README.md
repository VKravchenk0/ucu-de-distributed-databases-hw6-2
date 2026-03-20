# Distributed databases homework 6. Neo4j. Частина 2 - counter
Використовується клієнт з домашньої роботи №1

## 1. Попередні вимоги
- Встановлено Docker

## 2. Підготовка
### Запуск бек-енду
```bash
cd server
docker compose up --build
```

### Збираємо клієнт
```bash
cd client
./mvnw clean package
```

### Запуск клієнту
```bash
$ java -jar target/hw1-client-0.0.1-SNAPSHOT.jar <counter-type> <parallel-clients> <requests-per-client>
```
`counter-type` - тип лічильника. В поточному завданні може приймати тільки значення `neo4j`

### Завдання
```bash
java -jar target/hw1-client-0.0.1-SNAPSHOT.jar neo4j 10 10000
```


### Результати виконання
| Тип лічильника                        | Кількість клієнтів | Кількість запитів на клієнт   | Кінцеве значення  | Час виконання, сек    | Пропускна здатність, запитів/сек    |
| ------------                          | ---------          | -------------                 | -------------     | -------------         | -------------                       | 
| neo4j             | 10                  | 10000                         | 100000             | 183.134                   | 546.05                             |