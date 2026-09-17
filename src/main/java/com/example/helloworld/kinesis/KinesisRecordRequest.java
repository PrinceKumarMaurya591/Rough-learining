package com.example.helloworld.kinesis;

public record KinesisRecordRequest(String partitionKey, String data) {
}