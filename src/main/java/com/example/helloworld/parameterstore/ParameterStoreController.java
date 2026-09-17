package com.example.helloworld.parameterstore;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parameters")
public class ParameterStoreController {

    private final ParameterStoreService parameterStoreService;

    public ParameterStoreController(ParameterStoreService parameterStoreService) {
        this.parameterStoreService = parameterStoreService;
    }

    @PostMapping
    public ResponseEntity<String> put(@RequestBody ParameterRequest request) {
        return ResponseEntity.ok(parameterStoreService.put(
                request.name(), request.value(), request.secure()));
    }

    @GetMapping("/{*name}")
    public ResponseEntity<String> get(
            @PathVariable String name,
            @RequestParam(defaultValue = "false") boolean withDecryption) {
        return ResponseEntity.ok(parameterStoreService.get(name, withDecryption));
    }

    @DeleteMapping("/{*name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        parameterStoreService.delete(name);
        return ResponseEntity.noContent().build();
    }
}