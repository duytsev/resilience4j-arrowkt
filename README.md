# Resilience4j support for ArrowKt data types

todo add badges here

Provides a collection of decorators for [Resilience4j](https://github.com/resilience4j/resilience4j) types to support
 [Arrow](https://github.com/arrow-kt/arrow) data types.

## Adding to project
todo add this after adding project to bintray

## Usage example

```
// Simulates a Backend Service
interface BackendService {
    fun doSomething(): Either<Exception, String>
}

// Create a Retry with default configuration
val retry = Retry.ofDefaults("backendName")

// Decorate your call with automatic retry
val retryingSupplier = retry.decorateArrowEitherSupplier { backendService.doSomething() }

// and invoke it
val result: Either<Exception, String> = retryingSupplier()

// or simply execute the supplier
val result: Either<Exception, String> = retry.executeArrowEitherSupplier { backendService.doSomething() }
```

## Supported resilience4j types
* Bulkhead
* CircuitBreaker
* RateLimiter
* Retry

## Supported arrow data types
* Either
