package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Recepción de una OC (tabla recepciones_oc) con sus líneas. */
public class RecepcionOC {

    public int id;
    public int idCompra;
    public String numero;
    public Timestamp fecha;
    public boolean historica;
    public boolean anulada;
    public Timestamp fechaAnulacion;
    public String motivoAnulacion;
    public String usuario;
    public String anuladoPor;
    // Datos de la OC (para anular)
    public String estadoOC;
    public String numeroOC;
    public int idCentroDestino;
    public final List<LineaRecepcion> lineas = new ArrayList<LineaRecepcion>();
}
