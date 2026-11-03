package com.mycompany.proyectoprueba.modelo;

/** Evento del historial de un activo (movimientos_activos). Los textos NULL llegan como "". */
public class MovimientoActivo {

    public int id;
    /** yyyy-MM-dd HH:mm o "". */
    public String fecha;
    public String tipo;
    public String numero;
    public String estadoAnterior;
    public String estadoNuevo;
    public String observaciones;
    public String justificacion;
    public String personaNombre;
    public String personaApellido;
    public String personaCi;
    public String vehiculoChapa;
    public String vehiculoMarca;
    public String vehiculoModelo;
    public String centroOrigen;
    public String centroDestino;
    /** Ubicación dentro del centro: centro de costo (o almacén en movimientos anteriores). */
    public String subOrigen;
    public String subDestino;
    public String registradoPor;
    public String emisor;
    public String receptor;
    public String remision;
}
