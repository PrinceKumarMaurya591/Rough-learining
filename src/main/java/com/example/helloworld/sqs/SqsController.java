package com.example.helloworld.sqs;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sqs")
public class SqsController {

    private final SqsService sqsService;

    public SqsController(SqsService sqsService) {
        this.sqsService = sqsService;
    }

    @PostMapping("/queue")
    public String createQueue() {
        return sqsService.ensureQueue();
    }

    @PostMapping(value = "/messages", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> send(@RequestBody String body) {
        return ResponseEntity.ok(sqsService.send(body));
    }

    @GetMapping("/messages")
    public List<SqsMessage> receive(
            @RequestParam(defaultValue = "1") int maxMessages) {
        return sqsService.receive(maxMessages);
    }

    @DeleteMapping("/messages")
    public ResponseEntity<Void> delete(@RequestBody SqsDeleteRequest request) {
        sqsService.delete(request.receiptHandle());
        return ResponseEntity.noContent().build();
    }
}