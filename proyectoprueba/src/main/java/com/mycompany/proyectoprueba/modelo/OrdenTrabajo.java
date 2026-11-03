package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de trabajo (OT): cabecera con sus datos de listado y de ficha, más los insumos
 * suministrados, las remisiones y el historial de eventos.
 * Estados: PENDIENTE (sin insumos) -> EN_PROCESO (con insumos) -> FINALIZADA.
 * Los textos que en la base pueden ser NULL llegan como "".
 */
public class OrdenTrabajo {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String EN_PROCESO = "EN_PROCESO";
    public static final String FINALIZADA = "FINALIZADA";

    public int id;
    public String numero;
    /** Fecha de solicitud (yyyy-MM-dd) o "". */
    public String fecha;
    public String titulo;
    public String descripcion;
    public String estado;
    /** Centro destino: el que resuelve la OT. */
    public int idCentro;
    public String centro;
    public int idCentroCosto;
    public String centroCosto;
    public int idSolicitante;
    /** Quien finalizó la OT (0 si no está finalizada). */
    public int idFinaliza;
    public String solicitante;
    public String solicitanteEmail;
    public String centroSolicitante;
    /** 0 si no tiene responsable. */
    public int idResponsable;
    public String responsable;
    public String responsableEmail;
    public Timestamp fechaCreacion;
    public Timestamp fechaFinalizacion;
    public String finalizadoPor;
    public String observacionFinalizacion;
    public Timestamp fechaAnulacionFin;
    public String anuladoPor;
    public String motivoAnulacionFin;

    // Totales del listado
    public int items;
    public int unidades;
    /** Insumos suministrados sin remisión de salida vigente. */
    public int sinSalida;
    /** Remisiones de salida (insumos o activos) vigentes. */
    public int salidas;

    // Ficha
    public final List<Linea> lineas = new ArrayList<Linea>();
    public final List<Remision> remisiones = new ArrayList<Remision>();
    public final List<Evento> eventos = new ArrayList<Evento>();

    public static boolean abierta(String estado) {
        return PENDIENTE.equals(estado) || EN_PROCESO.equals(estado);
    }

    /** Se puede finalizar: abierta, con alguna salida y sin insumos pendientes de salida (listado). */
    public boolean finalizable() {
        return sinSalida == 0 && salidas > 0 && abierta(estado);
    }

    /** Insumo suministrado a la OT. */
    public static class Linea {
        public int id;
        public String codigo;
        public String descripcion;
        public String unidad;
        public int cantidad;
        public String almacen;
        public String centroAlmacen;
        /** yyyy-MM-dd HH:mm o "". */
        public String fecha;
        public String suministradoPor;
        public String subtotal;
        /** Número de la remisión de salida vigente o "". */
        public String remision;
    }

    /** Remisión (salida) asociada a la OT. */
    public static class Remision {
        public int id;
        public String numero;
        public String tipo;
        public Timestamp fecha;
        public boolean anulada;
        public String motivoAnulacion;
        public String transportista;
    }

    /** Evento del historial (solo inserción). */
    public static class Evento {
        /** yyyy-MM-dd HH:mm */
        public String fecha;
        public String tipo;
        public String detalle;
        public String usuario;
    }

    /** Estado de la OT leído con bloqueo (FOR UPDATE) para operar sobre ella. */
    public static class Bloqueo {
        public String estado;
        public int idCentro;
        public String numero;
        public Integer idCentroCosto;
        public Integer idSolicitante;
        public Integer idResponsable;
        public Integer idFinaliza;
    }

    /** Línea suministrada que se quiere quitar (con lo necesario para devolver el stock). */
    public static class LineaSuministrada {
        public int idOt;
        public int idInsumo;
        public int idAlmacen;
        public int cantidad;
        public BigDecimal precio;
        public String codigo;
        public String descripcion;
        /** Remisión de salida vigente o null. */
        public String remision;
    }
}
