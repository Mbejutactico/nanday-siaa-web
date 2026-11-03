package com.mycompany.proyectoprueba.modelo;

/** Totales de la pantalla de stock. */
public class ResumenStock {

    public final int registros;
    public final long unidades;
    public final int sinStock;
    public final int bajoMinimo;
    public final int almacenes;

    public ResumenStock(int registros, long unidades, int sinStock, int bajoMinimo, int almacenes) {
        this.registros = registros;
        this.unidades = unidades;
        this.sinStock = sinStock;
        this.bajoMinimo = bajoMinimo;
        this.almacenes = almacenes;
    }
}
