package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Insumo activo con su precio, para el combo de la solicitud de compra. */
public class InsumoPrecio {

    public final int id;
    public final String descripcion;
    public final BigDecimal precio;

    public InsumoPrecio(int id, String descripcion, BigDecimal precio) {
        this.id = id;
        this.descripcion = descripcion;
        this.precio = precio;
    }
}
