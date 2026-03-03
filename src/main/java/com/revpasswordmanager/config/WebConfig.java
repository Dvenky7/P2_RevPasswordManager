package com.revpasswordmanager.config;

import com.revpasswordmanager.security.TwoFactorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TwoFactorInterceptor twoFactorInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(twoFactorInterceptor)
                .addPathPatterns("/dashboard/**", "/vault/**", "/profile/**", "/audit/**", "/generator/**",
                        "/two-factor/**")
                .excludePathPatterns("/login/verify-2fa", "/logout", "/css/**", "/js/**", "/images/**");
    }
}
