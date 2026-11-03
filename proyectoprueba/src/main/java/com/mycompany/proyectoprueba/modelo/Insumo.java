package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Insumo (tabla insumos). */
public class Insumo {

    private int id;
    private String codigo;
    private String descripcion;
    private BigDecimal precioCompra;
    private Integer idUnidadMedida;
    private String unidadNombre;        // solo lectura
    private String unidadAbreviatura;   // solo lectura
    private boolean activo;

    public Insumo() {
    }

    public Insumo(int id, String codigo, String descripcion, BigDecimal precioCompra, Integer idUnidadMedida, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.idUnidadMedida = idUnidadMedida;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }
    public Integer getIdUnidadMedida() { return idUnidadMedida; }
    public void setIdUnidadMedida(Integer idUnidadMedida) { this.idUnidadMedida = idUnidadMedida; }
    public String getUnidadNombre() { return unidadNombre; }
    public void setUnidadNombre(String unidadNombre) { this.unidadNombre = unidadNombre; }
    public String getUnidadAbreviatura() { return unidadAbreviatura; }
    public void setUnidadAbreviatura(String unidadAbreviatura) { this.unidadAbreviatura = unidadAbreviatura; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** "Kilogramo (kg)" o "Sin unidad". */
    public String getUnidadMostrar() {
        return unidadNombre != null ? unidadNombre + " (" + unidadAbreviatura + ")" : "Sin unidad";
    }
}
