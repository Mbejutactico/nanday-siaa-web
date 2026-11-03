package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.CentroDAO;
import com.mycompany.proyectoprueba.modelo.Centro;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CentroDAOJdbc extends JdbcBase implements CentroDAO {

    private static final Fila<Centro> FILA = rs -> new Centro(rs.getInt("id_centro"), rs.getString("codigo"),
            rs.getString("nombre"), rs.getString("direccion"), rs.getBoolean("activo"));

    public CentroDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<Centro> listarTodos() throws SQLException {
        return lista("SELECT id_centro, codigo, nombre, direccion, activo FROM centros ORDER BY id_centro", FILA);
    }

    @Override
    public List<Centro> listarActivos() throws SQLException {
        return lista("SELECT id_centro, codigo, nombre, direccion, activo FROM centros WHERE activo=true ORDER BY nombre", FILA);
    }

    @Override
    public void insertar(Centro c) throws SQLException {
        ejecutar("INSERT INTO centros (codigo, nombre, direccion, activo) VALUES (?,?,?,?::boolean)",
                c.getCodigo(), c.getNombre(), c.getDireccion(), c.isActivo());
    }

    @Override
    public int actualizar(Centro c) throws SQLException {
        return ejecutar("UPDATE centros SET codigo=?, nombre=?, direccion=?, activo=?::boolean WHERE id_centro=?",
                c.getCodigo(), c.getNombre(), c.getDireccion(), c.isActivo(), c.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM centros WHERE id_centro=?", id);
    }
}
