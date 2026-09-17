package com.example.helloworld;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;


import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Service
public class DynamoDbService {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Users";

    public DynamoDbService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }





    public void putItem(String userId, String name) {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put("userId", AttributeValue.builder().s(userId).build());
    item.put("name", AttributeValue.builder().s(name).build());

    dynamoDbClient.putItem(PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(item)
            .build());
}

public String getItem(String userId) {
    Map<String, AttributeValue> key = new HashMap<>();
    key.put("userId", AttributeValue.builder().s(userId).build());

    GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(key)
            .build());

    if (response.hasItem()) {
        return response.item().get("name").s();
    }
    return "Not found";
}


    public String createTable() {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("userId")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("userId")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            return "Table created: " + TABLE_NAME;
        } catch (ResourceInUseException e) {
            return "Table already exists: " + TABLE_NAME;
        }
    }
}