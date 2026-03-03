package com.revpasswordmanager.controller;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IBackupService;
import com.revpasswordmanager.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/vault")
public class BackupController {

    @Autowired
    private IBackupService backupService;

    @Autowired
    private IUserService userService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportVault(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        String encryptedContent = backupService.exportVault(user);

        byte[] data = encryptedContent.getBytes(StandardCharsets.UTF_8);
        String filename = "vault_export_" + user.getUsername() + ".rev";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

    @PostMapping("/import")
    public String importVault(@RequestParam("file") MultipartFile file,
            @RequestParam("masterPassword") String masterPassword,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        try {
            if (!userService.verifyPassword(user, masterPassword)) {
                return "redirect:/dashboard?error=invalid_password";
            }

            if (file.isEmpty()) {
                return "redirect:/dashboard?error=empty_file";
            }

            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            backupService.importVault(user, content);
            return "redirect:/dashboard?success=imported";
        } catch (Exception e) {
            return "redirect:/dashboard?error=import_failed";
        }
    }
}
