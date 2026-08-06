### Spring Boot and Testing

* Refactored the transaction feature into a feature-oriented package structure.
* Introduced a `TransactionRepository` abstraction.
* Added `InMemoryTransactionRepository` as the current storage implementation.
* Updated `TransactionService` to persist and retrieve transactions through the repository.
* Added service unit tests using Mockito and `ArgumentCaptor`.
* Added repository-level tests.
* Added a full Spring Boot integration test that creates a transaction through POST and retrieves it through GET.
* Confirmed the complete test suite passes with:

```bash
mvn clean test
```

### Testing Boundaries

* Controller-slice tests verify HTTP request handling, status codes, JSON responses, and controller mappings while mocking the service.
* Service unit tests verify business logic while mocking the repository.
* Repository tests verify storage behavior directly.
* Full integration tests verify that the controller, service, repository, exception handling, and Spring context work together.
* Prefer the smallest test boundary that proves the required behavior.

### DSA: Kth Largest Element

* Implemented Kth Largest Element using a min-heap limited to size `k`.
* The heap retains the largest `k` values seen so far.
* The root is the smallest value among those retained values, making it the kth-largest value overall.
* `PriorityQueue.offer()` inserts an element and restores heap order using sift-up.
* `PriorityQueue.poll()` removes the root and restores heap order using sift-down.
* Java `PriorityQueue` uses natural ordering as a min-heap by default.
* A max-heap can be created using:

```java
PriorityQueue<Integer> maxHeap =
        new PriorityQueue<>(Comparator.reverseOrder());
```

### Complexity

```text
Time: O(n log k)
Space: O(k)
```