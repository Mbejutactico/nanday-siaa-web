package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;

/** Transferencia de insumos entre almacenes (TR-000001). Los textos NULL de la base llegan como "". */
public class Transferencia {

    public int id;
    public String numero;
    public Timestamp fecha;
    public String observacion;
    public boolean anulada;
    public Timestamp fechaAnulacion;
    public String motivoAnulacion;
    public int idAlmacenOrigen;
    public String origen;
    public String centroOrigen;
    public int idAlmacenDestino;
    public String destino;
    public String centroDestino;
    public String usuario;
    public int idRemision;
    public String remision;
    public int items;
}
