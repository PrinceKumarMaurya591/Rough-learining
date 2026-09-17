package com.example.helloworld.kinesis;

import java.util.List;

public record KinesisRecordsResponse(
        List<String> records,
        String shardId,
        String nextShardIterator) {
}