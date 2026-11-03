package com.mycompany.proyectoprueba.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Contraseñas con hash SHA-256 + BCrypt: se guarda BCrypt( Base64( SHA-256(contraseña) ) ).
 *  - SHA-256 lleva cualquier contraseña a 44 caracteres y evita el límite de 72 bytes de BCrypt.
 *  - BCrypt agrega sal aleatoria y es lento a propósito (costo 10), lo que hace inviable
 *    probar millones de claves si alguien obtiene la base.
 * Solo se aceptan contraseñas guardadas con hash BCrypt.
 */
public final class Password {

    private static final int COSTO = 10;

    private Password() {
    }

    public static String hash(String plano) {
        if (plano == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        return BCrypt.hashpw(sha256Base64(plano), BCrypt.gensalt(COSTO));
    }


    /**
     * Duración mínima de un inicio de sesión fallido: la respuesta tarda siempre lo mismo,
     * así no se puede deducir por el tiempo si el email existe. El bloqueo por intentos
     * está en IntentosLogin.
     */
    public static final long DEMORA_INTENTO_FALLIDO_MS = 1000L;

    public static long inicioIntento() {
        return System.currentTimeMillis();
    }

    public static void demorarIntentoFallido(long inicio) {
        long restante = DEMORA_INTENTO_FALLIDO_MS - (System.currentTimeMillis() - inicio);
        if (restante > 0) {
            try {
                Thread.sleep(restante);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public static boolean verificar(String plano, String almacenado) {
        if (plano == null || almacenado == null) {
            return false;
        }
        if (!esHash(almacenado)) {
            return false;
        }
        try {
            return BCrypt.checkpw(sha256Base64(plano), almacenado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean esHash(String almacenado) {
        return almacenado != null && almacenado.length() == 60 && almacenado.startsWith("$2");
    }

    /** Reglas mínimas para contraseñas nuevas. Devuelve null si es válida o el mensaje de error. */
    public static String validarNueva(String plano) {
        if (plano == null || plano.trim().isEmpty()) {
            return "La contraseña es obligatoria";
        }
        if (plano.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        return null;
    }

    static String sha256Base64(String plano) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
