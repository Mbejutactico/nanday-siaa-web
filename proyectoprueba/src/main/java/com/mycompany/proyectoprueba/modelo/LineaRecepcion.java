package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Línea de una recepción de OC (tabla recepciones_oc_detalle). */
public class LineaRecepcion {

    public int id;
    public int idRecepcion;
    public int idDetalle;
    public String tipoItem;
    public String codigo;
    public String descripcion;
    public int cantidad;
    public String almacen;
    public String ccCodigo;
    public String ccDescripcion;
    public String numeroActivo;
    // Para anular la recepción
    public int idAlmacen;
    public int idEquipo;
    public int idInsumo;
    public int recibidaEnOC;
    public BigDecimal precio;

    public boolean isEquipo() {
        return "EQUIPO".equals(tipoItem);
    }
}
