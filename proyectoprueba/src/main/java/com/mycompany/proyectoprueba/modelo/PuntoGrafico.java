package com.mycompany.proyectoprueba.modelo;

import java.math.BigDecimal;

/** Un punto de un gráfico del tablero: etiqueta, cantidad y valor en guaraníes (opcional). */
public class PuntoGrafico {

    public final String etiqueta;
    public final long cantidad;
    public final BigDecimal valor;

    public PuntoGrafico(String etiqueta, long cantidad, BigDecimal valor) {
        this.etiqueta = etiqueta;
        this.cantidad = cantidad;
        this.valor = valor == null ? BigDecimal.ZERO : valor;
    }
}
