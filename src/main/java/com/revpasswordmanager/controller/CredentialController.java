package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import com.revpasswordmanager.service.IVaultService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vault")
public class CredentialController {

    @Autowired
    private IVaultService vaultService;

    @Autowired
    private IUserService userService;

    @GetMapping("/add")
    public String showAddForm(@RequestParam(value = "prefilledPassword", required = false) String prefilledPassword,
            Model model) {
        CredentialDto dto = new CredentialDto();
        if (prefilledPassword != null) {
            dto.setPassword(prefilledPassword);
        }
        model.addAttribute("credential", dto);
        return "add_credential";
    }

    @PostMapping("/add")
    public String addCredential(@Valid @ModelAttribute("credential") CredentialDto dto,
            BindingResult bindingResult, Authentication authentication, Model model) throws Exception {
        if (bindingResult.hasErrors()) {
            return "add_credential";
        }
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        vaultService.addCredential(user, dto);
        return "redirect:/dashboard?success=added";
    }

    @PostMapping("/reveal/{id}")
    @ResponseBody
    public ResponseEntity<String> revealPassword(@PathVariable("id") Long id,
            @RequestParam("masterPassword") String masterPassword,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();

            if (!userService.verifyPassword(user, masterPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect master password");
            }

            String password = vaultService.revealPassword(id, user);
            return ResponseEntity.ok(password);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error revealing password");
        }
    }

    @PostMapping("/delete")
    public String deleteCredential(@RequestParam("id") Long id,
            @RequestParam("masterPassword") String masterPassword,
            Authentication authentication, Model model) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();

            if (!userService.verifyPassword(user, masterPassword)) {
                return "redirect:/dashboard?error=invalid_password";
            }

            vaultService.deleteCredential(id, user);
            return "redirect:/dashboard?success=deleted";
        } catch (Exception e) {
            return "redirect:/dashboard?error=delete_failed";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        CredentialDto credential = vaultService.getCredentialById(id, user);
        model.addAttribute("credential", credential);
        return "edit_credential";
    }
}
