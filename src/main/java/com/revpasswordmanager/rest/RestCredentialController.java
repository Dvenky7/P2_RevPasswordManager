package com.revpasswordmanager.rest;

import com.revpasswordmanager.service.IPasswordGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RestCredentialController {

    @Autowired
    private IPasswordGeneratorService generatorService;

    @GetMapping("/generate-password")
    public String generatePassword(@RequestParam(value = "length", defaultValue = "16") int length) {
        return generatorService.generatePasswords(length, true, true, true, true, false, 1).get(0);
    }
}
