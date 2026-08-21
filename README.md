# Java Backend Career Restart

This repository documents my structured return to hands-on Java backend engineering.

It contains progressive exercises and a Spring Boot transaction API used to rebuild practical confidence in Java, testing, data structures and algorithms, SQL, REST API development, persistence, database migrations, and backend design.

## Current Focus

- Core Java and modern Java practices
- Collections, Streams, Optional, equality, and immutability
- Exception handling and domain-specific exceptions
- JUnit 5 and Mockito
- Spring Boot REST APIs
- Jakarta Bean Validation
- MockMvc controller-slice testing
- Spring Boot integration testing
- Spring Data JPA
- Spring Security
- Spring Cache
- Spring Data Redis
- Redis
- Spring Kafka
- Apache Kafka
- Testcontainers
- BCrypt password hashing
- Relational persistence with MySQL
- Flyway database migrations
- Data structures and algorithms
- SQL aggregation, joins, subqueries, and window functions
- Backend layering and repository design
- Idempotency and transaction-processing design
- System design and interview preparation
- Spring Security fundamentals
- Stateless REST authentication
- Authentication and authorization testing
- Redis caching and cache-aside design
- Cache resilience and graceful degradation
- Kafka event publishing and partition-key design
- Kafka integration testing with Testcontainers
- Database/broker consistency and transactional-outbox design

## Tech Stack Used in This Repository

- Java 21
- Maven
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- Spring Cache
- Spring Data Redis
- Redis
- Spring Kafka
- Apache Kafka
- Testcontainers
- BCrypt password hashing
- Jakarta Bean Validation
- MySQL
- H2
- Flyway
- Docker
- Docker Compose
- JUnit 5
- Mockito
- MockMvc
- Git

Additional technologies are added only after they are used directly in hands-on exercises or project work.

## Current REST Endpoints

### Get a transaction by ID

```http
GET /transactions/{transactionId}
```

Returns:

- `200 OK` when the transaction exists
- `400 Bad Request` when the transaction ID is invalid
- `404 Not Found` when the transaction is missing

### Get transactions by account ID

```http
GET /transactions/account/{accountId}
```

Returns:

- `200 OK` with matching transactions
- `200 OK` with an empty JSON array when there are no matches

The endpoint currently makes no ordering guarantee.

### Create a transaction

```http
POST /transactions
Content-Type: application/json
```

Example request:

```json
{
  "id": "TXN-123",
  "accountId": "ACC-1111",
  "amount": 35000.00,
  "type": "CREDIT",
  "description": "Monthly Savings"
}
```

Returns:

- `201 Created` for a valid request
- `400 Bad Request` for invalid input, malformed JSON, or an unsupported transaction type
- `409 Conflict` when the business transaction ID already exists

The transaction ID acts as the business/idempotency identifier.

The service performs an early duplicate check for clear application behavior, while the database unique constraint on the transaction ID remains the final concurrency-safe guard. A confirmed duplicate unique-constraint violation is translated into a domain duplicate exception and exposed as `409 Conflict`.

Transactions are persisted through a `TransactionRepository` abstraction.

After a successful save, the current Kafka learning implementation also builds and publishes a `TransactionCreatedEvent`. The database write and Kafka publish are intentionally still separate operations at this milestone; transactional outbox is planned as the stronger consistency design.

## API Security

All transaction endpoints currently require authentication.

Protected paths include:

```text
POST /transactions
GET  /transactions/{transactionId}
GET  /transactions/account/{accountId}
```

The current learning implementation uses HTTP Basic authentication through Spring Security.

Security behavior is configured through `SecurityFilterChain`:

- `/transactions/**` requires authentication
- HTTP sessions are stateless
- HTTP Basic is enabled
- CSRF is disabled for the current stateless Authorization-header based authentication model

The application currently uses an in-memory `UserDetailsService` with `USER` and `ADMIN` roles for security learning and testing.

Passwords are hashed using BCrypt through Spring Security's `PasswordEncoder`.

Current authentication behavior:

```text
missing credentials
-> 401 Unauthorized

invalid credentials
-> 401 Unauthorized

valid credentials
-> request proceeds to controller/application logic
```

The intended authorization model is:

```text
USER
-> access only their own accounts and transactions

ADMIN
-> access any account or transaction
```

Resource-level ownership authorization is intentionally deferred until the application has a genuine user/account ownership model. An artificial ownership mapping was not added only to demonstrate `403 Forbidden`.

HTTP Basic is being used to establish and test the security fundamentals first. JWT/token-based authentication is planned only after the authentication/authorization model is stable.

Because HTTP Basic credentials are Base64 encoded rather than encrypted, HTTPS/TLS is required for any real deployment.

The active repository implementation depends on the Spring profile:

- `in-memory` uses the in-memory repository
- `jpa` uses the JPA-backed repository adapter


## Redis Caching

`GET /transactions/{transactionId}` is cached through Spring Cache with Redis when the JPA/Redis runtime configuration is active.

This endpoint was chosen because transactions are effectively immutable after creation in the current API, which keeps invalidation simple.

Current cache contract:

```text
Source of truth       MySQL
Strategy              cache-aside
Cache hit             return cached transaction and skip DB
Cache miss            load from DB, cache the value, then return it
Missing transaction   return 404; do not negative-cache initially
TTL                   30 minutes
Cache key             transactions::<transactionId>
Value serialization   JSON
Redis unavailable     fall back to MySQL
```

Redis is treated as a performance optimization rather than a hard dependency for transaction reads.

A custom cache error handler logs Redis GET/PUT/EVICT/CLEAR failures without propagating them into the API request. When Redis is unavailable, a transaction read falls back to MySQL and can still return `200 OK` if the database is healthy.

Short Redis connection/command timeouts are configured so fallback does not wait indefinitely.

The failure trade-off is explicit: Redis outage preserves correctness, but latency and database load increase. If the database cannot absorb the redirected traffic, a cache outage can still become a production incident.

Cache values use JSON rather than JDK-native serialization so they are easier to inspect and less tightly coupled to Java serialization.

Not-found results are not cached initially. A missing transaction may be created later, so negative caching would require a deliberate short TTL and/or invalidation rule to avoid stale `404` responses.

Cache-stampede and hot-key risks are understood but mitigation is deferred until a real scale requirement justifies it.

The real Redis integration test now uses Testcontainers, so a manually started Redis instance is no longer required for the normal full Maven test suite. Docker must be available for the Testcontainers-based integration test.


## Kafka Transaction Events

The project contains a transaction-event producer, a Spring Kafka application consumer, and persistent consumer-side idempotency.

Current Kafka contract:

```text
Topic                 transaction-events
Local partitions      3
Local replication     1
Record key            transactionId
Record value          TransactionCreatedEvent as JSON
Producer              KafkaTemplate<String, TransactionCreatedEvent>
Consumer group        transaction-created-events-cg
Consumer listener     TransactionCreatedEventConsumer
Processing boundary   TransactionCreatedEventHandler
Persistent handler    PersistentTransactionCreatedEventHandler
```

`transactionId` is used as the Kafka record key so events for the same transaction are routed consistently to the same partition. Kafka ordering is partition-local rather than global across the whole topic.

`TransactionCreatedEvent` contains:

- `eventId`
- `eventType`
- `eventTimestamp`
- `transactionId`
- `accountId`
- `amount`
- `transactionType`
- `transactionCreatedAt`

`eventId` is deliberately separate from the business `transactionId`. It is the stable event identity used for consumer-side deduplication.

Current create flow:

```text
validate request
-> save transaction
-> build TransactionCreatedEvent
-> publish to transaction-events
-> return saved transaction
```

Current consumer flow:

```text
transaction-events
-> @KafkaListener
-> TransactionCreatedEventConsumer
-> PersistentTransactionCreatedEventHandler
-> TransactionEventProcessingService
-> processed_events + transaction_event_audit
```

The handler boundary keeps Kafka-listener mechanics separate from transactional event-processing behavior.

### Persistent Consumer Idempotency

Consumer-side persistent idempotency is implemented using two tables:

```text
processed_events
- event_id primary key
- processed_at
- consumer_name

transaction_event_audit
- id primary key
- event_id unique
- transaction_id
- account_id
- amount
- transaction_type
- event_timestamp
- created_at
```

`transaction_event_audit.event_id` references `processed_events.event_id`. `transaction_id` is intentionally not a foreign key to the producer-side transaction table so the consumer persistence model does not require the producer and consumer to share the same business database boundary.

The processing service uses one local database transaction:

```text
receive event
-> check whether eventId is already processed
-> insert processed_events row
-> insert transaction_event_audit row
-> commit both atomically
```

If the audit/business insert fails, the processed-event insert is rolled back as part of the same transaction. This avoids recording an event as processed when its business effect did not commit.

Sequential duplicate delivery is handled by the stable `eventId`: a previously processed event returns without creating another audit/business row.

The database primary key/unique constraints remain the final race-safe uniqueness guard. A true concurrent race in which two consumers both pass the application pre-check and one later hits the uniqueness constraint is a known hardening case; graceful race-conflict handling is not yet implemented.

### Kafka Retry / Backoff / Dead-Letter Handling

Consumer failure handling now uses Spring Kafka `DefaultErrorHandler` with `DeadLetterPublishingRecoverer`. Retryable processing failures use bounded exponential backoff:

```text
initial delivery attempt
-> retry after 1 second
-> retry after 2 seconds
-> retry after 4 seconds
-> publish exhausted record to transaction-events-dlt
```

The DLT resolver preserves the original partition by publishing to the same partition number on `transaction-events-dlt`. The DLT therefore needs at least as many partitions as the source topic for this topology.

Listener payloads are Bean Validated before business-handler execution. Framework-fatal validation/conversion failures are sent to the DLT without invoking the business handler. Retry-count behavior is tested separately from Kafka integration behavior so integration tests do not depend on exact handler-invocation counts or Spring exception-wrapper classes.

The DLT integration tests consume values as raw `byte[]`, preserve the record key, and verify original-topic, original-partition, and exception metadata headers. Test consumer configuration disables JSON type-header precedence so the configured `TransactionCreatedEvent` default target type is used consistently.

Malformed JSON / raw-byte DLT serializer hardening remains a follow-up and is not claimed as covered by the current integration tests.

### Producer Reliability

The current producer still publishes directly after transaction persistence:

```text
DB save
-> Kafka publish
```

This deliberately leaves the DB/Kafka dual-write failure window visible. The planned stronger producer-side design is transactional outbox, where the business row and outbox event row are committed in one local database transaction and a separate publisher sends pending events to Kafka.

Outbox publication can still produce duplicate events if Kafka accepts a send and the publisher fails before recording publication success. Persistent consumer idempotency therefore remains necessary even after outbox is introduced.

### Kafka Testing

Kafka testing now covers:

Publisher integration:

```text
TransactionEventPublisher
-> KafkaTemplate
-> JSON serialization
-> real Kafka broker
-> test consumer
-> JSON deserialization
-> TransactionCreatedEvent
```

Application consumer persistence integration:

```text
TransactionEventPublisher
-> KafkaTemplate
-> real Kafka broker
-> JSON deserialization
-> @KafkaListener
-> TransactionCreatedEventConsumer
-> PersistentTransactionCreatedEventHandler
-> TransactionEventProcessingService
-> processed_events
-> transaction_event_audit
```

The end-to-end Testcontainers test verifies that a real published event reaches the application consumer and produces the expected persisted audit state.

A duplicate-delivery integration test first waits for the initial event to be fully processed, publishes the same event again, and verifies the persistent state remains at one processed-event row and one audit row for a bounded period using Awaitility.

A separate Spring/JPA integration test verifies transactional rollback: if the audit insert violates a database constraint, the processed-event insert is rolled back as well.

A manually started Kafka broker is not required for `mvn clean test`; Docker must be available for Testcontainers.
## Validation and Domain Rules

The HTTP boundary uses Jakarta Bean Validation.

Current request rules:

- `id` must not be null or blank
- `accountId` must not be null or blank
- `amount` must not be null
- `amount` must be greater than zero
- `type` must not be null
- `description` must not be null or blank

Important business invariants are also enforced in the service layer so non-HTTP callers cannot bypass them:

- transaction ID is required
- account ID is required
- amount must be positive
- transaction type is required

Description is currently enforced at the HTTP request boundary and is not treated as a service-level business invariant.

Supported transaction types are represented by an enum:

```java
public enum TransactionType {
    CREDIT,
    DEBIT,
    TRANSFER,
    REFUND
}
```

Unsupported JSON values such as `"SALARY"` fail during deserialization and are mapped to `400 Bad Request`.

## Exception Handling

A global exception handler maps application failures to structured API responses.

Handled cases include:

- transaction not found
- duplicate transaction IDs
- invalid arguments
- invalid transaction amounts
- Bean Validation failures
- malformed request bodies
- unsupported enum values
- unexpected server errors

Illustrative duplicate response:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Transaction with id TXN-123 already exists"
}
```

Illustrative validation response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "accountId cannot be null or blank"
}
```

## Persistence Architecture

The service depends on an application-facing repository abstraction rather than Spring Data directly.

```text
TransactionController
        |
        v
TransactionService
        |
        v
TransactionRepository
        |
        v
JpaTransactionRepositoryAdapter
        |
        +--> TransactionEntityMapper
        |
        v
SpringDataTransactionRepository
        |
        v
MySQL
```

The persistence implementation keeps the domain model separate from JPA-specific concerns.

### Domain model

`Transaction` represents the application/domain view of a transaction.

### Persistence entity

`TransactionEntity` represents the relational persistence model.

Important persistence choices:

- internal UUID primary key
- unique business `transactionId`
- `BigDecimal` mapped to `DECIMAL(19,2)`
- enum stored using string representation
- `Instant` used for transaction timestamps
- nullable description

The UUID primary key is persistence-specific, while `transactionId` remains the business and idempotency identifier.

## Repository Implementations

The project currently has two implementations of `TransactionRepository`.

### In-memory implementation

Used with:

```text
in-memory
```

This supports lightweight application and test scenarios without requiring a database.

### JPA implementation

Used with:

```text
jpa
```

The JPA implementation consists of:

- `JpaTransactionRepositoryAdapter`
- `TransactionEntityMapper`
- `TransactionEntity`
- `SpringDataTransactionRepository`

Spring profiles are used so only one repository implementation is active at a time.

## Database and Schema Management

MySQL is used for the real relational persistence path.

Flyway owns database schema creation and evolution.

Migration scripts are stored under:

```text
src/main/resources/db/migration
```

The initial migration is:

```text
V1__create_transactions_table.sql
```

Flyway maintains migration history in:

```text
flyway_schema_history
```

Hibernate is configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This means:

```text
Flyway
→ creates and changes the schema

Hibernate
→ validates that entity mappings match the schema
```

Hibernate does not own schema evolution.

Open Session in View is disabled:

```yaml
spring:
  jpa:
    open-in-view: false
```

## Database Configuration

Runtime database values are read from environment variables rather than being stored directly in application configuration.

Required variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Example local values:

```bash
export DB_URL="jdbc:mysql://localhost:3306/transaction_db"
export DB_USERNAME="app_user"
export DB_PASSWORD="app_password"
```

The `jpa` runtime profile reads these values through Spring configuration.

JPA integration tests use H2 through test-specific configuration, so MySQL is not required for the normal Maven test suite.

## Running the Application and MySQL with Docker Compose

The repository includes a multi-stage `Dockerfile` for the Spring Boot application and a root-level `compose.yml` that supports the application together with MySQL, Redis, and Kafka for local development.

The application container connects to infrastructure through Docker Compose service discovery using service names such as `mysql`, `redis`, and `kafka` rather than `localhost`.

```text
jdbc:mysql://mysql:3306/transaction_db
```

Inside a container, `localhost` refers to that same container, so Compose service names are used instead of `localhost` for application-to-database and application-to-Redis traffic.

Build and start the full stack:

```bash
docker compose up --build
```

Run in detached mode when startup logs do not need to remain attached:

```bash
docker compose up --build -d
```

Check status:

```bash
docker compose ps
```

Stop the Compose services while preserving the MySQL volume:

```bash
docker compose stop
```

Start the existing containers again:

```bash
docker compose start
```

The MySQL named volume preserves transaction data across `stop` / `start` cycles. Avoid:

```bash
docker compose down -v
```

unless the local database volume should intentionally be deleted.

`depends_on` controls startup order but does not by itself guarantee that MySQL is ready to accept connections. A database health check is a planned hardening improvement.

## Running the Project

### Run all tests

```bash
mvn clean test
```

Docker must be running because the Redis and Kafka integration tests use Testcontainers. Manually started Redis and Kafka containers are not required.

### Run using the in-memory repository

Activate the `in-memory` profile when required by the local setup.

### Run using MySQL/JPA

Ensure MySQL is running and database environment variables have been exported.

Then start the application with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=jpa
```

The application runs on the default Spring Boot port:

```text
http://localhost:8080
```

On startup with the JPA profile:

1. Flyway validates migration history.
2. Flyway applies any pending migrations.
3. Hibernate validates the entity mappings against the database schema.
4. Spring Boot starts the REST application.

## Repository Structure

```text
src/
├── main/
│   ├── java/com/prateek/learning/
│   │   ├── CareerRestartApplication.java
│   │   ├── common/
│   │   │   └── exception/
│   │   ├── dsa/
│   │   │   ├── day01/
│   │   │   ├── day02/
│   │   │   ├── day03/
│   │   │   ├── day04/
│   │   │   ├── day05/
│   │   │   ├── day06/
│   │   │   ├── day07/
│   │   │   ├── day08/
│   │   │   ├── day09/
│   │   │   ├── day10/
│   │   │   ├── day11/
│   │   │   ├── day12/
│   │   │   ├── day13/
│   │   │   └── day14/
│   │   ├── java/
│   │   │   ├── day01/
│   │   │   ├── day02/
│   │   │   └── day03/
│   │   ├── kafka/
│   │   │   ├── event/
│   │   │   ├── producer/
│   │   │   ├── consumer/
│   │   │   │   └── handler/
│   │   │   ├── processing/
│   │   │   └── persistence/
│   │   │       ├── entity/
│   │   │       └── repository/
│   │   └── transaction/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── exception/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── persistence/
│   │           ├── adapter/
│   │           ├── entity/
│   │           ├── mapper/
│   │           └── repository/
│   └── resources/
│       ├── application-jpa.yml
│       └── db/
│           └── migration/
│               ├── V1__create_transactions_table.sql
│               └── V2__create_event_processing_audit_table.sql
│
└── test/
    ├── java/com/prateek/learning/
    │   ├── dsa/
    │   ├── java/
    │   ├── kafka/
    │   │   ├── producer/
    │   │   ├── consumer/
    │   │   ├── processing/
    │   │   └── integration/
    │   └── transaction/
    │       ├── controller/
    │       ├── service/
    │       └── persistence/
    │           └── adapter/
    └── resources/
        └── application-jpa.yml

notes/
├── day-01.md
├── day-02.md
├── day-03.md
├── day-04.md
├── day-05.md
├── day-06.md
├── day-07.md
├── day-08.md
├── day-09.md
├── day-10.md
├── day-11.md
├── day-12.md
├── day-13.md
├── day-14.md
└── day-15.md
```

## Code Coverage

JaCoCo generates an HTML coverage report as part of the Maven test lifecycle.

Run:

```bash
mvn clean test
```

Report:

```text
target/site/jacoco/index.html
```

Current baseline:

- Instruction coverage: 82%
- Branch coverage: 77%
- Line coverage: approximately 87.4%
- Method coverage: approximately 86.2%
- Class coverage: approximately 96.1%

Coverage is currently used as a diagnostic signal rather than a hard build gate. Tests are prioritized around meaningful business, persistence, security, caching, failure, and messaging behavior rather than percentage maximization.

## Testing Approach

The project separates tests by responsibility.

- **Repository unit tests** verify repository contracts and lookup behaviour.
- **Service unit tests** verify business rules and repository delegation.
- **Controller-slice tests** use `@WebMvcTest`, `MockMvc`, and a mocked service.
- **Security controller tests** verify unauthenticated, invalid-credential, and authenticated request behavior.
- **Application integration tests** verify request-to-repository behaviour through the Spring context.
- **JPA integration tests** verify the adapter, mapper, Spring Data repository, and relational persistence path using H2.
- **Exception-handler tests** verify structured API error responses.
- **Cache-proxy tests** verify that repeated service lookups are intercepted by Spring caching and avoid repeated repository calls.
- **Redis cache error-handler tests** verify cache failures are logged/swallowed rather than rethrown.
- **Redis integration tests** verify real Redis JSON serialization, cache writes, cache reads, and repository bypass on cache hit using Testcontainers.
- **Kafka publisher unit tests** verify the expected topic, transaction-ID key, and event payload passed to `KafkaTemplate`.
- **Kafka consumer unit tests** verify listener-to-handler delegation without requiring a broker.
- **Kafka persistent-handler unit tests** verify handler-to-processing-service delegation.
- **Kafka processing-service unit tests** verify new-event and duplicate-event branching plus entity mapping.
- **Kafka processing integration tests** verify real JPA persistence, sequential duplicate suppression, and rollback of the dedup record when the audit/business insert fails.
- **Kafka publisher integration tests** verify real JSON serialization, broker communication, Kafka record key, and event deserialization using Testcontainers.
- **Kafka consumer-flow integration tests** verify a real publisher-to-broker-to-`@KafkaListener` path through the persistent handler and into the database, including duplicate redelivery suppression using Awaitility.
- **Kafka retry/DLT policy tests** verify three exponential retries at 1s, 2s, and 4s before STOP.
- **Kafka retry/DLT integration tests** verify retryable handler failure reaches `transaction-events-dlt`, DLT key/value and source metadata are preserved, and invalid Bean-Validation input reaches DLT without invoking the business handler.
- **DSA tests** cover happy paths, edge cases, invalid input, duplicates, ties, and ordering assumptions.

Invalid HTTP requests are tested to confirm that:

- `400 Bad Request` is returned
- the expected validation message is included
- the service layer is not called

Security tests verify that:

- missing authentication returns `401 Unauthorized`
- invalid HTTP Basic credentials return `401 Unauthorized`
- valid credentials allow the request to proceed to controller behavior
- authentication failures are rejected before the service layer is invoked
- controller-slice tests load the intended application `SecurityConfig`
- integration tests authenticate explicitly before exercising secured transaction flows

The JPA integration tests verify:

- saving transactions
- retrieving a transaction by business transaction ID
- retrieving transactions by account ID
- domain-to-entity mapping
- entity-to-domain mapping
- database-backed round trips
- duplicate business transaction IDs are rejected by the database unique constraint
- the expected unique-constraint violation is translated into `DuplicateTransactionException`

## Progress Summary

### Day 1

- Java Streams, Collectors, Optional, Comparator, and BigDecimal
- Transaction service exercises
- Two Sum
- Contains Duplicate
- JUnit tests

### Day 2

- `equals()`, `hashCode()`, and `HashSet`
- Mutable-key behavior
- Valid Anagram
- SQL aggregation
- Interview speaking practice

### Day 3

- Checked and unchecked exceptions
- Custom exceptions
- Immutable class and Java record
- Defensive copying
- Group Anagrams
- SQL joins
- Global Spring Boot exception handling

### Day 4

- Constructor injection and controller-service separation
- Spring Boot application setup
- GET and POST transaction endpoints
- MockMvc controller-slice tests
- Direct service unit tests
- Top K Frequent Elements using a size-limited min-heap
- SQL subqueries and window functions

### Day 5

- Introduced the `TransactionRepository` abstraction
- Added an in-memory repository implementation
- Persisted newly created transactions
- Retrieved created transactions by transaction ID
- Added repository, service, controller, and integration coverage
- Practised heap and `PriorityQueue` concepts

### Day 6

- Added repository-backed transaction retrieval by account ID
- Added Jakarta Bean Validation to transaction requests
- Mapped validation and deserialization failures to structured `400 Bad Request` responses
- Introduced `TransactionType` for type-safe domain representation
- Added controller tests proving invalid requests do not reach the service layer
- Added integration coverage for account-based transaction retrieval
- Implemented K Closest Points to Origin using a size-limited max-heap
- Practised SQL aggregation using `WHERE`, `GROUP BY`, `HAVING`, `ORDER BY`, and `LIMIT`

### Day 7

- Refactored duplicated service validation into focused private methods
- Removed obsolete sample transaction state from the service
- Reviewed layered validation across REST, Kafka, scheduled jobs, and internal callers
- Reviewed retry, acknowledgement, idempotency, and database uniqueness behaviour
- Implemented K Largest Elements using a bounded min-heap
- Practised SQL aggregation with `WHERE`, `GROUP BY`, `HAVING`, and `ORDER BY`
- Added Spring Data JPA
- Added a dedicated persistence entity separate from the domain model
- Added domain-to-entity and entity-to-domain mapping
- Added a JPA repository adapter behind the existing `TransactionRepository` abstraction
- Added profile-based selection between in-memory and JPA repositories
- Migrated transaction timestamps from `LocalDateTime` to `Instant`
- Added H2-backed JPA integration tests
- Added local MySQL through Docker Compose
- Moved runtime database credentials to environment variables
- Added Flyway and the first versioned migration
- Switched Hibernate from schema updates to schema validation
- Disabled Open Session in View
- Verified Flyway migration history and MySQL-backed application startup


### Day 8

- Reviewed the full persistence path without notes: controller -> service -> application repository -> JPA adapter -> mapper -> Spring Data repository -> MySQL
- Reinforced Flyway versus Hibernate responsibilities, persistence UUID versus business transaction ID, database uniqueness, and `Instant` for global timestamps
- Added a multi-stage `Dockerfile` using Maven/Java 21 for the build stage and a Java 21 JRE runtime stage
- Built and ran the Spring Boot application as a Docker image
- Extended `compose.yml` so the Spring Boot application and MySQL run in the same Compose stack
- Used Docker service discovery with `mysql` as the database hostname inside the application container
- Verified POST and GET transaction requests through the containerized application and JPA/MySQL persistence path
- Verified MySQL data survives `docker compose stop` / `docker compose start` through the named volume
- Reviewed concurrent duplicate transaction handling: application existence checks can race, while the database unique constraint is the final concurrency-safe guard
- Discussed REST duplicate behavior as `409 Conflict` and asynchronous duplicate handling as idempotent acknowledgement
- Discussed service-level `@Transactional` boundaries for a complete unit of work
- Implemented Merge Intervals using sorting plus greedy merging in `O(n log n)` time
- Added parameterized Merge Intervals tests and used a defensive deep copy to avoid mutating caller-owned interval arrays
- Learned SQL conditional aggregation using `SUM(CASE WHEN ... THEN ... ELSE ... END)` and reinforced `HAVING` for post-group filtering
- Practised an interview explanation of why the service depends on an application-facing `TransactionRepository` abstraction rather than Spring Data JPA directly


### Day 9

- Implemented explicit duplicate transaction behavior with `409 Conflict`
- Added `DuplicateTransactionException`
- Added service-level early duplicate detection for a cleaner application failure path
- Kept the database unique constraint on the business transaction ID as the final concurrency-safe duplicate guard
- Updated the JPA adapter to translate the expected transaction-ID unique-constraint violation into the domain duplicate exception
- Used `saveAndFlush()` so persistence violations can be classified before the adapter returns; flush remains separate from transaction commit
- Updated H2 JPA tests to use the Flyway-managed schema with Hibernate `ddl-auto=validate`
- Aligned UUID handling in H2 with the Flyway `BINARY(16)` schema
- Added Spring service-level `@Transactional` boundaries and `readOnly = true` for read operations where appropriate
- Added service, exception-handler, JPA integration, and REST integration coverage for duplicate behavior
- Reviewed client retries, asynchronous redelivery, acknowledgement ordering, failure classification, and idempotent processing
- Implemented Longest Substring Without Repeating Characters using an `O(n)` sliding window with a `HashSet`
- Added parameterized tests for the sliding-window solution, including empty and null input
- Reinforced SQL conditional aggregation, conditional counting, credit/debit totals, net amount, and `HAVING`
- Practised a concise interview explanation of an idempotent transaction-creation API
- Full `mvn clean test` passed


### Day 10

- Completed the Day 9 take-home problem: Longest Substring With At Most Two Distinct Characters using a sliding window plus character-frequency map
- Reinforced why frequency state requires a `HashMap` rather than only a `HashSet`
- Revised DSA recognition across hash/map lookup, frequency maps, heap/top-K, sorting + greedy intervals, and sliding window
- Reinforced `saveAndFlush()` versus transaction commit, transaction atomicity versus concurrent uniqueness, `readOnly = true`, and commit-before-acknowledgement behavior
- Added Spring Security and Spring Security test support
- Added a custom `SecurityFilterChain`
- Protected `/transactions/**` behind authentication
- Configured stateless HTTP sessions using `SessionCreationPolicy.STATELESS`
- Enabled HTTP Basic for the first security-learning implementation
- Disabled CSRF specifically for the current stateless Authorization-header authentication model
- Added BCrypt password hashing through `PasswordEncoder`
- Added temporary in-memory `USER` and `ADMIN` identities through `UserDetailsService`
- Debugged an incorrect `/transaction/**` security matcher that had allowed the real `/transactions/**` endpoints to fall through to `permitAll()`
- Updated controller-slice and application integration tests to authenticate explicitly
- Added focused tests for missing credentials, invalid credentials, and successful authenticated access
- Verified authentication failures are rejected before the service layer is invoked
- Designed USER ownership versus ADMIN unrestricted-access authorization but deliberately deferred implementation until a genuine user/account ownership model exists
- Reviewed `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, and `503 Service Unavailable` security/failure semantics
- Reviewed TLS requirements, sensitive authentication logging, identity-provider failure behavior, and stateless horizontal scaling
- Reinforced SQL conditional aggregation and conditional `HAVING` for accounts with at least three CREDIT transactions
- Practised a concise Spring Security interview explanation
- Full `mvn clean test` passed


### Day 11

- Completed Longest Repeating Character Replacement using a sliding window plus character-frequency map
- Reinforced DSA state, invariant, invalid-window condition, repair rule, answer update timing, and edge-case validation before coding
- Corrected the `k == 0` edge case and an incorrect map/index lookup in the initial implementation
- Reinforced the monotonic/stale `maxFrequency` optimization and the distinction between exact-current-window validity and maximum-length correctness
- Changed the DSA workflow so new DSA problems are completed inside the study session rather than routinely assigned as take-home work
- Completed no-notes Spring Security retrieval covering authentication/authorization, filter-chain flow, `SecurityContext`, `UserDetailsService`, BCrypt, `401`/`403`, statelessness, CSRF reasoning, and deliberate JWT deferral
- Designed Redis caching before implementation and chose `GET /transactions/{transactionId}` because transactions are effectively immutable in the current API
- Added Spring Cache and Spring Data Redis dependencies
- Added Redis 7.4 to Docker Compose and verified connectivity with `redis-cli ping`
- Added cache-aside transaction lookup with a 30-minute TTL and namespaced transaction keys
- Configured Redis cache values to use JSON serialization rather than JDK-native serialization
- Improved unexpected-exception logging while keeping the client `500` response generic
- Diagnosed the original Redis `SerializationException` from the default JDK serializer
- Added short Redis connection/command timeouts
- Added a custom `RedisCacheErrorHandler` so cache failures degrade to MySQL instead of failing the API
- Manually verified Redis-down behavior: cache GET failure -> MySQL query -> cache PUT failure -> HTTP `200`
- Reviewed cache stampede, hot keys, negative caching, TTL/invalidation trade-offs, and cache-outage DB pressure
- Added `TransactionServiceCacheTest` to prove two service lookups result in one repository lookup through Spring's cache proxy
- Added `RedisCacheErrorHandlerTest` for GET, PUT, EVICT, and CLEAR failure behavior
- Added a focused real-Redis integration test proving JSON serialization and repository bypass on the second lookup
- Reinforced SQL conditional aggregation and `HAVING` for accounts with at least three CREDIT transactions
- Full `mvn clean test` passed with Redis available for the real Redis integration test


### Day 12

- Made the Redis integration test self-contained with Testcontainers so the full Maven build no longer depends on a manually running Redis instance
- Completed Max Consecutive Ones III using an `O(n)` sliding window and reinforced precise zero-count bookkeeping during window repair
- Learned Kafka fundamentals: topics, partitions, consumer groups, offsets, lag, rebalancing, retention/replay, partition-local ordering, and at-least-once delivery
- Refreshed RabbitMQ exchanges, queues, bindings, routing keys, acknowledgements, DLX/DLQ, and RabbitMQ-versus-Kafka trade-offs
- Designed `transaction-events` with three local partitions and `transactionId` as the Kafka record key
- Added Spring Kafka and `TransactionCreatedEvent`
- Added `TransactionEventPublisher` using `KafkaTemplate<String, TransactionCreatedEvent>`
- Integrated event publication after successful transaction persistence
- Added publisher unit tests and transaction-service event assertions
- Kept controller integration tests isolated from Kafka using `@MockitoBean`
- Added Kafka Testcontainers support and a real producer-to-consumer integration test
- Diagnosed and fixed a producer serialization mismatch exposed by the real Kafka integration test
- Verified the Kafka integration test is self-contained by stopping the manually running broker and passing `mvn clean test`
- Reviewed the DB/Kafka dual-write problem, transactional outbox, duplicate publication, and consumer idempotency
- Practised SQL date-range aggregation, `HAVING`, deterministic ordering, and `ROW_NUMBER` / `RANK` / `DENSE_RANK`
- Full `mvn clean test` passed


### Day 13

- Reinforced Kafka topic, partition, consumer-group, offset, lag, rebalance, at-least-once delivery, idempotency, transactional-outbox, RabbitMQ routing, and HTTP `401`/`403` concepts through no-notes retrieval
- Completed Minimum Size Subarray Sum using a positive-integer sliding window
- Reinforced repeated shrinking while `sum >= target`, candidate-length update before removing the left value, and `O(n)` time / `O(1)` space reasoning
- Added `TransactionCreatedEventConsumer` using `@KafkaListener` with explicit group `transaction-created-events-cg`
- Added `TransactionCreatedEventHandler` and `LoggingTransactionCreatedEventHandler` so Kafka-listener concerns are separated from processing behavior
- Added focused consumer unit coverage verifying listener-to-handler delegation
- Added `TransactionEventFlowIntegrationTest` using Testcontainers Kafka to prove real publisher -> broker -> JSON deserialization -> `@KafkaListener` -> consumer -> handler delivery
- Reinforced offset/commit failure scenarios and why at-least-once delivery requires idempotent consumers
- Designed persistent consumer deduplication using stable `eventId`, a database unique constraint, and atomic local transaction boundaries for dedup record + business mutation
- Reviewed retryable versus non-retryable consumer failures, poison-event handling, DLT trade-offs, and same-key ordering implications
- Practised SQL conditional aggregation for CREDIT/DEBIT totals, net amount, last-30-day filtering, `HAVING`, and result ordering
- Added JaCoCo reporting and established the first project coverage baseline
- Kept persistent consumer idempotency, retry/DLT handling, and transactional outbox as future implementation milestones

### Day 14

- Reinforced Kafka offset/commit failure behavior, RabbitMQ routing terminology, `equals()`/`hashCode()`, and Java concurrency through no-notes retrieval
- Reviewed race conditions, atomicity versus visibility, `volatile`, `synchronized`, `AtomicInteger`, `ExecutorService`, and JVM-local versus distributed concurrency
- Connected Java synchronization to multi-pod backend behavior and reinforced database uniqueness as the final guard for duplicate transaction creation
- Reviewed optimistic and pessimistic locking, including stale-version detection and lost-update prevention
- Completed Container With Most Water using the two-pointer pattern in `O(n)` time and `O(1)` extra space
- Added Flyway migration `V2__create_event_processing_audit_table.sql`
- Added Kafka-owned persistence for `processed_events` and `transaction_event_audit`
- Added `TransactionEventProcessingService` with one local `@Transactional` boundary for deduplication + audit/business persistence
- Replaced the logging-only handler with `PersistentTransactionCreatedEventHandler`
- Added processing-service unit tests, persistent-handler unit tests, and Spring/JPA integration tests
- Verified sequential duplicate delivery does not create a second audit/business effect
- Verified audit/business insert failure rolls back the processed-event row
- Extended the real Kafka Testcontainers flow to prove publisher -> broker -> listener -> persistent handler -> processing service -> database
- Added Awaitility-based duplicate-delivery verification so the test waits for the first event, republishes the same event, and confirms persistent state remains stable
- Completed a guided senior system-design walkthrough covering requirements, consistency, API, MySQL, Redis, Kafka, outbox, consumer idempotency, scale, and failure behavior
- Practised SQL top-3 account net-amount aggregation and a spoken explanation of why `synchronized` does not coordinate across multiple Spring Boot pods
- Final `mvn clean test` passed

### Day 15

- Extended Java concurrency reinforcement with `ReentrantLock`, deadlock conditions/prevention, `ConcurrentHashMap`, and `CompletableFuture`
- Completed 3Sum using sorting plus two pointers, including duplicate skipping and `O(n^2)` time / `O(1)` algorithmic extra space reasoning
- Practised latest-transaction-per-account SQL using `ROW_NUMBER()` with deterministic ordering and MySQL last-30-day syntax
- Repeated the transaction/event system-design exercise with less guidance, covering scale, indexes, outbox backlog, Kafka partitions/consumers, failure handling, observability, and trade-offs
- Added Bean Validation to `TransactionCreatedEvent` listener payloads
- Added Spring Kafka `DefaultErrorHandler` and `DeadLetterPublishingRecoverer` for `transaction-events-dlt`
- Added bounded exponential retry/backoff with three retries at 1s, 2s, and 4s
- Added a deterministic backoff-policy unit test instead of asserting exact Kafka handler invocation counts
- Added Kafka retry/DLT integration coverage using raw `byte[]` DLT values, unique test keys, and source/exception metadata checks
- Added validation-failure DLT coverage proving invalid events do not reach the business handler
- Stabilized JSON consumer targeting in tests with `spring.json.use.type.headers=false`
- Kept malformed JSON/raw-byte DLT serializer hardening as a follow-up rather than over-claiming coverage
- Final `mvn clean test` passed


## Learning Approach

For each topic:

1. Understand the requirement and assumptions.
2. Write an initial solution.
3. Review correctness and edge cases.
4. Improve design or complexity.
5. Add focused tests.
6. Record key learnings.
7. For DSA, complete pattern recognition, manual walkthrough, implementation, review, complexity, edge cases, and tests inside the study session unless take-home work is explicitly requested.
8. Commit and push completed work.

## Next Planned Improvements

- Add database-backed integration testing against MySQL where it provides value
- Review the current Flyway version against MySQL 8.4 compatibility
- Continue SQL practice using the relational transaction schema
- Continue in-session DSA practice with explicit state/invariant reasoning across current patterns before expanding further
- Continue system-design exercises around transaction processing and idempotency
- Extend Spring Security from the current HTTP Basic authentication foundation to credible resource-level authorization once user/account ownership is modelled
- Add JWT/token authentication only after the current Spring Security fundamentals are stable and well tested
- Continue reinforcing Redis failure behaviour and cache design through retrieval rather than adding unnecessary cache features
- Harden concurrent duplicate races so a DB uniqueness conflict is translated into a safe already-processed outcome
- Add Kafka retry/backoff and dead-letter handling now that persistent idempotency is implemented and tested
- Implement transactional outbox incrementally after retry/DLT behavior is understood
- Refresh AWS through project work covering IAM, Secrets Manager, RDS/Aurora, S3, CloudWatch, VPC/security-group basics, ALB, and one practical container deployment path such as ECS/Fargate
- Add a backend-focused AI learning track after core backend foundations: LLM fundamentals, Java/Spring integration, structured outputs/tool calling, embeddings/vector search or RAG only with a justified use case, plus AI security, PII, latency, cost, evaluation, and observability
- Add API documentation
- Add observability and production-style logging incrementally

## Project Positioning

This repository represents hands-on learning and career-restart preparation.

Technologies are listed here only after they have been used directly in exercises or project work. Current Java 21, JPA, MySQL, Flyway, Docker, Spring Security, Redis/Spring Cache, Kafka, Testcontainers, and related work in this repository represents hands-on project practice and is kept distinct from professional experience.