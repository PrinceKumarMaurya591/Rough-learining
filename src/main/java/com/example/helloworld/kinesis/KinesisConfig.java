package com.example.helloworld.kinesis;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;

@Configuration
public class KinesisConfig {

    @Bean
    KinesisClient kinesisClient(
            @Value("${app.kinesis.region}") String region,
            @Value("${app.kinesis.endpoint:}") String endpoint,
            @Value("${app.kinesis.access-key}") String accessKey,
            @Value("${app.kinesis.secret-key}") String secretKey) {
        var builder = KinesisClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}