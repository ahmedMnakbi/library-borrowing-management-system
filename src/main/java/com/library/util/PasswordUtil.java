package com.library.util;

import com.library.exception.ValidationException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class PasswordUtil {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        Validator.requireNotBlank(plainPassword, "Le mot de passe est obligatoire.");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        Validator.requireNotBlank(plainPassword, "Le mot de passe est obligatoire.");
        Validator.requireNotBlank(storedHash, "Le hash du mot de passe est obligatoire.");
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            throw new ValidationException("Format de hash invalide.");
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt);
        if (expectedHash.length != actualHash.length) {
            return false;
        }
        int result = 0;
        for (int index = 0; index < expectedHash.length; index++) {
            result |= expectedHash[index] ^ actualHash[index];
        }
        return result == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new ValidationException("Impossible de securiser le mot de passe : " + exception.getMessage());
        }
    }
}
