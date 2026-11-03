package com.mycompany.proyectoprueba.modelo;

/** Almacén con stock disponible de un insumo. */
public class AlmacenStock {

    public final int id;
    public final String almacen;
    public final String centro;
    public final int stock;

    public AlmacenStock(int id, String almacen, String centro, int stock) {
        this.id = id;
        this.almacen = almacen;
        this.centro = centro;
        this.stock = stock;
    }
}
