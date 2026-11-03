package com.mycompany.proyectoprueba.modelo;

/** Almacén de un centro (tabla almacenes). */
public class Almacen {

    private int id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private Integer idCentro;
    private String centroNombre;   // solo lectura (JOIN con centros)
    private boolean activo;

    public Almacen() {
    }

    public Almacen(int id, String nombre, String descripcion, String ubicacion, Integer idCentro, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.idCentro = idCentro;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Integer getIdCentro() { return idCentro; }
    public void setIdCentro(Integer idCentro) { this.idCentro = idCentro; }
    public String getCentroNombre() { return centroNombre; }
    public void setCentroNombre(String centroNombre) { this.centroNombre = centroNombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
