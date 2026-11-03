package com.mycompany.proyectoprueba.modelo;

/** Rol de usuario (tabla roles). El código indica el tipo de permisos (ver TipoRol). */
public class Rol {

    private int id;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private String codigo;
    private int usuarios;   // solo lectura: cantidad de usuarios con el rol

    public Rol() {
    }

    public Rol(int id, String nombre, String descripcion, boolean activo, String codigo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.codigo = codigo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getUsuarios() { return usuarios; }
    public void setUsuarios(int usuarios) { this.usuarios = usuarios; }

    public String getNombreTipo() {
        return TipoRol.nombre(codigo);
    }
}
