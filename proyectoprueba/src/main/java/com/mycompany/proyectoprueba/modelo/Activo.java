package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/**
 * Activo fijo (tabla equipos).
 *  serial = número de activo / patrimonio (AF-000001), generado al recibir la OC
 *           (mientras no se recibe es un número provisorio PEND-...).
 *  ubicación = centro + centro de costo (sector). Los activos no van a almacenes.
 * Los textos que en la base pueden ser NULL llegan como "" (salvo serial).
 */
public class Activo {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String ASIGNADO = "ASIGNADO";

    private static final String[] ESTADOS = {"SOLICITADO", "DISPONIBLE", "ASIGNADO", "EN_TRANSITO", "EN_REPARACION", "BAJA", "ANULADO"};

    public int id;
    public String serial;
    public String numeroSerie;
    public String codigo;
    public String descripcion;
    /** Estado tal como está en la base (puede ser null). */
    public String estado;
    public String fechaIngreso;
    /** 0 si no tiene. */
    public int idCentro;
    public String centro;
    /** 0 si no tiene. */
    public int idCentroCosto;
    public String centroCosto;
    /** Responsable de la última asignación (null si no hay). */
    public String responsable;
    /** Situación por una solicitud abierta (null si no hay). */
    public String situacion;
    /** Número de la solicitud abierta en la que está (null si no hay). */
    public String solicitudAbierta;
    public String numeroOc;
    public String fechaCompra;
    public String estadoOc;
    public String proveedor;
    public String ruc;
    public String solicitante;
    public BigDecimal valor;

    public static boolean estadoValido(String estado) {
        for (String e : ESTADOS) {
            if (e.equals(estado)) {
                return true;
            }
        }
        return false;
    }

    /** Número de activo para mostrar: el provisorio (PEND-...) de un activo aún no recibido no se muestra. */
    public String numeroActivo() {
        return (serial == null || serial.startsWith("PEND-")) ? "" : serial;
    }

    /** Responsable para mostrar: solo si está asignado. */
    public String responsableActual() {
        return ASIGNADO.equals(estado) ? (responsable == null ? "" : responsable) : "";
    }

    /** Puede entrar en una solicitud nueva de salida o baja. */
    public boolean seleccionable() {
        return solicitudAbierta == null && (DISPONIBLE.equals(estado) || ASIGNADO.equals(estado));
    }

    /** Por qué no se puede seleccionar ("" si se puede o no hay un motivo particular). */
    public String motivoNoSeleccionable() {
        return solicitudAbierta != null ? "En la solicitud " + solicitudAbierta
                : ("EN_REPARACION".equals(estado) ? "En reparación" : ("EN_TRANSITO".equals(estado) ? "En tránsito" : ""));
    }
}
