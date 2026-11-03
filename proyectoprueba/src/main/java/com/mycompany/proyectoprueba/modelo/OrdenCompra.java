package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;
import java.sql.Date;

/** Orden de compra (tabla compras) con los datos para listados y la ficha. */
public class OrdenCompra {

    private int id;
    private String numero;
    private Date fecha;
    private String estado;
    private String observaciones;
    private int idProveedor;
    private int idCentroDestino;
    private String proveedor;
    private String ruc;
    private String telefono;
    private String emailProveedor;
    private String direccionProveedor;
    private String centro;
    private String solicitante;
    private String comprador;
    private BigDecimal total;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }
    public int getIdCentroDestino() { return idCentroDestino; }
    public void setIdCentroDestino(int idCentroDestino) { this.idCentroDestino = idCentroDestino; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmailProveedor() { return emailProveedor; }
    public void setEmailProveedor(String emailProveedor) { this.emailProveedor = emailProveedor; }
    public String getDireccionProveedor() { return direccionProveedor; }
    public void setDireccionProveedor(String direccionProveedor) { this.direccionProveedor = direccionProveedor; }
    public String getCentro() { return centro; }
    public void setCentro(String centro) { this.centro = centro; }
    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String solicitante) { this.solicitante = solicitante; }
    public String getComprador() { return comprador; }
    public void setComprador(String comprador) { this.comprador = comprador; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    /** Una OC solo se edita mientras está SOLICITADO o PARCIAL. */
    public static boolean editable(String estado) {
        return "SOLICITADO".equals(estado) || "PARCIAL".equals(estado);
    }

    /** Clase CSS del distintivo de estado. */
    public static String colorEstado(String estado) {
        if ("SOLICITADO".equals(estado)) return "badge-primary";
        if ("PARCIAL".equals(estado)) return "badge-warning";
        if ("RECIBIDO".equals(estado)) return "badge-success";
        if ("ANULADO".equals(estado)) return "badge-danger";
        return "badge-secondary";
    }
}
