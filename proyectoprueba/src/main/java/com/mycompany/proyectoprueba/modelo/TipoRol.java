package com.mycompany.proyectoprueba.modelo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tipos de rol (columna roles.codigo): nombre y explicación para la pantalla.
 * Los permisos de cada tipo están en seguridad/Permisos.java.
 */
public final class TipoRol {

    private static final Map<String, String[]> TIPOS = new LinkedHashMap<String, String[]>();

    static {
        TIPOS.put("ADMINISTRADOR", new String[]{"Administrador", "Acceso total a todas las pantallas y a todos los centros."});
        TIPOS.put("GERENTE", new String[]{"Gerente", "Compras (ver y recibir OC), órdenes de trabajo completas, inventario (con mantenimiento del stock y sus mínimos), activos con aprobaciones e informes. Solo sus centros."});
        TIPOS.put("AUXILIAR", new String[]{"Auxiliar", "Igual que Gerente, pero sin finalizar OT ni aprobar o recibir activos. Solo sus centros."});
        TIPOS.put("ENCARGADO_COMPRAS", new String[]{"Encargado de compras", "Proveedores, insumos, solicitudes y órdenes de compra, OT (sin suministrar ni finalizar) e informes. Todos los centros."});
        TIPOS.put("CONSULTOR", new String[]{"Consultor", "Solo informes. Todos los centros."});
    }

    private TipoRol() {
    }

    /** Código -> {nombre, explicación}, en el orden en que se muestran. */
    public static Map<String, String[]> todos() {
        return Collections.unmodifiableMap(TIPOS);
    }

    public static boolean existe(String codigo) {
        return TIPOS.containsKey(codigo);
    }

    /** Nombre para mostrar ("(sin tipo)" si el código no es válido). */
    public static String nombre(String codigo) {
        String[] t = codigo == null ? null : TIPOS.get(codigo);
        return t == null ? "(sin tipo)" : t[0];
    }

    /** Gerente y Auxiliar trabajan sobre sus centros: necesitan un centro principal. */
    public static boolean necesitaCentro(String codigo) {
        return "GERENTE".equals(codigo) || "AUXILIAR".equals(codigo);
    }
}
