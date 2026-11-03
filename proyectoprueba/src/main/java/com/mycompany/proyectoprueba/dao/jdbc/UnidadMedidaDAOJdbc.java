package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.UnidadMedidaDAO;
import com.mycompany.proyectoprueba.modelo.UnidadMedida;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Implementación JDBC (PostgreSQL) de UnidadMedidaDAO. */
public class UnidadMedidaDAOJdbc extends JdbcBase implements UnidadMedidaDAO {

    private static final Fila<UnidadMedida> FILA = rs -> new UnidadMedida(rs.getInt("id_unidad_medida"), rs.getString("nombre"),
            rs.getString("abreviatura"), rs.getBoolean("activo"));

    public UnidadMedidaDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<UnidadMedida> listarTodas() throws SQLException {
        return lista("SELECT id_unidad_medida, nombre, abreviatura, activo FROM unidad_medida ORDER BY id_unidad_medida", FILA);
    }

    @Override
    public List<UnidadMedida> listarActivas() throws SQLException {
        return lista("SELECT id_unidad_medida, nombre, abreviatura, activo FROM unidad_medida WHERE activo = true ORDER BY nombre", FILA);
    }

    @Override
    public void insertar(UnidadMedida u) throws SQLException {
        ejecutar("INSERT INTO unidad_medida (nombre, abreviatura, activo) VALUES (?,?,?::boolean)", u.getNombre(), u.getAbreviatura(), u.isActivo());
    }

    @Override
    public int actualizar(UnidadMedida u) throws SQLException {
        return ejecutar("UPDATE unidad_medida SET nombre=?, abreviatura=?, activo=?::boolean WHERE id_unidad_medida=?",
                u.getNombre(), u.getAbreviatura(), u.isActivo(), u.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM unidad_medida WHERE id_unidad_medida=?", id);
    }

    @Override
    public int contarInsumos(int id) throws SQLException {
        return entero("SELECT COUNT(*) FROM insumos WHERE id_unidad_medida=?", id);
    }
}
