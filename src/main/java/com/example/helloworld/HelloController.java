package com.example.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    private final DynamoDbService dynamoDbService;

public HelloController(DynamoDbService dynamoDbService) {
    this.dynamoDbService = dynamoDbService;
}

    @GetMapping("/hello")
    public String hello() {
        return "Hello World ";
    }


    @GetMapping("/dynamodb/create")
public String createDynamoTable() {
    return dynamoDbService.createTable();

}

@GetMapping("/dynamodb/demo")
public String dynamoDemo() {
    dynamoDbService.putItem("user-1", "Amit");
    dynamoDbService.putItem("user-2", "Priya");
    String user1 = dynamoDbService.getItem("user-1");
    String user2 = dynamoDbService.getItem("user-2");
    return "User-1: " + user1 + ", User-2: " + user2;
}


}
