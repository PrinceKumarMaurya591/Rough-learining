package com.example.helloworld.secretsmanager;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class SecretsManagerConfig {

    @Bean
    SecretsManagerClient secretsManagerClient(
            @Value("${app.secrets-manager.region}") String region,
            @Value("${app.secrets-manager.endpoint:}") String endpoint,
            @Value("${app.secrets-manager.access-key}") String accessKey,
            @Value("${app.secrets-manager.secret-key}") String secretKey) {
        var builder = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}