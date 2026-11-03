package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.CentroCosto;
import com.mycompany.proyectoprueba.modelo.Opcion;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de centros de costo. */
public interface CentroCostoDAO {

    /** Todos (con el nombre del centro), ordenados por id. */
    List<CentroCosto> listarTodos() throws SQLException;

    /** Activos de un centro para los combos ("CODIGO - Descripción"), ordenados por código. */
    List<Opcion> activosDelCentro(int idCentro) throws SQLException;

    void insertar(CentroCosto cc) throws SQLException;

    int actualizar(CentroCosto cc) throws SQLException;

    int eliminar(int id) throws SQLException;
}
