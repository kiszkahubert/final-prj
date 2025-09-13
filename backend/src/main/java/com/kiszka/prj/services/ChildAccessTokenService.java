package com.kiszka.prj.services;

import com.kiszka.prj.entities.ChildAccessToken;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.ChildAccessTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class ChildAccessTokenService {
    private final ChildAccessTokenRepository childAccessTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ChildAccessTokenService(ChildAccessTokenRepository childAccessTokenRepository) {
        this.childAccessTokenRepository = childAccessTokenRepository;
    }
    private String generateEightCharPin() {
        StringBuilder pin = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int ascii = 33 + secureRandom.nextInt(122 - 33 + 1); // losowanie 33-122
            pin.append((char) ascii);
        }
        return pin.toString();
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
    public void generateTokenForParent(Parent parent, Kid kid) {
        String pin = generateEightCharPin();
        String qrHash = generateQrHash(parent.getId(), pin);
        ChildAccessToken token = new ChildAccessToken()
                .setPin(pin)
                .setQrHash(qrHash)
                .setParent(parent)
                .setKid(kid);
        childAccessTokenRepository.save(token);
    }
    public Optional<ChildAccessToken> getTokenForPin(String pin){
        return childAccessTokenRepository.findByPin(pin.trim());
    }
}
