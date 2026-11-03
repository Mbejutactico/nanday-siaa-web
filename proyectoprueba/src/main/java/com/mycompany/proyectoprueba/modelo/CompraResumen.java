package com.mycompany.proyectoprueba.modelo;

import java.sql.Date;

/** Orden de compra resumida para el panel principal. */
public class CompraResumen {

    public final int id;
    public final String numero;
    public final String proveedor;
    public final Date fecha;
    public final String estado;

    public CompraResumen(int id, String numero, String proveedor, Date fecha, String estado) {
        this.id = id;
        this.numero = numero;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.estado = estado;
    }
}
