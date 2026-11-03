package com.mycompany.proyectoprueba.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Funciones auxiliares de texto y fecha compartidas por las capas (antes había una copia
 * idéntica de cada una dentro de varias JSP).
 */
public final class Comun {

    private Comun() {
    }

    /** null -> "". */
    public static String txt(String s) {
        return s == null ? "" : s;
    }

    /** null -> "", y sin espacios al principio ni al final. */
    public static String limpio(String v) {
        return v == null ? "" : v.trim();
    }

    /** true si es null o solo espacios. */
    public static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** "CODIGO - Descripción" de un centro de costo ("" si no tiene código). */
    public static String nombreCC(String codigo, String descripcion) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return "";
        }
        return (descripcion == null || descripcion.trim().isEmpty()) ? codigo : codigo + " - " + descripcion;
    }

    /** dd/MM/yyyy HH:mm ("" si es null). */
    public static String fechaHora(Timestamp t) {
        return t == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(t);
    }
}
