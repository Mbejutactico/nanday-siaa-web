package com.mycompany.proyectoprueba.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Protección contra CSRF (que otro sitio, un link o un correo hagan que el navegador
 * del usuario ejecute operaciones en el sistema sin que él lo sepa).
 *
 * Cada sesión tiene un token secreto. header.jsp lo publica en
 * &lt;meta name="csrf-token"&gt; y footer.jsp lo agrega a TODAS las peticiones AJAX
 * de jQuery en la cabecera X-CSRF-Token. El FiltroSeguridad rechaza las peticiones a
 * los controladores que no lo traen. Otro sitio no puede leer el token, así que no
 * puede armar una petición válida.
 *
 * Quedan sin token solo las impresiones (tipo = imprimir...), que se abren en una
 * pestaña nueva con window.open y no modifican datos.
 */
public final class Csrf {

    public static final String ATRIBUTO = "nanday.csrf";
    public static final String CABECERA = "X-CSRF-Token";
    public static final String PARAMETRO = "_csrf";

    private static final SecureRandom AZAR = new SecureRandom();

    private Csrf() {
    }

    /** Token de la sesión; lo crea si todavía no existe. */
    public static String token(HttpSession sesion) {
        Object t = sesion.getAttribute(ATRIBUTO);
        if (t instanceof String) {
            return (String) t;
        }
        synchronized (Csrf.class) {
            t = sesion.getAttribute(ATRIBUTO);
            if (t instanceof String) {
                return (String) t;
            }
            byte[] b = new byte[32];
            AZAR.nextBytes(b);
            String nuevo = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            sesion.setAttribute(ATRIBUTO, nuevo);
            return nuevo;
        }
    }

    /** true si la petición trae el token correcto (cabecera X-CSRF-Token o parámetro _csrf). */
    public static boolean valido(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return false;
        }
        Object esperado = s.getAttribute(ATRIBUTO);
        if (!(esperado instanceof String)) {
            return false;
        }
        String recibido = req.getHeader(CABECERA);
        if (recibido == null || recibido.isEmpty()) {
            recibido = req.getParameter(PARAMETRO);
        }
        if (recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(((String) esperado).getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Operaciones que no necesitan token: impresiones (PDF) que se abren en otra pestaña
     * (tipo=imprimir... de cada controlador y los reportes de ReporteIndividualServlet). Solo leen datos.
     */
    public static boolean exenta(String ruta, String tipo) {
        if ("/ReporteIndividualServlet".equals(ruta)) {
            return true;
        }
        return tipo != null && tipo.startsWith("imprimir")
                && !"/PerfilServlet".equals(ruta);
    }
}
