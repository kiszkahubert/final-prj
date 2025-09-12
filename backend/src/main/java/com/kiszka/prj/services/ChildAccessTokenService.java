package com.kiszka.prj.services;

import com.kiszka.prj.entities.ChildAccessToken;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.ChildAccessTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class ChildAccessTokenService {
    private final ChildAccessTokenRepository childAccessTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    public ChildAccessTokenService(ChildAccessTokenRepository childAccessTokenRepository) {
        this.childAccessTokenRepository = childAccessTokenRepository;
    }
    private String generateSixDigitPin() {
        int pin = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(pin);
    }
    private String generateQrHash(int parentId, String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = parentId + ":" + pin + ":" + System.currentTimeMillis() + ":" + secureRandom.nextLong();
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    @Transactional
    public void generateTokenForParent(Parent parent) {
        String pin = generateSixDigitPin();
        String qrHash = generateQrHash(parent.getId(), pin);
        ChildAccessToken token = new ChildAccessToken()
                .setPin(pin)
                .setQrHash(qrHash)
                .setParent(parent);
        childAccessTokenRepository.save(token);
    }
}
