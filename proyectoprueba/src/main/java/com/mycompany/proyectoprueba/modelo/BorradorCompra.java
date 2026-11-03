package com.mycompany.proyectoprueba.modelo;

/** Cabecera de la solicitud de compra en curso (compras en estado BORRADOR) de un usuario. */
public class BorradorCompra {

    public final int idCompra;
    public final int idProveedor;
    public final String ruc;
    public final int idCentroDestino;
    public final String observaciones;
    public final java.sql.Date fecha;

    public BorradorCompra(int idCompra, int idProveedor, String ruc, int idCentroDestino, String observaciones) {
        this(idCompra, idProveedor, ruc, idCentroDestino, observaciones, null);
    }

    public BorradorCompra(int idCompra, int idProveedor, String ruc, int idCentroDestino, String observaciones, java.sql.Date fecha) {
        this.idCompra = idCompra;
        this.idProveedor = idProveedor;
        this.ruc = ruc;
        this.idCentroDestino = idCentroDestino;
        this.observaciones = observaciones;
        this.fecha = fecha;
    }
}
