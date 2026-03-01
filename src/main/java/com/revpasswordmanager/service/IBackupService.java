package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.User;
import java.io.File;

public interface IBackupService {
    String exportVault(User user);

    void importVault(User user, String jsonContent);
}
