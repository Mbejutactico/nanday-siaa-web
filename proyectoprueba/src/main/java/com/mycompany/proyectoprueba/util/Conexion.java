package com.mycompany.proyectoprueba.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

/**
 * Entrega una conexión por petición HTTP y la guarda en el request;
 * FiltroSeguridad la cierra siempre al terminar la petición.
 * Origen: pool JNDI "java:comp/env/jdbc/nanday" (context.xml) o, si no hay pool,
 * conexión directa con db.url / db.user de context.xml y la contraseña de claveDirecta.
 */
public final class Conexion {

    public static final String ATRIBUTO = "nanday.conexion";
    private static final String JNDI = "java:comp/env/jdbc/nanday";

    private static volatile DataSource pool;
    private static volatile boolean poolBuscado = false;

    private Conexion() {
    }

    public static Connection obtener(ServletContext ctx, HttpServletRequest request) throws SQLException {
        Object existente = request.getAttribute(ATRIBUTO);
        if (existente instanceof Connection) {
            Connection c = (Connection) existente;
            if (!c.isClosed()) {
                return c;
            }
        }
        Connection nueva = crear(ctx);
        request.setAttribute(ATRIBUTO, nueva);
        return nueva;
    }

    public static void cerrar(HttpServletRequest request) {
        Object o = request.getAttribute(ATRIBUTO);
        if (o instanceof Connection) {
            Connection c = (Connection) o;
            try {
                if (!c.isClosed()) {
                    if (!c.getAutoCommit()) {
                        c.rollback();          
                        c.setAutoCommit(true);
                    }
                    c.close();
                }
            } catch (SQLException ignorada) {
            }
            request.removeAttribute(ATRIBUTO);
        }
    }

    private static Connection crear(ServletContext ctx) throws SQLException {
        DataSource ds = buscarPool();
        if (ds != null) {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                ctx.log("Pool JNDI jdbc/nanday no disponible (" + e.getMessage()
                        + "). Se usa conexion directa con los parametros db.* de context.xml.");
            }
        }
        String url = ctx.getInitParameter("db.url");
        String usuario = ctx.getInitParameter("db.user");
        String clave = claveDirecta(ctx);
        if (url == null || url.trim().isEmpty()) {
            throw new SQLException("Falta el parametro db.url en META-INF/context.xml");
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de PostgreSQL", e);
        }
        Connection c = DriverManager.getConnection(url, usuario, clave);
        if (ds != null) {
            pool = null;
        }
        return c;
    }

    /**
     * Contraseña para la conexión directa, en este orden: parámetro db.password de
     * context.xml, propiedad del sistema nanday.db.password o variable NANDAY_DB_PASSWORD.
     */
    static String claveDirecta(ServletContext ctx) {
        String clave = ctx.getInitParameter("db.password");
        if (clave != null && !clave.isEmpty()) {
            return clave;
        }
        clave = System.getProperty("nanday.db.password");
        if (clave != null && !clave.isEmpty()) {
            return clave;
        }
        clave = System.getenv("NANDAY_DB_PASSWORD");
        return clave == null ? "" : clave;
    }

    private static DataSource buscarPool() {
        if (!poolBuscado) {
            synchronized (Conexion.class) {
                if (!poolBuscado) {
                    try {
                        pool = (DataSource) new InitialContext().lookup(JNDI);
                    } catch (NamingException | ClassCastException e) {
                        pool = null;
                    }
                    poolBuscado = true;
                }
            }
        }
        return pool;
    }
}
