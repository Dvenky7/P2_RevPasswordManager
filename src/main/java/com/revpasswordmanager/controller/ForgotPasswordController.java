package com.revpasswordmanager.controller;

import com.revpasswordmanager.entity.SecurityQuestion;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private IUserService userService;

    @GetMapping
    public String showRequestForm() {
        return "forgot_password_request";
    }

    @PostMapping("/request")
    public String processRequest(@RequestParam("username") String username, HttpSession session, Model model) {
        return userService.findByUsername(username)
                .map(user -> {
                    session.setAttribute("forgotPasswordUser", username);
                    return "redirect:/forgot-password/verify";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Username not found");
                    return "forgot_password_request";
                });
    }

    @GetMapping("/verify")
    public String showVerifyForm(HttpSession session, Model model) {
        String username = (String) session.getAttribute("forgotPasswordUser");
        if (username == null) {
            return "redirect:/forgot-password";
        }

        User user = userService.findByUsernameWithSecurityQuestions(username).orElseThrow();
        List<String> questions = user.getSecurityQuestions().stream()
                .map(SecurityQuestion::getQuestion)
                .collect(Collectors.toList());

        model.addAttribute("questions", questions);
        return "forgot_password_verify";
    }

    @PostMapping("/verify")
    public String processVerify(@RequestParam("answers") List<String> answers, HttpSession session, Model model) {
        String username = (String) session.getAttribute("forgotPasswordUser");
        if (username == null) {
            return "redirect:/forgot-password";
        }

        if (userService.verifySecurityQuestions(username, answers)) {
            session.setAttribute("forgotPasswordVerified", true);
            return "redirect:/forgot-password/reset";
        } else {
            User user = userService.findByUsernameWithSecurityQuestions(username).orElseThrow();
            List<String> questions = user.getSecurityQuestions().stream()
                    .map(SecurityQuestion::getQuestion)
                    .collect(Collectors.toList());
            model.addAttribute("questions", questions);
            model.addAttribute("error", "Incorrect answers to security questions");
            return "forgot_password_verify";
        }
    }

    @GetMapping("/reset")
    public String showResetForm(HttpSession session) {
        String username = (String) session.getAttribute("forgotPasswordUser");
        Boolean verified = (Boolean) session.getAttribute("forgotPasswordVerified");

        if (username == null || verified == null || !verified) {
            return "redirect:/forgot-password";
        }

        return "forgot_password_reset";
    }

    @PostMapping("/reset")
    public String processReset(@RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session, Model model) {
        String username = (String) session.getAttribute("forgotPasswordUser");
        Boolean verified = (Boolean) session.getAttribute("forgotPasswordVerified");

        if (username == null || verified == null || !verified) {
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "forgot_password_reset";
        }

        userService.resetPassword(username, password);
        session.removeAttribute("forgotPasswordUser");
        session.removeAttribute("forgotPasswordVerified");

        return "redirect:/login?success=password-reset";
    }
}
