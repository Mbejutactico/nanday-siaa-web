package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.DashboardDAO;
import com.mycompany.proyectoprueba.modelo.CompraResumen;
import com.mycompany.proyectoprueba.modelo.OtResumen;
import com.mycompany.proyectoprueba.modelo.PuntoGrafico;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DashboardDAOJdbc extends JdbcBase implements DashboardDAO {

    private static final List<String> TABLAS = Arrays.asList("proveedores", "almacenes", "centros", "usuarios", "insumos", "roles");

    public DashboardDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public int contarActivos(String tabla) throws SQLException {
        if (!TABLAS.contains(tabla)) {
            throw new IllegalArgumentException("Tabla no permitida: " + tabla);
        }
        return entero("SELECT COUNT(*) FROM " + tabla + " WHERE activo = true");
    }

    /** OT visibles: las de sus centros (destino), las que pidió y las que tiene a cargo (idUsuario es un entero). */
    private static String visibles(int idUsuario, Permisos p) {
        return "(" + p.sqlCentros("o.id_centro") + " OR o.id_usuario_solic = " + idUsuario + " OR o.id_usuario_responsable = " + idUsuario + ")";
    }

    @Override
    public int contarOt(String cual, int idUsuario, Permisos p) throws SQLException {
        String v = visibles(idUsuario, p);
        if ("PENDIENTES".equals(cual)) {
            return entero("SELECT COUNT(*) FROM ot o WHERE o.estado = 'PENDIENTE' AND " + v);
        }
        if ("EN_PROCESO".equals(cual)) {
            return entero("SELECT COUNT(*) FROM ot o WHERE o.estado = 'EN_PROCESO' AND " + v);
        }
        if ("FINALIZADAS_MES".equals(cual)) {
            return entero("SELECT COUNT(*) FROM ot o WHERE o.estado = 'FINALIZADA' AND o.fecha_finalizacion >= date_trunc('month', CURRENT_DATE) AND " + v);
        }
        if ("A_MI_CARGO".equals(cual)) {
            return entero("SELECT COUNT(*) FROM ot o WHERE o.estado IN ('PENDIENTE', 'EN_PROCESO') AND o.id_usuario_responsable = " + idUsuario);
        }
        if ("DEMORADAS".equals(cual)) {
            return entero("SELECT COUNT(*) FROM ot o WHERE o.estado IN ('PENDIENTE', 'EN_PROCESO') "
                    + "AND o.fecha_creacion < CURRENT_TIMESTAMP - INTERVAL '15 days' AND " + v);
        }
        throw new IllegalArgumentException("Contador desconocido: " + cual);
    }

    @Override
    public List<OtResumen> otAbiertas(int idUsuario, Permisos p) throws SQLException {
        return lista("SELECT o.id_ot, o.numero_ot, o.titulo, o.estado, c.nombre AS centro, "
                + "       cc.codigo AS cc_codigo, ur.nombre || ' ' || ur.apellido AS responsable, "
                + "       (CURRENT_DATE - CAST(o.fecha_creacion AS date)) AS dias, "
                + "       (SELECT COUNT(*) FROM detalle_ot d WHERE d.id_ot = o.id_ot) AS items "
                + "FROM ot o LEFT JOIN centros c ON c.id_centro = o.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo "
                + "LEFT JOIN usuarios ur ON ur.id_usuario = o.id_usuario_responsable "
                + "WHERE o.estado IN ('PENDIENTE', 'EN_PROCESO') AND " + visibles(idUsuario, p) + " "
                + "ORDER BY o.fecha_creacion, o.id_ot LIMIT 5",
                rs -> new OtResumen(rs.getInt("id_ot"), rs.getString("numero_ot"), rs.getString("titulo"), rs.getString("estado"),
                        rs.getString("centro"), rs.getString("cc_codigo"), rs.getInt("dias"), rs.getInt("items"), rs.getString("responsable")));
    }

    @Override
    public List<CompraResumen> ultimasCompras(Permisos p) throws SQLException {
        return lista("SELECT c.id_compra, c.numero_oc, c.fecha_compra, p.razon_social, c.estado "
                + "FROM compras c JOIN proveedores p ON c.id_proveedor = p.id_proveedor "
                + "WHERE c.estado IN ('SOLICITADO', 'RECIBIDO', 'PARCIAL') AND " + p.sqlCentros("c.id_centro_destino") + " "
                + "ORDER BY c.id_compra DESC LIMIT 5",
                rs -> new CompraResumen(rs.getInt("id_compra"), rs.getString("numero_oc"), rs.getString("razon_social"),
                        rs.getDate("fecha_compra"), rs.getString("estado")));
    }

    @Override
    public List<PuntoGrafico> otPorEstado(int idUsuario, Permisos p) throws SQLException {
        return lista("SELECT e.estado, COUNT(o.id_ot) "
                + "FROM (VALUES (1, 'PENDIENTE'), (2, 'EN_PROCESO'), (3, 'FINALIZADA'), (4, 'ANULADA')) AS e(orden, estado) "
                + "LEFT JOIN ot o ON o.estado = e.estado AND o.fecha_creacion >= CURRENT_DATE - INTERVAL '12 months' AND " + visibles(idUsuario, p) + " "
                + "GROUP BY e.orden, e.estado ORDER BY e.orden",
                rs -> new PuntoGrafico(rs.getString(1), rs.getLong(2), null));
    }

    @Override
    public List<PuntoGrafico> ocPorMes(Permisos p) throws SQLException {
        return lista("SELECT to_char(m.mes, 'MM/YYYY'), COUNT(x.id_compra), COALESCE(SUM(x.monto), 0) "
                + "FROM generate_series(date_trunc('month', CURRENT_DATE) - INTERVAL '5 months', date_trunc('month', CURRENT_DATE), INTERVAL '1 month') AS m(mes) "
                + "LEFT JOIN (SELECT c.id_compra, date_trunc('month', c.fecha_compra) AS mes, "
                + "                  (SELECT COALESCE(SUM(d.cantidad * d.precio_unitario), 0) FROM detalle_compra d WHERE d.id_compra = c.id_compra) AS monto "
                + "           FROM compras c WHERE c.estado IN ('SOLICITADO', 'PARCIAL', 'RECIBIDO') AND " + p.sqlCentros("c.id_centro_destino") + ") x "
                + "ON x.mes = m.mes GROUP BY m.mes ORDER BY m.mes",
                rs -> new PuntoGrafico(rs.getString(1), rs.getLong(2), rs.getBigDecimal(3)));
    }

    @Override
    public List<PuntoGrafico> consumoPorCentroCosto(Permisos p) throws SQLException {
        return lista("SELECT cc.codigo, SUM(-m.cantidad), COALESCE(SUM(-m.cantidad * COALESCE(m.costo_unitario, 0)), 0) AS valor "
                + "FROM movimientos_stock m JOIN centros_costo cc ON cc.id_centro_costo = m.id_centro_costo "
                + "WHERE m.tipo = 'SALIDA_OT' AND m.fecha >= CURRENT_DATE - INTERVAL '90 days' AND " + p.sqlCentros("cc.id_centro") + " "
                + "GROUP BY cc.codigo ORDER BY valor DESC, cc.codigo LIMIT 8",
                rs -> new PuntoGrafico(rs.getString(1), rs.getLong(2), rs.getBigDecimal(3)));
    }
}
