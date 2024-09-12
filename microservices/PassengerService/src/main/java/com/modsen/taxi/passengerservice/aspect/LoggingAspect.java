package com.modsen.taxi.passengerservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before("execution(* com.modsen.taxi.passengerservice.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering method: {} with arguments: {}",
                joinPoint.getSignature(),
                Arrays.toString(joinPoint.getArgs())
        );
    }


    @AfterReturning(
            pointcut = "execution(* com.modsen.taxi.passengerservice.service.*.*(..))",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: {} with result: {}",
                joinPoint.getSignature(),
                result
        );
    }


    @AfterThrowing(
            pointcut = "execution(* com.modsen.taxi.passengerservice.service.*.*(..))",
            throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in method: {} with message: {}",
                joinPoint.getSignature(),
                exception.getMessage()
        );
    }
}
