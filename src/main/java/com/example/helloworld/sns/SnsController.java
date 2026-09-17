package com.example.helloworld.sns;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.services.sns.model.Subscription;

@RestController
@RequestMapping("/api/sns")
public class SnsController {

    private final SnsService snsService;

    public SnsController(SnsService snsService) {
        this.snsService = snsService;
    }

    @PostMapping("/topic")
    public String createTopic() {
        return snsService.ensureTopic();
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestBody SnsPublishRequest request) {
        return ResponseEntity.ok(snsService.publish(request.subject(), request.message()));
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<String> subscribe(@RequestBody SnsSubscribeRequest request) {
        return ResponseEntity.ok(snsService.subscribe(request.protocol(), request.endpoint()));
    }

    @GetMapping("/subscriptions")
    public List<Subscription> subscriptions() {
        return snsService.subscriptions();
    }
}