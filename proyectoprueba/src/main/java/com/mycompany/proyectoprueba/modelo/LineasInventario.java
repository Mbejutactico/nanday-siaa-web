package com.mycompany.proyectoprueba.modelo;

import java.util.List;

/**
 * Lista de líneas enviada por la pantalla. Si la lista entera no se pudo leer, el error se
 * informa recién cuando el servicio la necesita (después de validar los demás datos, como antes).
 */
public class LineasInventario {

    private final List<LineaInventario> lineas;
    private final RuntimeException errorLectura;

    public LineasInventario(List<LineaInventario> lineas) {
        this.lineas = lineas;
        this.errorLectura = null;
    }

    public LineasInventario(RuntimeException errorLectura) {
        this.lineas = null;
        this.errorLectura = errorLectura;
    }

    public List<LineaInventario> obtener() {
        if (errorLectura != null) {
            throw errorLectura;
        }
        return lineas;
    }
}
