package com.mycompany.proyectoprueba.modelo;

/** Proveedor (tabla proveedores). */
public class Proveedor {

    private int id;
    private String razonSocial;
    private String ruc;
    private String direccion;
    private String telefono;
    private String email;
    private boolean activo;

    public Proveedor() {
    }

    public Proveedor(int id, String razonSocial, String ruc, String direccion, String telefono, String email, boolean activo) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
