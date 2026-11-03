package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Centro;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de centros. */
public interface CentroDAO {

    /** Todos, ordenados por id. */
    List<Centro> listarTodos() throws SQLException;

    /** Activos, ordenados por nombre (combos). */
    List<Centro> listarActivos() throws SQLException;

    void insertar(Centro c) throws SQLException;

    int actualizar(Centro c) throws SQLException;

    int eliminar(int id) throws SQLException;
}
