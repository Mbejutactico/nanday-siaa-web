package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Línea de una OC con el estado de la OC, para editarla o recibirla. */
public class LineaOC {

    public int idDetalle;
    public int idCompra;
    public String estadoOC;
    public String tipoItem;
    public int idInsumo;
    public int idEquipo;
    public int cantidad;
    public int recibida;
    public int lineasOC;
    public BigDecimal precio;
    public String descripcion;

    public boolean isEquipo() {
        return "EQUIPO".equals(tipoItem);
    }

    public int getPendiente() {
        return cantidad - recibida;
    }
}
