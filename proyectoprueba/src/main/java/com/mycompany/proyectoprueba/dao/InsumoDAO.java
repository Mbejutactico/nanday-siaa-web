package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Insumo;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de insumos. */
public interface InsumoDAO {

    /** Todos (con su unidad de medida), ordenados por id. */
    List<Insumo> listarTodos() throws SQLException;

    void insertar(Insumo i) throws SQLException;

    int actualizar(Insumo i) throws SQLException;

    int eliminar(int id) throws SQLException;
}
