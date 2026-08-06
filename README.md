# Java Backend Career Restart

This repository documents my structured return to hands-on Java backend engineering.

It contains progressive exercises and a small Spring Boot API used to rebuild practical confidence in Java, testing, data structures and algorithms, SQL, REST API development, and backend design.

## Current Focus

- Core Java and modern Java practices
- Collections, Streams, Optional, equality, and immutability
- Exception handling and domain-specific exceptions
- JUnit 5 and Mockito
- Spring Boot REST APIs
- MockMvc controller-slice testing
- Data structures and algorithms
- SQL joins, subqueries, and window functions
- Backend layering and repository design
- System design and interview preparation

## Tech Stack Used in This Repository

- Java 21
- Maven
- Spring Boot
- Spring MVC
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
- `404 Not Found` when the transaction is missing

### Get transactions by account ID

```http
GET /transactions/account/{accountId}
```

Returns:

- `200 OK` with matching transactions
- `200 OK` with an empty JSON array when there are no matches

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
- `400 Bad Request` for invalid input

> Current limitation: transaction data is stored in memory inside the service, and newly created transactions are not yet persisted for later retrieval. The next planned step is to introduce an in-memory repository abstraction.

## Repository Structure

```text
src/
├── main/java/com/prateek/learning/
│   ├── CareerRestartApplication.java
│   ├── day01/
│   │   ├── dsa/
│   │   └── java/
│   ├── day02/
│   │   ├── dsa/
│   │   └── java/collections/
│   ├── day03/
│   │   ├── dsa/
│   │   └── java/
│   │       ├── exceptions/
│   │       ├── immutability/
│   │       └── spring/exceptionhandling/
│   └── day04/
│       ├── dsa/
│       └── java/springboot/
└── test/java/com/prateek/learning/
    ├── day01/
    ├── day02/
    ├── day03/
    └── day04/

notes/
├── day-01.md
├── day-02.md
├── day-03.md
└── day-04.md
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

- **Service unit tests** verify business rules directly.
- **Controller-slice tests** use `@WebMvcTest`, `MockMvc`, and a mocked service.
- **Exception-handler tests** verify API error responses.
- **DSA tests** cover happy paths, edge cases, invalid input, duplicates, ties, and ordering assumptions.

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

- Introduce `TransactionRepository`
- Add an in-memory repository implementation
- Persist newly created transactions
- Retrieve newly created transactions through the GET endpoint
- Define duplicate transaction ID behavior
- Add Jakarta Bean Validation
- Add a full Spring Boot integration test
- Refactor the growing Spring application into feature-oriented packages
- Add persistent database integration later

## Project Positioning

This repository represents hands-on learning and career-restart preparation. Technologies are listed here only after they have been used directly in the exercises or project.
f