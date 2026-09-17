package com.example.helloworld.secretsmanager;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecretsManagerControllerTest {

    @Test
    void createDelegatesToSecretsManagerService() throws Exception {
        SecretsManagerService service = mock(SecretsManagerService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SecretsManagerController(service)).build();

        mockMvc.perform(post("/api/secrets")
                        .contentType("application/json")
                        .content("{\"name\":\"demo-secret\",\"secretString\":\"top-secret\"}"))
                .andExpect(status().isOk());

        verify(service).create("demo-secret", "top-secret");
    }
}