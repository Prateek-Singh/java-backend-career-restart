# Java Backend Career Restart

Hands-on Java backend refresh project focused on rebuilding current implementation depth and senior-level interview readiness through practical coding, testing, debugging, system design, SQL, DSA, and reliability work.

## Current Project Stack

- Java 21
- Spring Boot
- Spring Web / REST
- Spring Data JPA
- MySQL
- Flyway
- H2 for focused JPA integration testing
- Spring Security
- Spring Cache
- Redis
- Spring Kafka
- Apache Kafka
- Docker / Docker Compose
- Testcontainers
- JUnit 5
- Mockito
- Maven

## Current API Domain

The project currently centers on transaction APIs with:

- create transaction,
- retrieve transaction by ID,
- retrieve transactions by account ID,
- validation and structured error responses,
- duplicate transaction protection,
- persistence through Spring Data JPA,
- cache-aside transaction lookup,
- authentication-protected transaction endpoints,
- transaction-created event publishing to Kafka.

## Persistence and Database

Implemented:

- Spring Data JPA repository layer,
- MySQL persistence,
- profile-based persistence configuration,
- Flyway migrations,
- H2-backed integration testing,
- `Instant` timestamp persistence,
- duplicate business-ID handling,
- API `409 Conflict` behavior,
- database uniqueness as the final concurrency guard,
- service-level transaction boundaries and read-only intent where appropriate.

## Docker and Local Infrastructure

Docker Compose currently supports the infrastructure needed for hands-on local development, including MySQL, Redis, and Kafka.

The application has also been exercised with a multi-stage Docker build.

Integration tests that need Redis or Kafka use Testcontainers so a normal `mvn clean test` does not require manually starting either broker/cache service.

Docker itself must be available for those Testcontainers tests.

## Spring Security

Current security milestone:

- `/transactions/**` requires authentication,
- stateless HTTP Basic authentication,
- BCrypt password encoding,
- in-memory USER / ADMIN identities for the current learning milestone,
- authentication and authorization fundamentals,
- explicit 401 behavior tests,
- controller integration coverage.

Deliberately deferred:

- JWT,
- persisted users,
- account/resource ownership authorization,
- service-to-service authentication.

These will be added only when the underlying domain and security contract justify them.

## Redis / Spring Cache

First cache target:

`GET /transactions/{transactionId}`

Cache contract:

- MySQL remains the source of truth.
- Spring Cache provides cache-aside behavior.
- Cache hit returns Redis data without querying MySQL.
- Cache miss queries MySQL and caches a successful result.
- Missing transactions are not negative-cached initially.
- Cache TTL is 30 minutes.
- Cache keys use a transaction namespace.
- Cache values use JSON serialization.
- Redis failures fail open to MySQL for reads.
- Cache GET/PUT/EVICT/CLEAR failures are logged rather than propagated.

Reliability lesson:

A cache outage may preserve correctness while still causing a real production incident through increased latency, DB traffic, connection pressure, and saturation.

### Redis Testing

Coverage includes:

- pure service unit tests,
- Spring cache-proxy behavior,
- cache error-handler tests,
- real Redis serialization/cache-hit integration behavior,
- Testcontainers-based Redis lifecycle.

A manually running Redis instance is not required for the full Maven test suite.

## Kafka Transaction Events

The first Kafka milestone is implemented.

### Topic

`transaction-events`

Local learning setup:

- 3 partitions
- replication factor 1 on a single local broker

Replication factor 1 is intentionally local-only and is not the production HA design.

### Event

`TransactionCreatedEvent`

The event currently carries:

- `eventId`
- `eventType`
- `eventTimestamp`
- `transactionId`
- `accountId`
- `amount`
- `transactionType`
- `transactionCreatedAt`

`eventId` is deliberately separate from the business `transactionId`.

### Partition Key

Kafka records are published with:

`transactionId` as the record key.

This preserves ordering for events belonging to the same transaction while allowing different transactions to be distributed across partitions and processed in parallel.

Kafka ordering is partition-local, not global across the topic.

### Producer Flow

Current learning implementation:

`validate -> save transaction -> build TransactionCreatedEvent -> publish to Kafka -> return saved transaction`

`TransactionEventPublisher` uses:

`KafkaTemplate<String, TransactionCreatedEvent>`

with:

- topic: `transaction-events`
- key: transaction ID
- payload: JSON `TransactionCreatedEvent`

### Known Dual-Write Limitation

The current direct-publish milestone intentionally exposes an important distributed-systems problem:

1. MySQL commit succeeds.
2. Kafka publish fails.
3. The transaction exists but downstream consumers may never receive the event.

The stronger future design is a transactional outbox:

- persist business row and outbox row in the same DB transaction,
- asynchronously publish pending outbox events,
- mark them published after successful broker send.

Even with an outbox, duplicate publication is possible if Kafka accepts an event and the publisher crashes before recording publication success. Future consumers therefore need idempotent processing, ideally based on a stable `eventId`.

## Kafka Testing

### Publisher Unit Test

Mocks `KafkaTemplate<String, TransactionCreatedEvent>` and verifies the expected topic, key, and payload are passed to KafkaTemplate.

### Transaction Service Unit Test

Captures the generated `TransactionCreatedEvent` and verifies its important event and business fields.

Duplicate transaction creation verifies that neither persistence nor event publication proceeds incorrectly.

### Controller Integration Test

The Kafka publisher is replaced with `@MockitoBean` so the controller integration suite remains focused on HTTP/security/service/repository behavior.

### Real Kafka Integration Test

`TransactionEventPublisherIntegrationTest` uses Testcontainers to start a real Kafka broker.

It verifies the complete round trip:

`TransactionEventPublisher -> KafkaTemplate -> JsonSerializer -> Kafka -> consumer -> JsonDeserializer -> TransactionCreatedEvent`

The test confirms:

- a record reaches Kafka,
- the Kafka key is the expected transaction ID,
- the deserialized event matches the event that was published.

The test also exposed and corrected a real serializer configuration issue that mocked tests could not detect.

A manually started Kafka broker is not required for `mvn clean test`.

## Messaging Concepts Reinforced

### Kafka

Current understanding covers:

- topics,
- partitions,
- partition-local ordering,
- keys,
- consumer groups,
- offsets,
- consumer lag,
- rebalancing,
- retention and replay,
- at-least-once delivery,
- duplicate processing,
- idempotent consumers,
- bounded retries,
- dead-letter topics,
- poison-message ordering trade-offs.

### RabbitMQ

Refreshed:

- exchanges,
- queues,
- bindings,
- routing keys,
- acknowledgements,
- DLX / DLQ,
- competing consumers.

### RabbitMQ vs Kafka

General selection principle:

Choose RabbitMQ when queue-oriented delivery, acknowledgements, commands/tasks, and routing through exchanges/bindings are the primary requirements.

Choose Kafka when durable retained event streams, replay, partition-based parallelism, and multiple independent consumer groups are the primary requirements.

The choice should be driven by delivery semantics, retention/replay, routing, ordering scope, and consumer independence rather than product popularity.

## DSA Progress

Current practice is attempt-first and completed inside the study session.

Before coding each problem, explicitly state:

- pattern,
- why it fits,
- state,
- invariant,
- invalid condition,
- repair rule,
- answer-update point,
- edge cases.

Recent sliding-window work includes:

- longest substring without repeating characters,
- at most two distinct characters,
- longest repeating character replacement,
- Max Consecutive Ones III.

Current reinforcement area:

Translate a recognized pattern into precise state transitions, especially when shrinking or repairing a window.

## SQL Progress

Recent reinforcement includes:

- conditional aggregation,
- `GROUP BY`,
- `HAVING`,
- date-range filtering,
- top-N queries,
- deterministic ordering,
- window functions.

Window-function distinctions retained:

- `ROW_NUMBER()` - unique sequential number,
- `RANK()` - ties share rank and leave gaps,
- `DENSE_RANK()` - ties share rank without gaps.

## System Design and Reliability

Current active concepts include:

- database transaction boundaries,
- race-safe uniqueness,
- idempotency,
- retry and acknowledgement behavior,
- cache-aside,
- TTL and invalidation,
- negative caching,
- cache stampede,
- hot keys,
- graceful degradation,
- broker delivery semantics,
- partition ordering,
- consumer groups and lag,
- DB/broker dual-write failure,
- transactional outbox,
- idempotent consumers,
- retry / dead-letter handling.

System design is treated as a daily track and connected directly to implementation work.

## Testing Strategy

The project deliberately separates test responsibilities:

- unit tests for mapping/business logic and collaborator interaction,
- Spring-focused tests when framework proxies or configuration matter,
- controller integration tests for HTTP/security/persistence behavior,
- real infrastructure integration tests for Redis/Kafka serialization and communication,
- Testcontainers to keep infrastructure tests self-contained.

## Current Roadmap

Completed major milestones:

1. Core Java / collections / streams / exception refresh.
2. Spring Boot REST API foundation.
3. Validation and structured exception handling.
4. JPA + MySQL.
5. Flyway + H2 integration testing.
6. Docker / Docker Compose foundation.
7. Duplicate handling + DB uniqueness + Spring transaction boundaries.
8. Spring Security fundamentals.
9. Redis / Spring Cache with graceful database fallback.
10. Self-contained Redis integration testing with Testcontainers.
11. Kafka fundamentals, RabbitMQ comparison, transaction-event producer, and real Kafka integration testing.

Next areas will continue incrementally rather than adding technologies only for checklist value. Likely follow-up work includes:

- Kafka consumer-side reliability and idempotency,
- transactional outbox,
- event retry/dead-letter implementation,
- schema evolution,
- AWS hands-on refresh/deployment,
- observability,
- Kubernetes / CI-CD refresh,
- backend-focused AI when justified.

## Build and Test

Run:

```bash
mvn clean test
```

Redis and Kafka integration tests use Testcontainers. Docker must be running, but manually started Redis/Kafka containers are not required.

## Working Principle

Technology is added only when there is:

- a credible use case,
- a working implementation,
- focused tests,
- clear failure-mode reasoning,
- an interview-ready explanation of the trade-offs.

The project is intended to demonstrate verified current hands-on work, not inflate professional experience claims.
