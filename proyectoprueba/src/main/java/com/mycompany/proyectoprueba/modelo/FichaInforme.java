package com.mycompany.proyectoprueba.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Toda la información de un registro de un informe (OC, OT, movimiento de stock o de activo).
 * La misma estructura se muestra en pantalla y en el PDF individual (reportes/fichaInforme.jrxml).
 * Bloques en orden: secciones (etiqueta / valor) y tablas (columnas / filas).
 */
public class FichaInforme {

    /** Bloque de la ficha. */
    public static final class Bloque {
        public final boolean tabla;
        public final String titulo;
        /** Solo en tablas. */
        public final String[] columnas;
        /** Secciones: {etiqueta, valor}; tablas: una fila por elemento. */
        public final List<String[]> filas = new ArrayList<String[]>();

        Bloque(boolean tabla, String titulo, String[] columnas) {
            this.tabla = tabla;
            this.titulo = titulo;
            this.columnas = columnas;
        }
    }

    public final String titulo;
    public final String numero;
    public final List<Bloque> bloques = new ArrayList<Bloque>();
    private Bloque actual;

    public FichaInforme(String titulo, String numero) {
        this.titulo = titulo;
        this.numero = numero;
    }

    public void seccion(String t) {
        actual = new Bloque(false, t, null);
        bloques.add(actual);
    }

    /** Campo de la sección actual (se omite si no tiene valor). */
    public void campo(String etiqueta, String valor) {
        if (valor != null && !valor.trim().isEmpty()) {
            actual.filas.add(new String[]{etiqueta, valor});
        }
    }

    public void tabla(String t, String... columnas) {
        actual = new Bloque(true, t, columnas);
        bloques.add(actual);
    }

    public void fila(String... valores) {
        actual.filas.add(valores);
    }

    /** Filas para el PDF: SEC (título), CAMPO (etiqueta, valor), ENC/FILA (tabla de n columnas), NOTA. */
    public List<Map<String, Object>> filasPdf() {
        List<Map<String, Object>> r = new ArrayList<Map<String, Object>>();
        for (Bloque b : bloques) {
            if (!b.tabla && b.filas.isEmpty()) {
                continue; // sección sin datos: no se imprime el título vacío
            }
            r.add(filaPdf("SEC", "0", new String[]{b.titulo}));
            if (!b.tabla) {
                for (String[] c : b.filas) {
                    r.add(filaPdf("CAMPO", "2", c));
                }
            } else if (b.filas.isEmpty()) {
                r.add(filaPdf("NOTA", "0", new String[]{"Sin registros"}));
            } else {
                r.add(filaPdf("ENC", String.valueOf(b.columnas.length), b.columnas));
                for (String[] f : b.filas) {
                    r.add(filaPdf("FILA", String.valueOf(b.columnas.length), f));
                }
            }
        }
        return r;
    }

    /** Fila del PDF con su tipo, cantidad de columnas y hasta 7 valores (c1..c7). */
    public static Map<String, Object> filaPdf(String tipo, String n, String[] v) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("tipo", tipo);
        m.put("n", n);
        for (int i = 0; i < 7; i++) {
            m.put("c" + (i + 1), i < v.length && v[i] != null ? v[i] : "");
        }
        return m;
    }
}
