package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.BitacoraDAO;
import com.mycompany.proyectoprueba.modelo.CambioBitacora;
import com.mycompany.proyectoprueba.modelo.FiltrosBitacora;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.RegistroBitacora;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BitacoraDAOJdbc extends JdbcBase implements BitacoraDAO {

    public BitacoraDAOJdbc(Connection conn) {
        super(conn);
    }

    private static Object txt(String v) {
        return v == null ? Nulo.TEXTO : v;
    }

    private static Object ent(Integer v) {
        return v == null ? Nulo.ENTERO : v;
    }

    @Override
    public int registrar(Integer idUsuario, String email, String ip, String modulo, String accion,
            String tabla, Integer idRegistro, String descripcion) throws SQLException {
        return insertarConId("INSERT INTO bitacora (id_usuario, usuario_email, ip, modulo, accion, tabla, id_registro, descripcion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_bitacora",
                ent(idUsuario), txt(corto(email, 150)), txt(corto(ip, 45)), modulo, accion, txt(tabla), ent(idRegistro), txt(corto(descripcion, 300)));
    }

    @Override
    public void registrarCambio(int idBitacora, String campo, String anterior, String nuevo) throws SQLException {
        ejecutar("INSERT INTO bitacora_detalle (id_bitacora, campo, valor_anterior, valor_nuevo) VALUES (?, ?, ?, ?)",
                idBitacora, corto(campo, 60), txt(anterior), txt(nuevo));
    }

    private static String corto(String v, int max) {
        return v == null || v.length() <= max ? v : v.substring(0, max - 3) + "...";
    }

    @Override
    public Map<String, String> foto(String tabla, String pk, int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tabla + " WHERE " + pk + " = ?");
        try {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            try {
                if (!rs.next()) {
                    return null;
                }
                ResultSetMetaData md = rs.getMetaData();
                Map<String, String> m = new LinkedHashMap<String, String>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    m.put(md.getColumnName(i), rs.getString(i));
                }
                return m;
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    @Override
    public String describir(String tabla, String pk, String expresion, int id) throws SQLException {
        return uno("SELECT " + expresion + " FROM " + tabla + " t WHERE t." + pk + " = ?", rs -> rs.getString(1), id);
    }

    @Override
    public int mayorId(String tabla, String pk, String condicionUsuario, int idUsuario) throws SQLException {
        if (condicionUsuario == null) {
            return entero("SELECT COALESCE(MAX(" + pk + "), 0) FROM " + tabla);
        }
        return entero("SELECT COALESCE(MAX(" + pk + "), 0) FROM " + tabla + " WHERE " + condicionUsuario, idUsuario);
    }

    @Override
    public int nuevasDesde(String tabla, String pk, int desdeId, String condicionUsuario, int idUsuario) throws SQLException {
        if (condicionUsuario == null) {
            return entero("SELECT COUNT(*) FROM " + tabla + " WHERE " + pk + " > ?", desdeId);
        }
        return entero("SELECT COUNT(*) FROM " + tabla + " WHERE " + pk + " > ? AND " + condicionUsuario, desdeId, idUsuario);
    }

    @Override
    public String nombre(String sql, int id) throws SQLException {
        return uno(sql, rs -> rs.getString(1), id);
    }

    private static final String COLUMNAS = "SELECT b.id_bitacora, b.fecha, TRIM(COALESCE(u.nombre, '') || ' ' || COALESCE(u.apellido, '')) AS usuario, "
            + "b.usuario_email, b.ip, b.modulo, b.accion, b.tabla, b.id_registro, b.descripcion, "
            + "(SELECT COUNT(*) FROM bitacora_detalle d WHERE d.id_bitacora = b.id_bitacora) AS cambios "
            + "FROM bitacora b LEFT JOIN usuarios u ON u.id_usuario = b.id_usuario ";

    private static RegistroBitacora leer(ResultSet rs) throws SQLException {
        RegistroBitacora r = new RegistroBitacora();
        r.id = rs.getInt("id_bitacora");
        r.fecha = rs.getTimestamp("fecha");
        r.usuario = rs.getString("usuario");
        r.email = rs.getString("usuario_email");
        r.ip = rs.getString("ip");
        r.modulo = rs.getString("modulo");
        r.accion = rs.getString("accion");
        r.tabla = rs.getString("tabla");
        int idReg = rs.getInt("id_registro");
        r.idRegistro = rs.wasNull() ? null : Integer.valueOf(idReg);
        r.descripcion = rs.getString("descripcion");
        r.cambios = rs.getInt("cambios");
        return r;
    }

    @Override
    public List<RegistroBitacora> listar(FiltrosBitacora f, int limite) throws SQLException {
        StringBuilder sql = new StringBuilder(COLUMNAS).append("WHERE TRUE ");
        List<Object> p = new ArrayList<Object>();
        if (!f.desde.isEmpty()) {
            sql.append("AND b.fecha >= CAST(? AS date) ");
            p.add(f.desde);
        }
        if (!f.hasta.isEmpty()) {
            sql.append("AND b.fecha < CAST(? AS date) + 1 ");
            p.add(f.hasta);
        }
        if (f.idUsuario > 0) {
            sql.append("AND b.id_usuario = ? ");
            p.add(f.idUsuario);
        }
        if (!f.modulo.isEmpty()) {
            sql.append("AND b.modulo = ? ");
            p.add(f.modulo);
        }
        if (!f.accion.isEmpty()) {
            sql.append("AND b.accion = ? ");
            p.add(f.accion);
        }
        if (!f.texto.isEmpty()) {
            sql.append("AND (b.descripcion ILIKE ? OR b.usuario_email ILIKE ? OR b.ip ILIKE ? "
                    + "OR EXISTS (SELECT 1 FROM bitacora_detalle d WHERE d.id_bitacora = b.id_bitacora "
                    + "AND (d.valor_anterior ILIKE ? OR d.valor_nuevo ILIKE ?))) ");
            String t = "%" + f.texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            for (int i = 0; i < 5; i++) {
                p.add(t);
            }
        }
        if (!f.tabla.isEmpty() && f.idRegistro > 0) {
            sql.append("AND b.tabla = ? AND b.id_registro = ? ");
            p.add(f.tabla);
            p.add(f.idRegistro);
        }
        sql.append("ORDER BY b.fecha DESC, b.id_bitacora DESC LIMIT ").append(limite);
        return lista(sql.toString(), BitacoraDAOJdbc::leer, p.toArray());
    }

    @Override
    public RegistroBitacora obtener(int id) throws SQLException {
        return uno(COLUMNAS + "WHERE b.id_bitacora = ?", BitacoraDAOJdbc::leer, id);
    }

    @Override
    public List<CambioBitacora> cambios(int idBitacora) throws SQLException {
        return lista("SELECT campo, valor_anterior, valor_nuevo FROM bitacora_detalle WHERE id_bitacora = ? ORDER BY id_detalle",
                rs -> new CambioBitacora(rs.getString(1), rs.getString(2), rs.getString(3)), idBitacora);
    }

    @Override
    public Map<String, Integer> resumenHoy() throws SQLException {
        return uno("SELECT COUNT(*), "
                + "COUNT(*) FILTER (WHERE accion = 'LOGIN'), "
                + "COUNT(*) FILTER (WHERE accion = 'LOGIN_FALLIDO'), "
                + "COUNT(*) FILTER (WHERE accion IN ('ALTA', 'MODIFICACION')), "
                + "COUNT(*) FILTER (WHERE accion IN ('BAJA', 'ANULACION')) "
                + "FROM bitacora WHERE fecha >= CURRENT_DATE", rs -> {
                    Map<String, Integer> m = new LinkedHashMap<String, Integer>();
                    m.put("TOTAL", rs.getInt(1));
                    m.put("LOGIN", rs.getInt(2));
                    m.put("LOGIN_FALLIDO", rs.getInt(3));
                    m.put("CAMBIOS", rs.getInt(4));
                    m.put("BAJAS", rs.getInt(5));
                    return m;
                });
    }

    @Override
    public List<Opcion> usuarios() throws SQLException {
        return lista("SELECT u.id_usuario, u.nombre || ' ' || u.apellido || ' (' || u.email || ')' FROM usuarios u "
                + "WHERE EXISTS (SELECT 1 FROM bitacora b WHERE b.id_usuario = u.id_usuario) ORDER BY u.nombre, u.apellido",
                rs -> new Opcion(rs.getInt(1), rs.getString(2)));
    }

    @Override
    public int fallidosHoy() throws SQLException {
        return entero("SELECT COUNT(*) FROM bitacora WHERE accion = 'LOGIN_FALLIDO' AND fecha >= CURRENT_DATE");
    }
}
