package com.mycompany.proyectoprueba.seguridad;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Configuración que se aplica una sola vez al arrancar la aplicación.
 * Registrado en WEB-INF/web.xml.
 *
 *  - secureCookies (META-INF/context.xml): si es "true", la cookie de sesión se marca
 *    como Secure y solo viaja por HTTPS. Activarlo SOLO cuando el sitio se sirva por HTTPS;
 *    con HTTP puro nadie podría mantener la sesión iniciada.
 */
public class InicioAplicacion implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext ctx = evento.getServletContext();
        boolean seguras = "true".equalsIgnoreCase(trim(ctx.getInitParameter("secureCookies")));
        try {
            ctx.getSessionCookieConfig().setSecure(seguras);
            ctx.log("Ñanday: cookie de sesión " + (seguras ? "Secure (solo HTTPS)" : "sin Secure (HTTP permitido)"));
        } catch (IllegalStateException | UnsupportedOperationException e) {
            ctx.log("Ñanday: no se pudo configurar la cookie de sesión (secureCookies)", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
