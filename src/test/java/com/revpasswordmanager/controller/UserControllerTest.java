package com.revpasswordmanager.controller;

import com.revpasswordmanager.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @Test
    public void testShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "new@example.com")
                .param("masterPassword", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));
    }

    @Test
    public void testRegisterUser_ValidationError() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "") // Invalid
                .param("email", "invalid-email")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }
}
