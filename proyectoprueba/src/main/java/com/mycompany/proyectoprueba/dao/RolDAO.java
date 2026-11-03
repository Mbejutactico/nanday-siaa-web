package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Rol;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de roles. */
public interface RolDAO {

    /** Todos (con la cantidad de usuarios), ordenados por id. */
    List<Rol> listarTodos() throws SQLException;

    /** Activos, ordenados por nombre (combos). */
    List<Rol> listarActivos() throws SQLException;

    /** Rol por id o null. */
    Rol buscar(int id) throws SQLException;

    void insertar(Rol r) throws SQLException;

    int actualizar(Rol r) throws SQLException;

    int eliminar(int id) throws SQLException;

    int contarUsuarios(int idRol) throws SQLException;

    /** Usuarios activos con un rol activo de tipo Administrador. */
    int contarAdministradoresActivos() throws SQLException;
}
