package com.mycompany.proyectoprueba.dao.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Base de las implementaciones JDBC: ejecuta consultas con parámetros y cierra siempre
 * los recursos. Usa la conexión de la petición (la cierra el FiltroSeguridad).
 *
 * Parámetros admitidos: Integer, Long, String, Boolean, BigDecimal, Timestamp, java.sql.Date
 * y Nulo (un NULL con su tipo SQL, por ejemplo Nulo.ENTERO).
 */
public abstract class JdbcBase {

    /** Lee una fila del ResultSet y la convierte en un objeto del modelo. */
    public interface Fila<T> {
        T leer(ResultSet rs) throws SQLException;
    }

    /** Un NULL con tipo SQL, para los parámetros que pueden ir vacíos. */
    public static final class Nulo {
        public static final Nulo ENTERO = new Nulo(Types.INTEGER);
        public static final Nulo TEXTO = new Nulo(Types.VARCHAR);
        public static final Nulo NUMERICO = new Nulo(Types.NUMERIC);
        public static final Nulo FECHA = new Nulo(Types.DATE);
        public static final Nulo BOOLEANO = new Nulo(Types.BOOLEAN);
        final int tipo;

        private Nulo(int tipo) {
            this.tipo = tipo;
        }
    }

    protected final Connection conn;

    protected JdbcBase(Connection conn) {
        this.conn = conn;
    }

    /** Integer o NULL entero. */
    protected static Object entero(Integer v) {
        return v == null ? Nulo.ENTERO : v;
    }

    /** String o NULL de texto. */
    protected static Object texto(String v) {
        return v == null ? Nulo.TEXTO : v;
    }

    protected <T> List<T> lista(String sql, Fila<T> fila, Object... params) throws SQLException {
        List<T> resultado = new ArrayList<T>();
        PreparedStatement ps = conn.prepareStatement(sql);
        try {
            asignar(ps, params);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    resultado.add(fila.leer(rs));
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return resultado;
    }

    /** Primera fila o null si no hay. */
    protected <T> T uno(String sql, Fila<T> fila, Object... params) throws SQLException {
        List<T> l = lista(sql, fila, params);
        return l.isEmpty() ? null : l.get(0);
    }

    /** true si la consulta devuelve alguna fila. */
    protected boolean existe(String sql, Object... params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        try {
            asignar(ps, params);
            ResultSet rs = ps.executeQuery();
            try {
                return rs.next();
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    /** Primer entero de la consulta (por ejemplo un COUNT). */
    protected int entero(String sql, Object... params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        try {
            asignar(ps, params);
            ResultSet rs = ps.executeQuery();
            try {
                rs.next();
                return rs.getInt(1);
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    /** INSERT/UPDATE/DELETE: devuelve la cantidad de filas afectadas. */
    protected int ejecutar(String sql, Object... params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        try {
            asignar(ps, params);
            return ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    /** INSERT ... RETURNING id: devuelve el id generado. */
    protected int insertarConId(String sql, Object... params) throws SQLException {
        return entero(sql, params);
    }

    static void asignar(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object v = params[i];
            int n = i + 1;
            if (v instanceof Nulo) {
                ps.setNull(n, ((Nulo) v).tipo);
            } else if (v instanceof Integer) {
                ps.setInt(n, ((Integer) v).intValue());
            } else if (v instanceof Long) {
                ps.setLong(n, ((Long) v).longValue());
            } else if (v instanceof Boolean) {
                ps.setBoolean(n, ((Boolean) v).booleanValue());
            } else if (v instanceof BigDecimal) {
                ps.setBigDecimal(n, (BigDecimal) v);
            } else if (v instanceof Timestamp) {
                ps.setTimestamp(n, (Timestamp) v);
            } else if (v instanceof java.sql.Date) {
                ps.setDate(n, (java.sql.Date) v);
            } else if (v == null) {
                ps.setString(n, null);
            } else {
                ps.setString(n, v.toString());
            }
        }
    }

    /** Entero que puede ser NULL en la base. */
    protected static Integer enteroONull(ResultSet rs, String columna) throws SQLException {
        int v = rs.getInt(columna);
        return rs.wasNull() ? null : Integer.valueOf(v);
    }
}
