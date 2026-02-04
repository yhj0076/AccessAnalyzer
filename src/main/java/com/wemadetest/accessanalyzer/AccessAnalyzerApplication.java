package com.wemadetest.accessanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AccessAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessAnalyzerApplication.class, args);
    }

}
