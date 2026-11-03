package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.ReporteIndividualDAO;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;

public class ReporteIndividualDAOJdbc extends JdbcBase implements ReporteIndividualDAO {

    public ReporteIndividualDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public boolean ordenCompraVisible(int idCompra, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM (SELECT id_centro_destino AS id_centro FROM compras WHERE id_compra = ?) x WHERE TRUE AND " + p.sqlCentros("x.id_centro"), idCompra);
    }

    @Override
    public boolean stockVisible(int idStock, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM (SELECT a.id_centro FROM stock_almacen s JOIN almacenes a ON a.id_almacen = s.id_almacen WHERE s.id_stock = ?) x WHERE TRUE AND "
                + p.sqlCentros("x.id_centro"), idStock);
    }

    @Override
    public boolean activoVisible(int idEquipo, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM (SELECT COALESCE(e.id_centro_actual, c.id_centro_destino) AS id_centro FROM equipos e "
                + "LEFT JOIN detalle_compra d ON d.id_equipo = e.id_equipo LEFT JOIN compras c ON c.id_compra = d.id_compra WHERE e.id_equipo = ?) x WHERE TRUE AND "
                + p.sqlCentros("x.id_centro"), idEquipo);
    }
}
