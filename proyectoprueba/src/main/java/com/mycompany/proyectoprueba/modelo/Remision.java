package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;

/**
 * Remisión: documento de salida (insumos de una OT, transferencia entre almacenes o salida
 * de activos). Los textos que en la base pueden ser NULL llegan como "" salvo tituloOt y tipoSolicitud.
 */
public class Remision {

    public static final String SALIDA_INSUMOS = "SALIDA_INSUMOS";
    public static final String TRANSFERENCIA = "TRANSFERENCIA";
    public static final String SALIDA_ACTIVOS = "SALIDA_ACTIVOS";

    public int id;
    public String numero;
    /** Tipo tal como está en la base (puede ser null). */
    public String tipo;
    public Timestamp fecha;
    public boolean anulada;
    public String motivoAnulacion;
    public Timestamp fechaAnulacion;
    public String transportista;
    public String ci;
    public String emitio;
    public int items;
    /** 0 si no es de una OT. */
    public int idOt;
    public String numeroOt;
    public String tituloOt;
    /** 0 si no es de una transferencia. */
    public int idTransferencia;
    public String numeroTransferencia;
    public String almacenOrigen;
    public String almacenDestino;
    public String numeroSolicitud;
    public String tipoSolicitud;

    /** Documento al que corresponde, para el listado. */
    public String referencia() {
        if (SALIDA_ACTIVOS.equals(tipo)) {
            return "Activos " + numeroSolicitud + ("REPARACION".equals(tipoSolicitud) ? " (reparación)"
                    : "ENTREGA_OT".equals(tipoSolicitud) ? " (" + numeroOt + ")" : " (transferencia)");
        }
        if (TRANSFERENCIA.equals(tipo)) {
            return numeroTransferencia + ": " + almacenOrigen + " -> " + almacenDestino;
        }
        return numeroOt + (tituloOt == null ? "" : " - " + tituloOt);
    }
}
