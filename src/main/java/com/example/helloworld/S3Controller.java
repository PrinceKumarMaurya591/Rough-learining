package com.example.helloworld;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.services.s3.model.S3Object;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3StorageService storageService;

    public S3Controller(S3StorageService storageService) {
        this.storageService = storageService;
    }

    @PutMapping(value = "/objects/{*key}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable String key,
            @RequestHeader(value = "Content-Type", defaultValue = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            String contentType,
            @RequestBody byte[] content) {
        storageService.put(normalizeKey(key), content, contentType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/objects/{*key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) {
        return ResponseEntity.ok(storageService.get(normalizeKey(key)));
    }

    @GetMapping("/objects")
    public List<S3Object> list() {
        return storageService.list();
    }

    @DeleteMapping("/objects/{*key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        storageService.delete(normalizeKey(key));
        return ResponseEntity.noContent().build();
    }

    private String normalizeKey(String key) {
        return key.startsWith("/") ? key.substring(1) : key;
    }
}