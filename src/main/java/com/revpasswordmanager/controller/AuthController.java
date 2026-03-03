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
            bindingResult.getAllErrors().forEach(error -> {
                logger.warn("Validation error: {} - {}",
                        (error instanceof org.springframework.validation.FieldError)
                                ? ((org.springframework.validation.FieldError) error).getField()
                                : error.getObjectName(),
                        error.getDefaultMessage());
            });
            return "register";
        }
        try {
            userService.registerUser(registrationDto);
            logger.info("User registered successfully. Redirecting to verification for: {}",
                    registrationDto.getUsername());
            return "redirect:/register/verify?username=" + registrationDto.getUsername();
        } catch (Exception e) {
            logger.error("Error during user registration for {}: {}", registrationDto.getUsername(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("mobile") String mobile,
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
                session.setAttribute("pendingMobile", mobile);

                userService.generateOtp(user, "PROFILE_CHANGE");

                return "redirect:/profile/verify-change";
            } else {
                user.setName(name);
                user.setEmail(email);
                user.setMobile(mobile);
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
        String mobile = (String) session.getAttribute("pendingMobile");

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
            user.setMobile(mobile);
            userService.updateMasterPassword(user, oldPassword, newPassword);

            session.removeAttribute("pendingNewPassword");
            session.removeAttribute("pendingOldPassword");
            session.removeAttribute("pendingName");
            session.removeAttribute("pendingEmail");
            session.removeAttribute("pendingMobile");

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

    @GetMapping("/register/verify")
    public String showRegisterVerify(@RequestParam("username") String username, Model model) {
        model.addAttribute("username", username);
        return "register_verify";
    }

    @PostMapping("/register/verify")
    public String verifyRegister(@RequestParam("username") String username,
            @RequestParam("otp") String otp,
            Model model) {
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new Exception("User not found"));

            if (userService.verifyOtp(user, otp, "REGISTRATION")) {
                user.setEnabled(true);
                userService.updateUser(user);
                logger.info("User verified and enabled: {}", username);
                return "redirect:/login?verified";
            } else {
                model.addAttribute("error", "Invalid verification code");
                model.addAttribute("username", username);
                return "register_verify";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "register_verify";
        }
    }

    @GetMapping("/login/verify-2fa")
    public String showLoginVerify2fa(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "login_verify_2fa";
    }

    @PostMapping("/login/verify-2fa")
    public String verifyLogin2fa(@RequestParam("otp") String otp,
            Authentication authentication, HttpSession session, Model model) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            if (userService.verifyOtp(user, otp, "2FA")) {
                session.setAttribute("2fa_verified", true);
                logger.info("2FA verified for user: {}", user.getUsername());
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "Invalid verification code");
                return "login_verify_2fa";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login_verify_2fa";
        }
    }
}
