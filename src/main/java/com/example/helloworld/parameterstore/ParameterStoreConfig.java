package com.example.helloworld.parameterstore;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class ParameterStoreConfig {

    @Bean
    SsmClient ssmClient(
            @Value("${app.parameter-store.region}") String region,
            @Value("${app.parameter-store.endpoint:}") String endpoint,
            @Value("${app.parameter-store.access-key}") String accessKey,
            @Value("${app.parameter-store.secret-key}") String secretKey) {
        var builder = SsmClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}