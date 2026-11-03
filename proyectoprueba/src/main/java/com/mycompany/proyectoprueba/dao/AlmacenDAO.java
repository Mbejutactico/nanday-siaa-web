package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Almacen;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de almacenes. */
public interface AlmacenDAO {

    /** Todos (con el nombre del centro), ordenados por id. */
    List<Almacen> listarTodos() throws SQLException;

    void insertar(Almacen a) throws SQLException;

    int actualizar(Almacen a) throws SQLException;

    int eliminar(int id) throws SQLException;
}
