package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.ActivoDAO;
import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.MovimientoActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Comun;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActivoDAOJdbc extends JdbcBase implements ActivoDAO {

    /** Situación por una solicitud abierta: pendiente de aprobación, en reparación o en tránsito. */
    static final String SITUACION =
            "(SELECT CASE WHEN d.estado_linea = 'PENDIENTE' THEN 'Solicitud ' || s.numero || ' pendiente de aprobación' "
            + "             WHEN s.tipo = 'REPARACION' THEN 'En reparación: ' || COALESCE(p.razon_social, '') "
            + "             ELSE 'En tránsito a ' || COALESCE(cd.nombre, '') || ' (' || s.numero || ')' END "
            + "   FROM solicitudes_activos_detalle d JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud "
            + "   LEFT JOIN proveedores p ON p.id_proveedor = s.id_proveedor LEFT JOIN centros cd ON cd.id_centro = s.id_centro_destino "
            + "  WHERE d.id_equipo = e.id_equipo AND d.estado_linea IN ('PENDIENTE', 'EN_CURSO') LIMIT 1)";

    /** Responsable actual: la última ASIGNACION (usuario del sistema o persona externa). */
    static final String RESPONSABLE =
            "(SELECT COALESCE(ur.nombre || ' ' || ur.apellido, "
            + "        NULLIF(TRIM(COALESCE(ma.datos_receptor_nombre, '') || ' ' || COALESCE(ma.datos_receptor_apellido, '')), '')) "
            + "   FROM movimientos_activos ma LEFT JOIN usuarios ur ON ur.id_usuario = ma.id_usuario_receptor "
            + "  WHERE ma.id_equipo = e.id_equipo AND ma.tipo_movimiento = 'ASIGNACION' "
            + "  ORDER BY ma.fecha_movimiento DESC, ma.id_movimiento DESC LIMIT 1)";

    public ActivoDAOJdbc(Connection conn) {
        super(conn);
    }

    private static String t(ResultSet rs, String columna) throws SQLException {
        return Comun.txt(rs.getString(columna));
    }

    private static String fecha(ResultSet rs, String columna) throws SQLException {
        java.sql.Date d = rs.getDate(columna);
        return d == null ? "" : d.toString();
    }

    @Override
    public Map<String, Integer> cantidadPorEstado(Permisos p) throws SQLException {
        final Map<String, Integer> m = new LinkedHashMap<String, Integer>();
        lista("SELECT e.estado, COUNT(*) AS cant FROM equipos e "
                + "LEFT JOIN detalle_compra d ON d.id_equipo = e.id_equipo "
                + "LEFT JOIN compras c ON c.id_compra = d.id_compra "
                // los pedidos que siguen en un borrador de solicitud no cuentan todavía
                + "WHERE e.estado <> 'ANULADO' AND NOT (e.estado = 'SOLICITADO' AND COALESCE(c.estado, 'BORRADOR') = 'BORRADOR') "
                + "AND " + p.sqlCentros("COALESCE(e.id_centro_actual, c.id_centro_destino)") + " "
                + "GROUP BY e.estado", rs -> {
                    m.put(rs.getString("estado"), Integer.valueOf(rs.getInt("cant")));
                    return null;
                });
        return m;
    }

    @Override
    public int centrosCostoConActivos(Permisos p) throws SQLException {
        return entero("SELECT COUNT(DISTINCT id_centro_costo_actual) FROM equipos "
                + "WHERE estado IN ('DISPONIBLE', 'ASIGNADO') AND id_centro_costo_actual IS NOT NULL AND " + p.sqlCentros("id_centro_actual"));
    }

    @Override
    public List<Opcion> centros(Permisos p) throws SQLException {
        return lista("SELECT id_centro, nombre FROM centros WHERE " + p.sqlCentros("id_centro") + " ORDER BY nombre",
                rs -> new Opcion(rs.getInt("id_centro"), rs.getString("nombre")));
    }

    @Override
    public List<Opcion> centrosCosto(int idCentro, boolean soloActivos, Permisos p) throws SQLException {
        return lista("SELECT id_centro_costo, codigo, descripcion FROM centros_costo "
                + "WHERE (? = 0 OR id_centro = ?) AND " + p.sqlCentros("id_centro") + " " + (soloActivos ? "AND activo = true " : "")
                + "ORDER BY codigo",
                rs -> new Opcion(rs.getInt("id_centro_costo"), Comun.nombreCC(rs.getString("codigo"), rs.getString("descripcion"))), idCentro, idCentro);
    }

    @Override
    public List<Opcion> usuariosActivos() throws SQLException {
        return lista("SELECT id_usuario, nombre, apellido FROM usuarios WHERE activo = true ORDER BY apellido, nombre",
                rs -> new Opcion(rs.getInt("id_usuario"), t(rs, "apellido") + ", " + t(rs, "nombre")));
    }

    @Override
    public List<Activo> listar(String estado, int idCentro, int idCentroCosto, boolean pendientes, Permisos p) throws SQLException {
        return lista("SELECT e.id_equipo, e.serial_activo, e.numero_serie, e.estado, e.fecha_ingreso, "
                + "       i.codigo, i.descripcion, ce.nombre AS centro, cc.codigo AS cc_codigo, cc.descripcion AS cc_descripcion, "
                + "       c.numero_oc, " + RESPONSABLE + " AS responsable, " + SITUACION + " AS situacion "
                + "FROM equipos e "
                + "LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
                + "LEFT JOIN centros ce ON ce.id_centro = e.id_centro_actual "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = e.id_centro_costo_actual "
                + "LEFT JOIN detalle_compra d ON d.id_equipo = e.id_equipo "
                + "LEFT JOIN compras c ON c.id_compra = d.id_compra "
                + "WHERE (? = '' OR e.estado = ?) "
                + "  AND (? = 0 OR e.id_centro_actual = ?) "
                + "  AND (? = 0 OR e.id_centro_costo_actual = ?) "
                + "  AND (? OR e.estado NOT IN ('SOLICITADO', 'ANULADO')) "
                + "  AND " + p.sqlCentros("COALESCE(e.id_centro_actual, c.id_centro_destino)") + " "
                + "ORDER BY e.fecha_ingreso DESC NULLS LAST, e.id_equipo DESC", rs -> {
                    Activo a = new Activo();
                    a.id = rs.getInt("id_equipo");
                    a.serial = rs.getString("serial_activo");
                    a.numeroSerie = t(rs, "numero_serie");
                    a.codigo = t(rs, "codigo");
                    a.descripcion = t(rs, "descripcion");
                    a.estado = rs.getString("estado");
                    a.centro = t(rs, "centro");
                    a.centroCosto = Comun.nombreCC(rs.getString("cc_codigo"), rs.getString("cc_descripcion"));
                    a.responsable = rs.getString("responsable");
                    a.fechaIngreso = fecha(rs, "fecha_ingreso");
                    a.numeroOc = t(rs, "numero_oc");
                    a.situacion = rs.getString("situacion");
                    return a;
                }, estado, estado, idCentro, idCentro, idCentroCosto, idCentroCosto, Boolean.valueOf(pendientes));
    }

    @Override
    public Activo ficha(int idEquipo) throws SQLException {
        return uno("SELECT e.id_equipo, e.serial_activo, e.numero_serie, e.estado, e.fecha_ingreso, e.id_centro_actual, e.id_centro_costo_actual, "
                + "       i.codigo, i.descripcion, ce.nombre AS centro, cc.codigo AS cc_codigo, cc.descripcion AS cc_descripcion, "
                + "       c.numero_oc, c.fecha_compra, c.estado AS estado_oc, p.razon_social AS proveedor, p.ruc, "
                + "       d.precio_unitario, u.nombre || ' ' || u.apellido AS solicitante, " + RESPONSABLE + " AS responsable, " + SITUACION + " AS situacion "
                + "FROM equipos e "
                + "LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
                + "LEFT JOIN centros ce ON ce.id_centro = e.id_centro_actual "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = e.id_centro_costo_actual "
                + "LEFT JOIN detalle_compra d ON d.id_equipo = e.id_equipo "
                + "LEFT JOIN compras c ON c.id_compra = d.id_compra "
                + "LEFT JOIN proveedores p ON p.id_proveedor = c.id_proveedor "
                + "LEFT JOIN usuarios u ON u.id_usuario = c.id_usuario_solicitante "
                + "WHERE e.id_equipo = ?", rs -> {
                    Activo a = new Activo();
                    a.id = rs.getInt("id_equipo");
                    a.serial = rs.getString("serial_activo");
                    a.numeroSerie = t(rs, "numero_serie");
                    a.codigo = t(rs, "codigo");
                    a.descripcion = t(rs, "descripcion");
                    a.estado = rs.getString("estado");
                    a.fechaIngreso = fecha(rs, "fecha_ingreso");
                    a.idCentro = rs.getInt("id_centro_actual");
                    a.idCentroCosto = rs.getInt("id_centro_costo_actual");
                    a.centro = t(rs, "centro");
                    a.centroCosto = Comun.nombreCC(rs.getString("cc_codigo"), rs.getString("cc_descripcion"));
                    a.responsable = rs.getString("responsable");
                    a.situacion = rs.getString("situacion");
                    a.numeroOc = t(rs, "numero_oc");
                    a.fechaCompra = fecha(rs, "fecha_compra");
                    a.estadoOc = t(rs, "estado_oc");
                    a.proveedor = t(rs, "proveedor");
                    a.ruc = t(rs, "ruc");
                    a.solicitante = t(rs, "solicitante");
                    a.valor = rs.getBigDecimal("precio_unitario");
                    return a;
                }, idEquipo);
    }

    @Override
    public List<MovimientoActivo> historial(int idEquipo) throws SQLException {
        return lista("SELECT m.id_movimiento, m.fecha_movimiento, m.tipo_movimiento, m.numero_movimiento_unico, "
                + "       m.estado_anterior, m.estado_nuevo, m.observaciones, m.justificacion_rechazo, "
                + "       m.datos_receptor_nombre, m.datos_receptor_apellido, m.datos_receptor_ci, "
                + "       m.datos_receptor_vehiculo_chapa, m.datos_receptor_vehiculo_marca, m.datos_receptor_vehiculo_modelo, "
                + "       co.nombre AS centro_origen, cd.nombre AS centro_destino, "
                + "       cco.codigo AS cco_codigo, cco.descripcion AS cco_descripcion, "
                + "       ccd.codigo AS ccd_codigo, ccd.descripcion AS ccd_descripcion, "
                + "       ao.nombre AS almacen_origen, ad.nombre AS almacen_destino, "
                + "       ug.nombre || ' ' || ug.apellido AS registrado_por, "
                + "       ue.nombre || ' ' || ue.apellido AS emisor, "
                + "       ur.nombre || ' ' || ur.apellido AS receptor, "
                + "       r.numero_remision "
                + "FROM movimientos_activos m "
                + "LEFT JOIN centros co ON co.id_centro = m.id_centro_origen "
                + "LEFT JOIN centros cd ON cd.id_centro = m.id_centro_destino "
                + "LEFT JOIN centros_costo cco ON cco.id_centro_costo = m.id_centro_costo_origen "
                + "LEFT JOIN centros_costo ccd ON ccd.id_centro_costo = m.id_centro_costo_destino "
                + "LEFT JOIN almacenes ao ON ao.id_almacen = m.id_almacen_origen "
                + "LEFT JOIN almacenes ad ON ad.id_almacen = m.id_almacen_destino "
                + "LEFT JOIN usuarios ug ON ug.id_usuario = m.id_usuario_registro "
                + "LEFT JOIN usuarios ue ON ue.id_usuario = m.id_usuario_emisor "
                + "LEFT JOIN usuarios ur ON ur.id_usuario = m.id_usuario_receptor "
                + "LEFT JOIN remisiones r ON r.id_remision = m.id_remision "
                + "WHERE m.id_equipo = ? ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC", rs -> {
                    MovimientoActivo m = new MovimientoActivo();
                    Timestamp f = rs.getTimestamp("fecha_movimiento");
                    m.id = rs.getInt("id_movimiento");
                    m.fecha = f == null ? "" : f.toString().substring(0, 16);
                    m.tipo = t(rs, "tipo_movimiento");
                    m.numero = t(rs, "numero_movimiento_unico");
                    m.estadoAnterior = t(rs, "estado_anterior");
                    m.estadoNuevo = t(rs, "estado_nuevo");
                    m.observaciones = t(rs, "observaciones");
                    m.justificacion = t(rs, "justificacion_rechazo");
                    m.personaNombre = t(rs, "datos_receptor_nombre");
                    m.personaApellido = t(rs, "datos_receptor_apellido");
                    m.personaCi = t(rs, "datos_receptor_ci");
                    m.vehiculoChapa = t(rs, "datos_receptor_vehiculo_chapa");
                    m.vehiculoMarca = t(rs, "datos_receptor_vehiculo_marca");
                    m.vehiculoModelo = t(rs, "datos_receptor_vehiculo_modelo");
                    m.centroOrigen = t(rs, "centro_origen");
                    m.centroDestino = t(rs, "centro_destino");
                    String subOrigen = Comun.nombreCC(rs.getString("cco_codigo"), rs.getString("cco_descripcion"));
                    String subDestino = Comun.nombreCC(rs.getString("ccd_codigo"), rs.getString("ccd_descripcion"));
                    m.subOrigen = subOrigen.isEmpty() ? t(rs, "almacen_origen") : subOrigen;
                    m.subDestino = subDestino.isEmpty() ? t(rs, "almacen_destino") : subDestino;
                    m.registradoPor = t(rs, "registrado_por");
                    m.emisor = t(rs, "emisor");
                    m.receptor = t(rs, "receptor");
                    m.remision = t(rs, "numero_remision");
                    return m;
                }, idEquipo);
    }

    @Override
    public Activo bloquear(int idEquipo) throws SQLException {
        return uno("SELECT estado, id_centro_actual, id_centro_costo_actual FROM equipos WHERE id_equipo = ? FOR UPDATE", rs -> {
            Activo a = new Activo();
            a.id = idEquipo;
            a.estado = rs.getString("estado");
            a.idCentro = rs.getInt("id_centro_actual");
            a.idCentroCosto = rs.getInt("id_centro_costo_actual");
            return a;
        }, idEquipo);
    }

    @Override
    public boolean enSolicitudAbierta(int idEquipo) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos_detalle WHERE id_equipo = ? AND estado_linea IN ('PENDIENTE', 'EN_CURSO')", idEquipo);
    }

    @Override
    public boolean usuarioActivo(int idUsuario) throws SQLException {
        return existe("SELECT 1 FROM usuarios WHERE id_usuario = ? AND activo = true", idUsuario);
    }

    @Override
    public boolean centroCostoActivoDelCentro(int idCentroCosto, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM centros_costo WHERE id_centro_costo = ? AND id_centro = ? AND activo = true", idCentroCosto, idCentro);
    }

    @Override
    public void actualizarEstadoYCentroCosto(int idEquipo, String estado, Integer idCentroCosto) throws SQLException {
        ejecutar("UPDATE equipos SET estado = ?, id_centro_costo_actual = ? WHERE id_equipo = ?", estado, entero(idCentroCosto), idEquipo);
    }
}
