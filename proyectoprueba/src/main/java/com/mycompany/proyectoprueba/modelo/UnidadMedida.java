package com.mycompany.proyectoprueba.modelo;

/** Unidad de medida de los insumos (tabla unidad_medida). */
public class UnidadMedida {

    private int id;
    private String nombre;
    private String abreviatura;
    private boolean activo;

    public UnidadMedida() {
    }

    public UnidadMedida(int id, String nombre, String abreviatura, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.abreviatura = abreviatura;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
