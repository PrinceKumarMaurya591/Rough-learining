package com.example.helloworld.sqs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsService {

    private final SqsClient sqsClient;
    private final String queueName;

    public SqsService(
            SqsClient sqsClient,
            @Value("${app.sqs.queue-name}") String queueName) {
        this.sqsClient = sqsClient;
        this.queueName = queueName;
    }

    public String ensureQueue() {
        return sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();
    }

    public String send(String body) {
        return sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(ensureQueue())
                .messageBody(body)
                .build()).messageId();
    }

    public List<SqsMessage> receive(int maxMessages) {
        var response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(ensureQueue())
                .maxNumberOfMessages(Math.min(Math.max(maxMessages, 1), 10))
                .waitTimeSeconds(1)
                .build());

        return response.messages().stream()
                .map(this::toMessage)
                .toList();
    }

    public void delete(String receiptHandle) {
        sqsClient.deleteMessage(request -> request
                .queueUrl(ensureQueue())
                .receiptHandle(receiptHandle));
    }

    public String queueName() {
        return queueName;
    }

    private SqsMessage toMessage(Message message) {
        return new SqsMessage(message.messageId(), message.body(), message.receiptHandle());
    }
}