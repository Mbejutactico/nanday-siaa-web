package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.InsumoDAO;
import com.mycompany.proyectoprueba.modelo.Insumo;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InsumoDAOJdbc extends JdbcBase implements InsumoDAO {

    public InsumoDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<Insumo> listarTodos() throws SQLException {
        return lista("SELECT i.id_insumo, i.codigo, i.descripcion, i.precio_compra, "
                + "i.activo, u.nombre AS unidad_nombre, u.abreviatura, u.id_unidad_medida "
                + "FROM insumos i LEFT JOIN unidad_medida u ON i.id_unidad_medida = u.id_unidad_medida ORDER BY i.id_insumo", rs -> {
                    Insumo i = new Insumo(rs.getInt("id_insumo"), rs.getString("codigo"), rs.getString("descripcion"),
                            rs.getBigDecimal("precio_compra"), enteroONull(rs, "id_unidad_medida"), rs.getBoolean("activo"));
                    i.setUnidadNombre(rs.getString("unidad_nombre"));
                    i.setUnidadAbreviatura(rs.getString("abreviatura"));
                    return i;
                });
    }

    @Override
    public void insertar(Insumo i) throws SQLException {
        ejecutar("INSERT INTO insumos (codigo, descripcion, precio_compra, id_unidad_medida, activo) VALUES (?,?,?,?::integer,?::boolean)",
                i.getCodigo(), i.getDescripcion(), i.getPrecioCompra(), entero(i.getIdUnidadMedida()), i.isActivo());
    }

    @Override
    public int actualizar(Insumo i) throws SQLException {
        return ejecutar("UPDATE insumos SET codigo=?, descripcion=?, precio_compra=?, id_unidad_medida=?, activo=?::boolean WHERE id_insumo=?",
                i.getCodigo(), i.getDescripcion(), i.getPrecioCompra(), entero(i.getIdUnidadMedida()), i.isActivo(), i.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM insumos WHERE id_insumo=?", id);
    }
}
