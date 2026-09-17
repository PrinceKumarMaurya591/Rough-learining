package com.example.helloworld.sns;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SnsControllerTest {

    @Test
    void publishDelegatesToSnsService() throws Exception {
        SnsService snsService = mock(SnsService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SnsController(snsService)).build();

        mockMvc.perform(post("/api/sns/publish")
                        .contentType("application/json")
                        .content("{\"subject\":\"Practice\",\"message\":\"hello sns\"}"))
                .andExpect(status().isOk());

        verify(snsService).publish("Practice", "hello sns");
    }
}