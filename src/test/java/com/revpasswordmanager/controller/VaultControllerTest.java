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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.revpasswordmanager.entity.User;

@RunWith(SpringRunner.class)
@WebMvcTest({ CredentialController.class, DashboardController.class })
public class VaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private IVaultService vaultService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private IUserService userService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.revpasswordmanager.service.ISecurityAuditService auditService;

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    public void testDashboard() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(vaultService.getCredentialsByUser(any())).thenReturn(Collections.emptyList());
        when(auditService.findWeakPasswords(any())).thenReturn(Collections.emptyList());
        when(auditService.findReusedPasswords(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("credentials"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    public void testShowAddCredentialForm() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/vault/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_credential"))
                .andExpect(model().attributeExists("credential"));
    }
}
