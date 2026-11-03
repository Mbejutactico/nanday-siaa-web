package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;

/**
 * Activo dentro de una solicitud (solicitudes_activos_detalle) con lo que hace falta para
 * listarlo, recibirlo, rechazarlo o devolverlo a su origen.
 * Estados de línea: PENDIENTE -> EN_CURSO -> RECIBIDO / RECHAZADO_DESTINO (o FINALIZADO en bajas, ANULADO).
 * Ids en 0 = sin dato.
 */
public class LineaSolicitudActivo {

    public int id;
    public int idEquipo;
    public String numeroActivo;
    public String numeroSerie;
    public String codigo;
    public String descripcion;
    public int idCentroOrigen;
    public int idCcOrigen;
    public String centroOrigen;
    /** Código del centro de costo de origen (null si no tiene). */
    public String ccOrigen;
    public String estadoOrigen;
    public String estadoLinea;
    public Timestamp fechaRecepcion;
    public String observacionRecepcion;
    /** Estado actual del activo. */
    public String estadoActual;
    public int idCentroActual;
    public int idCcActual;
    // Datos de la solicitud
    public String solicitud;
    public String tipo;
    public Timestamp fechaResolucion;
    public int idRemision;
    public String remision;
    public int idCentroDestino;
    public int idCcDestino;
    public String centroDestino;
    public String ccDestino;
    public String proveedor;
    /** null si no es de una OT. */
    public String numeroOt;

    /** "Centro / CC" de origen. */
    public String origen() {
        return centroOrigen + (ccOrigen == null ? "" : " / " + ccOrigen);
    }

    /** Destino para la recepción. */
    public String destino() {
        return SolicitudActivo.REPARACION.equals(tipo) ? "Proveedor: " + proveedor
                : centroDestino + " / " + ccDestino + (numeroOt == null ? "" : " (" + numeroOt + ")");
    }
}
