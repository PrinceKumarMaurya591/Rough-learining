package com.example.helloworld.cloudwatch;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cloudwatch")
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;

    public CloudWatchController(CloudWatchService cloudWatchService) {
        this.cloudWatchService = cloudWatchService;
    }

    @PostMapping("/metrics")
    public ResponseEntity<Void> publish(@RequestBody CloudWatchMetricRequest request) {
        cloudWatchService.publish(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/metrics")
    public List<String> metrics() {
        return cloudWatchService.metrics();
    }
}