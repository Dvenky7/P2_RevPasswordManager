package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.User;
import java.io.File;

public interface IBackupService {
    String exportVault(User user) throws Exception;

    void importVault(User user, String jsonContent) throws Exception;
}
