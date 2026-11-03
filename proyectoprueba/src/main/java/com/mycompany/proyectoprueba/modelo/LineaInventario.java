package com.mycompany.proyectoprueba.modelo;

/**
 * Línea enviada por la pantalla en ajustes, transferencias e inventario físico: insumo y
 * cantidad (en el conteo, la cantidad contada). Si la línea no se pudo leer, errorLectura
 * guarda el error y se informa al llegar a esa línea, como antes.
 */
public class LineaInventario {

    public final int idInsumo;
    public final int cantidad;
    public final RuntimeException errorLectura;

    public LineaInventario(int idInsumo, int cantidad) {
        this.idInsumo = idInsumo;
        this.cantidad = cantidad;
        this.errorLectura = null;
    }

    public LineaInventario(RuntimeException errorLectura) {
        this.idInsumo = 0;
        this.cantidad = 0;
        this.errorLectura = errorLectura;
    }

    /** Lanza el error de lectura de la línea, si lo hubo. */
    public LineaInventario leer() {
        if (errorLectura != null) {
            throw errorLectura;
        }
        return this;
    }
}
