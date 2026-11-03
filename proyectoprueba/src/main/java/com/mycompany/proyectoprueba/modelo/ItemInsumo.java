package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/**
 * Insumo con una cantidad dentro de un documento o almacén: stock de un almacén, insumos
 * pendientes de salida de una OT, líneas de un ajuste o de una transferencia.
 * Los textos que en la base pueden ser NULL llegan como "".
 */
public class ItemInsumo {

    public int idInsumo;
    public String codigo;
    public String nombre;
    public String unidad;
    public int cantidad;
    public BigDecimal costo;
    public String almacen;
}
