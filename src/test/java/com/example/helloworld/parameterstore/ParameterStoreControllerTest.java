package com.example.helloworld.parameterstore;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ParameterStoreControllerTest {

    @Test
    void putDelegatesToParameterStoreService() throws Exception {
        ParameterStoreService service = mock(ParameterStoreService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ParameterStoreController(service)).build();

        mockMvc.perform(post("/api/parameters")
                        .contentType("application/json")
                        .content("{\"name\":\"/demo/url\",\"value\":\"http://example\",\"secure\":false}"))
                .andExpect(status().isOk());

        verify(service).put("/demo/url", "http://example", false);
    }
}