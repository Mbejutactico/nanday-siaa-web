package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.SolicitudCompraDAO;
import com.mycompany.proyectoprueba.modelo.BorradorCompra;
import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.InsumoPrecio;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class SolicitudCompraDAOJdbc extends JdbcBase implements SolicitudCompraDAO {

    public SolicitudCompraDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<InsumoPrecio> insumosActivos() throws SQLException {
        return lista("SELECT id_insumo, descripcion, precio_compra FROM insumos WHERE activo=true ORDER BY descripcion",
                rs -> new InsumoPrecio(rs.getInt("id_insumo"), rs.getString("descripcion"), rs.getBigDecimal("precio_compra")));
    }

    @Override
    public BigDecimal precioInsumoActivo(int idInsumo) throws SQLException {
        return uno("SELECT precio_compra FROM insumos WHERE id_insumo = ? AND activo = true", rs -> rs.getBigDecimal("precio_compra"), idInsumo);
    }

    @Override
    public boolean proveedorActivo(int idProveedor) throws SQLException {
        return existe("SELECT 1 FROM proveedores WHERE id_proveedor = ? AND activo = true", idProveedor);
    }

    @Override
    public BorradorCompra borrador(int idUsuario) throws SQLException {
        return uno("SELECT c.id_compra, c.id_proveedor, p.ruc, c.id_centro_destino, c.observaciones "
                + "FROM compras c LEFT JOIN proveedores p ON p.id_proveedor = c.id_proveedor "
                + "WHERE c.id_comprador = ? AND c.estado = 'BORRADOR' ORDER BY c.id_compra DESC LIMIT 1",
                rs -> new BorradorCompra(rs.getInt("id_compra"), rs.getInt("id_proveedor"), rs.getString("ruc"),
                        rs.getInt("id_centro_destino"), rs.getString("observaciones")), idUsuario);
    }

    @Override
    public int idBorrador(int idUsuario) throws SQLException {
        Integer id = uno("SELECT id_compra FROM compras WHERE id_comprador = ? AND estado = 'BORRADOR' ORDER BY id_compra DESC LIMIT 1",
                rs -> rs.getInt(1), idUsuario);
        return id == null ? 0 : id.intValue();
    }

    @Override
    public BorradorCompra bloquearBorrador(int idUsuario) throws SQLException {
        return uno("SELECT id_compra, fecha_compra FROM compras WHERE id_comprador = ? AND estado = 'BORRADOR' ORDER BY id_compra DESC LIMIT 1 FOR UPDATE",
                rs -> new BorradorCompra(rs.getInt(1), 0, null, 0, null, rs.getDate("fecha_compra")), idUsuario);
    }

    @Override
    public int crearBorrador(int idUsuario, int idProveedor, int idCentroDestino, String observaciones) throws SQLException {
        return insertarConId("INSERT INTO compras (fecha_compra, id_proveedor, id_comprador, id_usuario_solicitante, id_centro_destino, estado, observaciones) "
                + "VALUES (CURRENT_DATE, ?, ?, ?, ?, 'BORRADOR', ?) RETURNING id_compra",
                idProveedor, idUsuario, idUsuario, idCentroDestino, observaciones == null ? "" : observaciones);
    }

    @Override
    public List<DetalleCompra> detalle(int idCompra) throws SQLException {
        return lista("SELECT d.id_detalle_compra, i.codigo, i.descripcion, d.precio_unitario, d.cantidad, "
                + "(d.precio_unitario * d.cantidad) AS subtotal, 'INSUMO' AS tipo_item "
                + "FROM detalle_compra d "
                + "INNER JOIN insumos i ON d.id_insumo = i.id_insumo "
                + "WHERE d.id_compra = ? AND d.tipo_item = 'INSUMO' "
                + "UNION ALL "
                + "SELECT d.id_detalle_compra, i.codigo, i.descripcion, d.precio_unitario, d.cantidad, "
                + "(d.precio_unitario * d.cantidad) AS subtotal, 'EQUIPO' AS tipo_item "
                + "FROM detalle_compra d "
                + "INNER JOIN equipos e ON d.id_equipo = e.id_equipo "
                + "INNER JOIN insumos i ON e.id_insumo_base = i.id_insumo "
                + "WHERE d.id_compra = ? AND d.tipo_item = 'EQUIPO' "
                + "ORDER BY id_detalle_compra", rs -> {
                    DetalleCompra d = new DetalleCompra();
                    d.setId(rs.getInt("id_detalle_compra"));
                    d.setTipoItem(rs.getString("tipo_item"));
                    d.setCodigo(rs.getString("codigo"));
                    d.setDescripcion(rs.getString("descripcion"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    return d;
                }, idCompra, idCompra);
    }

    @Override
    public int contarLineas(int idCompra) throws SQLException {
        return entero("SELECT COUNT(*) FROM detalle_compra WHERE id_compra = ?", idCompra);
    }

    @Override
    public int sumarCantidadInsumo(int idCompra, int idInsumo, int cantidad) throws SQLException {
        return ejecutar("UPDATE detalle_compra SET cantidad = cantidad + ? WHERE id_compra = ? AND tipo_item = 'INSUMO' AND id_insumo = ?",
                cantidad, idCompra, idInsumo);
    }

    @Override
    public void insertarLineaInsumo(int idCompra, int idInsumo, int cantidad, BigDecimal precio) throws SQLException {
        ejecutar("INSERT INTO detalle_compra (id_compra, tipo_item, id_insumo, cantidad, cantidad_recibida, precio_unitario) VALUES (?, 'INSUMO', ?, ?, 0, ?)",
                idCompra, idInsumo, cantidad, precio);
    }

    @Override
    public int crearEquipoSolicitado(String serialTemporal, int idInsumoBase) throws SQLException {
        return insertarConId("INSERT INTO equipos (serial_activo, id_insumo_base, fecha_ingreso, estado) VALUES (?, ?, NULL, 'SOLICITADO') RETURNING id_equipo",
                serialTemporal, idInsumoBase);
    }

    @Override
    public void insertarLineaEquipo(int idCompra, int idEquipo, BigDecimal precio) throws SQLException {
        ejecutar("INSERT INTO detalle_compra (id_compra, tipo_item, id_equipo, cantidad, cantidad_recibida, precio_unitario) VALUES (?, 'EQUIPO', ?, 1, 0, ?)",
                idCompra, idEquipo, precio);
    }

    @Override
    public DetalleCompra lineaDelBorrador(int idDetalle, int idUsuario) throws SQLException {
        return uno("SELECT d.tipo_item, d.id_equipo FROM detalle_compra d "
                + "INNER JOIN compras c ON c.id_compra = d.id_compra "
                + "WHERE d.id_detalle_compra = ? AND c.id_comprador = ? AND c.estado = 'BORRADOR'",
                rs -> {
                    DetalleCompra d = new DetalleCompra();
                    d.setId(idDetalle);
                    d.setTipoItem(rs.getString("tipo_item"));
                    d.setIdEquipo(enteroONull(rs, "id_equipo"));
                    return d;
                }, idDetalle, idUsuario);
    }

    @Override
    public void eliminarLinea(int idDetalle) throws SQLException {
        ejecutar("DELETE FROM detalle_compra WHERE id_detalle_compra = ?", idDetalle);
    }

    @Override
    public void eliminarEquipoSolicitado(int idEquipo) throws SQLException {
        ejecutar("DELETE FROM equipos WHERE id_equipo = ? AND estado = 'SOLICITADO'", idEquipo);
    }

    @Override
    public void actualizarCabecera(int idCompra, int idProveedor, int idCentroDestino, String observaciones) throws SQLException {
        ejecutar("UPDATE compras SET id_proveedor = ?, id_centro_destino = ?, observaciones = ? WHERE id_compra = ?",
                idProveedor, idCentroDestino, observaciones == null ? "" : observaciones, idCompra);
    }

    @Override
    public int siguienteCorrelativo(Date fecha) throws SQLException {
        return entero("INSERT INTO correlativo_oc (fecha, ultimo) VALUES (?, 1) "
                + "ON CONFLICT (fecha) DO UPDATE SET ultimo = correlativo_oc.ultimo + 1 RETURNING ultimo", fecha);
    }

    @Override
    public void emitir(int idCompra, String numeroOc) throws SQLException {
        ejecutar("UPDATE compras SET estado = 'SOLICITADO', numero_oc = ? WHERE id_compra = ?", numeroOc, idCompra);
    }

    @Override
    public List<Integer> equiposDelBorrador(int idUsuario) throws SQLException {
        return lista("SELECT d.id_equipo FROM detalle_compra d "
                + "INNER JOIN compras c ON d.id_compra = c.id_compra "
                + "WHERE c.id_comprador = ? AND c.estado = 'BORRADOR' AND d.tipo_item = 'EQUIPO'",
                rs -> rs.getInt("id_equipo"), idUsuario);
    }

    @Override
    public int eliminarBorrador(int idUsuario) throws SQLException {
        return ejecutar("DELETE FROM compras WHERE id_comprador = ? AND estado = 'BORRADOR'", idUsuario);
    }
}
