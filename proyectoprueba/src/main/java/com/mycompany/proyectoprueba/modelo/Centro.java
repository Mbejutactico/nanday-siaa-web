package com.mycompany.proyectoprueba.modelo;

/** Centro (sede) de la empresa (tabla centros). */
public class Centro {

    private int id;
    private String codigo;
    private String nombre;
    private String direccion;
    private boolean activo;

    public Centro() {
    }

    public Centro(int id, String codigo, String nombre, String direccion, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
