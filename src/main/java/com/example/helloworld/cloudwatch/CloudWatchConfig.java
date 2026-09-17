package com.example.helloworld.cloudwatch;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class CloudWatchConfig {

    @Bean
    CloudWatchClient cloudWatchClient(
            @Value("${app.cloudwatch.region}") String region,
            @Value("${app.cloudwatch.endpoint:}") String endpoint,
            @Value("${app.cloudwatch.access-key}") String accessKey,
            @Value("${app.cloudwatch.secret-key}") String secretKey) {
        var builder = CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}