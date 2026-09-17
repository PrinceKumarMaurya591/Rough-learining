package com.example.helloworld.cloudwatch;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

@Service
public class CloudWatchService {

    private final CloudWatchClient cloudWatchClient;
    private final String namespace;

    public CloudWatchService(
            CloudWatchClient cloudWatchClient,
            @Value("${app.cloudwatch.namespace}") String namespace) {
        this.cloudWatchClient = cloudWatchClient;
        this.namespace = namespace;
    }

    public void publish(CloudWatchMetricRequest request) {
        var unit = request.unit() == null || request.unit().isBlank()
                ? StandardUnit.NONE
                : StandardUnit.fromValue(request.unit());
        cloudWatchClient.putMetricData(builder -> builder
                .namespace(namespace)
                .metricData(MetricDatum.builder()
                        .metricName(request.metricName())
                        .value(request.value())
                        .unit(unit)
                        .timestamp(Instant.now())
                        .build()));
    }

    public List<String> metrics() {
        return cloudWatchClient.listMetrics(ListMetricsRequest.builder()
                .namespace(namespace)
                .build()).metrics().stream()
                .map(metric -> metric.metricName())
                .distinct()
                .toList();
    }
}