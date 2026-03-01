package com.revpasswordmanager.controller;

import com.revpasswordmanager.service.IUserService;
import com.revpasswordmanager.service.IVaultService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CredentialController.class)
public class VaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IVaultService vaultService;

    @MockitoBean
    private IUserService userService; // Needed because of Security configuration/Authentication principal usage

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    public void testDashboard() throws Exception {
        when(vaultService.getCredentialsByUser(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("credentials"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    public void testShowAddCredentialForm() throws Exception {
        mockMvc.perform(get("/vault/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_credential"))
                .andExpect(model().attributeExists("credential"));
    }
}
