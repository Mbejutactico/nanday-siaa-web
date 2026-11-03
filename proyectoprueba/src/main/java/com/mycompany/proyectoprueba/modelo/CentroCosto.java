package com.mycompany.proyectoprueba.modelo;

/** Centro de costo de un centro (tabla centros_costo). */
public class CentroCosto {

    private int id;
    private String codigo;
    private String descripcion;
    private Integer idCentro;
    private String centroNombre;   // solo lectura (JOIN con centros)
    private boolean activo;

    public CentroCosto() {
    }

    public CentroCosto(int id, String codigo, String descripcion, Integer idCentro, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.idCentro = idCentro;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getIdCentro() { return idCentro; }
    public void setIdCentro(Integer idCentro) { this.idCentro = idCentro; }
    public String getCentroNombre() { return centroNombre; }
    public void setCentroNombre(String centroNombre) { this.centroNombre = centroNombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
