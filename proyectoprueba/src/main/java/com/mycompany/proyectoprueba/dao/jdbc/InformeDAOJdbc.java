package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.InformeDAO;
import com.mycompany.proyectoprueba.modelo.FiltrosInforme;
import com.mycompany.proyectoprueba.modelo.Informe;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Comun;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformeDAOJdbc extends JdbcBase implements InformeDAO {

    public InformeDAOJdbc(Connection conn) {
        super(conn);
    }

    /** Condiciones opcionales (siempre con parámetros, nunca texto del usuario en el SQL). */
    private static final class Consulta {
        final StringBuilder where = new StringBuilder(" WHERE TRUE");
        final List<Object> params = new ArrayList<Object>();

        void y(String condicion, Object... valores) {
            where.append(" AND ").append(condicion);
            for (Object v : valores) {
                params.add(v);
            }
        }

        String sql(String select, String resto) {
            return select + where + " " + resto + " LIMIT " + (Informe.LIMITE + 1);
        }
    }

    /** Filtros comunes. colCentro / colCC pueden repetir el valor con {v}; colUsuario puede ser null. */
    private static void comunes(Consulta q, FiltrosInforme f, String colFecha, String colCentro, String colCC, String colUsuario) {
        if (!f.desde.isEmpty()) {
            q.y(colFecha + " >= CAST(? AS date)", f.desde);
        }
        if (!f.hasta.isEmpty()) {
            q.y(colFecha + " < CAST(? AS date) + 1", f.hasta);
        }
        if (f.idCentro > 0) {
            q.y(colCentro.replace("{v}", "?"), repetir(f.idCentro, contar(colCentro)));
        }
        if (f.idCentroCosto > 0) {
            q.y(colCC.replace("{v}", "?"), repetir(f.idCentroCosto, contar(colCC)));
        }
        if (f.idUsuario > 0) {
            q.y(colUsuario + " = ?", Integer.valueOf(f.idUsuario));
        }
    }

    private static int contar(String s) {
        int n = 0;
        for (int i = s.indexOf("{v}"); i >= 0; i = s.indexOf("{v}", i + 1)) {
            n++;
        }
        return n;
    }

    private static Object[] repetir(int v, int n) {
        Object[] a = new Object[n];
        for (int i = 0; i < n; i++) {
            a[i] = Integer.valueOf(v);
        }
        return a;
    }

    private static Map<String, Object> fila() {
        return new HashMap<String, Object>();
    }

    private static void texto(Map<String, Object> m, ResultSet rs, String... columnas) throws SQLException {
        for (String c : columnas) {
            m.put(c, rs.getString(c));
        }
    }

    // Combos

    private List<Opcion> opciones(String sql, Object... params) throws SQLException {
        return lista(sql, rs -> new Opcion(rs.getInt(1), Comun.txt(rs.getString(2))), params);
    }

    @Override
    public List<Opcion> centros(Permisos p) throws SQLException {
        return opciones("SELECT id_centro, nombre FROM centros WHERE " + p.sqlCentros("id_centro") + " ORDER BY nombre");
    }

    @Override
    public List<Opcion> usuarios() throws SQLException {
        return opciones("SELECT id_usuario, apellido || ', ' || nombre FROM usuarios ORDER BY apellido, nombre");
    }

    @Override
    public List<Opcion> proveedores() throws SQLException {
        return opciones("SELECT id_proveedor, razon_social FROM proveedores ORDER BY razon_social");
    }

    @Override
    public List<Opcion> insumos() throws SQLException {
        return opciones("SELECT id_insumo, codigo || ' - ' || descripcion FROM insumos ORDER BY descripcion");
    }

    @Override
    public List<Opcion> almacenes(Permisos p) throws SQLException {
        return opciones("SELECT a.id_almacen, a.nombre || ' (' || COALESCE(c.nombre, '') || ')' FROM almacenes a LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "WHERE " + p.sqlCentros("a.id_centro") + " ORDER BY c.nombre, a.nombre");
    }

    @Override
    public List<Opcion> centrosCosto(int idCentro) throws SQLException {
        return opciones("SELECT id_centro_costo, codigo || COALESCE(' - ' || descripcion, '') FROM centros_costo WHERE id_centro = ? ORDER BY codigo", idCentro);
    }

    // Informes

    @Override
    public List<Map<String, Object>> compras(FiltrosInforme f, String estado, String tipoItem, Permisos p) throws SQLException {
        Consulta q = new Consulta();
        q.y("c.numero_oc IS NOT NULL AND c.estado <> 'BORRADOR'");
        q.y(p.sqlCentros("c.id_centro_destino"));
        // Centro de costo: solo aplica a activos, que se ubican por centro de costo al recibirse
        comunes(q, f, "c.fecha_compra", "c.id_centro_destino = {v}",
                "EXISTS (SELECT 1 FROM detalle_compra dx WHERE dx.id_compra = c.id_compra AND dx.id_centro_costo_asignado = {v})", "c.id_usuario_solicitante");
        if (f.idProveedor > 0) {
            q.y("c.id_proveedor = ?", Integer.valueOf(f.idProveedor));
        }
        if (!estado.isEmpty()) {
            q.y("c.estado = ?", estado);
        }
        if (!tipoItem.isEmpty()) {
            q.y("EXISTS (SELECT 1 FROM detalle_compra dx WHERE dx.id_compra = c.id_compra AND dx.tipo_item = ?)", tipoItem);
        }
        return lista(q.sql(
                "SELECT c.id_compra, c.numero_oc, c.fecha_compra, p.razon_social, ce.nombre AS centro, u.nombre || ' ' || u.apellido AS solicitante, c.estado, "
                + "       (SELECT COUNT(*) FROM detalle_compra d WHERE d.id_compra = c.id_compra) AS items, "
                + "       (SELECT COALESCE(SUM(d.cantidad), 0) FROM detalle_compra d WHERE d.id_compra = c.id_compra) AS pedidas, "
                + "       (SELECT COALESCE(SUM(d.cantidad_recibida), 0) FROM detalle_compra d WHERE d.id_compra = c.id_compra) AS recibidas, "
                + "       (SELECT COALESCE(SUM(d.cantidad * d.precio_unitario), 0) FROM detalle_compra d WHERE d.id_compra = c.id_compra) AS total "
                + "FROM compras c LEFT JOIN proveedores p ON p.id_proveedor = c.id_proveedor "
                + "LEFT JOIN centros ce ON ce.id_centro = c.id_centro_destino LEFT JOIN usuarios u ON u.id_usuario = c.id_usuario_solicitante",
                "ORDER BY c.fecha_compra DESC, c.numero_oc DESC"), rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_compra", Integer.valueOf(rs.getInt("id_compra")));
                    texto(m, rs, "numero_oc", "razon_social", "centro", "solicitante", "estado");
                    m.put("fecha_compra", rs.getDate("fecha_compra"));
                    m.put("items", Integer.valueOf(rs.getInt("items")));
                    m.put("pedidas", Long.valueOf(rs.getLong("pedidas")));
                    m.put("recibidas", Long.valueOf(rs.getLong("recibidas")));
                    m.put("total", rs.getBigDecimal("total"));
                    return m;
                }, q.params.toArray());
    }

    @Override
    public List<Map<String, Object>> ordenesTrabajo(FiltrosInforme f, String estado, Permisos p) throws SQLException {
        Consulta q = new Consulta();
        q.y(p.sqlCentros("o.id_centro"));
        comunes(q, f, "o.fecha_creacion", "o.id_centro = {v}", "o.id_centro_costo = {v}", "o.id_usuario_solic");
        if (!estado.isEmpty()) {
            q.y("o.estado = ?", estado);
        }
        if (f.idInsumo > 0) {
            q.y("EXISTS (SELECT 1 FROM detalle_ot dx WHERE dx.id_ot = o.id_ot AND dx.id_insumo = ?)", Integer.valueOf(f.idInsumo));
        }
        return lista(q.sql(
                "SELECT o.id_ot, o.numero_ot, o.fecha_creacion, o.titulo, c.nombre AS centro, cc.codigo AS cc_codigo, cc.descripcion AS cc_desc, "
                + "       u.nombre || ' ' || u.apellido AS solicitante, ur.nombre || ' ' || ur.apellido AS responsable, o.estado, "
                + "       (SELECT COUNT(*) FROM detalle_ot d WHERE d.id_ot = o.id_ot) AS items, "
                + "       (SELECT COALESCE(SUM(d.cantidad * d.precio_unitario), 0) FROM detalle_ot d WHERE d.id_ot = o.id_ot) AS valor "
                + "FROM ot o LEFT JOIN centros c ON c.id_centro = o.id_centro LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo "
                + "LEFT JOIN usuarios u ON u.id_usuario = o.id_usuario_solic LEFT JOIN usuarios ur ON ur.id_usuario = o.id_usuario_responsable",
                "ORDER BY o.fecha_creacion DESC, o.id_ot DESC"), rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_ot", Integer.valueOf(rs.getInt("id_ot")));
                    texto(m, rs, "numero_ot", "titulo", "centro", "cc_codigo", "cc_desc", "solicitante", "responsable", "estado");
                    m.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                    m.put("items", Integer.valueOf(rs.getInt("items")));
                    m.put("valor", rs.getBigDecimal("valor"));
                    return m;
                }, q.params.toArray());
    }

    @Override
    public List<Map<String, Object>> movimientosStock(FiltrosInforme f, String tipoMovimiento, Permisos p) throws SQLException {
        Consulta q = new Consulta();
        q.y(p.sqlCentros("a.id_centro"));
        comunes(q, f, "m.fecha", "a.id_centro = {v}", "m.id_centro_costo = {v}", "m.id_usuario");
        if (!tipoMovimiento.isEmpty()) {
            q.y("m.tipo = ?", tipoMovimiento);
        }
        if (f.idAlmacen > 0) {
            q.y("m.id_almacen = ?", Integer.valueOf(f.idAlmacen));
        }
        if (f.idInsumo > 0) {
            q.y("m.id_insumo = ?", Integer.valueOf(f.idInsumo));
        }
        return lista(q.sql(
                "SELECT m.id_mov, m.fecha, m.tipo, m.documento, i.codigo, i.descripcion, a.nombre AS almacen, cc.codigo AS cc, m.cantidad, m.costo_unitario, "
                + "       u.nombre || ' ' || u.apellido AS usuario "
                + "FROM movimientos_stock m JOIN insumos i ON i.id_insumo = m.id_insumo JOIN almacenes a ON a.id_almacen = m.id_almacen "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = m.id_centro_costo LEFT JOIN usuarios u ON u.id_usuario = m.id_usuario",
                "ORDER BY m.fecha DESC, m.id_mov DESC"), rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_mov", Integer.valueOf(rs.getInt("id_mov")));
                    m.put("fecha", rs.getTimestamp("fecha"));
                    texto(m, rs, "tipo", "documento", "codigo", "descripcion", "almacen", "cc", "usuario");
                    m.put("cantidad", Integer.valueOf(rs.getInt("cantidad")));
                    m.put("costo_unitario", rs.getBigDecimal("costo_unitario"));
                    return m;
                }, q.params.toArray());
    }

    @Override
    public List<Map<String, Object>> movimientosActivos(FiltrosInforme f, String tipoMovimiento, String estadoActivo, Permisos p) throws SQLException {
        Consulta q = new Consulta();
        q.y("(" + p.sqlCentros("m.id_centro_origen") + " OR " + p.sqlCentros("m.id_centro_destino") + ")");
        comunes(q, f, "m.fecha_movimiento", "(m.id_centro_origen = {v} OR m.id_centro_destino = {v})",
                "(m.id_centro_costo_origen = {v} OR m.id_centro_costo_destino = {v})", "m.id_usuario_registro");
        if (!tipoMovimiento.isEmpty()) {
            q.y("m.tipo_movimiento = ?", tipoMovimiento);
        }
        if (!estadoActivo.isEmpty()) {
            q.y("e.estado = ?", estadoActivo);
        }
        if (!f.buscar.isEmpty()) {
            q.y("(e.serial_activo ILIKE ? OR e.numero_serie ILIKE ?)", "%" + f.buscar + "%", "%" + f.buscar + "%");
        }
        return lista(q.sql(
                "SELECT m.id_movimiento, m.fecha_movimiento, m.tipo_movimiento, m.numero_movimiento_unico, e.serial_activo, i.descripcion, "
                + "       co.nombre AS centro_origen, cco.codigo AS cc_origen, cd.nombre AS centro_destino, ccd.codigo AS cc_destino, "
                + "       u.nombre || ' ' || u.apellido AS usuario "
                + "FROM movimientos_activos m JOIN equipos e ON e.id_equipo = m.id_equipo LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
                + "LEFT JOIN centros co ON co.id_centro = m.id_centro_origen LEFT JOIN centros cd ON cd.id_centro = m.id_centro_destino "
                + "LEFT JOIN centros_costo cco ON cco.id_centro_costo = m.id_centro_costo_origen LEFT JOIN centros_costo ccd ON ccd.id_centro_costo = m.id_centro_costo_destino "
                + "LEFT JOIN usuarios u ON u.id_usuario = m.id_usuario_registro",
                "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC"), rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_movimiento", Integer.valueOf(rs.getInt("id_movimiento")));
                    m.put("fecha_movimiento", rs.getTimestamp("fecha_movimiento"));
                    texto(m, rs, "tipo_movimiento", "numero_movimiento_unico", "serial_activo", "descripcion", "centro_origen", "cc_origen",
                            "centro_destino", "cc_destino", "usuario");
                    return m;
                }, q.params.toArray());
    }

    // Nombres

    private String nombre(String sql, int id) throws SQLException {
        String n = uno(sql, rs -> Comun.txt(rs.getString(1)), id);
        return n == null ? "" : n;
    }

    @Override
    public String nombreCentro(int id) throws SQLException {
        return nombre("SELECT nombre FROM centros WHERE id_centro = ?", id);
    }

    @Override
    public String nombreCentroCosto(int id) throws SQLException {
        return nombre("SELECT codigo || COALESCE(' - ' || descripcion, '') FROM centros_costo WHERE id_centro_costo = ?", id);
    }

    @Override
    public String nombreUsuario(int id) throws SQLException {
        return nombre("SELECT nombre || ' ' || apellido FROM usuarios WHERE id_usuario = ?", id);
    }

    @Override
    public String nombreProveedor(int id) throws SQLException {
        return nombre("SELECT razon_social FROM proveedores WHERE id_proveedor = ?", id);
    }

    @Override
    public String nombreInsumo(int id) throws SQLException {
        return nombre("SELECT codigo || ' - ' || descripcion FROM insumos WHERE id_insumo = ?", id);
    }

    @Override
    public String nombreAlmacen(int id) throws SQLException {
        return nombre("SELECT a.nombre || COALESCE(' (' || c.nombre || ')', '') FROM almacenes a LEFT JOIN centros c ON c.id_centro = a.id_centro WHERE a.id_almacen = ?", id);
    }

    // Fichas

    @Override
    public Map<String, Object> compra(int id) throws SQLException {
        return uno("SELECT c.numero_oc, c.fecha_compra, c.estado, c.observaciones, c.id_centro_destino, pr.razon_social, pr.ruc, pr.telefono, pr.direccion, "
                + "       ce.nombre AS centro, us.nombre || ' ' || us.apellido AS solicitante, uc.nombre || ' ' || uc.apellido AS comprador "
                + "FROM compras c LEFT JOIN proveedores pr ON pr.id_proveedor = c.id_proveedor LEFT JOIN centros ce ON ce.id_centro = c.id_centro_destino "
                + "LEFT JOIN usuarios us ON us.id_usuario = c.id_usuario_solicitante LEFT JOIN usuarios uc ON uc.id_usuario = c.id_comprador WHERE c.id_compra = ?",
                rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_centro_destino", Integer.valueOf(rs.getInt("id_centro_destino")));
                    texto(m, rs, "numero_oc", "estado", "observaciones", "razon_social", "ruc", "telefono", "direccion", "centro", "solicitante", "comprador");
                    m.put("fecha_compra", rs.getDate("fecha_compra"));
                    return m;
                }, id);
    }

    @Override
    public List<Map<String, Object>> lineasCompra(int id) throws SQLException {
        return lista("SELECT d.tipo_item, COALESCE(i.codigo, ib.codigo) AS codigo, COALESCE(i.descripcion, ib.descripcion) AS descripcion, "
                + "       d.cantidad, d.cantidad_recibida, d.precio_unitario, e.serial_activo, e.numero_serie "
                + "FROM detalle_compra d LEFT JOIN insumos i ON i.id_insumo = d.id_insumo LEFT JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "LEFT JOIN insumos ib ON ib.id_insumo = e.id_insumo_base WHERE d.id_compra = ? ORDER BY d.id_detalle_compra", rs -> {
                    Map<String, Object> m = fila();
                    texto(m, rs, "tipo_item", "codigo", "descripcion", "serial_activo", "numero_serie");
                    m.put("cantidad", Integer.valueOf(rs.getInt("cantidad")));
                    m.put("cantidad_recibida", Integer.valueOf(rs.getInt("cantidad_recibida")));
                    m.put("precio_unitario", rs.getBigDecimal("precio_unitario"));
                    return m;
                }, id);
    }

    @Override
    public Map<String, Object> ordenTrabajo(int id) throws SQLException {
        return uno("SELECT o.*, c.nombre AS centro, cc.codigo AS cc_codigo, cc.descripcion AS cc_desc, us.nombre || ' ' || us.apellido AS solicitante, "
                + "       ur.nombre || ' ' || ur.apellido AS responsable, "
                + "       uf.nombre || ' ' || uf.apellido AS finalizado_por, ua.nombre || ' ' || ua.apellido AS anulado_por "
                + "FROM ot o LEFT JOIN centros c ON c.id_centro = o.id_centro LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo "
                + "LEFT JOIN usuarios us ON us.id_usuario = o.id_usuario_solic LEFT JOIN usuarios uf ON uf.id_usuario = o.id_usuario_finaliza "
                + "LEFT JOIN usuarios ua ON ua.id_usuario = o.id_usuario_anula_fin LEFT JOIN usuarios ur ON ur.id_usuario = o.id_usuario_responsable WHERE o.id_ot = ?",
                rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_centro", Integer.valueOf(rs.getInt("id_centro")));
                    texto(m, rs, "numero_ot", "titulo", "descripcion", "estado", "centro", "cc_codigo", "cc_desc", "solicitante", "responsable",
                            "finalizado_por", "observacion_finalizacion", "anulado_por", "motivo_anulacion_fin");
                    m.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                    m.put("fecha_finalizacion", rs.getTimestamp("fecha_finalizacion"));
                    m.put("fecha_anulacion_fin", rs.getTimestamp("fecha_anulacion_fin"));
                    return m;
                }, id);
    }

    /** Filas de texto ("" en lugar de NULL) de un SELECT con el id como único parámetro. */
    private List<String[]> textos(int columnas, String sql, int id) throws SQLException {
        return lista(sql, rs -> {
            String[] v = new String[columnas];
            for (int i = 0; i < columnas; i++) {
                v[i] = Comun.txt(rs.getString(i + 1));
            }
            return v;
        }, id);
    }

    @Override
    public List<String[]> insumosOT(int id) throws SQLException {
        return textos(7, "SELECT i.codigo, i.descripcion, d.cantidad || ' ' || COALESCE(u.abreviatura, ''), a.nombre, to_char(d.fecha_suministro, 'DD/MM/YYYY HH24:MI'), "
                + "       us.nombre || ' ' || us.apellido, COALESCE(r.numero_remision, 'Pendiente de salida') "
                + "FROM detalle_ot d JOIN insumos i ON i.id_insumo = d.id_insumo LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida "
                + "LEFT JOIN almacenes a ON a.id_almacen = d.id_almacen LEFT JOIN usuarios us ON us.id_usuario = d.id_usuario_suministra "
                + "LEFT JOIN remisiones r ON r.id_remision = d.id_remision AND r.anulada = false WHERE d.id_ot = ? ORDER BY d.id_detalle_ot", id);
    }

    @Override
    public List<String[]> remisionesOT(int id) throws SQLException {
        return textos(5, "SELECT r.numero_remision, CASE r.tipo_remision WHEN 'SALIDA_ACTIVOS' THEN 'Salida de activos' ELSE 'Salida de insumos' END, "
                + "       to_char(r.fecha_hora, 'DD/MM/YYYY HH24:MI'), TRIM(COALESCE(r.transportista_nombre, '') || ' ' || COALESCE(r.transportista_apellido, '')), "
                + "       CASE WHEN r.anulada THEN 'Anulada: ' || COALESCE(r.motivo_anulacion, '') ELSE 'Vigente' END "
                + "FROM remisiones r WHERE r.id_ot = ? ORDER BY r.fecha_hora", id);
    }

    @Override
    public List<String[]> historialOT(int id) throws SQLException {
        return textos(4, "SELECT to_char(e.fecha, 'DD/MM/YYYY HH24:MI'), e.tipo, u.nombre || ' ' || u.apellido, e.detalle "
                + "FROM ot_eventos e LEFT JOIN usuarios u ON u.id_usuario = e.id_usuario WHERE e.id_ot = ? ORDER BY e.fecha, e.id_evento", id);
    }

    @Override
    public Map<String, Object> movimientoStock(int id) throws SQLException {
        return uno("SELECT m.*, i.codigo, i.descripcion, un.nombre AS unidad, a.nombre AS almacen, a.id_centro, ce.nombre AS centro, "
                + "       cc.codigo AS cc_codigo, cc.descripcion AS cc_desc, u.nombre || ' ' || u.apellido AS usuario, "
                + "       o.numero_ot, o.titulo AS ot_titulo, co.numero_oc, pr.razon_social, aj.numero AS aj_numero, aj.tipo AS aj_tipo, aj.motivo AS aj_motivo, "
                + "       aj.anulado AS aj_anulado, t.numero AS tr_numero, ao.nombre AS tr_origen, ad.nombre AS tr_destino, t.anulada AS tr_anulada "
                + "FROM movimientos_stock m JOIN insumos i ON i.id_insumo = m.id_insumo LEFT JOIN unidad_medida un ON un.id_unidad_medida = i.id_unidad_medida "
                + "JOIN almacenes a ON a.id_almacen = m.id_almacen LEFT JOIN centros ce ON ce.id_centro = a.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = m.id_centro_costo LEFT JOIN usuarios u ON u.id_usuario = m.id_usuario "
                + "LEFT JOIN ot o ON o.id_ot = m.id_ot LEFT JOIN compras co ON co.id_compra = m.id_compra LEFT JOIN proveedores pr ON pr.id_proveedor = co.id_proveedor "
                + "LEFT JOIN ajustes aj ON aj.id_ajuste = m.id_ajuste LEFT JOIN transferencias t ON t.id_transferencia = m.id_transferencia "
                + "LEFT JOIN almacenes ao ON ao.id_almacen = t.id_almacen_origen LEFT JOIN almacenes ad ON ad.id_almacen = t.id_almacen_destino "
                + "WHERE m.id_mov = ?", rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_centro", Integer.valueOf(rs.getInt("id_centro")));
                    m.put("cantidad", Integer.valueOf(rs.getInt("cantidad")));
                    m.put("saldo", Integer.valueOf(rs.getInt("saldo")));
                    m.put("costo_unitario", rs.getBigDecimal("costo_unitario"));
                    m.put("fecha", rs.getTimestamp("fecha"));
                    texto(m, rs, "tipo", "documento", "unidad", "usuario", "observacion", "codigo", "descripcion", "almacen", "centro", "cc_codigo", "cc_desc",
                            "numero_ot", "ot_titulo", "numero_oc", "razon_social", "aj_numero", "aj_tipo", "aj_motivo", "tr_numero", "tr_origen", "tr_destino");
                    m.put("aj_anulado", Boolean.valueOf(rs.getBoolean("aj_anulado")));
                    m.put("tr_anulada", Boolean.valueOf(rs.getBoolean("tr_anulada")));
                    return m;
                }, id);
    }

    @Override
    public Map<String, Object> movimientoActivo(int id) throws SQLException {
        return uno("SELECT m.*, e.serial_activo, e.numero_serie, e.estado AS estado_actual, i.codigo, i.descripcion, "
                + "       co.nombre AS c_origen, cd.nombre AS c_destino, cco.codigo AS cc_origen, ccd.codigo AS cc_destino, "
                + "       ca.nombre AS c_actual, cca.codigo AS cc_actual, ug.nombre || ' ' || ug.apellido AS registrado_por, "
                + "       ue.nombre || ' ' || ue.apellido AS emisor, ur.nombre || ' ' || ur.apellido AS receptor, "
                + "       r.numero_remision, sa.numero AS sol_numero, pv.razon_social AS proveedor "
                + "FROM movimientos_activos m JOIN equipos e ON e.id_equipo = m.id_equipo LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
                + "LEFT JOIN centros co ON co.id_centro = m.id_centro_origen LEFT JOIN centros cd ON cd.id_centro = m.id_centro_destino "
                + "LEFT JOIN centros_costo cco ON cco.id_centro_costo = m.id_centro_costo_origen LEFT JOIN centros_costo ccd ON ccd.id_centro_costo = m.id_centro_costo_destino "
                + "LEFT JOIN centros ca ON ca.id_centro = e.id_centro_actual LEFT JOIN centros_costo cca ON cca.id_centro_costo = e.id_centro_costo_actual "
                + "LEFT JOIN usuarios ug ON ug.id_usuario = m.id_usuario_registro LEFT JOIN usuarios ue ON ue.id_usuario = m.id_usuario_emisor "
                + "LEFT JOIN usuarios ur ON ur.id_usuario = m.id_usuario_receptor LEFT JOIN remisiones r ON r.id_remision = m.id_remision "
                + "LEFT JOIN solicitudes_activos sa ON sa.id_solicitud = r.id_solicitud_activo LEFT JOIN proveedores pv ON pv.id_proveedor = sa.id_proveedor "
                + "WHERE m.id_movimiento = ?", rs -> {
                    Map<String, Object> m = fila();
                    m.put("id_centro_origen", Integer.valueOf(rs.getInt("id_centro_origen")));
                    m.put("id_centro_destino", Integer.valueOf(rs.getInt("id_centro_destino")));
                    m.put("fecha_movimiento", rs.getTimestamp("fecha_movimiento"));
                    texto(m, rs, "serial_activo", "numero_movimiento_unico", "tipo_movimiento", "estado_anterior", "estado_nuevo", "registrado_por",
                            "observaciones", "justificacion_rechazo", "numero_serie", "codigo", "descripcion", "estado_actual", "c_actual", "cc_actual",
                            "c_origen", "cc_origen", "c_destino", "cc_destino", "proveedor", "emisor", "receptor", "datos_receptor_nombre",
                            "datos_receptor_apellido", "datos_receptor_ci", "datos_receptor_vehiculo_marca", "datos_receptor_vehiculo_modelo",
                            "datos_receptor_vehiculo_chapa", "numero_remision", "sol_numero");
                    return m;
                }, id);
    }
}
