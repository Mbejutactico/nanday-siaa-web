package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Existencia de un insumo en un almacén (tabla stock_almacen) con los datos para mostrar. */
public class Stock {

    private int id;
    private int idInsumo;
    private int idAlmacen;
    private int actual;
    private int minimo;
    private String codigo;
    private String descripcion;
    private BigDecimal precio;
    private String unidad;
    private String abreviatura;
    private String almacen;
    private String ubicacion;
    private String centro;
    private String situacion;   // SIN_STOCK, BAJO_MINIMO o NORMAL

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdInsumo() { return idInsumo; }
    public void setIdInsumo(int idInsumo) { this.idInsumo = idInsumo; }
    public int getIdAlmacen() { return idAlmacen; }
    public void setIdAlmacen(int idAlmacen) { this.idAlmacen = idAlmacen; }
    public int getActual() { return actual; }
    public void setActual(int actual) { this.actual = actual; }
    public int getMinimo() { return minimo; }
    public void setMinimo(int minimo) { this.minimo = minimo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getAbreviatura() { return abreviatura; }
    public void setAbreviatura(String abreviatura) { this.abreviatura = abreviatura; }
    public String getAlmacen() { return almacen; }
    public void setAlmacen(String almacen) { this.almacen = almacen; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getCentro() { return centro; }
    public void setCentro(String centro) { this.centro = centro; }
    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }

    /** Valor del stock (precio × cantidad) en texto; "0" si el insumo no tiene precio. */
    public String getValorTexto() {
        return precio == null ? "0" : precio.multiply(new BigDecimal(actual)).toPlainString();
    }

    public String getPrecioTexto() {
        return precio == null ? "0" : precio.toPlainString();
    }
}
