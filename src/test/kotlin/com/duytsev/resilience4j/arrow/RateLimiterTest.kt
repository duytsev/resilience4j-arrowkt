package com.duytsev.resilience4j.arrow

import arrow.core.left
import arrow.core.right
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.time.Duration

@ExtendWith(MockKExtension::class)
class RateLimiterTest {

    val config = RateLimiterConfig
        .custom()
        .limitRefreshPeriod(Duration.ofSeconds(10))
        .limitForPeriod(3)
        .timeoutDuration(Duration.ZERO)
        .build()

    @MockK
    lateinit var helloWorldService: HelloWorldService

    @Test
    fun `should execute successful function`() {
        val rateLimiter = RateLimiter.of("test", config)
        val metrics = rateLimiter.metrics

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val result = rateLimiter.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(result).isEqualTo("Hello")
        expect {
            that(metrics.availablePermissions).isEqualTo(2)
            that(metrics.numberOfWaitingThreads).isEqualTo(0)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should execute function with failure`() {
        val rateLimiter = RateLimiter.of("test", config)
        val metrics = rateLimiter.metrics

        val ex = RuntimeException("test")
        every { helloWorldService.returnHelloWorld() } returns ex.left()

        val result = rateLimiter.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectLeft(result).isEqualTo(ex)
        expect {
            that(metrics.availablePermissions).isEqualTo(2)
            that(metrics.numberOfWaitingThreads).isEqualTo(0)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should not execute function when rate limit reached`() {
        val rateLimiter = RateLimiter.of("test", config)
        val metrics = rateLimiter.metrics

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        repeat(3) {
            rateLimiter.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }
        }
        val result = rateLimiter.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectLeft(result).isA<RequestNotPermitted>()
        expect {
            that(metrics.availablePermissions).isEqualTo(0)
            that(metrics.numberOfWaitingThreads).isEqualTo(0)
        }
        verify(exactly = 3) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should decorate successful function`() {
        val rateLimiter = RateLimiter.of("test", config)
        val metrics = rateLimiter.metrics

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val func = rateLimiter.decorateArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(func()).isEqualTo("Hello")
        expect {
            that(metrics.availablePermissions).isEqualTo(2)
            that(metrics.numberOfWaitingThreads).isEqualTo(0)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }
}
