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
    public String showAddForm(Model model) {
        model.addAttribute("credential", new CredentialDto());
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
    public String deleteCredential(@RequestParam("id") Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        vaultService.deleteCredential(id, user);
        return "redirect:/dashboard?success=deleted";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Authentication authentication, Model model) {
        // Finding credential logic would normally be in service, but let's assume we
        // can get it from service
        // Need to add getCredentialById to IVaultService if it's missing
        return "edit_credential";
    }
}
