package com.mycompany.proyectoprueba.modelo;

/** Usuario que puede ser responsable de una OT (activo, rol ADMINISTRADOR, GERENTE o AUXILIAR). */
public class ResponsableOT {

    public final int id;
    public final String nombre;
    public final String rol;
    /** Centro principal (0 si no tiene). */
    public final int idCentro;
    public final String centro;

    public ResponsableOT(int id, String nombre, String rol, int idCentro, String centro) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
        this.idCentro = idCentro;
        this.centro = centro;
    }
}
