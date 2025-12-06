package com.seohee.online.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class FutureConfig {

    @Bean
    public ExecutorService customExecutor() {
        return new ThreadPoolExecutor(
                20,
                100,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(200)
        );
    }
}
