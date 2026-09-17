package com.example.helloworld.secretsmanager;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;

@Service
public class SecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;

    public SecretsManagerService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public String create(String name, String secretString) {
        return secretsManagerClient.createSecret(CreateSecretRequest.builder()
                .name(name)
                .secretString(secretString)
                .build()).arn();
    }

    public String get(String name) {
        return secretsManagerClient.getSecretValue(GetSecretValueRequest.builder()
                .secretId(name)
                .build()).secretString();
    }

    public String update(String name, String secretString) {
        return secretsManagerClient.putSecretValue(PutSecretValueRequest.builder()
                .secretId(name)
                .secretString(secretString)
                .build()).arn();
    }

    public void delete(String name) {
        secretsManagerClient.deleteSecret(DeleteSecretRequest.builder()
                .secretId(name)
                .forceDeleteWithoutRecovery(true)
                .build());
    }
}