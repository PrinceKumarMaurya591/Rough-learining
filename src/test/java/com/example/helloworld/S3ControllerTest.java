package com.example.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class S3ControllerTest {

    @Test
    void uploadSupportsNestedObjectKeys() throws Exception {
        S3StorageService storageService = mock(S3StorageService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new S3Controller(storageService)).build();

        mockMvc.perform(put("/api/s3/objects/notes/example.txt")
                        .contentType("text/plain")
                        .content("hello s3"))
                .andExpect(status().isNoContent());

        verify(storageService).put("notes/example.txt", "hello s3".getBytes(), "text/plain");
    }
}