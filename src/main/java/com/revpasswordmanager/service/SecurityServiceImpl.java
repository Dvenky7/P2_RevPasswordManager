package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.entity.VerificationCode;
import com.revpasswordmanager.repository.IVerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class SecurityServiceImpl implements ISecurityService {

    @Autowired
    private IVerificationCodeRepository verificationCodeRepository;

    private final Random random = new Random();

    @Transactional
    public String generateVerificationCode(User user, String purpose) {
        String code = String.format("%06d", random.nextInt(1000000));
        VerificationCode vc = new VerificationCode();
        vc.setUser(user);
        vc.setCode(code);
        vc.setPurpose(purpose);
        vc.setExpiryTime(LocalDateTime.now().plusMinutes(5)); // Valid for 5 minutes
        verificationCodeRepository.save(vc);
        // In a real app, send this via email. For simulation, we return it or log it.
        return code;
    }

    @Transactional
    public boolean verifyCode(User user, String code) {
        Optional<VerificationCode> vcOpt = verificationCodeRepository.findByUserAndCodeAndIsUsedFalse(user, code);
        if (vcOpt.isPresent()) {
            VerificationCode vc = vcOpt.get();
            if (vc.getExpiryTime().isAfter(LocalDateTime.now())) {
                vc.setIsUsed(true);
                verificationCodeRepository.save(vc);
                return true;
            }
        }
        return false;
    }
}

