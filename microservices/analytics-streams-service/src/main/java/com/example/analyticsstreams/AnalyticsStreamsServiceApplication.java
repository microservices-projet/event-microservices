package com.example.analyticsstreams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoRepositories(basePackages = "com.example.analyticsstreams.mongo")
@EnableScheduling
public class AnalyticsStreamsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsStreamsServiceApplication.class, args);
    }
}

