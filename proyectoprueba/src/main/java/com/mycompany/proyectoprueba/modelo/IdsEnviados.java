package com.mycompany.proyectoprueba.modelo;

import java.util.List;

/**
 * Lista de ids enviada por la pantalla (JSON). Los errores de lectura se informan recién
 * cuando el servicio necesita la lista o ese id, en el mismo orden que antes.
 */
public class IdsEnviados {

    private final List<Object> ids;
    private final RuntimeException errorLectura;

    /** ids: Integer o la RuntimeException que dio al leer ese elemento. */
    public IdsEnviados(List<Object> ids) {
        this.ids = ids;
        this.errorLectura = null;
    }

    public IdsEnviados(RuntimeException errorLectura) {
        this.ids = null;
        this.errorLectura = errorLectura;
    }

    public int cantidad() {
        if (errorLectura != null) {
            throw errorLectura;
        }
        return ids.size();
    }

    public int id(int i) {
        Object v = ids.get(i);
        if (v instanceof RuntimeException) {
            throw (RuntimeException) v;
        }
        return ((Integer) v).intValue();
    }
}
