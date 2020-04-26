# Resilience4j support for ArrowKt data types

[![Build Status](https://travis-ci.org/duytsev/resilience4j-arrowkt.svg?branch=master)](https://travis-ci.org/duytsev/resilience4j-arrowkt)
[ ![Download](https://api.bintray.com/packages/duytsev/resilience4j-arrowkt/resilience4j-arrowkt/images/download.svg) ](https://bintray.com/duytsev/resilience4j-arrowkt/resilience4j-arrowkt/_latestVersion)

Provides a collection of decorators for [Resilience4j](https://github.com/resilience4j/resilience4j) types to support
 [Arrow](https://github.com/arrow-kt/arrow) data types.

## Adding to project
<details><summary>Gradle</summary>

```
repositories {
    jcenter()
}

// Note: Arrow and Resilience4j dependencies are necessary
implementation 'io.arrow-kt:arrow-core:$arrowVersion'
implementation 'io.arrow-kt:arrow-syntax:$$arrowVersion'
implementation 'io.github.resilience4j:resilience4j-all:$resilienceVersion'
implementation 'com.duytsev:resilience4j-arrowkt:$version'
```
</details>

<details><summary>Maven</summary>

```
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>

<!--Note: Arrow and Resilience4j dependencies are necessary-->
<dependency>
  <groupId>io.arrow-kt</groupId>
  <artifactId>arrow-core</artifactId>
  <version>${$arrowVersion}</version>
</dependency>

<dependency>
  <groupId>io.arrow-kt</groupId>
  <artifactId>arrow-syntax</artifactId>
  <version>${$arrowVersion}</version>
</dependency>

<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-all</artifactId>
  <version>${$resilienceVersion}</version>
</dependency>

<dependency>
  <groupId>com.duytsev</groupId>
  <artifactId>resilience4j-arrowkt</artifactId>
  <version>${version}</version>
</dependency>
```
</details>

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
