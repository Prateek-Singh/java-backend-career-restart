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
- Relational persistence with MySQL
- Flyway database migrations
- Data structures and algorithms
- SQL aggregation, joins, subqueries, and window functions
- Backend layering and repository design
- Idempotency and transaction-processing design
- System design and interview preparation

## Tech Stack Used in This Repository

- Java 21
- Maven
- Spring Boot
- Spring MVC
- Spring Data JPA
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

The active repository implementation depends on the Spring profile:

- `in-memory` uses the in-memory repository
- `jpa` uses the JPA-backed repository adapter

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

The repository includes a multi-stage `Dockerfile` for the Spring Boot application and a root-level `compose.yml` that runs the application together with MySQL 8.4.

The application container connects to MySQL through Docker Compose service discovery using the service name `mysql`:

```text
jdbc:mysql://mysql:3306/transaction_db
```

Inside a container, `localhost` refers to that same container, so the Compose service name is used instead of `localhost` for application-to-database traffic.

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

Stop both containers while preserving the MySQL volume:

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
│   │   │   └── day09/
│   │   ├── java/
│   │   │   ├── day01/
│   │   │   ├── day02/
│   │   │   └── day03/
│   │   └── transaction/
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
│               └── V1__create_transactions_table.sql
│
└── test/
    ├── java/com/prateek/learning/
    │   ├── dsa/
    │   ├── java/
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
└── day-09.md
```

## Testing Approach

The project separates tests by responsibility.

- **Repository unit tests** verify repository contracts and lookup behaviour.
- **Service unit tests** verify business rules and repository delegation.
- **Controller-slice tests** use `@WebMvcTest`, `MockMvc`, and a mocked service.
- **Application integration tests** verify request-to-repository behaviour through the Spring context.
- **JPA integration tests** verify the adapter, mapper, Spring Data repository, and relational persistence path using H2.
- **Exception-handler tests** verify structured API error responses.
- **DSA tests** cover happy paths, edge cases, invalid input, duplicates, ties, and ordering assumptions.

Invalid HTTP requests are tested to confirm that:

- `400 Bad Request` is returned
- the expected validation message is included
- the service layer is not called

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

### Day 9 Closure Snapshot

- Focused study time: 3 hours 15 minutes
- Confidence: Spring/JPA 8/10; Testing 8/10; SQL 8/10; DSA 6/10; System Design 7/10; Docker/Compose 7/10
- `mvn clean test` passed
- Day 9 changes committed and pushed
- Duplicate transaction handling and service transaction boundaries are complete for the current milestone
- DSA coaching will now include pattern revision, pattern discussion before coding, and one related take-home problem at day closure

## Learning Approach

For each topic:

1. Understand the requirement and assumptions.
2. Write an initial solution.
3. Review correctness and edge cases.
4. Improve design or complexity.
5. Add focused tests.
6. Record key learnings.
7. Commit and push completed work.

## Next Planned Improvements

- Add database-backed integration testing against MySQL where it provides value
- Review the current Flyway version against MySQL 8.4 compatibility
- Continue SQL practice using the relational transaction schema
- Continue DSA practice with heap, map, sorting, and interval patterns
- Continue system-design exercises around transaction processing and idempotency
- Add Spring Security with a concrete authentication/authorization use case and tests
- Add Redis with a concrete caching use case
- Add Kafka with a concrete transaction-processing use case
- Refresh AWS through project work covering IAM, Secrets Manager, RDS/Aurora, S3, CloudWatch, VPC/security-group basics, ALB, and one practical container deployment path such as ECS/Fargate
- Add a backend-focused AI learning track after core backend foundations: LLM fundamentals, Java/Spring integration, structured outputs/tool calling, embeddings/vector search or RAG only with a justified use case, plus AI security, PII, latency, cost, evaluation, and observability
- Add API documentation
- Add observability and production-style logging incrementally

## Project Positioning

This repository represents hands-on learning and career-restart preparation.

Technologies are listed here only after they have been used directly in exercises or project work. Current Java 21, JPA, MySQL, Flyway, Docker, and related work in this repository represents hands-on project practice and is kept distinct from professional experience.
