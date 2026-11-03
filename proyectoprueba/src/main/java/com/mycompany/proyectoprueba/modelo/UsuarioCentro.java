package com.mycompany.proyectoprueba.modelo;

import java.sql.Date;

/** Centro adicional de un usuario, permanente o por un período (tabla usuario_centros). */
public class UsuarioCentro {

    private int idCentro;
    private String centro;
    private Date desde;
    private Date hasta;
    private String observacion;
    private boolean vigente;

    public UsuarioCentro(int idCentro, String centro, Date desde, Date hasta, String observacion, boolean vigente) {
        this.idCentro = idCentro;
        this.centro = centro;
        this.desde = desde;
        this.hasta = hasta;
        this.observacion = observacion;
        this.vigente = vigente;
    }

    public int getIdCentro() { return idCentro; }
    public String getCentro() { return centro; }
    public Date getDesde() { return desde; }
    public Date getHasta() { return hasta; }
    public String getObservacion() { return observacion; }
    public boolean isVigente() { return vigente; }
}
