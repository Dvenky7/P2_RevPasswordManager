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
    public String generate(@RequestParam(value = "length", defaultValue = "12") int length,
            @RequestParam(value = "useUpper", defaultValue = "false") boolean useUpper,
            @RequestParam(value = "useLower", defaultValue = "false") boolean useLower,
            @RequestParam(value = "useDigits", defaultValue = "false") boolean useDigits,
            @RequestParam(value = "useSpecial", defaultValue = "false") boolean useSpecial,
            @RequestParam(value = "excludeSimilar", defaultValue = "false") boolean excludeSimilar,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            Model model) {

        List<String> passwords = generatorService.generatePasswords(length, useUpper, useLower, useDigits, useSpecial,
                excludeSimilar, quantity);
        model.addAttribute("passwords", passwords);
        model.addAttribute("length", length);
        model.addAttribute("useUpper", useUpper);
        model.addAttribute("useLower", useLower);
        model.addAttribute("useDigits", useDigits);
        model.addAttribute("useSpecial", useSpecial);
        model.addAttribute("excludeSimilar", excludeSimilar);
        model.addAttribute("quantity", quantity);

        return "password_generator";
    }
}
