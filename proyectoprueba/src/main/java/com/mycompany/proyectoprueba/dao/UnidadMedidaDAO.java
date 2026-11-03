package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.UnidadMedida;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de unidad_medida. Solo SQL: las reglas de negocio están en UnidadMedidaServicio. */
public interface UnidadMedidaDAO {

    /** Todas las unidades, ordenadas por id. */
    List<UnidadMedida> listarTodas() throws SQLException;

    /** Solo las activas, ordenadas por nombre (para combos). */
    List<UnidadMedida> listarActivas() throws SQLException;

    void insertar(UnidadMedida u) throws SQLException;

    /** Devuelve la cantidad de filas modificadas. */
    int actualizar(UnidadMedida u) throws SQLException;

    /** Devuelve la cantidad de filas borradas. */
    int eliminar(int id) throws SQLException;

    /** Cantidad de insumos que usan la unidad. */
    int contarInsumos(int id) throws SQLException;
}
