package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.ISecurityAuditService;
import com.revpasswordmanager.service.IUserService;
import com.revpasswordmanager.service.IVaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private IVaultService vaultService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISecurityAuditService auditService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();
        List<CredentialDto> credentials = vaultService.getCredentialsByUser(user);

        // Calculate stats
        long totalPasswords = credentials.size();
        long weakPasswordsCount = auditService.findWeakPasswords(user).size();
        long reusedPasswordsCount = auditService.findReusedPasswords(user).size();

        List<CredentialDto> recentlyAdded = credentials.stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("credentials", credentials);
        model.addAttribute("user", user);
        model.addAttribute("totalPasswords", totalPasswords);
        model.addAttribute("weakPasswordsCount", weakPasswordsCount);
        model.addAttribute("reusedPasswordsCount", reusedPasswordsCount);
        model.addAttribute("recentlyAdded", recentlyAdded);

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "profile";
    }
}
