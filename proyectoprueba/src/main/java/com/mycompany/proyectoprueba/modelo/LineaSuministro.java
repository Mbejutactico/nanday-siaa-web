package com.mycompany.proyectoprueba.modelo;

/**
 * Línea pedida al suministrar insumos a una OT (insumo, almacén de salida y cantidad).
 * Si la línea enviada no se pudo leer, errorLectura guarda el error: se informa al
 * llegar a esa línea, igual que antes (las anteriores ya validadas no cambian el mensaje).
 */
public class LineaSuministro {

    public final int idInsumo;
    public final int idAlmacen;
    public final int cantidad;
    public final RuntimeException errorLectura;

    public LineaSuministro(int idInsumo, int idAlmacen, int cantidad) {
        this.idInsumo = idInsumo;
        this.idAlmacen = idAlmacen;
        this.cantidad = cantidad;
        this.errorLectura = null;
    }

    public LineaSuministro(RuntimeException errorLectura) {
        this.idInsumo = 0;
        this.idAlmacen = 0;
        this.cantidad = 0;
        this.errorLectura = errorLectura;
    }
}
