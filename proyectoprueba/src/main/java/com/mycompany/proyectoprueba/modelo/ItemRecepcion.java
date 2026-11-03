package com.mycompany.proyectoprueba.modelo;

/**
 * Lo que el usuario indica recibir de una línea de OC: cantidad (0 = no se recibe ahora),
 * centro de costo y serie para los activos, almacén para los insumos.
 * Si el dato llegó mal formado, errorLectura se lanza al procesar esa línea (en su orden).
 */
public class ItemRecepcion {

    public int idDetalle;
    public int cantidad;
    public int idCentroCosto;
    public int idAlmacen;
    public String serie = "";
    public RuntimeException errorLectura;
}
