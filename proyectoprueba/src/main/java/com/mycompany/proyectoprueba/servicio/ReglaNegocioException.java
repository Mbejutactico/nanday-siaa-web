package com.mycompany.proyectoprueba.servicio;

/**
 * Una regla del negocio impide la operación (por ejemplo, borrar una unidad que usan insumos).
 * El mensaje es para el usuario. Si es un "aviso", la pantalla lo muestra en amarillo.
 */
public class ReglaNegocioException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final boolean aviso;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, false);
    }

    private ReglaNegocioException(String mensaje, boolean aviso) {
        super(mensaje);
        this.aviso = aviso;
    }

    /** Regla que se muestra como advertencia (amarillo). */
    public static ReglaNegocioException aviso(String mensaje) {
        return new ReglaNegocioException(mensaje, true);
    }

    public boolean isAviso() {
        return aviso;
    }
}
