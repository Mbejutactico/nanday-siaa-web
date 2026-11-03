package com.mycompany.proyectoprueba.modelo;

/** Insumo activo con su stock total (todos los almacenes), para el combo de suministro. */
public class InsumoDisponible {

    public final int id;
    public final String codigo;
    public final String descripcion;
    public final long total;

    public InsumoDisponible(int id, String codigo, String descripcion, long total) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.total = total;
    }
}
