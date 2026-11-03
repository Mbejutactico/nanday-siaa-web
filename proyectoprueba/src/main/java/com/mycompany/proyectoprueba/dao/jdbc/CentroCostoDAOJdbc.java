package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.CentroCostoDAO;
import com.mycompany.proyectoprueba.modelo.CentroCosto;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.util.Comun;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CentroCostoDAOJdbc extends JdbcBase implements CentroCostoDAO {

    public CentroCostoDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<CentroCosto> listarTodos() throws SQLException {
        return lista("SELECT cc.id_centro_costo, cc.codigo, cc.descripcion, cc.id_centro, c.nombre AS centro_nombre, cc.activo "
                + "FROM centros_costo cc LEFT JOIN centros c ON cc.id_centro = c.id_centro ORDER BY cc.id_centro_costo", rs -> {
                    CentroCosto cc = new CentroCosto(rs.getInt("id_centro_costo"), rs.getString("codigo"), rs.getString("descripcion"),
                            enteroONull(rs, "id_centro"), rs.getBoolean("activo"));
                    cc.setCentroNombre(rs.getString("centro_nombre"));
                    return cc;
                });
    }

    @Override
    public void insertar(CentroCosto cc) throws SQLException {
        ejecutar("INSERT INTO centros_costo (codigo, descripcion, id_centro, activo) VALUES (?, ?, ?, ?)",
                cc.getCodigo(), cc.getDescripcion(), entero(cc.getIdCentro()), cc.isActivo());
    }

    @Override
    public int actualizar(CentroCosto cc) throws SQLException {
        return ejecutar("UPDATE centros_costo SET codigo=?, descripcion=?, id_centro=?, activo=? WHERE id_centro_costo=?",
                cc.getCodigo(), cc.getDescripcion(), entero(cc.getIdCentro()), cc.isActivo(), cc.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM centros_costo WHERE id_centro_costo=?", id);
    }

    @Override
    public List<Opcion> activosDelCentro(int idCentro) throws SQLException {
        return lista("SELECT id_centro_costo, codigo, descripcion FROM centros_costo WHERE id_centro = ? AND activo = true ORDER BY codigo",
                rs -> new Opcion(rs.getInt("id_centro_costo"), Comun.nombreCC(rs.getString("codigo"), rs.getString("descripcion"))), idCentro);
    }
}
