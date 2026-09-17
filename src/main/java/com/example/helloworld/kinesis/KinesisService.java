package com.example.helloworld.kinesis;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

@Service
public class KinesisService {

    private final KinesisClient kinesisClient;
    private final String streamName;

    public KinesisService(
            KinesisClient kinesisClient,
            @Value("${app.kinesis.stream-name}") String streamName) {
        this.kinesisClient = kinesisClient;
        this.streamName = streamName;
    }

    public String ensureStream() {
        try {
            return describeStream().streamDescription().streamARN();
        } catch (Exception ignored) {
            kinesisClient.createStream(request -> request
                    .streamName(streamName)
                    .shardCount(1));
            return describeStream().streamDescription().streamARN();
        }
    }

    public KinesisRecordResponse put(KinesisRecordRequest request) {
        var response = kinesisClient.putRecord(PutRecordRequest.builder()
                .streamName(streamName)
                .partitionKey(request.partitionKey())
                .data(SdkBytes.fromString(request.data(), StandardCharsets.UTF_8))
                .build());
        return new KinesisRecordResponse(response.sequenceNumber(), response.shardId(), null);
    }

    public List<String> shards() {
        return describeStream().streamDescription().shards().stream()
                .map(shard -> shard.shardId())
                .toList();
    }

    public KinesisRecordsResponse records(String shardId, String iterator) {
        var shardIterator = iterator;
        if (shardIterator == null || shardIterator.isBlank()) {
            shardIterator = kinesisClient.getShardIterator(GetShardIteratorRequest.builder()
                    .streamName(streamName)
                    .shardId(shardId)
                    .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                    .build()).shardIterator();
        }
        var response = kinesisClient.getRecords(GetRecordsRequest.builder()
                .shardIterator(shardIterator)
                .limit(10)
                .build());
        var data = response.records().stream()
                .map(Record::data)
                .map(bytes -> Base64.getEncoder().encodeToString(bytes.asByteArray()))
                .toList();
        return new KinesisRecordsResponse(data, shardId, response.nextShardIterator());
    }

    private software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse describeStream() {
        return kinesisClient.describeStream(DescribeStreamRequest.builder()
                .streamName(streamName)
                .build());
    }
}