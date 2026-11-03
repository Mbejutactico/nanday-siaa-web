package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.RolDAO;
import com.mycompany.proyectoprueba.modelo.Rol;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RolDAOJdbc extends JdbcBase implements RolDAO {

    private static final Fila<Rol> FILA = rs -> new Rol(rs.getInt("id_rol"), rs.getString("nombre"), rs.getString("descripcion"),
            rs.getBoolean("activo"), rs.getString("codigo"));

    public RolDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<Rol> listarTodos() throws SQLException {
        return lista("SELECT r.id_rol, r.nombre, r.descripcion, r.activo, r.codigo, "
                + "(SELECT COUNT(*) FROM usuarios u WHERE u.id_rol = r.id_rol) AS usuarios "
                + "FROM roles r ORDER BY r.id_rol", rs -> {
                    Rol r = FILA.leer(rs);
                    r.setUsuarios(rs.getInt("usuarios"));
                    return r;
                });
    }

    @Override
    public List<Rol> listarActivos() throws SQLException {
        return lista("SELECT id_rol, nombre, descripcion, activo, codigo FROM roles WHERE activo = true ORDER BY nombre", FILA);
    }

    @Override
    public Rol buscar(int id) throws SQLException {
        return uno("SELECT id_rol, nombre, descripcion, activo, codigo FROM roles WHERE id_rol = ?", FILA, id);
    }

    @Override
    public void insertar(Rol r) throws SQLException {
        ejecutar("INSERT INTO roles (nombre, descripcion, activo, codigo) VALUES (?, ?, ?, ?)",
                r.getNombre(), r.getDescripcion(), r.isActivo(), r.getCodigo());
    }

    @Override
    public int actualizar(Rol r) throws SQLException {
        return ejecutar("UPDATE roles SET nombre = ?, descripcion = ?, activo = ?, codigo = ? WHERE id_rol = ?",
                r.getNombre(), r.getDescripcion(), r.isActivo(), r.getCodigo(), r.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM roles WHERE id_rol = ?", id);
    }

    @Override
    public int contarUsuarios(int idRol) throws SQLException {
        return entero("SELECT COUNT(*) FROM usuarios WHERE id_rol = ?", idRol);
    }

    @Override
    public int contarAdministradoresActivos() throws SQLException {
        return entero("SELECT COUNT(*) FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol "
                + "WHERE u.activo = true AND r.activo = true AND r.codigo = 'ADMINISTRADOR'");
    }
}
