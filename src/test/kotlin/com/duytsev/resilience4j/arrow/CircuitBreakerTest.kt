package com.duytsev.resilience4j.arrow

import arrow.core.left
import arrow.core.right
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
class CircuitBreakerTest {

    @MockK
    lateinit var helloWorldService: HelloWorldService

    @Test
    fun `should execute successful function`() {
        val circuitBreaker = CircuitBreaker.ofDefaults("test")
        val metrics = circuitBreaker.metrics
        expectThat(metrics.numberOfBufferedCalls).isEqualTo(0)

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val result = circuitBreaker
            .executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(result).isEqualTo("Hello")
        expect {
            that(metrics.numberOfBufferedCalls).isEqualTo(1)
            that(metrics.numberOfFailedCalls).isEqualTo(0)
            that(metrics.numberOfSuccessfulCalls).isEqualTo(1)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should execute function with failure`() {
        val circuitBreaker = CircuitBreaker.ofDefaults("test")
        val metrics = circuitBreaker.metrics
        expectThat(metrics.numberOfBufferedCalls).isEqualTo(0)

        val ex = RuntimeException("test")
        every { helloWorldService.returnHelloWorld() } returns ex.left()

        val result = circuitBreaker
            .executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectLeft(result).isEqualTo(ex)
        expect {
            that(metrics.numberOfBufferedCalls).isEqualTo(1)
            that(metrics.numberOfFailedCalls).isEqualTo(1)
            that(metrics.numberOfSuccessfulCalls).isEqualTo(0)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should not execute function when open`() {
        val circuitBreaker = CircuitBreaker.ofDefaults("test")
        val metrics = circuitBreaker.metrics
        expectThat(metrics.numberOfBufferedCalls).isEqualTo(0)
        circuitBreaker.transitionToOpenState()

        val result = circuitBreaker
            .executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectLeft(result).isA<CallNotPermittedException>()
        expect {
            that(metrics.numberOfBufferedCalls).isEqualTo(0)
            that(metrics.numberOfNotPermittedCalls).isEqualTo(1)
        }
        verify(exactly = 0) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should decorate successful function`() {
        val circuitBreaker = CircuitBreaker.ofDefaults("test")
        val metrics = circuitBreaker.metrics
        expectThat(metrics.numberOfBufferedCalls).isEqualTo(0)

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val func = circuitBreaker
            .decorateArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(func()).isEqualTo("Hello")
        expect {
            that(metrics.numberOfBufferedCalls).isEqualTo(1)
            that(metrics.numberOfFailedCalls).isEqualTo(0)
            that(metrics.numberOfSuccessfulCalls).isEqualTo(1)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }
}
