package com.revpasswordmanager.controller;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import com.revpasswordmanager.service.IVaultService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vault")
public class ExportController {

    private static final Logger logger = LogManager.getLogger(ExportController.class);

    @Autowired
    private IVaultService vaultService;

    @Autowired
    private IUserService userService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportVault(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            byte[] data = vaultService.exportVault(user);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"revvault_export.json.enc\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            logger.error("Vault export failed for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/import")
    public String importVault(@RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to import");
            return "redirect:/dashboard";
        }

        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            vaultService.importVault(user, file.getBytes());
            redirectAttributes.addFlashAttribute("success", "Vault imported successfully");
            logger.info("Vault imported successfully for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Vault import failed for user {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }
}
