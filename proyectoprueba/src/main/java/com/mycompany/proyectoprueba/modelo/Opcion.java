package com.mycompany.proyectoprueba.modelo;

/** Par id / texto para los combos (select) de las pantallas. */
public class Opcion {

    private final int id;
    private final String texto;

    public Opcion(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public int getId() { return id; }
    public String getTexto() { return texto; }
}
