package com.example.helloworld.cloudwatch;

public record CloudWatchMetricRequest(
        String metricName,
        double value,
        String unit) {
}