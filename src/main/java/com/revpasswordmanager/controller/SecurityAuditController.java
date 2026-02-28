package com.revpasswordmanager.controller;

import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.ISecurityAuditService;
import com.revpasswordmanager.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/audit")
public class SecurityAuditController {

    @Autowired
    private ISecurityAuditService auditService;

    @Autowired
    private IUserService userService;

    @GetMapping
    public String showAuditReport(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        List<Credential> weakPasswords = auditService.findWeakPasswords(user);
        List<Credential> reusedPasswords = auditService.findReusedPasswords(user);
        List<Credential> oldPasswords = auditService.findOldPasswords(user, 90);

        model.addAttribute("weakPasswords", weakPasswords);
        model.addAttribute("reusedPasswords", reusedPasswords);
        model.addAttribute("oldPasswords", oldPasswords);

        return "audit_report";
    }
}
