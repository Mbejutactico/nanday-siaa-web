package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.ProveedorDAO;
import com.mycompany.proyectoprueba.modelo.Proveedor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProveedorDAOJdbc extends JdbcBase implements ProveedorDAO {

    private static final Fila<Proveedor> FILA = rs -> new Proveedor(rs.getInt("id_proveedor"), rs.getString("razon_social"),
            rs.getString("ruc"), rs.getString("direccion"), rs.getString("telefono"), rs.getString("email"), rs.getBoolean("activo"));
    private static final String COLUMNAS = "SELECT id_proveedor, razon_social, ruc, direccion, telefono, email, activo FROM proveedores ";

    public ProveedorDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<Proveedor> listarTodos() throws SQLException {
        return lista(COLUMNAS + "ORDER BY id_proveedor", FILA);
    }

    @Override
    public List<Proveedor> listarActivos() throws SQLException {
        return lista(COLUMNAS + "WHERE activo=true ORDER BY razon_social", FILA);
    }

    @Override
    public void insertar(Proveedor p) throws SQLException {
        ejecutar("INSERT INTO proveedores (razon_social, ruc, direccion, telefono, email, activo) VALUES (?, ?, ?, ?, ?, ?::boolean)",
                p.getRazonSocial(), p.getRuc(), p.getDireccion(), p.getTelefono(), p.getEmail(), p.isActivo());
    }

    @Override
    public int actualizar(Proveedor p) throws SQLException {
        return ejecutar("UPDATE proveedores SET razon_social=?, ruc=?, direccion=?, telefono=?, email=?, activo=?::boolean WHERE id_proveedor=?",
                p.getRazonSocial(), p.getRuc(), p.getDireccion(), p.getTelefono(), p.getEmail(), p.isActivo(), p.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM proveedores WHERE id_proveedor=?", id);
    }
}
