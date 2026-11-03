package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.LineaRemision;
import java.sql.SQLException;
import java.util.List;

/** Detalle de las remisiones (tabla remisiones_detalle). */
public interface RemisionDAO {

    /**
     * Graba el detalle de la remisión copiando las líneas de su proceso de origen
     * (detalle de la OT, de la transferencia o de la solicitud de activos). Devuelve las líneas grabadas.
     */
    int copiarDetalle(int idRemision) throws SQLException;

    /** Líneas de la remisión, en orden. */
    List<LineaRemision> detalle(int idRemision) throws SQLException;
}
