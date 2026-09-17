package com.example.helloworld.sns;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.Subscription;

@Service
public class SnsService {

    private final SnsClient snsClient;
    private final String topicName;

    public SnsService(
            SnsClient snsClient,
            @Value("${app.sns.topic-name}") String topicName) {
        this.snsClient = snsClient;
        this.topicName = topicName;
    }

    public String ensureTopic() {
        return snsClient.createTopic(request -> request.name(topicName)).topicArn();
    }

    public String publish(String subject, String message) {
        var request = PublishRequest.builder()
                .topicArn(ensureTopic())
                .message(message);
        if (subject != null && !subject.isBlank()) {
            request.subject(subject);
        }
        return snsClient.publish(request.build()).messageId();
    }

    public String subscribe(String protocol, String endpoint) {
        return snsClient.subscribe(SubscribeRequest.builder()
                .topicArn(ensureTopic())
                .protocol(protocol)
                .endpoint(endpoint)
                .returnSubscriptionArn(true)
                .build()).subscriptionArn();
    }

    public List<Subscription> subscriptions() {
        return snsClient.listSubscriptionsByTopic(ListSubscriptionsByTopicRequest.builder()
                .topicArn(ensureTopic())
                .build()).subscriptions();
    }

    public String topicName() {
        return topicName;
    }
}