package com.modsen.taxi.passengerservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

@Configuration
open class ApplicationConfig(
    @Value("\${passenger.scheduler.threadPoolSize}") private val threadPoolSize: Int,
    @Value("\${passenger.scheduler.taskQueueSize}") private val taskQueueSize: Int
) {

    @Bean
    open fun jdbcScheduler(): Scheduler {
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool")
    }
}
