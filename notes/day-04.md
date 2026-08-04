# Day 4

## Spring dependency injection and layering

- Reviewed dependency injection and inversion of control
- Reviewed why constructor injection is preferred over field injection
- Kept the controller responsible for HTTP request/response handling
- Kept business logic in `TransactionService`
- Refactored the controller to delegate to the service
- Added `@Service` to `TransactionService`
- Moved in-memory transaction data out of the controller
- Added a public `findById(String transactionId)` service method

## Spring Boot application setup

- Added the Spring Boot application entry point
- Configured component scanning from the parent package
- Added Maven compiler parameter metadata support
- Resolved path-variable binding error caused by missing parameter-name metadata
- Successfully started and called the application endpoint

## GET transaction endpoint

- Added `GET /transactions/{transactionId}`
- Used `@PathVariable`
- Returned `200 OK` for an existing transaction
- Returned `404 Not Found` through `GlobalExceptionHandler` for a missing transaction
- Added MockMvc tests for success and not-found responses
- Reviewed `@WebMvcTest`, `@MockitoBean`, `MockMvc`, Mockito stubbing, and `jsonPath`

## GET transactions by account endpoint

- Added `GET /transactions/account/{accountId}`
- Added a public service wrapper for `findByAccountId`
- Returned matching transactions for an existing account
- Returned `200 OK` with an empty JSON array for an unknown account
- Added MockMvc tests for populated and empty array responses
- Reviewed JSONPath array assertions such as `$[0].id` and `$.length()`

## POST transaction endpoint

- Added `CreateTransactionRequest` record
- Added `POST /transactions`
- Returned `201 Created` for a valid request
- Added validation for:
    - null request
    - blank transaction ID
    - blank account ID
    - null amount
    - zero or negative amount
    - blank transaction type
- Used `InvalidTransactionAmountException` for invalid amounts
- Created a `Transaction` with `LocalDateTime.now()`
- Added isolated MockMvc tests using `thenReturn()` and `thenThrow()`
- Avoided `thenCallRealMethod()` in controller tests
- Added direct `TransactionService` tests for validation and DTO-to-domain mapping
- Maven tests passed

## DSA — Top K Frequent Elements

- Implemented frequency counting using `HashMap`
- Implemented a size-limited min-heap using `PriorityQueue`
- Kept only the top `k` frequent elements
- Compared the heap approach with a full-sort stream solution
- Reviewed `O(n log k)` versus `O(n log n)`
- Added tests for null input, empty input, invalid `k`, negative numbers, ties, one distinct value, and `k` greater than the distinct count
- Avoided assuming output order
- Maven tests passed

## SQL — Subqueries and Window Functions

- Learned the purpose of window functions
- Reviewed `PARTITION BY` and `ORDER BY`
- Used `ROW_NUMBER()` to return one highest transaction per account
- Used `RANK()` to rank transactions within each account
- Used `DENSE_RANK()` to find the second-highest distinct amount per account
- Used an outer query to filter window-function results
- Reviewed why window-function aliases cannot normally be used in the same query's `WHERE` clause
- Used a scalar subquery to return transactions above the overall average amount
- Reviewed differences between `ROW_NUMBER()`, `RANK()`, and `DENSE_RANK()`