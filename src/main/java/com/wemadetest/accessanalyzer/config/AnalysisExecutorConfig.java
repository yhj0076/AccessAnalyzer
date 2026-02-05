package com.wemadetest.accessanalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AnalysisExecutorConfig {
    @Bean
    public ExecutorService analysisExecutor(){
        return Executors.newFixedThreadPool(8);
    }
}
