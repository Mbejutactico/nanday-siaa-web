package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.seguridad.Bitacora;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import org.json.JSONArray;
import org.json.JSONException;
import com.mycompany.proyectoprueba.util.Conexion;
import com.mycompany.proyectoprueba.util.Errores;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base de los controladores (MVC).
 * Flujo: FiltroSeguridad (sesión, permisos, CSRF) -> Servlet (lee parámetros) ->
 * Servicio (reglas de negocio) -> DAO (SQL) -> vista JSP en /WEB-INF/vistas o respuesta JSON.
 * La operación se elige con el parámetro "tipo"; GET y POST se atienden igual.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        procesar(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        procesar(req, resp);
    }

    private void procesar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        String tipo = req.getParameter("tipo");
        // Bitácora: si la operación se audita, se guarda cómo estaba antes y se compara al terminar
        Bitacora.Seguimiento seguimiento = null;
        if (Bitacora.audita(req.getServletPath(), tipo)) {
            try {
                seguimiento = Bitacora.antes(req, conexion(req), req.getServletPath(), tipo);
            } catch (SQLException e) {
                seguimiento = null;
            }
        }
        try {
            atender(tipo, req, resp);
        } catch (ServletException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            error(req, resp, e);
        } finally {
            Bitacora.despues(seguimiento);
        }
    }

    /** Atiende la operación indicada en "tipo". Una operación desconocida no responde nada. */
    protected abstract void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception;

    /** Respuesta ante un error no controlado: "Error: mensaje" con el formato de alerta de la pantalla. */
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        falla(resp, "Error: " + mensaje(e));
    }

    protected String etiquetaAlerta() {
        return "div";
    }

    /** Alerta verde (el texto no se escapa: son mensajes fijos del sistema). */
    protected void exito(HttpServletResponse resp, String texto) throws IOException {
        alerta(resp, etiquetaAlerta(), "success", texto);
    }

    /** Alerta roja con el texto escapado. */
    protected void falla(HttpServletResponse resp, String texto) throws IOException {
        alerta(resp, etiquetaAlerta(), "danger", com.mycompany.proyectoprueba.util.Html.esc(texto));
    }

    protected void alerta(HttpServletResponse resp, String etiqueta, String clase, String html) throws IOException {
        escribir(resp, "<" + etiqueta + " class='alert alert-" + clase + "'>" + html + "</" + etiqueta + ">");
    }

    /**
     * Imprime un listado general (/reportes/{reporte}.jasper) con TITULO, FECHA y USUARIO.
     * Si falla, muestra "Error al generar reporte: ..." con la etiqueta indicada.
     */
    protected void imprimirListado(HttpServletRequest req, HttpServletResponse resp, String reporte, String titulo,
            String prefijoArchivo, String etiquetaError) throws IOException {
        imprimirListado(req, resp, reporte, titulo, prefijoArchivo, etiquetaError, "Error al generar reporte: ");
    }

    /** Igual, con el texto que precede al error (algunas pantallas dicen "Error al generar el reporte: "). */
    protected void imprimirListado(HttpServletRequest req, HttpServletResponse resp, String reporte, String titulo,
            String prefijoArchivo, String etiquetaError, String textoError) throws IOException {
        try {
            java.util.Map<String, Object> p = new java.util.HashMap<String, Object>();
            p.put("TITULO", titulo);
            p.put("FECHA", new java.util.Date());
            p.put("USUARIO", usuarioReporte(req));
            if (!com.mycompany.proyectoprueba.util.ReportePdf.enviarCompilado(getServletContext(), resp, reporte, p, conexion(req), prefijoArchivo)) {
                escribir(resp, "Error: El archivo de reporte no existe en: " + com.mycompany.proyectoprueba.util.ReportePdf.ruta(getServletContext(), reporte));
            }
        } catch (Exception e) {
            resp.setContentType("text/html");
            alerta(resp, etiquetaError, "danger", textoError + com.mycompany.proyectoprueba.util.Html.esc(mensaje(e)));
        }
    }

    /** Id del usuario de la sesión (null si no hay). */
    protected Integer idUsuario(HttpServletRequest req) {
        Object v = req.getSession().getAttribute("id_usuario");
        return v instanceof Integer ? (Integer) v : null;
    }

    /** Entero positivo de hasta 9 cifras (con espacios alrededor permitidos) o -1 si no es válido. */
    protected static int idValido(String valor) {
        return (valor != null && valor.trim().matches("[0-9]{1,9}")) ? Integer.parseInt(valor.trim()) : -1;
    }

    /** Número de hasta 9 cifras o 0 si viene vacío o inválido (filtros y ids opcionales). */
    protected static int numero(String v) {
        return (v != null && v.trim().matches("[0-9]{1,9}")) ? Integer.parseInt(v.trim()) : 0;
    }

    /** Muestra un combo genérico: primera opción vacía con el texto dado y luego las opciones. */
    protected void opciones(HttpServletRequest req, HttpServletResponse resp, String primera,
            java.util.List<com.mycompany.proyectoprueba.modelo.Opcion> lista) throws ServletException, IOException {
        req.setAttribute("primera", primera);
        req.setAttribute("opciones", lista);
        vista(req, resp, "comun/opciones.jsp");
    }

    /** Responde un objeto o arreglo JSON. */
    protected void json(HttpServletResponse resp, Object json) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        escribir(resp, json.toString());
    }

    /** Entero opcional como en las pantallas viejas: vacío o ausente = null; si no es número, NumberFormatException. */
    protected static Integer enteroOpcional(String valor) {
        return (valor != null && !valor.isEmpty()) ? Integer.valueOf(Integer.parseInt(valor)) : null;
    }

    // Ayudas

    /** Conexión de la petición (la cierra el FiltroSeguridad al terminar). */
    protected Connection conexion(HttpServletRequest req) throws SQLException {
        return Conexion.obtener(getServletContext(), req);
    }

    protected Permisos permisos(HttpServletRequest req) {
        return Permisos.de(req);
    }

    /** Mensaje para el usuario (el detalle técnico queda en el log del servidor). */
    protected String mensaje(Exception e) {
        return Errores.mensaje(getServletContext(), e);
    }

    protected void escribir(HttpServletResponse resp, String texto) throws IOException {
        resp.getWriter().print(texto);
    }

    /** Muestra una vista JSP de /WEB-INF/vistas (por ejemplo "unidadMedida/filas.jsp"). */
    protected void vista(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/vistas/" + jsp).include(req, resp);
    }

    /** Usuario de la sesión para los reportes ("Sistema" si no hay nombre). */
    protected String usuarioReporte(HttpServletRequest req) {
        Object n = req.getSession().getAttribute("nombre_completo");
        return (n instanceof String && !((String) n).isEmpty()) ? (String) n : "Sistema";
    }

    /** Entero obligatorio (lanza NumberFormatException como antes si no es número). */
    protected static int entero(String valor) {
        return Integer.parseInt(valor);
    }

    /**
     * Id obligatorio: entero positivo. Si falta o no es un número válido lanza
     * ReglaNegocioException con el mensaje indicado (la pantalla lo muestra tal cual).
     */
    protected static int idObligatorio(String valor, String mensaje) {
        int id = idValido(valor);
        if (id <= 0) {
            throw new ReglaNegocioException(mensaje);
        }
        return id;
    }

    /**
     * Lista JSON obligatoria (por ejemplo items=[...] o ids=[...]).
     * Si no viene o viene vacía: siFalta. Si no es una lista JSON válida: "Los datos enviados no son válidos".
     */
    protected static JSONArray listaObligatoria(String valor, String siFalta) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ReglaNegocioException(siFalta);
        }
        JSONArray lista;
        try {
            lista = new JSONArray(valor);
        } catch (JSONException e) {
            throw new ReglaNegocioException("Los datos enviados no son válidos. Recargue la página e intente de nuevo.");
        }
        if (lista.length() == 0) {
            throw new ReglaNegocioException(siFalta);
        }
        return lista;
    }
}
