package com.revpasswordmanager.dto;

import lombok.Data;

@Data
public class CredentialDto {
    private Long id;
    private String accountName;
    private String username;
    private String password; // Raw password for UI
    private String url;
    private String notes;
}


