package com.revpasswordmanager.security;

import com.revpasswordmanager.service.IUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEvents {

    private static final Logger logger = LogManager.getLogger(AuthenticationEvents.class);

    @Autowired
    private IUserService userService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        String username = success.getAuthentication().getName();
        logger.info("Successful login for user: {}", username);
        userService.resetFailedAttempts(username);
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent failure) {
        String username = failure.getAuthentication().getName();
        logger.warn("Failed login attempt for user: {}", username);
        userService.incrementFailedAttempts(username);
    }
}
