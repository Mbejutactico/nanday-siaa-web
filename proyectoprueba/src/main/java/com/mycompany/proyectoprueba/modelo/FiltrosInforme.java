package com.mycompany.proyectoprueba.modelo;

/**
 * Filtros de los informes. Comunes: fecha inicio y fin (yyyy-MM-dd o ""), centro, centro de
 * costo y usuario (0 = todos). Propios: compras (proveedor, estado, tipo de ítem), OT (estado,
 * insumo), inventario (tipo de movimiento, almacén, insumo), activos (tipo de movimiento,
 * estado actual, N° de activo / serie). Los textos llegan tal cual; el servicio solo acepta
 * los valores de su lista.
 */
public class FiltrosInforme {

    public String desde = "";
    public String hasta = "";
    public int idCentro;
    public int idCentroCosto;
    public int idUsuario;
    public int idProveedor;
    public int idInsumo;
    public int idAlmacen;
    public String estado;
    public String tipoItem;
    public String tipoMovimiento;
    public String estadoActivo;
    /** Texto a buscar en N° de activo o serie (sin espacios sobrantes, hasta 60 caracteres; "" = cualquiera). */
    public String buscar = "";
}
