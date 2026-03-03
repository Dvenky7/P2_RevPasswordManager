package com.revpasswordmanager.rest;

import com.revpasswordmanager.service.IPasswordGeneratorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(RestCredentialController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class RestCredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private IPasswordGeneratorService generatorService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.revpasswordmanager.service.IVaultService vaultService;

    @Test
    @WithMockUser(roles = "USER")
    public void testGeneratePassword() throws Exception {
        when(generatorService.generatePasswords(anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(),
                anyBoolean(), anyInt()))
                .thenReturn(Arrays.asList("mockedPassword123"));

        mockMvc.perform(get("/api/generate-password")
                .param("length", "16"))
                .andExpect(status().isOk())
                .andExpect(content().string("mockedPassword123"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetCredentials() throws Exception {
        when(vaultService.getCredentialsByUser(any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/credentials"))
                .andExpect(status().isOk());
    }
}
