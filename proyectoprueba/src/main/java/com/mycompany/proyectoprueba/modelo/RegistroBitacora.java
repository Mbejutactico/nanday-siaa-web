package com.mycompany.proyectoprueba.modelo;

import java.sql.Timestamp;

/** Un registro de la bitácora (cabecera). */
public class RegistroBitacora {

    public int id;
    public Timestamp fecha;
    public String usuario;      // nombre y apellido (vacío si el usuario ya no existe)
    public String email;
    public String ip;
    public String modulo;
    public String accion;
    public String tabla;
    public Integer idRegistro;
    public String descripcion;
    public int cambios;         // cantidad de campos en bitacora_detalle
}
