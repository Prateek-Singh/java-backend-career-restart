package com.prateek.learning.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class RedisCacheErrorHandlerTest {

    private final RedisCacheErrorHandler handler =
            new RedisCacheErrorHandler();

    private final Cache cache = mock(Cache.class);

    @Test
    void shouldNotThrowWhenCacheGetFails() {
        RuntimeException exception =
                new RuntimeException("Redis GET failed");

        assertDoesNotThrow(() ->
                handler.handleCacheGetError(
                        exception,
                        cache,
                        "TXN-101"
                )
        );
    }

    @Test
    void shouldNotThrowWhenCachePutFails() {
        RuntimeException exception =
                new RuntimeException("Redis PUT failed");

        assertDoesNotThrow(() ->
                handler.handleCachePutError(
                        exception,
                        cache,
                        "TXN-101",
                        new Object()
                )
        );
    }

    @Test
    void shouldNotThrowWhenEvictFails() {
        RuntimeException exception =
                new RuntimeException("Redis Evict failed");

        assertDoesNotThrow(() ->
                handler.handleCacheEvictError(
                        exception,
                        cache,
                        "TXN-101"
                )
        );
    }

    @Test
    void shouldNotThrowWhenClearFails() {
        RuntimeException exception =
                new RuntimeException("Redis Clear failed");

        assertDoesNotThrow(() ->
                handler.handleCacheClearError(
                        exception,
                        cache
                )
        );
    }
}