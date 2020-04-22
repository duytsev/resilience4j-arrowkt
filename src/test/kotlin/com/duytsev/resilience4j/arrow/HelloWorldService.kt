package com.duytsev.resilience4j.arrow

import arrow.core.Either
import arrow.fx.IO

interface HelloWorldService {

    fun returnHelloWorld(): Either<Exception, String>

    fun returnIO(): IO<String>
}

class IgnoredException : RuntimeException("ignored")
