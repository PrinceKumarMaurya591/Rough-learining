package com.example.helloworld.sqs;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SqsControllerTest {

    @Test
    void sendMessageDelegatesToSqsService() throws Exception {
        SqsService sqsService = mock(SqsService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SqsController(sqsService)).build();

        mockMvc.perform(post("/api/sqs/messages")
                        .contentType("text/plain")
                        .content("hello sqs"))
                .andExpect(status().isOk());

        verify(sqsService).send("hello sqs");
    }
}