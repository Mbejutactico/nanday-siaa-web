package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Ajuste de inventario (AJ-000001): consumo sin OT, rotura/pérdida, vencimiento o corrección
 * por conteo. Los textos que en la base pueden ser NULL llegan como "".
 */
public class Ajuste {

    public static final String CORRECCION_CONTEO = "CORRECCION_CONTEO";

    public int id;
    public String numero;
    public Timestamp fecha;
    public String tipo;
    public String origen;
    public String motivo;
    public boolean anulado;
    public Timestamp fechaAnulacion;
    public String motivoAnulacion;
    public String centro;
    public String centroCosto;
    public int idAlmacen;
    public Integer idCentroCosto;
    public String almacen;
    public String usuario;
    public int items;
    public BigDecimal valor;

    /** Tipos de ajuste que se pueden cargar a mano. */
    public static boolean tipoValido(String tipo) {
        return "CONSUMO".equals(tipo) || "ROTURA_PERDIDA".equals(tipo) || "VENCIMIENTO".equals(tipo) || CORRECCION_CONTEO.equals(tipo);
    }
}
