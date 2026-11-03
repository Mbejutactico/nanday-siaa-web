package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;

/**
 * Solicitud de salida (SA-000001) o de baja (BA-000001) de activos fijos.
 *   REPARACION    : al proveedor; quedan EN_REPARACION.
 *   TRANSFERENCIA : a otra sucursal (centro + centro de costo); quedan EN_TRANSITO.
 *   ENTREGA_OT    : al centro / centro de costo de una OT; quedan EN_TRANSITO.
 *   BAJA          : al aprobarse quedan en BAJA (definitivo).
 * Estados: PENDIENTE -> APROBADA / RECHAZADA; una salida APROBADA sin recepciones se puede ANULAR.
 * En los listados, los textos NULL de la base llegan como "" (salvo numeroOt).
 */
public class SolicitudActivo {

    public static final String REPARACION = "REPARACION";
    public static final String TRANSFERENCIA = "TRANSFERENCIA";
    public static final String ENTREGA_OT = "ENTREGA_OT";
    public static final String BAJA = "BAJA";

    private static final String[] MOTIVOS_BAJA = {"OBSOLESCENCIA", "ROTURA_IRREPARABLE", "ROBO_PERDIDA", "VENTA", "DONACION", "OTRO"};

    public int id;
    public String numero;
    /** Tipo tal como está en la base. */
    public String tipo;
    public String estado;
    public Timestamp fecha;
    public String observacion;
    public String motivoBaja;
    public String motivoRechazo;
    public String motivoAnulacion;
    public Timestamp fechaResolucion;
    public Timestamp fechaAnulacion;
    public int idRemision;
    public String remision;
    public String solicitante;
    public String resolvio;
    public String proveedor;
    public String centroDestino;
    public String ccDestino;
    /** null si no es de una OT. */
    public String numeroOt;
    public String transportista;
    public int activos;
    public int recibidos;

    // Al resolver (valores de la base, null si no tienen)
    public Integer idProveedor;
    public Integer idCentroDestino;
    public Integer idCcDestino;
    public Integer idOt;
    /** Transportista y vehículo: nombre, apellido, CI, marca, modelo y chapa (tal como están en la base). */
    public String[] datosTransporte;

    public static boolean tipoValido(String tipo) {
        return REPARACION.equals(tipo) || TRANSFERENCIA.equals(tipo) || ENTREGA_OT.equals(tipo) || BAJA.equals(tipo);
    }

    public static boolean motivoBajaValido(String motivo) {
        for (String m : MOTIVOS_BAJA) {
            if (m.equals(motivo)) {
                return true;
            }
        }
        return false;
    }

    /** Destino para los listados. */
    public String destino() {
        return REPARACION.equals(tipo) ? "Proveedor: " + proveedor
                : (BAJA.equals(tipo) ? "Baja" : centroDestino + " / " + ccDestino + (numeroOt == null ? "" : " (" + numeroOt + ")"));
    }
}
