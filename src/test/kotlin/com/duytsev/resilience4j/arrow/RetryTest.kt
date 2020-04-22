package com.duytsev.resilience4j.arrow

import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.lang.RuntimeException

@ExtendWith(MockKExtension::class)
class RetryTest {

    @MockK
    lateinit var helloWorldService: HelloWorldService

    @Test
    fun `should execute successful function`() {
        val config = RetryConfig.custom<RetryConfig>().build()
        val retry = Retry.of("test", config)
        val metrics = retry.metrics

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val result = retry.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(result).isEqualTo("Hello")
        expectThat(metrics.numberOfSuccessfulCallsWithoutRetryAttempt).isEqualTo(1)
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should execute function with repeated failures`() {
        val config = RetryConfig.custom<RetryConfig>().build()
        val retry = Retry.of("test", config)
        val metrics = retry.metrics

        every { helloWorldService.returnHelloWorld() } returns RuntimeException("error").left()

        retry.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectThat(metrics.numberOfFailedCallsWithRetryAttempt).isEqualTo(1)
        verify(exactly = config.maxAttempts) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should retry function with errors`() {
        val config = RetryConfig.custom<RetryConfig>().build()
        val retry = Retry.of("test", config)
        val metrics = retry.metrics

        every { helloWorldService.returnHelloWorld() } returns RuntimeException("error").left() andThen "Hello".right()

        val result = retry.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(result).isEqualTo("Hello")
        expectThat(metrics.numberOfSuccessfulCallsWithRetryAttempt).isEqualTo(1)
        verify(exactly = 2) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should ignore exception`() {
        val config = RetryConfig.custom<RetryConfig>()
            .ignoreExceptions(IgnoredException::class.java)
            .build()
        val retry = Retry.of("test", config)
        val metrics = retry.metrics

        every { helloWorldService.returnHelloWorld() } returns IgnoredException().left()

        retry.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectThat(metrics.numberOfFailedCallsWithoutRetryAttempt).isEqualTo(1)
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should decorate successful function`() {
        val config = RetryConfig.custom<RetryConfig>().build()
        val retry = Retry.of("test", config)
        val metrics = retry.metrics

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val func = retry.decorateArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(func()).isEqualTo("Hello")
        expectThat(metrics.numberOfSuccessfulCallsWithoutRetryAttempt).isEqualTo(1)
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `test IO`() {
        val config = RetryConfig.custom<RetryConfig>().build()
        val retry = Retry.of("test", config)
        val metrics = retry.metrics

        every { helloWorldService.returnIO() } returns IO { "Hello" }

        val result = retry.executeIOSupplier { helloWorldService.returnIO() }.unsafeRunSync()

        expectThat(result).isEqualTo("Hello")
        expectThat(metrics.numberOfSuccessfulCallsWithoutRetryAttempt).isEqualTo(1)
        verify(exactly = 1) { helloWorldService.returnIO() }
    }
}
