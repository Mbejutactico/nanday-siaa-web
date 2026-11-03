package com.mycompany.proyectoprueba.modelo;

/** Datos del usuario de la sesión que se muestran al pedir una OT. */
public class ContextoUsuario {

    public final int id;
    /** Nombre y apellido. */
    public final String nombre;
    public final String email;
    public final String rol;
    /** Centro principal (0 si no tiene). */
    public final int idCentro;
    public final String centro;

    public ContextoUsuario(int id, String nombre, String email, String rol, int idCentro, String centro) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.idCentro = idCentro;
        this.centro = centro;
    }
}
