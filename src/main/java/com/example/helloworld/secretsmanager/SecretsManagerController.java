package com.example.helloworld.secretsmanager;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secrets")
public class SecretsManagerController {

    private final SecretsManagerService secretsManagerService;

    public SecretsManagerController(SecretsManagerService secretsManagerService) {
        this.secretsManagerService = secretsManagerService;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody SecretRequest request) {
        return ResponseEntity.ok(secretsManagerService.create(request.name(), request.secretString()));
    }

    @GetMapping("/{*name}")
    public ResponseEntity<String> get(@PathVariable String name) {
        return ResponseEntity.ok(secretsManagerService.get(name));
    }

    @PutMapping("/{*name}")
    public ResponseEntity<String> update(
            @PathVariable String name,
            @RequestBody SecretRequest request) {
        return ResponseEntity.ok(secretsManagerService.update(name, request.secretString()));
    }

    @DeleteMapping("/{*name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        secretsManagerService.delete(name);
        return ResponseEntity.noContent().build();
    }
}