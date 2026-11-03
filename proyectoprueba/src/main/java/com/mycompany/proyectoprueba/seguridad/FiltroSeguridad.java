package com.mycompany.proyectoprueba.seguridad;

import com.mycompany.proyectoprueba.util.Conexion;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro de seguridad (mapeado a /* en WEB-INF/web.xml):
 *  1) Exige sesión iniciada para las páginas .jsp, los controladores, los reportes
 *     y las fotos, salvo el login (index.jsp, LoginServlet) y logout.jsp.
 *  2) Bloquea el acceso directo a archivos internos (*.jspf y plantillas de reportes/).
 *  3) Cierra siempre la conexión a la base abierta durante la petición.
 *  4) Exige el token CSRF en las peticiones a los controladores (ver Csrf).
 *  5) Agrega cabeceras de seguridad a todas las respuestas.
 * Los recursos estáticos (dist/, plugins/, imagenes/, js/) son públicos porque los usa el login.
 */
public class FiltroSeguridad implements Filter {

    /** Atributo del request con los Permisos del usuario (ver Permisos.de). */
    public static final String ATRIBUTO_PERMISOS = "nanday.permisos";
    private static final long CACHE_PERMISOS_MS = 60000L;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        cabecerasSeguridad(resp);
        try {
            String ruta = ruta(req);

            if (bloqueada(ruta)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (requiereSesion(ruta) && !haySesion(req)) {
                if (esPeticionDeDatos(req, ruta)) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.setContentType("text/html; charset=UTF-8");
                    resp.getWriter().print("<div class='alert alert-danger'>Sesión expirada o no iniciada. Vuelva a iniciar sesión.</div>");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/index.jsp");
                }
                return;
            }
            // Permisos por rol y centro
            if (requiereSesion(ruta) && !ruta.startsWith("/uploads/")) {
                Permisos p = permisos(req);
                // Usuario desactivado mientras estaba conectado: se cierra su sesión (a lo sumo en 1 minuto)
                if (p.usuarioInactivo()) {
                    HttpSession s = req.getSession(false);
                    if (s != null) {
                        try {
                            s.invalidate();
                        } catch (IllegalStateException yaCerrada) {
                            // otra petición la cerró al mismo tiempo
                        }
                    }
                    if (esPeticionDeDatos(req, ruta)) {
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.setContentType("text/html; charset=UTF-8");
                        resp.getWriter().print("<div class='alert alert-danger'>Su usuario fue desactivado. Consulte con el administrador.</div>");
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/index.jsp");
                    }
                    return;
                }
                req.setAttribute(ATRIBUTO_PERMISOS, p);
                String tipo = operacion(req, ruta);
                if (!ReglasAcceso.permitido(p, ruta, tipo)) {
                    if (esPeticionDeDatos(req, ruta)) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.setContentType("text/html; charset=UTF-8");
                        resp.getWriter().print("<div class='alert alert-danger'>No tiene permiso para realizar esta operación con su rol.</div>");
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/dashboard.jsp?acceso=denegado");
                    }
                    return;
                }
                // CSRF: los controladores solo aceptan peticiones que traen el token de la sesión
                if (ReglasAcceso.esControlador(ruta) && !Csrf.exenta(ruta, tipo) && !Csrf.valido(req)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.setContentType("text/html; charset=UTF-8");
                    resp.getWriter().print("<div class='alert alert-danger'>La página quedó desactualizada o la petición no es válida. "
                            + "Recargue la página (F5) e intente de nuevo.</div>");
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            Conexion.cerrar(req);
        }
    }

    /**
     * Cabeceras de seguridad:
     *  - X-Frame-Options / frame-ancestors: el sistema no se puede mostrar dentro de otro sitio
     *    (protección contra clickjacking).
     *  - X-Content-Type-Options: el navegador respeta el tipo de contenido declarado.
     *  - Referrer-Policy: las direcciones internas no se envían a otros sitios.
     */
    static void cabecerasSeguridad(HttpServletResponse resp) {
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        resp.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Referrer-Policy", "same-origin");
    }

    static String ruta(HttpServletRequest req) {
        String servletPath = req.getServletPath() == null ? "" : req.getServletPath();
        String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
        return servletPath + pathInfo;
    }

    static boolean bloqueada(String ruta) {
        String r = ruta.toLowerCase();
        return r.endsWith(".jspf")
                || r.startsWith("/reportes/");
    }

    static boolean requiereSesion(String ruta) {
        if (ruta.equals("/index.jsp") || ruta.equals("/logout.jsp")
                || ruta.equals("/LoginServlet")) {
            return false;
        }
        return ReglasAcceso.esControlador(ruta)
                || ruta.startsWith("/uploads/")
                || ruta.endsWith(".jsp");
    }

    /** Permisos del usuario, en caché de sesión por 60 segundos. */
    private static Permisos permisos(HttpServletRequest req) throws ServletException {
        HttpSession s = req.getSession(false);
        Object o = s.getAttribute(ATRIBUTO_PERMISOS);
        if (o instanceof Permisos && ((Permisos) o).edadMs() < CACHE_PERMISOS_MS) {
            return (Permisos) o;
        }
        try {
            int idUsuario = ((Integer) s.getAttribute("id_usuario")).intValue();
            Permisos p = Permisos.cargar(Conexion.obtener(req.getServletContext(), req), idUsuario);
            s.setAttribute(ATRIBUTO_PERMISOS, p);
            return p;
        } catch (SQLException e) {
            throw new ServletException("No se pudieron cargar los permisos del usuario", e);
        }
    }

    /**
     * Operación pedida al controlador, para controlar el permiso: el parámetro "reporte"
     * en ReporteIndividualServlet, ninguna en PerfilServlet y el parámetro "tipo" en los demás.
     */
    static String operacion(HttpServletRequest req, String ruta) {
        if (!ReglasAcceso.esControlador(ruta) || "/PerfilServlet".equals(ruta)) {
            return null;
        }
        if ("/ReporteIndividualServlet".equals(ruta)) {
            return req.getParameter("reporte");
        }
        return req.getParameter("tipo");
    }

    static boolean haySesion(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null
                && "1".equals(String.valueOf(s.getAttribute("logueado")))
                && s.getAttribute("id_usuario") != null;
    }

    private static boolean esPeticionDeDatos(HttpServletRequest req, String ruta) {
        return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || ReglasAcceso.esControlador(ruta);
    }

    @Override
    public void destroy() {
    }
}
