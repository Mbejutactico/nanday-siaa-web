package com.mycompany.proyectoprueba.modelo;

/** Línea del detalle de una remisión (tabla remisiones_detalle): copia fija de lo que salió. */
public class LineaRemision {

    public int orden;
    public String tipoItem;     // INSUMO o EQUIPO
    public String codigo;       // código del insumo o número de activo
    public String descripcion;
    public String unidad;
    public int cantidad;
    public String origen;       // almacén de salida (insumos) o centro / centro de costo (activos)
}
