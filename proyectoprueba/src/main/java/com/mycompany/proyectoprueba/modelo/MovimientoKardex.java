package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Movimiento del Kardex de un insumo (movimientos_stock). Los textos NULL llegan como "". */
public class MovimientoKardex {

    public Timestamp fecha;
    public String tipo;
    public String documento;
    public int cantidad;
    public int saldo;
    public BigDecimal costo;
    public String observacion;
    public String almacen;
    public String usuario;
    public String centroCosto;
}
