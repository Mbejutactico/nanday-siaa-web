package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;

/**
 * Aviso de la campana de la barra superior.
 * PENDIENTE: algo por hacer (sigue mientras no se resuelva).
 * NOVEDAD: algo que pasó (deja de contarse cuando el usuario abre la campana).
 */
public class Notificacion {

    public String clase;        // PENDIENTE o NOVEDAD
    public String tipo;         // STOCK, APROBAR, RECIBIR, OT, SOLICITUD
    public String texto;
    public String detalle;
    public int cantidad;
    public String url;
    public boolean ventanaNueva;
    public Timestamp fecha;
    public boolean nueva;
    public String clave;        // identifica la novedad (para avisar una sola vez)
}
