package com.mycompany.proyectoprueba.modelo;

/** Quien transporta una remisión y su vehículo (textos sin espacios sobrantes, "" si no se cargaron). */
public class Transportista {

    public final String nombre;
    public final String apellido;
    public final String ci;
    public final String marca;
    public final String modelo;
    public final String chapa;

    public Transportista(String nombre, String apellido, String ci, String marca, String modelo, String chapa) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.ci = ci;
        this.marca = marca;
        this.modelo = modelo;
        this.chapa = chapa;
    }
}
