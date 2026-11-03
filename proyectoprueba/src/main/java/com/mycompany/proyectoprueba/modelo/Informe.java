package com.mycompany.proyectoprueba.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resultado de un informe (compras, OT, inventario o activos). La pantalla (JSON) y el PDF
 * usan las mismas filas. Cada fila es un mapa columna -> texto (más "id" para abrir la ficha).
 */
public class Informe {

    /** Máximo de filas de un informe. */
    public static final int LIMITE = 5000;
    /** Máximo de registros con su ficha completa en el PDF detallado (el resumen cuenta todos). */
    public static final int LIMITE_DETALLE = 300;

    public String titulo;
    /** Plantilla del listado general en /reportes. */
    public String plantilla;
    /** Columnas: {clave, título, "L" | "R" (alineación; R = numérica)}. */
    public String[][] columnas;
    public final List<Map<String, Object>> filas = new ArrayList<Map<String, Object>>();
    /** Líneas del resumen: {etiqueta, valor}. */
    public final List<String[]> resumen = new ArrayList<String[]>();
    /** true si se cortó en LIMITE filas. */
    public boolean limitado;

    public void resumen(String etiqueta, String valor) {
        resumen.add(new String[]{etiqueta, valor});
    }

    /** Resumen en una línea, para el PDF. */
    public String resumenTexto() {
        StringBuilder sb = new StringBuilder();
        for (String[] r : resumen) {
            sb.append(sb.length() == 0 ? "" : "   |   ").append(r[0]).append(": ").append(r[1]);
        }
        return sb.toString();
    }

    /** Agrega la fila; devuelve false si ya se alcanzó el límite. */
    public boolean agregar(Map<String, Object> f) {
        if (filas.size() >= LIMITE) {
            limitado = true;
            return false;
        }
        filas.add(f);
        return true;
    }
}
