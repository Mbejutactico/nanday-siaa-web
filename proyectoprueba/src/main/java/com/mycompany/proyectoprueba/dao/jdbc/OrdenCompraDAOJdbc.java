package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.OrdenCompraDAO;
import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.EquipoEstado;
import com.mycompany.proyectoprueba.modelo.LineaOC;
import com.mycompany.proyectoprueba.modelo.LineaRecepcion;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenCompra;
import com.mycompany.proyectoprueba.modelo.RecepcionOC;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class OrdenCompraDAOJdbc extends JdbcBase implements OrdenCompraDAO {

    public OrdenCompraDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<OrdenCompra> listar(Permisos p) throws SQLException {
        return lista("SELECT c.id_compra, c.numero_oc, c.fecha_compra, p.razon_social AS proveedor, "
                + "cen.nombre AS centro, u.nombre || ' ' || u.apellido AS solicitante, c.estado, "
                + "COALESCE(SUM(d.precio_unitario * d.cantidad), 0) AS total "
                + "FROM compras c "
                + "LEFT JOIN proveedores p ON c.id_proveedor = p.id_proveedor "
                + "LEFT JOIN centros cen ON c.id_centro_destino = cen.id_centro "
                + "LEFT JOIN usuarios u ON c.id_usuario_solicitante = u.id_usuario "
                + "LEFT JOIN detalle_compra d ON c.id_compra = d.id_compra "
                + "WHERE c.numero_oc IS NOT NULL AND c.numero_oc != '' AND " + p.sqlCentros("c.id_centro_destino") + " "
                + "GROUP BY c.id_compra, c.numero_oc, c.fecha_compra, p.razon_social, cen.nombre, u.nombre, u.apellido, c.estado "
                + "ORDER BY c.fecha_compra DESC, c.numero_oc DESC", rs -> {
                    OrdenCompra o = new OrdenCompra();
                    o.setId(rs.getInt("id_compra"));
                    o.setNumero(rs.getString("numero_oc"));
                    o.setFecha(rs.getDate("fecha_compra"));
                    o.setProveedor(rs.getString("proveedor"));
                    o.setCentro(rs.getString("centro"));
                    o.setSolicitante(rs.getString("solicitante"));
                    o.setEstado(rs.getString("estado"));
                    o.setTotal(rs.getBigDecimal("total"));
                    return o;
                });
    }

    @Override
    public OrdenCompra cabecera(int idCompra, Permisos p) throws SQLException {
        return uno("SELECT id_proveedor, id_centro_destino, observaciones, estado FROM compras WHERE id_compra = ? AND "
                + p.sqlCentros("id_centro_destino"), rs -> {
                    OrdenCompra o = new OrdenCompra();
                    o.setId(idCompra);
                    o.setIdProveedor(rs.getInt("id_proveedor"));
                    o.setIdCentroDestino(rs.getInt("id_centro_destino"));
                    o.setObservaciones(rs.getString("observaciones"));
                    o.setEstado(rs.getString("estado"));
                    return o;
                }, idCompra);
    }

    @Override
    public boolean visible(int idCompra, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM compras WHERE id_compra = ? AND " + p.sqlCentros("id_centro_destino"), idCompra);
    }

    @Override
    public OrdenCompra ficha(int idCompra) throws SQLException {
        return uno("SELECT c.numero_oc, c.fecha_compra, c.estado, c.observaciones, c.id_centro_destino, "
                + "       p.razon_social, p.ruc, p.telefono, p.email AS prov_email, p.direccion, "
                + "       ce.nombre AS centro, u.nombre || ' ' || u.apellido AS solicitante, "
                + "       uc.nombre || ' ' || uc.apellido AS comprador "
                + "FROM compras c LEFT JOIN proveedores p ON p.id_proveedor = c.id_proveedor "
                + "LEFT JOIN centros ce ON ce.id_centro = c.id_centro_destino "
                + "LEFT JOIN usuarios u ON u.id_usuario = c.id_usuario_solicitante "
                + "LEFT JOIN usuarios uc ON uc.id_usuario = c.id_comprador "
                + "WHERE c.id_compra = ?", rs -> {
                    OrdenCompra o = new OrdenCompra();
                    o.setId(idCompra);
                    o.setNumero(rs.getString("numero_oc"));
                    o.setFecha(rs.getDate("fecha_compra"));
                    o.setEstado(rs.getString("estado"));
                    o.setObservaciones(rs.getString("observaciones"));
                    o.setIdCentroDestino(rs.getInt("id_centro_destino"));
                    o.setProveedor(rs.getString("razon_social"));
                    o.setRuc(rs.getString("ruc"));
                    o.setTelefono(rs.getString("telefono"));
                    o.setEmailProveedor(rs.getString("prov_email"));
                    o.setDireccionProveedor(rs.getString("direccion"));
                    o.setCentro(rs.getString("centro"));
                    o.setSolicitante(rs.getString("solicitante"));
                    o.setComprador(rs.getString("comprador"));
                    return o;
                }, idCompra);
    }

    @Override
    public List<DetalleCompra> lineasFicha(int idCompra) throws SQLException {
        return lista("SELECT d.tipo_item, COALESCE(i.codigo, ib.codigo) AS codigo, "
                + "       COALESCE(i.descripcion, ib.descripcion) AS descripcion, "
                + "       d.cantidad, d.cantidad_recibida, d.precio_unitario, "
                + "       (d.precio_unitario * d.cantidad) AS subtotal, e.serial_activo "
                + "FROM detalle_compra d LEFT JOIN insumos i ON i.id_insumo = d.id_insumo "
                + "LEFT JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "LEFT JOIN insumos ib ON ib.id_insumo = e.id_insumo_base "
                + "WHERE d.id_compra = ? ORDER BY d.id_detalle_compra", rs -> {
                    DetalleCompra d = new DetalleCompra();
                    d.setTipoItem(rs.getString("tipo_item"));
                    d.setCodigo(rs.getString("codigo"));
                    d.setDescripcion(rs.getString("descripcion"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setCantidadRecibida(rs.getInt("cantidad_recibida"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setSerialActivo(rs.getString("serial_activo"));
                    return d;
                }, idCompra);
    }

    @Override
    public List<DetalleCompra> lineasEditables(int idCompra) throws SQLException {
        return lista("SELECT d.id_detalle_compra, d.tipo_item, i.codigo, i.descripcion, d.precio_unitario, d.cantidad, "
                + "d.cantidad_recibida, (d.precio_unitario * d.cantidad) AS subtotal, e.serial_activo "
                + "FROM detalle_compra d "
                + "LEFT JOIN equipos e ON d.id_equipo = e.id_equipo "
                + "INNER JOIN insumos i ON i.id_insumo = COALESCE(d.id_insumo, e.id_insumo_base) "
                + "WHERE d.id_compra = ? ORDER BY d.id_detalle_compra", rs -> {
                    DetalleCompra d = new DetalleCompra();
                    d.setId(rs.getInt("id_detalle_compra"));
                    d.setTipoItem(rs.getString("tipo_item"));
                    d.setCodigo(rs.getString("codigo"));
                    d.setDescripcion(rs.getString("descripcion"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setCantidadRecibida(rs.getInt("cantidad_recibida"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setSerialActivo(rs.getString("serial_activo"));
                    return d;
                }, idCompra);
    }

    @Override
    public List<RecepcionOC> recepciones(int idCompra) throws SQLException {
        return lista("SELECT r.id_recepcion, r.numero, r.fecha, r.historica, r.anulada, r.fecha_anulacion, r.motivo_anulacion, "
                + "       u.nombre || ' ' || u.apellido AS usuario, ua.nombre || ' ' || ua.apellido AS anulado_por "
                + "FROM recepciones_oc r LEFT JOIN usuarios u ON u.id_usuario = r.id_usuario "
                + "LEFT JOIN usuarios ua ON ua.id_usuario = r.id_usuario_anula "
                + "WHERE r.id_compra = ? ORDER BY r.fecha, r.id_recepcion", rs -> {
                    RecepcionOC r = new RecepcionOC();
                    r.id = rs.getInt("id_recepcion");
                    r.numero = rs.getString("numero");
                    r.historica = rs.getBoolean("historica");
                    r.fecha = rs.getTimestamp("fecha");
                    r.usuario = rs.getString("usuario");
                    r.anulada = rs.getBoolean("anulada");
                    r.fechaAnulacion = rs.getTimestamp("fecha_anulacion");
                    r.anuladoPor = rs.getString("anulado_por");
                    r.motivoAnulacion = rs.getString("motivo_anulacion");
                    return r;
                }, idCompra);
    }

    @Override
    public List<LineaRecepcion> lineasDeRecepciones(int idCompra) throws SQLException {
        return lista("SELECT rd.id_recepcion, rd.id, d.tipo_item, COALESCE(i.codigo, ib.codigo) AS codigo, "
                + "       COALESCE(i.descripcion, ib.descripcion) AS descripcion, rd.cantidad, a.nombre AS almacen, "
                + "       cc.codigo AS cc_codigo, cc.descripcion AS cc_desc, rd.numero_activo "
                + "FROM recepciones_oc_detalle rd JOIN recepciones_oc r ON r.id_recepcion = rd.id_recepcion "
                + "JOIN detalle_compra d ON d.id_detalle_compra = rd.id_detalle_compra "
                + "LEFT JOIN insumos i ON i.id_insumo = d.id_insumo LEFT JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "LEFT JOIN insumos ib ON ib.id_insumo = e.id_insumo_base "
                + "LEFT JOIN almacenes a ON a.id_almacen = rd.id_almacen "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = rd.id_centro_costo "
                + "WHERE r.id_compra = ? ORDER BY rd.id", rs -> {
                    LineaRecepcion l = new LineaRecepcion();
                    l.idRecepcion = rs.getInt("id_recepcion");
                    l.id = rs.getInt("id");
                    l.tipoItem = rs.getString("tipo_item");
                    l.codigo = rs.getString("codigo");
                    l.descripcion = rs.getString("descripcion");
                    l.cantidad = rs.getInt("cantidad");
                    l.almacen = rs.getString("almacen");
                    l.ccCodigo = rs.getString("cc_codigo");
                    l.ccDescripcion = rs.getString("cc_desc");
                    l.numeroActivo = rs.getString("numero_activo");
                    return l;
                }, idCompra);
    }

    @Override
    public List<Opcion> almacenesDelCentro(int idCentro) throws SQLException {
        return lista("SELECT id_almacen, nombre FROM almacenes WHERE id_centro = ? ORDER BY activo DESC, nombre",
                rs -> new Opcion(rs.getInt(1), rs.getString(2)), idCentro);
    }

    @Override
    public List<Opcion> almacenesActivos(int idCentro) throws SQLException {
        return lista("SELECT id_almacen, nombre FROM almacenes WHERE id_centro = ? AND activo=true ORDER BY nombre",
                rs -> new Opcion(rs.getInt("id_almacen"), rs.getString("nombre")), idCentro);
    }

    @Override
    public List<Opcion> centrosCostoActivos(int idCentro) throws SQLException {
        return lista("SELECT id_centro_costo, codigo, descripcion FROM centros_costo WHERE id_centro = ? AND activo = true ORDER BY codigo", rs -> {
            String d = rs.getString("descripcion");
            String cod = rs.getString("codigo");
            return new Opcion(rs.getInt("id_centro_costo"), (cod == null ? "" : cod) + (d == null || d.trim().isEmpty() ? "" : " - " + d));
        }, idCentro);
    }

    @Override
    public List<Opcion> pendientesDeRecibir(Permisos p) throws SQLException {
        return lista("SELECT id_compra, numero_oc FROM compras WHERE estado IN ('SOLICITADO', 'PARCIAL') AND numero_oc IS NOT NULL AND "
                + p.sqlCentros("id_centro_destino") + " ORDER BY numero_oc", rs -> new Opcion(rs.getInt("id_compra"), rs.getString("numero_oc")));
    }

    @Override
    public OrdenCompra paraRecibir(int idCompra) throws SQLException {
        return uno("SELECT numero_oc, estado, id_centro_destino FROM compras WHERE id_compra = ?", rs -> {
            OrdenCompra o = new OrdenCompra();
            o.setId(idCompra);
            o.setNumero(rs.getString("numero_oc"));
            o.setEstado(rs.getString("estado"));
            o.setIdCentroDestino(rs.getInt("id_centro_destino"));
            return o;
        }, idCompra);
    }

    @Override
    public List<DetalleCompra> lineasPendientes(int idCompra) throws SQLException {
        return lista("SELECT d.id_detalle_compra, d.tipo_item, i.codigo, i.descripcion, d.cantidad, d.cantidad_recibida, "
                + "(d.cantidad - d.cantidad_recibida) AS pendiente "
                + "FROM detalle_compra d "
                + "LEFT JOIN equipos e ON d.id_equipo = e.id_equipo "
                + "INNER JOIN insumos i ON i.id_insumo = COALESCE(d.id_insumo, e.id_insumo_base) "
                + "WHERE d.id_compra = ? AND d.cantidad > d.cantidad_recibida "
                + "ORDER BY d.tipo_item DESC, d.id_detalle_compra", rs -> {
                    DetalleCompra d = new DetalleCompra();
                    d.setId(rs.getInt("id_detalle_compra"));
                    d.setTipoItem(rs.getString("tipo_item"));
                    d.setCodigo(rs.getString("codigo"));
                    d.setDescripcion(rs.getString("descripcion"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setCantidadRecibida(rs.getInt("cantidad_recibida"));
                    d.setPendiente(rs.getInt("pendiente"));
                    return d;
                }, idCompra);
    }

    @Override
    public String estado(int idCompra) throws SQLException {
        return uno("SELECT estado FROM compras WHERE id_compra = ?", rs -> rs.getString(1), idCompra);
    }

    @Override
    public void actualizarCabecera(int idCompra, int idProveedor, int idCentro, String observaciones) throws SQLException {
        ejecutar("UPDATE compras SET id_proveedor=?, id_centro_destino=?, observaciones=? WHERE id_compra=?",
                idProveedor, idCentro, observaciones, idCompra);
    }

    @Override
    public LineaOC linea(int idDetalle) throws SQLException {
        return uno("SELECT c.id_compra, c.estado, d.tipo_item, d.id_equipo, d.cantidad_recibida, "
                + "(SELECT COUNT(*) FROM detalle_compra x WHERE x.id_compra = c.id_compra) AS lineas "
                + "FROM detalle_compra d INNER JOIN compras c ON d.id_compra = c.id_compra WHERE d.id_detalle_compra = ?", rs -> {
                    LineaOC l = new LineaOC();
                    l.idDetalle = idDetalle;
                    l.idCompra = rs.getInt("id_compra");
                    l.estadoOC = rs.getString("estado");
                    l.tipoItem = rs.getString("tipo_item");
                    l.idEquipo = rs.getInt("id_equipo");
                    l.recibida = rs.getInt("cantidad_recibida");
                    l.lineasOC = rs.getInt("lineas");
                    return l;
                }, idDetalle);
    }

    @Override
    public void actualizarLinea(int idDetalle, int cantidad, BigDecimal precio) throws SQLException {
        ejecutar("UPDATE detalle_compra SET cantidad=?, precio_unitario=? WHERE id_detalle_compra=?", cantidad, precio, idDetalle);
    }

    @Override
    public void eliminarLinea(int idDetalle) throws SQLException {
        ejecutar("DELETE FROM detalle_compra WHERE id_detalle_compra=?", idDetalle);
    }

    @Override
    public void eliminarEquipoSolicitado(int idEquipo) throws SQLException {
        ejecutar("DELETE FROM equipos WHERE id_equipo = ? AND estado = 'SOLICITADO'", idEquipo);
    }

    @Override
    public boolean tieneRecepciones(int idDetalle) throws SQLException {
        return existe("SELECT 1 FROM recepciones_oc_detalle WHERE id_detalle_compra = ? LIMIT 1", idDetalle);
    }

    @Override
    public void agregarLineaInsumo(int idCompra, int idInsumo, int cantidad, BigDecimal precio) throws SQLException {
        ejecutar("INSERT INTO detalle_compra (id_compra, tipo_item, id_insumo, cantidad, cantidad_recibida, precio_unitario) "
                + "VALUES (?, 'INSUMO', ?, ?, 0, ?)", idCompra, idInsumo, cantidad, precio);
    }

    @Override
    public void anular(int idCompra) throws SQLException {
        ejecutar("UPDATE compras SET estado='ANULADO' WHERE id_compra=?", idCompra);
    }

    @Override
    public void anularEquiposPendientes(int idCompra) throws SQLException {
        ejecutar("UPDATE equipos SET estado='ANULADO', activo=false "
                + "WHERE estado='SOLICITADO' AND id_equipo IN "
                + "(SELECT id_equipo FROM detalle_compra WHERE id_compra=? AND tipo_item='EQUIPO')", idCompra);
    }

    @Override
    public String recalcularEstado(int idCompra) throws SQLException {
        long[] r = uno("SELECT COUNT(*) FILTER (WHERE cantidad > cantidad_recibida) AS pendientes, "
                + "COALESCE(SUM(cantidad_recibida), 0) AS recibidas FROM detalle_compra WHERE id_compra = ?",
                rs -> new long[]{rs.getInt("pendientes"), rs.getLong("recibidas")}, idCompra);
        String nuevo = (r[0] == 0) ? "RECIBIDO" : (r[1] > 0 ? "PARCIAL" : "SOLICITADO");
        ejecutar("UPDATE compras SET estado = ? WHERE id_compra = ? AND estado IN ('SOLICITADO', 'PARCIAL', 'RECIBIDO')", nuevo, idCompra);
        return nuevo;
    }

    @Override
    public OrdenCompra bloquear(int idCompra) throws SQLException {
        return uno("SELECT estado, id_centro_destino, numero_oc FROM compras WHERE id_compra = ? FOR UPDATE", rs -> {
            OrdenCompra o = new OrdenCompra();
            o.setId(idCompra);
            o.setEstado(rs.getString("estado"));
            o.setIdCentroDestino(rs.getInt("id_centro_destino"));
            o.setNumero(rs.getString("numero_oc"));
            return o;
        }, idCompra);
    }

    @Override
    public int contarRecepciones(int idCompra) throws SQLException {
        return entero("SELECT COUNT(*) FROM recepciones_oc WHERE id_compra = ?", idCompra);
    }

    @Override
    public int crearRecepcion(int idCompra, String numero, int idUsuario) throws SQLException {
        return insertarConId("INSERT INTO recepciones_oc (id_compra, numero, id_usuario) VALUES (?, ?, ?) RETURNING id_recepcion",
                idCompra, numero, idUsuario);
    }

    @Override
    public LineaOC bloquearLinea(int idDetalle, int idCompra) throws SQLException {
        return uno("SELECT d.tipo_item, d.id_insumo, d.id_equipo, d.cantidad, d.cantidad_recibida, d.precio_unitario, i.descripcion "
                + "FROM detalle_compra d "
                + "LEFT JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "LEFT JOIN insumos i ON i.id_insumo = COALESCE(d.id_insumo, e.id_insumo_base) "
                + "WHERE d.id_detalle_compra = ? AND d.id_compra = ? FOR UPDATE OF d", rs -> {
                    LineaOC l = new LineaOC();
                    l.idDetalle = idDetalle;
                    l.idCompra = idCompra;
                    l.tipoItem = rs.getString("tipo_item");
                    l.idInsumo = rs.getInt("id_insumo");
                    l.idEquipo = rs.getInt("id_equipo");
                    l.descripcion = rs.getString("descripcion");
                    l.precio = rs.getBigDecimal("precio_unitario");
                    l.cantidad = rs.getInt("cantidad");
                    l.recibida = rs.getInt("cantidad_recibida");
                    return l;
                }, idDetalle, idCompra);
    }

    @Override
    public boolean centroCostoDelCentro(int idCentroCosto, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM centros_costo WHERE id_centro_costo = ? AND id_centro = ? AND activo = true", idCentroCosto, idCentro);
    }

    @Override
    public boolean almacenActivoDelCentro(int idAlmacen, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM almacenes WHERE id_almacen = ? AND id_centro = ? AND activo = true", idAlmacen, idCentro);
    }

    @Override
    public boolean almacenDelCentro(int idAlmacen, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM almacenes WHERE id_almacen = ? AND id_centro = ?", idAlmacen, idCentro);
    }

    @Override
    public boolean serieDuplicada(String serie, int idEquipoExcluido) throws SQLException {
        return existe("SELECT 1 FROM equipos WHERE UPPER(numero_serie) = UPPER(?) AND id_equipo <> ?", serie, idEquipoExcluido);
    }

    @Override
    public String siguienteNumeroActivo() throws SQLException {
        return uno("SELECT 'AF-' || lpad(nextval('public.seq_numero_activo')::text, 6, '0')", rs -> rs.getString(1));
    }

    @Override
    public void ingresarEquipo(int idEquipo, String numeroActivo, String serie, int idCentro, int idCentroCosto) throws SQLException {
        ejecutar("UPDATE equipos SET serial_activo = ?, numero_serie = ?, estado = 'DISPONIBLE', fecha_ingreso = CURRENT_DATE, "
                + "id_centro_actual = ?, id_centro_costo_actual = ?, id_almacen_actual = NULL, activo = true WHERE id_equipo = ?",
                numeroActivo, texto(serie), idCentro, idCentroCosto, idEquipo);
    }

    @Override
    public void recibirLineaEquipo(int idDetalle, int cantidad, int idCentro, int idCentroCosto, int idEquipo) throws SQLException {
        ejecutar("UPDATE detalle_compra SET cantidad_recibida = cantidad_recibida + ?, "
                + "id_centro_asignado = ?, id_centro_costo_asignado = ?, id_equipo_generado = ? WHERE id_detalle_compra = ?",
                cantidad, idCentro, idCentroCosto, idEquipo, idDetalle);
    }

    @Override
    public void recibirLineaInsumo(int idDetalle, int cantidad, int idAlmacen) throws SQLException {
        ejecutar("UPDATE detalle_compra SET cantidad_recibida = cantidad_recibida + ?, id_almacen_destino = ? WHERE id_detalle_compra = ?",
                cantidad, idAlmacen, idDetalle);
    }

    @Override
    public void registrarLineaEquipo(int idRecepcion, int idDetalle, int cantidad, int idEquipo, int idCentroCosto, String numeroActivo) throws SQLException {
        ejecutar("INSERT INTO recepciones_oc_detalle (id_recepcion, id_detalle_compra, cantidad, id_equipo, id_centro_costo, numero_activo) VALUES (?, ?, ?, ?, ?, ?)",
                idRecepcion, idDetalle, cantidad, idEquipo, idCentroCosto, numeroActivo);
    }

    @Override
    public void registrarLineaInsumo(int idRecepcion, int idDetalle, int cantidad, int idAlmacen) throws SQLException {
        ejecutar("INSERT INTO recepciones_oc_detalle (id_recepcion, id_detalle_compra, cantidad, id_almacen) VALUES (?, ?, ?, ?)",
                idRecepcion, idDetalle, cantidad, idAlmacen);
    }

    @Override
    public RecepcionOC bloquearRecepcion(int idRecepcion) throws SQLException {
        return uno("SELECT r.id_compra, r.numero, r.fecha, r.anulada, c.estado, c.numero_oc, c.id_centro_destino "
                + "FROM recepciones_oc r JOIN compras c ON c.id_compra = r.id_compra "
                + "WHERE r.id_recepcion = ? FOR UPDATE OF r, c", rs -> {
                    RecepcionOC r = new RecepcionOC();
                    r.id = idRecepcion;
                    r.idCompra = rs.getInt("id_compra");
                    r.numero = rs.getString("numero");
                    r.fecha = rs.getTimestamp("fecha");
                    r.anulada = rs.getBoolean("anulada");
                    r.estadoOC = rs.getString("estado");
                    r.numeroOC = rs.getString("numero_oc");
                    r.idCentroDestino = rs.getInt("id_centro_destino");
                    return r;
                }, idRecepcion);
    }

    @Override
    public List<LineaRecepcion> lineasParaAnular(int idRecepcion) throws SQLException {
        return lista("SELECT rd.id, rd.id_detalle_compra, rd.cantidad, rd.id_almacen, rd.id_equipo, d.tipo_item, d.id_insumo, "
                + "       d.cantidad_recibida, d.precio_unitario, COALESCE(i.descripcion, ib.descripcion) AS descripcion "
                + "FROM recepciones_oc_detalle rd JOIN detalle_compra d ON d.id_detalle_compra = rd.id_detalle_compra "
                + "LEFT JOIN insumos i ON i.id_insumo = d.id_insumo LEFT JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "LEFT JOIN insumos ib ON ib.id_insumo = e.id_insumo_base "
                + "WHERE rd.id_recepcion = ? ORDER BY rd.id FOR UPDATE OF d", rs -> {
                    LineaRecepcion l = new LineaRecepcion();
                    l.id = rs.getInt("id");
                    l.idRecepcion = idRecepcion;
                    l.idDetalle = rs.getInt("id_detalle_compra");
                    l.cantidad = rs.getInt("cantidad");
                    l.idAlmacen = rs.getInt("id_almacen");
                    l.idEquipo = rs.getInt("id_equipo");
                    l.tipoItem = rs.getString("tipo_item");
                    l.idInsumo = rs.getInt("id_insumo");
                    l.recibidaEnOC = rs.getInt("cantidad_recibida");
                    l.precio = rs.getBigDecimal("precio_unitario");
                    l.descripcion = rs.getString("descripcion") == null ? "" : rs.getString("descripcion");
                    return l;
                }, idRecepcion);
    }

    @Override
    public EquipoEstado bloquearEquipo(int idEquipo) throws SQLException {
        return uno("SELECT serial_activo, estado, id_centro_actual, id_centro_costo_actual FROM equipos WHERE id_equipo = ? FOR UPDATE", rs -> {
            EquipoEstado e = new EquipoEstado();
            e.id = idEquipo;
            e.numeroActivo = rs.getString("serial_activo");
            e.estado = rs.getString("estado");
            e.idCentro = enteroONull(rs, "id_centro_actual");
            e.idCentroCosto = enteroONull(rs, "id_centro_costo_actual");
            return e;
        }, idEquipo);
    }

    @Override
    public int movimientosPosteriores(int idEquipo, Timestamp desde) throws SQLException {
        return entero("SELECT (SELECT COUNT(*) FROM movimientos_activos WHERE id_equipo = ? AND fecha_movimiento > ?) + "
                + "       (SELECT COUNT(*) FROM solicitudes_activos_detalle sd JOIN solicitudes_activos s ON s.id_solicitud = sd.id_solicitud "
                + "         WHERE sd.id_equipo = ? AND s.estado = 'PENDIENTE')", idEquipo, desde, idEquipo);
    }

    @Override
    public void devolverEquipoASolicitado(int idEquipo, String serialTemporal) throws SQLException {
        ejecutar("UPDATE equipos SET estado = 'SOLICITADO', serial_activo = ?, numero_serie = NULL, fecha_ingreso = NULL, "
                + "id_centro_actual = NULL, id_centro_costo_actual = NULL, id_almacen_actual = NULL WHERE id_equipo = ?", serialTemporal, idEquipo);
    }

    @Override
    public void restarRecibidoEquipo(int idDetalle, int cantidad) throws SQLException {
        ejecutar("UPDATE detalle_compra SET cantidad_recibida = cantidad_recibida - ?, id_centro_asignado = NULL, "
                + "id_centro_costo_asignado = NULL, id_equipo_generado = NULL WHERE id_detalle_compra = ?", cantidad, idDetalle);
    }

    @Override
    public void restarRecibidoInsumo(int idDetalle, int cantidad) throws SQLException {
        ejecutar("UPDATE detalle_compra SET cantidad_recibida = cantidad_recibida - ? WHERE id_detalle_compra = ?", cantidad, idDetalle);
    }

    @Override
    public void asignarAlmacenALinea(int idLineaRecepcion, int idAlmacen) throws SQLException {
        ejecutar("UPDATE recepciones_oc_detalle SET id_almacen = ? WHERE id = ?", idAlmacen, idLineaRecepcion);
    }

    @Override
    public void marcarRecepcionAnulada(int idRecepcion, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE recepciones_oc SET anulada = true, fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? "
                + "WHERE id_recepcion = ?", idUsuario, motivo, idRecepcion);
    }
}
