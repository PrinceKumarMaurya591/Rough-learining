package com.example.helloworld.parameterstore;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

@Service
public class ParameterStoreService {

    private final SsmClient ssmClient;

    public ParameterStoreService(SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    public String put(String name, String value, boolean secure) {
        var request = PutParameterRequest.builder()
                .name(name)
                .value(value)
                .type(secure ? ParameterType.SECURE_STRING : ParameterType.STRING)
                .overwrite(true)
                .build();
        return ssmClient.putParameter(request).version().toString();
    }

    public String get(String name, boolean withDecryption) {
        return ssmClient.getParameter(request -> request
                .name(name)
                .withDecryption(withDecryption)).parameter().value();
    }

    public void delete(String name) {
        ssmClient.deleteParameter(DeleteParameterRequest.builder()
                .name(name)
                .build());
    }
}