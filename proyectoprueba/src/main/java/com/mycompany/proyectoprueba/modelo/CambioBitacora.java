package com.mycompany.proyectoprueba.modelo;

/** Un campo modificado (bitacora_detalle). */
public class CambioBitacora {

    public final String campo;
    public final String anterior;
    public final String nuevo;

    public CambioBitacora(String campo, String anterior, String nuevo) {
        this.campo = campo;
        this.anterior = anterior;
        this.nuevo = nuevo;
    }
}
