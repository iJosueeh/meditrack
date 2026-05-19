package com.utp.meditrackapp.core.util;

/**
 * Utilidad para verificar el hash de contraseña proporcionado.
 * Nota: Aunque el comentario en init.sql menciona BCrypt, el proyecto utiliza PBKDF2WithHmacSHA256.
 */
public class HashVerifier {
    public static void main(String[] args) {
        String storedHash = "gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=";
        
        System.out.println("=== Analisis de Hash ===");
        System.out.println("Hash completo: " + storedHash);
        
        String[] parts = storedHash.split(":");
        if (parts.length == 2) {
            System.out.println("Salt (Base64): " + parts[0]);
            System.out.println("Hash (Base64): " + parts[1]);
            
            // Verificacion contra la contrasena documentada en init.sql
            String testPassword = "admin123";
            System.out.println("\nVerificando contra la contrasena: '" + testPassword + "'");
            
            boolean isValid = PasswordHasher.checkPassword(testPassword, storedHash);
            if (isValid) {
                System.out.println("RESULTADO: La contrasena '" + testPassword + "' es CORRECTA para este hash.");
            } else {
                System.out.println("RESULTADO: La contrasena '" + testPassword + "' NO coincide.");
            }
        } else {
            System.out.println("ERROR: El formato del hash no es valido (se esperaba salt:hash)");
        }
    }
}
