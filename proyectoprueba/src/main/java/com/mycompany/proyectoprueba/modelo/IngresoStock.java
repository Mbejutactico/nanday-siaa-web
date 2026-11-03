package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;
import java.sql.Date;

/** Ingreso de un insumo a un almacén por una compra recibida. */
public class IngresoStock {

    public final String numeroOc;
    public final Date fecha;
    public final String estadoOc;
    public final String proveedor;
    public final int pedido;
    public final int recibido;
    public final BigDecimal precio;

    public IngresoStock(String numeroOc, Date fecha, String estadoOc, String proveedor, int pedido, int recibido, BigDecimal precio) {
        this.numeroOc = numeroOc;
        this.fecha = fecha;
        this.estadoOc = estadoOc;
        this.proveedor = proveedor;
        this.pedido = pedido;
        this.recibido = recibido;
        this.precio = precio;
    }
}
