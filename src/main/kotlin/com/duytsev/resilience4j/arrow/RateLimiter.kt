package com.duytsev.resilience4j.arrow

import arrow.core.Either
import arrow.core.left
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RequestNotPermitted

/**
 * Returns a supplier which is decorated by a RateLimiter.
 *
 * @param permits  number of permits that this call requires
 * @param supplier       the original supplier
 * @param <T>            the type of results supplied by this supplier
 * @return a supplier which is decorated by a RateLimiter.
 */
fun <E : Exception, T> RateLimiter.decorateArrowEitherSupplier(
    permits: Int = 1, supplier: () -> Either<E, T>
): () -> Either<Exception, T> = {
    executeArrowEitherSupplier(permits, supplier)
}

/**
 * Executes the decorated Supplier.
 *
 * @param permits  number of permits that this call requires
 * @param supplier the original Supplier
 * @param <T>      the type of results supplied by this supplier
 * @return the result of the decorated Supplier.
 */
fun <E : Exception, T> RateLimiter.executeArrowEitherSupplier(
    permits: Int = 1, supplier: () -> Either<E, T>
): Either<Exception, T> =
    try {
        RateLimiter.waitForPermission(this, permits)
        supplier()
    } catch (e: RequestNotPermitted) {
        e.left()
    }
