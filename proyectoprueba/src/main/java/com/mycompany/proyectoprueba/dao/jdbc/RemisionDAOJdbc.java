package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.RemisionDAO;
import com.mycompany.proyectoprueba.modelo.LineaRemision;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RemisionDAOJdbc extends JdbcBase implements RemisionDAO {

    /**
     * Copia las líneas del proceso de origen según el tipo de remisión:
     *  - SALIDA_INSUMOS: líneas de detalle_ot asignadas a esta remisión.
     *  - TRANSFERENCIA: líneas de transferencias_detalle (salen del almacén de origen).
     *  - SALIDA_ACTIVOS: activos de solicitudes_activos_detalle (una unidad cada uno).
     */
    private static final String COPIAR =
            "INSERT INTO remisiones_detalle (id_remision, orden, tipo_item, id_insumo, id_equipo, codigo, descripcion, unidad, cantidad, id_almacen, origen) "
            + "SELECT r.id_remision, ROW_NUMBER() OVER (ORDER BY x.orden), x.tipo_item, x.id_insumo, x.id_equipo, "
            + "       x.codigo, x.descripcion, x.unidad, x.cantidad, x.id_almacen, x.origen "
            + "FROM remisiones r "
            + "JOIN LATERAL ( "
            + "    SELECT 'INSUMO'::varchar AS tipo_item, d.id_insumo, NULL::integer AS id_equipo, i.codigo, i.descripcion, um.abreviatura AS unidad, "
            + "           d.cantidad, d.id_almacen, a.nombre::varchar AS origen, d.id_detalle_ot AS orden "
            + "      FROM detalle_ot d JOIN insumos i ON i.id_insumo = d.id_insumo "
            + "      LEFT JOIN unidad_medida um ON um.id_unidad_medida = i.id_unidad_medida "
            + "      LEFT JOIN almacenes a ON a.id_almacen = d.id_almacen "
            + "     WHERE r.tipo_remision = 'SALIDA_INSUMOS' AND d.id_remision = r.id_remision "
            + "    UNION ALL "
            + "    SELECT 'INSUMO', td.id_insumo, NULL, i.codigo, i.descripcion, um.abreviatura, td.cantidad, t.id_almacen_origen, ao.nombre, td.id_detalle "
            + "      FROM transferencias_detalle td JOIN transferencias t ON t.id_transferencia = td.id_transferencia "
            + "      JOIN insumos i ON i.id_insumo = td.id_insumo "
            + "      LEFT JOIN unidad_medida um ON um.id_unidad_medida = i.id_unidad_medida "
            + "      LEFT JOIN almacenes ao ON ao.id_almacen = t.id_almacen_origen "
            + "     WHERE r.tipo_remision = 'TRANSFERENCIA' AND td.id_transferencia = r.id_transferencia "
            + "    UNION ALL "
            + "    SELECT 'EQUIPO', NULL, sd.id_equipo, e.serial_activo, "
            + "           COALESCE(i.descripcion, 'Activo') || COALESCE(' - Serie ' || e.numero_serie, ''), 'u', 1, NULL, "
            + "           COALESCE(co.nombre, '') || COALESCE(' / ' || cco.codigo, ''), sd.id_detalle "
            + "      FROM solicitudes_activos_detalle sd JOIN equipos e ON e.id_equipo = sd.id_equipo "
            + "      LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
            + "      LEFT JOIN centros co ON co.id_centro = sd.id_centro_origen "
            + "      LEFT JOIN centros_costo cco ON cco.id_centro_costo = sd.id_centro_costo_origen "
            + "     WHERE r.tipo_remision = 'SALIDA_ACTIVOS' AND sd.id_solicitud = r.id_solicitud_activo "
            + ") x ON true "
            + "WHERE r.id_remision = ?";

    public RemisionDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public int copiarDetalle(int idRemision) throws SQLException {
        return ejecutar(COPIAR, idRemision);
    }

    @Override
    public List<LineaRemision> detalle(int idRemision) throws SQLException {
        return lista("SELECT orden, tipo_item, codigo, descripcion, unidad, cantidad, origen FROM remisiones_detalle "
                + "WHERE id_remision = ? ORDER BY orden", rs -> {
                    LineaRemision l = new LineaRemision();
                    l.orden = rs.getInt(1);
                    l.tipoItem = rs.getString(2);
                    l.codigo = rs.getString(3);
                    l.descripcion = rs.getString(4);
                    l.unidad = rs.getString(5);
                    l.cantidad = rs.getInt(6);
                    l.origen = rs.getString(7);
                    return l;
                }, idRemision);
    }
}
