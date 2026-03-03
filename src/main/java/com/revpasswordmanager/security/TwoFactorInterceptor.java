package com.revpasswordmanager.security;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TwoFactorInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Only intercept if user is authenticated and not on the verification page or
        // logout
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String path = request.getRequestURI();

            // Allow access to static resources, verification page, and logout
            if (path.startsWith("/css/") || path.startsWith("/js/") || path.equals("/login/verify-2fa")
                    || path.equals("/logout")) {
                return true;
            }

            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user != null && user.getTwoFactorEnabled()) {
                HttpSession session = request.getSession();
                Boolean verified = (Boolean) session.getAttribute("2fa_verified");

                if (verified == null || !verified) {
                    // Check if OTP was already sent in this session
                    Boolean otpSent = (Boolean) session.getAttribute("2fa_otp_sent");
                    if (otpSent == null || !otpSent) {
                        userService.generateOtp(user, "2FA");
                        session.setAttribute("2fa_otp_sent", true);
                    }

                    response.sendRedirect("/login/verify-2fa");
                    return false;
                }
            }
        }

        return true;
    }
}
