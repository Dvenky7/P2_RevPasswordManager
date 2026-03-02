package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private IUserService userService;

    @GetMapping("/")
    public String index() {
        logger.info("Accessing index page.");
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        logger.info("Accessing login page.");
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.info("Displaying registration form.");
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult bindingResult, Model model) {
        logger.info("Attempting to register new user with username: {}", registrationDto.getUsername());
        if (bindingResult.hasErrors()) {
            logger.warn("Registration form has errors for user: {}", registrationDto.getUsername());
            return "register";
        }
        try {
            userService.registerUser(registrationDto);
            logger.info("User registered successfully: {}", registrationDto.getUsername());
            return "redirect:/login?success";
        } catch (Exception e) {
            logger.error("Error during user registration for {}: {}", registrationDto.getUsername(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            Authentication authentication, HttpSession session, Model model) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();

            if (newPassword != null && !newPassword.isEmpty()) {
                // Verify old password first
                if (!userService.verifyPassword(user, oldPassword)) {
                    throw new Exception("Old password does not match");
                }

                // If 2FA or security questions are required for change
                session.setAttribute("pendingNewPassword", newPassword);
                session.setAttribute("pendingOldPassword", oldPassword);
                session.setAttribute("pendingName", name);
                session.setAttribute("pendingEmail", email);

                userService.generateOtp(user, "PROFILE_CHANGE");

                return "redirect:/profile/verify-change";
            } else {
                user.setName(name);
                user.setEmail(email);
                userService.updateUser(user);
                return "redirect:/profile?success";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            model.addAttribute("user", user);
            return "profile";
        }
    }

    @GetMapping("/profile/verify-change")
    public String showVerifyChangePage(Authentication authentication, HttpSession session, Model model) {
        if (session.getAttribute("pendingNewPassword") == null) {
            return "redirect:/profile";
        }
        User user = userService.findByUsernameWithSecurityQuestions(authentication.getName()).orElseThrow();
        List<String> questions = user.getSecurityQuestions().stream()
                .map(sq -> sq.getQuestion())
                .collect(Collectors.toList());
        model.addAttribute("questions", questions);
        return "profile_verify_change";
    }

    @PostMapping("/profile/verify-change")
    public String verifyProfileChange(@RequestParam("otp") String otp,
            @RequestParam("answers") List<String> answers,
            Authentication authentication, HttpSession session, Model model) {
        String newPassword = (String) session.getAttribute("pendingNewPassword");
        String oldPassword = (String) session.getAttribute("pendingOldPassword");
        String name = (String) session.getAttribute("pendingName");
        String email = (String) session.getAttribute("pendingEmail");

        if (newPassword == null)
            return "redirect:/profile";

        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        try {
            if (!userService.verifyOtp(user, otp, "PROFILE_CHANGE")) {
                throw new Exception("Invalid or expired verification code");
            }

            if (!userService.verifySecurityQuestions(user.getUsername(), answers)) {
                throw new Exception("Incorrect answers to security questions");
            }

            // All verified, apply changes
            user.setName(name);
            user.setEmail(email);
            userService.updateMasterPassword(user, oldPassword, newPassword);

            session.removeAttribute("pendingNewPassword");
            session.removeAttribute("pendingOldPassword");
            session.removeAttribute("pendingName");
            session.removeAttribute("pendingEmail");

            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            User userWithQuestions = userService.findByUsernameWithSecurityQuestions(user.getUsername()).orElseThrow();
            List<String> questions = userWithQuestions.getSecurityQuestions().stream()
                    .map(sq -> sq.getQuestion())
                    .collect(Collectors.toList());
            model.addAttribute("questions", questions);
            return "profile_verify_change";
        }
    }
}
