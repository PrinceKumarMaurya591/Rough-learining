package com.example.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HelloControllerTest {

    @Test
    void helloReturnsHelloWorld() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }
}
