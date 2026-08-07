# Day 7 - Refactoring, Persistence, MySQL and Flyway

## What I worked on

### 1. Service and validation refactoring
- Refactored duplicated transaction validation into focused private methods:
    - `validateTransactionId`
    - `validateAccountId`
    - `validateAmount`
    - `validateTransactionType`
- Removed obsolete sample transaction data from `TransactionService`.
- Kept business/domain validation inside the service so the same rules apply regardless of caller:
    - REST controller
    - Kafka consumer
    - scheduler
    - internal service call
- Reviewed the difference between transport validation and business validation.

### 2. Testing cleanup
- Reviewed service tests and separated responsibilities:
    - concrete in-memory repository tests for behavior
    - Mockito tests for repository collaboration and mapping
- Preserved exact validation messages.
- Full Maven test suite remained green throughout refactoring.

### 3. DSA - Heap
Implemented `K Largest Elements` using a bounded min-heap.

Approach:
- Maintain a min-heap of size `k`.
- Add every number.
- When heap size exceeds `k`, remove the smallest element.
- At the end, the heap contains the `k` largest values.

Complexity:
- Time: `O(n log k)`
- Space: `O(k)`

Added tests for:
- invalid input
- `k = 1`
- `k = n`
- duplicates
- negative numbers
- normal distinct values

### 4. SQL
Solved an aggregation query to find accounts whose total CREDIT amount is greater than 10000.

Key revision:
- `WHERE` filters rows before aggregation.
- `HAVING` filters groups after aggregation.

### 5. System design
Reviewed transaction processing across REST and asynchronous consumers.

Important points:
- Business validation belongs in the reusable service/domain layer.
- Malformed JSON is a transport concern.
- Deterministic business validation failures should generally not be repeatedly retried.
- Temporary database/infrastructure failures may use bounded retries and backoff.
- Kafka processing should not acknowledge a message before the DB transaction succeeds.
- DB success followed by Kafka acknowledgement failure can cause redelivery, therefore idempotency is required.
- `existsById()` alone cannot prevent concurrent duplicate inserts.
- A database unique constraint on `transaction_id` is the final concurrency-safe duplicate guard.
- REST and Kafka can safely share the same service/repository/database rules.

## Persistence implementation

### Domain and persistence separation
Kept the domain `Transaction` model separate from the JPA entity.

Added:

- `TransactionEntity`
- `TransactionEntityMapper`
- `SpringDataTransactionRepository`
- `JpaTransactionRepositoryAdapter`

Architecture:

Controller
→ TransactionService
→ TransactionRepository
→ JpaTransactionRepositoryAdapter
→ SpringDataTransactionRepository
→ Database

This keeps Spring Data/JPA details out of the service layer.

### Entity design
Persistence entity uses:

- `UUID` generated primary key
- unique `transactionId` as business/idempotency identifier
- `BigDecimal` mapped to `DECIMAL(19,2)`
- `EnumType.STRING`
- nullable description
- `Instant` timestamp

Changed the domain timestamp from `LocalDateTime` to `Instant` to represent an unambiguous point in time.

### Repository profiles
Added profile-based repository selection:

- `in-memory` → `InMemoryTransactionRepository`
- `jpa` → `JpaTransactionRepositoryAdapter`

This avoids ambiguous `TransactionRepository` beans and makes infrastructure selection explicit.

## JPA integration testing

Added a JPA adapter integration test using H2.

Verified:
- save transaction
- retrieve by business transaction ID
- retrieve transactions by account ID
- domain ↔ entity mapping
- full adapter → Spring Data → database round trip

Used a test-specific `application-jpa` configuration so runtime MySQL configuration does not interfere with H2 tests.

## MySQL with Docker

Used Docker Compose to run MySQL locally instead of installing MySQL directly.

Runtime credentials are supplied through environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Important learning:
A shell variable must be `export`ed before Maven/Java child processes can access it.

Verified the application starts successfully against real MySQL.

## Flyway

Added:
- `flyway-core`
- `flyway-mysql`

Created:

`V1__create_transactions_table.sql`

Flyway now owns schema creation and migration history.

Changed Hibernate from:

`ddl-auto=update`

to:

`ddl-auto=validate`

Current responsibility split:

Flyway
→ changes/version-controls the schema

Hibernate
→ validates entity/schema compatibility

Verified:
- `flyway_schema_history` created
- V1 applied successfully
- `transactions` table created
- application restart reports schema version 1 and no migration required

Also configured:

`spring.jpa.open-in-view=false`

## Commands used

Start MySQL:

`docker compose up -d`

Stop MySQL:

`docker compose stop`

Run tests:

`mvn clean test`

Run application with JPA profile:

`mvn spring-boot:run -Dspring-boot.run.profiles=jpa`

## Current status

Day 7 completed successfully.

Persistence stack now includes:

Spring Data JPA + H2 integration testing + MySQL + Docker Compose + Flyway.

Known follow-up:
- Review Flyway version because the current version warns that MySQL 8.4 is newer than its tested compatibility range.