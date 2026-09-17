package com.example.helloworld.sqs;

public record SqsMessage(String messageId, String body, String receiptHandle) {
}