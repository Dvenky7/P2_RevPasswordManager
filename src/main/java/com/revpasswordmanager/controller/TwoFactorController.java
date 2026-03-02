package com.revpasswordmanager.controller;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/two-factor")
public class TwoFactorController {

    @Autowired
    private IUserService userService;

    @GetMapping
    public String showTwoFactorPage(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "two_factor";
    }

    @PostMapping("/toggle")
    public String toggleTwoFactor(@RequestParam("enabled") boolean enabled, Authentication authentication) {
        userService.toggleTwoFactor(authentication.getName(), enabled);
        return "redirect:/two-factor?success";
    }
}
