package com.example.helloworld.kinesis;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kinesis")
public class KinesisController {

    private final KinesisService kinesisService;

    public KinesisController(KinesisService kinesisService) {
        this.kinesisService = kinesisService;
    }

    @PostMapping("/stream")
    public String createStream() {
        return kinesisService.ensureStream();
    }

    @PostMapping("/records")
    public ResponseEntity<KinesisRecordResponse> put(@RequestBody KinesisRecordRequest request) {
        return ResponseEntity.ok(kinesisService.put(request));
    }

    @GetMapping("/shards")
    public List<String> shards() {
        return kinesisService.shards();
    }

    @GetMapping("/records")
    public KinesisRecordsResponse records(
            @RequestParam String shardId,
            @RequestParam(required = false) String iterator) {
        return kinesisService.records(shardId, iterator);
    }
}