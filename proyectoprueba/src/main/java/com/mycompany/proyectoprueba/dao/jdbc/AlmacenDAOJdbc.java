package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.AlmacenDAO;
import com.mycompany.proyectoprueba.modelo.Almacen;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AlmacenDAOJdbc extends JdbcBase implements AlmacenDAO {

    public AlmacenDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<Almacen> listarTodos() throws SQLException {
        return lista("SELECT a.id_almacen, a.nombre, a.descripcion, a.ubicacion, a.id_centro, c.nombre as centro_nombre, a.activo "
                + "FROM almacenes a LEFT JOIN centros c ON a.id_centro = c.id_centro ORDER BY a.id_almacen", rs -> {
                    Almacen a = new Almacen(rs.getInt("id_almacen"), rs.getString("nombre"), rs.getString("descripcion"),
                            rs.getString("ubicacion"), enteroONull(rs, "id_centro"), rs.getBoolean("activo"));
                    a.setCentroNombre(rs.getString("centro_nombre"));
                    return a;
                });
    }

    @Override
    public void insertar(Almacen a) throws SQLException {
        ejecutar("INSERT INTO almacenes (nombre, descripcion, ubicacion, id_centro, activo) VALUES (?,?,?,?::int,?::boolean)",
                a.getNombre(), a.getDescripcion(), a.getUbicacion(), entero(a.getIdCentro()), a.isActivo());
    }

    @Override
    public int actualizar(Almacen a) throws SQLException {
        return ejecutar("UPDATE almacenes SET nombre=?, descripcion=?, ubicacion=?, id_centro=?::int, activo=?::boolean WHERE id_almacen=?",
                a.getNombre(), a.getDescripcion(), a.getUbicacion(), entero(a.getIdCentro()), a.isActivo(), a.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM almacenes WHERE id_almacen=?", id);
    }
}
