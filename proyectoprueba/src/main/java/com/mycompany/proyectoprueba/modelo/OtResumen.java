package com.mycompany.proyectoprueba.modelo;

/** OT abierta para el panel principal. */
public class OtResumen {

    public final int id;
    public final String numero;
    public final String titulo;
    public final String estado;
    public final String centro;
    public final String centroCosto;
    public final int dias;
    public final int items;
    public final String responsable;

    public OtResumen(int id, String numero, String titulo, String estado, String centro, String centroCosto, int dias, int items, String responsable) {
        this.id = id;
        this.numero = numero;
        this.titulo = titulo;
        this.estado = estado;
        this.centro = centro;
        this.centroCosto = centroCosto;
        this.dias = dias;
        this.items = items;
        this.responsable = responsable;
    }
}
