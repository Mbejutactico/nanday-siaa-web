package com.mycompany.proyectoprueba.modelo;

/** Usuario del sistema (tabla usuarios). La contraseña nunca se guarda en el modelo en claro. */
public class Usuario {

    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private Integer idRol;
    private String rolNombre;        // solo lectura
    private boolean activo;
    private Integer centroAsignado;
    private String centroNombre;     // solo lectura
    private String fotoPerfil;

    public Usuario() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Integer getCentroAsignado() { return centroAsignado; }
    public void setCentroAsignado(Integer centroAsignado) { this.centroAsignado = centroAsignado; }
    public String getCentroNombre() { return centroNombre; }
    public void setCentroNombre(String centroNombre) { this.centroNombre = centroNombre; }
    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
}
