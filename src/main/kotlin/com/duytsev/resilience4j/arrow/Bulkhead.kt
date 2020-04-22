package com.duytsev.resilience4j.arrow

import arrow.core.Either
import arrow.core.left
import io.github.resilience4j.bulkhead.Bulkhead
import io.github.resilience4j.bulkhead.BulkheadFullException

/**
 * Returns a supplier which is decorated by a Bulkhead.
 *
 * @param supplier       the original supplier
 * @param <T>            the type of results supplied by this supplier
 * @return a supplier which is decorated by a RateLimiter.
 */
fun <E : Exception, T> Bulkhead.decorateArrowEitherSupplier(
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
fun <E : Exception, T> Bulkhead.executeArrowEitherSupplier(
    supplier: () -> Either<E, T>
): Either<Exception, T> =
    if (tryAcquirePermission()) {
        try {
            supplier()
        } finally {
            this.onComplete()
        }
    } else {
        BulkheadFullException.createBulkheadFullException(this).left()
    }
