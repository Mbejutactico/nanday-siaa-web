package com.mycompany.proyectoprueba.servicio;

/**
 * Validaciones comunes de los servicios. Lanzan ReglaNegocioException con un mensaje
 * para el usuario (el controlador lo muestra con el formato de su pantalla).
 */
public final class Validar {

    private Validar() {
    }

    /** Texto sin espacios al principio ni al final (null queda null). */
    public static String limpio(String v) {
        return v == null ? null : v.trim();
    }

    /** Exige un texto no vacío. Devuelve el texto sin espacios sobrantes. */
    public static String obligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ReglaNegocioException(mensaje);
        }
        return valor.trim();
    }

    /**
     * Revisa el formato de un email: algo@dominio.algo, sin espacios. Se aceptan letras
     * como la ñ (por ejemplo usuario@ñanday.com). Vacío o null se acepta: si el campo es
     * obligatorio, se controla antes con obligatorio().
     */
    public static String email(String valor, String mensaje) {
        String v = limpio(valor);
        if (v == null || v.isEmpty()) {
            return v;
        }
        if (!v.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new ReglaNegocioException(mensaje);
        }
        return v;
    }

    /** Exige que la operación haya afectado al menos un registro (si no, el registro ya no existe). */
    public static void existe(int filas, String mensaje) {
        if (filas == 0) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
