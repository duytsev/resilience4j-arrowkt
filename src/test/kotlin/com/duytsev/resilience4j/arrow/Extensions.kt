package com.duytsev.resilience4j.arrow

import arrow.core.Either
import org.junit.jupiter.api.fail
import strikt.api.DescribeableBuilder
import strikt.api.expectThat

fun <A, B> expectRight(either: Either<A, B>): DescribeableBuilder<B> = either.fold(
    ifLeft = { fail("'success' expected, but was: $it") },
    ifRight = { expectThat(it) }
)

fun <A, B> expectLeft(either: Either<A, B>): DescribeableBuilder<A> = either.fold(
    ifLeft = { expectThat(it) },
    ifRight = { fail("'error' expected, but was: $it") }
)
