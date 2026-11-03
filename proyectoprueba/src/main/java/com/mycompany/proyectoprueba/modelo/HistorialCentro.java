package com.mycompany.proyectoprueba.modelo;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Un movimiento del historial de accesos de un usuario a un centro (tabla usuario_centros_historial):
 * cuándo se le dio (ALTA), se le cambió (MODIFICACION) o se le quitó (BAJA) el acceso, y quién lo hizo.
 */
public class HistorialCentro {

    public Timestamp fechaRegistro;
    public int idCentro;
    public String centro;
    /** PRINCIPAL (centro asignado del usuario) o ADICIONAL (usuario_centros). */
    public String tipoAcceso;
    /** ALTA, MODIFICACION o BAJA. */
    public String accion;
    public Date desde;
    public Date hasta;
    public String observacion;
    public String registradoPor;
}
