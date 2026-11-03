package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Proveedor;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de proveedores. */
public interface ProveedorDAO {

    /** Todos, ordenados por id. */
    List<Proveedor> listarTodos() throws SQLException;

    /** Activos, ordenados por razón social (combos). */
    List<Proveedor> listarActivos() throws SQLException;

    void insertar(Proveedor p) throws SQLException;

    int actualizar(Proveedor p) throws SQLException;

    int eliminar(int id) throws SQLException;
}
