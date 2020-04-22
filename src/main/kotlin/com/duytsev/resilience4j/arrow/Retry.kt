package com.duytsev.resilience4j.arrow

import arrow.core.Either
import arrow.core.Function0
import arrow.core.left
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.fx.handleErrorWith
import io.github.resilience4j.retry.Retry

/**
 * Returns a supplier which is decorated by a Retry.
 *  @param supplier the original function
 *  @param <T>      the type of results supplied by this supplier
 *  @return a retryable function
 */
fun <E : Exception, T> Retry.decorateArrowEitherSupplier(
    supplier: () -> Either<E, T>
): () -> Either<E, T> = {
    executeArrowEitherSupplier(supplier)
}

/**
 * Executes the decorated Supplier.
 *
 * @param supplier the original Supplier
 * @param <T>      the type of results supplied by this supplier
 * @return the result of the decorated Supplier.
 */
fun <E : Exception, T> Retry.executeArrowEitherSupplier(
    supplier: () -> Either<E, T>
): Either<E, T> {
    val context = this.context<T>()
    while (true) {
        val result = supplier()
        result.fold(
            ifLeft = {
                try {
                    context.onError(it)
                } catch (e: Exception) {
                    return it.left()
                }
            },
            ifRight = {
                val validationResult = context.onResult(it)
                if (!validationResult) {
                    context.onComplete()
                    return result
                }
            }
        )
    }
}

fun <T> Retry.executeIOSupplier(
    supplier: () -> IO<T>
): IO<T> {
    val context = this.context<T>()
    while (true) {
        IO.fx {
            val result = supplier().bind()
            val validationResult = context.onResult(result)
            if (!validationResult) {
                context.onComplete()
            }
        }.handleError {
            context.onError(it as Exception)
        }
    }
}
