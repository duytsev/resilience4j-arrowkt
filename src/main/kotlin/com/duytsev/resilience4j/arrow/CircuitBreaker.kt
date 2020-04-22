package com.duytsev.resilience4j.arrow

import arrow.core.Either
import arrow.core.left
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import java.util.concurrent.TimeUnit

/**
 * Returns a supplier which is decorated by a CircuitBreaker.
 *
 * @param supplier       the original supplier
 * @param <T>            the type of results supplied by this supplier
 * @return a supplier which is decorated by a CircuitBreaker.
 */
fun <E : Exception, T> CircuitBreaker.decorateArrowEitherSupplier(
    supplier: () -> Either<E, T>
): () -> Either<Exception, T> = {
    executeArrowEitherSupplier(supplier)
}

/**
 * Executes the decorated Supplier.
 *
 * @param supplier the original Supplier
 * @param <T>      the type of results supplied by this supplier
 * @return the result of the decorated Supplier.
 */
fun <E : Exception, T> CircuitBreaker.executeArrowEitherSupplier(
    supplier: () -> Either<E, T>
): Either<Exception, T> {
    return if (tryAcquirePermission()) {
        acquirePermission()
        val start = System.nanoTime()
        val result = supplier()
        val durationInNanos = System.nanoTime() - start
        result.fold(
            ifLeft = { onError(durationInNanos, TimeUnit.NANOSECONDS, it) },
            ifRight = { onSuccess(durationInNanos, TimeUnit.NANOSECONDS) }
        )
        result
    } else {
        CallNotPermittedException.createCallNotPermittedException(this).left()
    }
}
