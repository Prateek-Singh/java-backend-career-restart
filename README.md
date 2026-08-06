# Java Backend Career Restart

This repository documents my structured return to hands-on Java backend engineering.

It contains progressive exercises and a small Spring Boot API used to rebuild practical confidence in Java, testing, data structures and algorithms, SQL, REST API development, and backend design.

## Current Focus

- Core Java and modern Java practices
- Collections, Streams, Optional, equality, and immutability
- Exception handling and domain-specific exceptions
- JUnit 5 and Mockito
- Spring Boot REST APIs
- Jakarta Bean Validation
- MockMvc controller-slice testing
- Spring Boot integration testing
- Data structures and algorithms
- SQL aggregation, joins, subqueries, and window functions
- Backend layering and repository design
- System design and interview preparation

## Tech Stack Used in This Repository

- Java 21
- Maven
- Spring Boot
- Spring MVC
- Jakarta Bean Validation
- JUnit 5
- Mockito
- MockMvc
- Git

Additional technologies will be added only after they are used in hands-on exercises or projects.

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

- `200 OK` with matching persisted transactions
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

Created transactions are stored through an in-memory repository and can be retrieved through the GET endpoints during the application lifecycle.

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

Description is currently enforced at the HTTP request boundary and is not yet treated as a service-level business invariant.

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
- invalid arguments
- invalid transaction amounts
- Bean Validation failures
- malformed request bodies
- unsupported enum values
- unexpected server errors

Illustrative validation response:

```json
{
  "timestamp": "2026-08-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "accountId cannot be null or blank",
  "path": "/transactions"
}
```

## Repository Structure

```text
src/
├── main/java/com/prateek/learning/
│   ├── CareerRestartApplication.java
│   ├── common/exception/
│   ├── dsa/
│   │   ├── day01/
│   │   ├── day02/
│   │   ├── day03/
│   │   ├── day04/
│   │   ├── day05/
│   │   └── day06/
│   ├── java/
│   │   ├── day01/
│   │   ├── day02/
│   │   └── day03/
│   └── transaction/
│       ├── controller/
│       ├── dto/
│       ├── exception/
│       ├── model/
│       ├── repository/
│       └── service/
└── test/java/com/prateek/learning/
    ├── dsa/
    ├── java/
    └── transaction/

notes/
├── day-01.md
├── day-02.md
├── day-03.md
├── day-04.md
├── day-05.md
└── day-06.md
```

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

## Running the Project

Run all tests:

```bash
mvn clean test
```

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

The application runs on the default Spring Boot port:

```text
http://localhost:8080
```

## Testing Approach

The project separates tests by responsibility:

- **Repository unit tests** verify persistence contracts and lookup behaviour.
- **Service unit tests** verify business rules and repository delegation.
- **Controller-slice tests** use `@WebMvcTest`, `MockMvc`, and a mocked service.
- **Integration tests** verify request-to-repository behaviour through the Spring application context.
- **Exception-handler tests** verify structured API error responses.
- **DSA tests** cover happy paths, edge cases, invalid input, duplicates, ties, and ordering assumptions.

Invalid HTTP requests are tested to confirm that:

- `400 Bad Request` is returned
- the expected validation message is included
- the service layer is not called

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

- Complete the Day 7 review and refactoring checkpoint
- Refactor duplicated service validation without changing behaviour
- Review test names and remove redundant or implementation-detail assertions
- Add Spring Data JPA
- Replace the in-memory repository with a relational database-backed implementation
- Add database integration tests
- Define duplicate transaction ID behaviour explicitly
- Improve validation responses to report multiple field errors when useful
- Continue SQL practice with joins, subqueries, and database-backed exercises
- Continue DSA practice with heap, map, and sorting patterns
- Add API documentation

## Project Positioning

This repository represents hands-on learning and career-restart preparation. Technologies are listed here only after they have been used directly in the exercises or project.
