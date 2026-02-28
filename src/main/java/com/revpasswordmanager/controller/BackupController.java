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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private IBackupService backupService;

    @Autowired
    private IUserService userService;

    @GetMapping
    public String showBackupPage() {
        return "backup";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportVault(Authentication authentication) throws Exception {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        String encryptedJson = backupService.exportVault(user);

        byte[] data = encryptedJson.getBytes();
        String filename = "vault_backup_" + user.getUsername() + ".rev";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @PostMapping("/import")
    public String importVault(@RequestParam("file") MultipartFile file, Authentication authentication, Model model) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            String encryptedJson = new String(file.getBytes());
            backupService.importVault(user, encryptedJson);
            return "redirect:/dashboard?success=imported";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to import backup: " + e.getMessage());
            return "backup";
        }
    }
}
