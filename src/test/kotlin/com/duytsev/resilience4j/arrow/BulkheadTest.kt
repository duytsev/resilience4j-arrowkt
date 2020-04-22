package com.duytsev.resilience4j.arrow

import arrow.core.left
import arrow.core.right
import io.github.resilience4j.bulkhead.Bulkhead
import io.github.resilience4j.bulkhead.BulkheadConfig
import io.github.resilience4j.bulkhead.BulkheadFullException
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
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@ExtendWith(MockKExtension::class)
class BulkheadTest {

    @MockK
    lateinit var helloWorldService: HelloWorldService

    data class BulkHeadMetrics(
        var permittedEvents: Int = 0,
        var rejectedEvents: Int = 0,
        var finishedEvents: Int = 0
    )

    private fun Bulkhead.registerEventListener(metrics: BulkHeadMetrics): Bulkhead {
        eventPublisher.apply {
            onCallPermitted { metrics.permittedEvents++ }
            onCallRejected { metrics.rejectedEvents++ }
            onCallFinished { metrics.finishedEvents++ }
        }
        return this
    }

    @Test
    fun `should execute successful function`() {
        val metrics = BulkHeadMetrics()
        val bulkhead = Bulkhead.ofDefaults("testName").registerEventListener(metrics)

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val result = bulkhead.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(result).isEqualTo("Hello")
        expect {
            that(metrics.permittedEvents).isEqualTo(1)
            that(metrics.rejectedEvents).isEqualTo(0)
            that(metrics.finishedEvents).isEqualTo(1)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should execute function with failure`() {
        val metrics = BulkHeadMetrics()
        val bulkhead = Bulkhead.ofDefaults("testName").registerEventListener(metrics)

        every { helloWorldService.returnHelloWorld() } returns RuntimeException("error").left()

        val result = bulkhead.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectLeft(result).isA<RuntimeException>()
        expect {
            that(metrics.permittedEvents).isEqualTo(1)
            that(metrics.rejectedEvents).isEqualTo(0)
            that(metrics.finishedEvents).isEqualTo(1)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should not execute function when full`() {
        val metrics = BulkHeadMetrics()
        val bulkhead = Bulkhead.of("testName") {
            BulkheadConfig.custom()
                .maxConcurrentCalls(1)
                .maxWaitDuration(Duration.ZERO)
                .build()
        }.registerEventListener(metrics)

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val txRunningLatch = CountDownLatch(1)
        val txCompletedLatch = CountDownLatch(1)
        val mainStartLatch = CountDownLatch(1)

        thread {
            bulkhead.executeArrowEitherSupplier {
                mainStartLatch.countDown()
                txRunningLatch.await()
                helloWorldService.returnHelloWorld()
            }
            txCompletedLatch.countDown()
        }

        mainStartLatch.await()

        expect {
            that(metrics.permittedEvents).isEqualTo(1)
            that(metrics.rejectedEvents).isEqualTo(0)
            that(metrics.finishedEvents).isEqualTo(0)
        }

        val result = bulkhead.executeArrowEitherSupplier { helloWorldService.returnHelloWorld() }
        expect {
            that(metrics.permittedEvents).isEqualTo(1)
            that(metrics.rejectedEvents).isEqualTo(1)
            that(metrics.finishedEvents).isEqualTo(0)
        }

        txRunningLatch.countDown()
        txCompletedLatch.await()

        expectLeft(result).isA<BulkheadFullException>()
        expect {
            that(metrics.permittedEvents).isEqualTo(1)
            that(metrics.rejectedEvents).isEqualTo(1)
            that(metrics.finishedEvents).isEqualTo(1)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }

    @Test
    fun `should decorate successful function`() {
        val metrics = BulkHeadMetrics()
        val bulkhead = Bulkhead.ofDefaults("testName").registerEventListener(metrics)

        every { helloWorldService.returnHelloWorld() } returns "Hello".right()

        val func = bulkhead.decorateArrowEitherSupplier { helloWorldService.returnHelloWorld() }

        expectRight(func()).isEqualTo("Hello")
        expect {
            that(metrics.permittedEvents).isEqualTo(1)
            that(metrics.rejectedEvents).isEqualTo(0)
            that(metrics.finishedEvents).isEqualTo(1)
        }
        verify(exactly = 1) { helloWorldService.returnHelloWorld() }
    }
}
