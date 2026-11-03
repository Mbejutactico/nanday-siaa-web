package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Línea de una solicitud u orden de compra (tabla detalle_compra). tipoItem: INSUMO o EQUIPO. */
public class DetalleCompra {

    private int id;
    private String tipoItem;
    private String codigo;
    private String descripcion;
    private BigDecimal precioUnitario;
    private int cantidad;
    private int cantidadRecibida;
    private BigDecimal subtotal;
    private String serialActivo;
    private Integer idEquipo;
    private int pendiente;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(int cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public String getSerialActivo() { return serialActivo; }
    public void setSerialActivo(String serialActivo) { this.serialActivo = serialActivo; }

    public int getPendiente() { return pendiente; }
    public void setPendiente(int pendiente) { this.pendiente = pendiente; }
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }

    public boolean isEquipo() {
        return "EQUIPO".equals(tipoItem);
    }
}
