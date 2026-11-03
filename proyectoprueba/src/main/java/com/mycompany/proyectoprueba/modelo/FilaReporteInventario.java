package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/**
 * Fila de los reportes de inventario (consumo por centro de costo, insumos más usados,
 * valorización por almacén) y de las alertas de stock mínimo. Cada reporte usa sus campos.
 * Los textos NULL de la base llegan como "".
 */
public class FilaReporteInventario {

    public String centro;
    public String centroCosto;
    public String almacen;
    public String codigo;
    public String nombre;
    public String unidad;
    public long unidades;
    public BigDecimal valor;
    public int documentos;
    public int insumos;
    public int stock;
    public int minimo;
}
