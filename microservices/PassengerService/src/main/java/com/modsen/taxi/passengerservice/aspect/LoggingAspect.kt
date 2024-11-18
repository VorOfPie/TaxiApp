package com.modsen.taxi.passengerservice.aspect

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
open class LoggingAspect {

    private val logger: Logger = LoggerFactory.getLogger(LoggingAspect::class.java)

    @Before("execution(* com.modsen.taxi.passengerservice.service.*.*(..))")
    fun logBefore(joinPoint: JoinPoint) {
        logger.info("Entering method: {} with arguments: {}",
            joinPoint.signature,
            joinPoint.args.joinToString(", ")
        )
    }

    @AfterReturning(
        pointcut = "execution(* com.modsen.taxi.passengerservice.service.*.*(..))",
        returning = "result"
    )
    fun logAfterReturning(joinPoint: JoinPoint, result: Any?) {
        logger.info("Exiting method: {} with result: {}",
            joinPoint.signature,
            result
        )
    }

    @AfterThrowing(
        pointcut = "execution(* com.modsen.taxi.passengerservice.service.*.*(..))",
        throwing = "exception"
    )
    fun logAfterThrowing(joinPoint: JoinPoint, exception: Throwable) {
        logger.error("Exception in method: {} with message: {}",
            joinPoint.signature,
            exception.message
        )
    }
}
