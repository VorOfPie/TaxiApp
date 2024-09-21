package com.modsen.taxi.passengerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ApplicationConfig {

    @Value("${passenger.scheduler.threadPoolSize}")
    private Integer threadPoolSize;

    @Value("${passenger.scheduler.taskQueueSize}")
    private Integer taskQueueSize;

    @Bean
    public Scheduler jdbcScheduler() {
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool");
    }
}
