package com.mycompany.proyectoprueba.modelo;

/** Usuario y hash de su contraseña, solo para verificar el inicio de sesión. */
public class Credencial {

    public final Usuario usuario;
    public final String hashPassword;

    public Credencial(Usuario usuario, String hashPassword) {
        this.usuario = usuario;
        this.hashPassword = hashPassword;
    }
}
