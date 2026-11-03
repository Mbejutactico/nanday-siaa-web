package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.StockDAO;
import com.mycompany.proyectoprueba.modelo.IngresoStock;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.ResumenStock;
import com.mycompany.proyectoprueba.modelo.Stock;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StockDAOJdbc extends JdbcBase implements StockDAO {

    public StockDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public ResumenStock resumen(Permisos p) throws SQLException {
        return uno("SELECT COUNT(*) AS registros, "
                + "       COALESCE(SUM(s.stock_actual), 0) AS unidades, "
                + "       COUNT(*) FILTER (WHERE s.stock_actual <= 0) AS sin_stock, "
                + "       COUNT(*) FILTER (WHERE s.stock_actual > 0 AND s.stock_actual <= s.stock_minimo) AS bajo_minimo, "
                + "       COUNT(DISTINCT s.id_almacen) FILTER (WHERE s.stock_actual > 0) AS almacenes "
                + "FROM stock_almacen s INNER JOIN insumos i ON i.id_insumo = s.id_insumo "
                + "INNER JOIN almacenes ap ON ap.id_almacen = s.id_almacen WHERE " + p.sqlCentros("ap.id_centro"),
                rs -> new ResumenStock(rs.getInt("registros"), rs.getLong("unidades"), rs.getInt("sin_stock"),
                        rs.getInt("bajo_minimo"), rs.getInt("almacenes")));
    }

    @Override
    public List<Opcion> centros(Permisos p) throws SQLException {
        return lista("SELECT id_centro, nombre FROM centros WHERE " + p.sqlCentros("id_centro") + " ORDER BY nombre",
                rs -> new Opcion(rs.getInt("id_centro"), rs.getString("nombre")));
    }

    @Override
    public List<Opcion> insumosActivos() throws SQLException {
        return lista("SELECT id_insumo, codigo || ' - ' || descripcion FROM insumos WHERE activo = true ORDER BY descripcion",
                rs -> new Opcion(rs.getInt(1), rs.getString(2)));
    }

    @Override
    public List<Opcion> almacenesActivos(Permisos p) throws SQLException {
        return lista("SELECT a.id_almacen, a.nombre || ' (' || c.nombre || ')' FROM almacenes a JOIN centros c ON c.id_centro = a.id_centro "
                + "WHERE a.activo = true AND " + p.sqlCentros("a.id_centro") + " ORDER BY c.nombre, a.nombre",
                rs -> new Opcion(rs.getInt(1), rs.getString(2)));
    }

    @Override
    public Integer centroDeAlmacen(int idAlmacen) throws SQLException {
        return uno("SELECT id_centro FROM almacenes WHERE id_almacen = ? AND activo = true", rs -> enteroONull(rs, "id_centro"), idAlmacen);
    }

    @Override
    public boolean insumoActivo(int idInsumo) throws SQLException {
        return existe("SELECT 1 FROM insumos WHERE id_insumo = ? AND activo = true", idInsumo);
    }

    @Override
    public boolean existe(int idInsumo, int idAlmacen) throws SQLException {
        return existe("SELECT 1 FROM stock_almacen WHERE id_insumo = ? AND id_almacen = ?", idInsumo, idAlmacen);
    }

    @Override
    public int insertar(int idInsumo, int idAlmacen, int minimo) throws SQLException {
        return insertarConId("INSERT INTO stock_almacen (id_insumo, id_almacen, stock_actual, stock_minimo) VALUES (?, ?, 0, ?) RETURNING id_stock",
                idInsumo, idAlmacen, minimo);
    }

    @Override
    public int actualizarMinimo(int idStock, int minimo) throws SQLException {
        return ejecutar("UPDATE stock_almacen SET stock_minimo = ? WHERE id_stock = ?", minimo, idStock);
    }

    @Override
    public boolean tieneMovimientos(int idInsumo, int idAlmacen) throws SQLException {
        return existe("SELECT 1 FROM movimientos_stock WHERE id_insumo = ? AND id_almacen = ?", idInsumo, idAlmacen);
    }

    @Override
    public int eliminarVacio(int idStock) throws SQLException {
        return ejecutar("DELETE FROM stock_almacen s WHERE s.id_stock = ? AND s.stock_actual = 0 AND NOT EXISTS "
                + "(SELECT 1 FROM movimientos_stock m WHERE m.id_insumo = s.id_insumo AND m.id_almacen = s.id_almacen)", idStock);
    }

    @Override
    public List<Opcion> almacenes(int idCentro, Permisos p) throws SQLException {
        return lista("SELECT id_almacen, nombre FROM almacenes WHERE (? = 0 OR id_centro = ?) AND " + p.sqlCentros("id_centro") + " ORDER BY nombre",
                rs -> new Opcion(rs.getInt("id_almacen"), rs.getString("nombre")), idCentro, idCentro);
    }

    @Override
    public List<Stock> listar(int idCentro, int idAlmacen, String situacion, Permisos p) throws SQLException {
        return lista("SELECT * FROM ("
                + "  SELECT s.id_stock, s.stock_actual, s.stock_minimo, i.codigo, i.descripcion, i.precio_compra, "
                + "         u.abreviatura, a.nombre AS almacen, a.id_centro, s.id_almacen, c.nombre AS centro, "
                + "         CASE WHEN s.stock_actual <= 0 THEN 'SIN_STOCK' "
                + "              WHEN s.stock_actual <= s.stock_minimo THEN 'BAJO_MINIMO' ELSE 'NORMAL' END AS situacion "
                + "  FROM stock_almacen s "
                + "  INNER JOIN insumos i ON i.id_insumo = s.id_insumo "
                + "  INNER JOIN almacenes a ON a.id_almacen = s.id_almacen "
                + "  LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "  LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida"
                + ") x "
                + "WHERE (? = 0 OR x.id_centro = ?) AND (? = 0 OR x.id_almacen = ?) AND (? = '' OR x.situacion = ?) AND " + p.sqlCentros("x.id_centro") + " "
                + "ORDER BY CASE x.situacion WHEN 'SIN_STOCK' THEN 0 WHEN 'BAJO_MINIMO' THEN 1 ELSE 2 END, x.centro, x.almacen, x.descripcion", rs -> {
                    Stock s = new Stock();
                    s.setId(rs.getInt("id_stock"));
                    s.setCodigo(rs.getString("codigo"));
                    s.setDescripcion(rs.getString("descripcion"));
                    s.setAbreviatura(rs.getString("abreviatura"));
                    s.setCentro(rs.getString("centro"));
                    s.setAlmacen(rs.getString("almacen"));
                    s.setActual(rs.getInt("stock_actual"));
                    s.setMinimo(rs.getInt("stock_minimo"));
                    s.setSituacion(rs.getString("situacion"));
                    s.setPrecio(rs.getBigDecimal("precio_compra"));
                    return s;
                }, idCentro, idCentro, idAlmacen, idAlmacen, situacion, situacion);
    }

    @Override
    public Stock buscar(int idStock, Permisos p) throws SQLException {
        return uno("SELECT s.id_stock, s.id_insumo, s.id_almacen, s.stock_actual, s.stock_minimo, "
                + "       i.codigo, i.descripcion, i.precio_compra, u.nombre AS unidad, u.abreviatura, "
                + "       a.nombre AS almacen, a.ubicacion, c.nombre AS centro "
                + "FROM stock_almacen s "
                + "INNER JOIN insumos i ON i.id_insumo = s.id_insumo "
                + "INNER JOIN almacenes a ON a.id_almacen = s.id_almacen "
                + "LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida "
                + "WHERE s.id_stock = ? AND " + p.sqlCentros("a.id_centro"), rs -> {
                    Stock s = new Stock();
                    s.setId(rs.getInt("id_stock"));
                    s.setIdInsumo(rs.getInt("id_insumo"));
                    s.setIdAlmacen(rs.getInt("id_almacen"));
                    s.setActual(rs.getInt("stock_actual"));
                    s.setMinimo(rs.getInt("stock_minimo"));
                    s.setCodigo(rs.getString("codigo"));
                    s.setDescripcion(rs.getString("descripcion"));
                    s.setPrecio(rs.getBigDecimal("precio_compra"));
                    s.setUnidad(rs.getString("unidad"));
                    s.setAbreviatura(rs.getString("abreviatura"));
                    s.setAlmacen(rs.getString("almacen"));
                    s.setUbicacion(rs.getString("ubicacion"));
                    s.setCentro(rs.getString("centro"));
                    return s;
                }, idStock);
    }

    @Override
    public List<IngresoStock> ingresos(int idInsumo, int idAlmacen) throws SQLException {
        return lista("SELECT c.numero_oc, c.fecha_compra, c.estado, p.razon_social, d.cantidad, d.cantidad_recibida, d.precio_unitario "
                + "FROM detalle_compra d "
                + "INNER JOIN compras c ON c.id_compra = d.id_compra "
                + "LEFT JOIN proveedores p ON p.id_proveedor = c.id_proveedor "
                + "WHERE d.tipo_item = 'INSUMO' AND d.id_insumo = ? AND d.id_almacen_destino = ? AND d.cantidad_recibida > 0 "
                + "ORDER BY c.fecha_compra DESC, c.id_compra DESC",
                rs -> new IngresoStock(rs.getString("numero_oc"), rs.getDate("fecha_compra"), rs.getString("estado"),
                        rs.getString("razon_social"), rs.getInt("cantidad"), rs.getInt("cantidad_recibida"), rs.getBigDecimal("precio_unitario")),
                idInsumo, idAlmacen);
    }

    @Override
    public List<Stock> otrosAlmacenes(int idInsumo, int idStockExcluido, Permisos p) throws SQLException {
        return lista("SELECT a.nombre AS almacen, c.nombre AS centro, s.stock_actual, s.stock_minimo "
                + "FROM stock_almacen s INNER JOIN almacenes a ON a.id_almacen = s.id_almacen "
                + "LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "WHERE s.id_insumo = ? AND s.id_stock <> ? AND " + p.sqlCentros("a.id_centro") + " ORDER BY c.nombre, a.nombre", rs -> {
                    Stock s = new Stock();
                    s.setAlmacen(rs.getString("almacen"));
                    s.setCentro(rs.getString("centro"));
                    s.setActual(rs.getInt("stock_actual"));
                    s.setMinimo(rs.getInt("stock_minimo"));
                    return s;
                }, idInsumo, idStockExcluido);
    }
}
