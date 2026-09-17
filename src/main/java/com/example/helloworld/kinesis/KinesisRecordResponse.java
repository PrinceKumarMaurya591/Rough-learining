package com.example.helloworld.kinesis;

public record KinesisRecordResponse(
        String sequenceNumber,
        String shardId,
        String nextShardIterator) {
}