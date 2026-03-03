package com.revpasswordmanager.controller;

import com.revpasswordmanager.service.IPasswordGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/generator")
public class PasswordGeneratorController {

    @Autowired
    private IPasswordGeneratorService generatorService;

    @GetMapping
    public String showGenerator(Model model) {
        return "password_generator";
    }

    @PostMapping("/generate")
    public String generate(@RequestParam(value = "length", defaultValue = "16") int length,
            @RequestParam(value = "upper", defaultValue = "false") boolean useUpper,
            @RequestParam(value = "lower", defaultValue = "false") boolean useLower,
            @RequestParam(value = "digits", defaultValue = "false") boolean useDigits,
            @RequestParam(value = "special", defaultValue = "false") boolean useSpecial,
            @RequestParam(value = "excludeSimilar", defaultValue = "false") boolean excludeSimilar,
            Model model) {

        List<String> passwords = generatorService.generatePasswords(length, useUpper, useLower, useDigits, useSpecial,
                excludeSimilar, 10);

        model.addAttribute("passwords", passwords);
        model.addAttribute("generatedPassword", passwords.isEmpty() ? "" : passwords.get(0));
        model.addAttribute("length", length);
        model.addAttribute("upper", useUpper);
        model.addAttribute("lower", useLower);
        model.addAttribute("digits", useDigits);
        model.addAttribute("special", useSpecial);
        model.addAttribute("excludeSimilar", excludeSimilar);

        // Add strength for the first password
        if (!passwords.isEmpty()) {
            model.addAttribute("strength", generatorService.calculateStrength(passwords.get(0)));
        }

        return "password_generator";
    }
}
