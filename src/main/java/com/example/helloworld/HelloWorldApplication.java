package com.example.helloworld;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class HelloWorldApplication {

    private final S3StorageService storageService;

    public HelloWorldApplication(S3StorageService storageService) {
        this.storageService = storageService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void createLearningBucket() {
        if (storageService.autoCreateBucket()) {
            storageService.ensureBucket();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }
}
