package com.revpasswordmanager.mapper;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.Credential;
import org.springframework.stereotype.Component;

@Component
public class CredentialMapper {

    public CredentialDto toDto(Credential entity) {
        if (entity == null) {
            return null;
        }
        CredentialDto dto = new CredentialDto();
        dto.setId(entity.getId());
        dto.setAccountName(entity.getAccountName());
        dto.setUsername(entity.getUsername());
        dto.setUrl(entity.getUrl());
        dto.setNotes(entity.getNotes());
        return dto;
    }

    public Credential toEntity(CredentialDto dto) {
        if (dto == null) {
            return null;
        }
        Credential entity = new Credential();
        entity.setId(dto.getId());
        entity.setAccountName(dto.getAccountName());
        entity.setUsername(dto.getUsername());
        entity.setUrl(dto.getUrl());
        entity.setNotes(dto.getNotes());
        return entity;
    }
}
