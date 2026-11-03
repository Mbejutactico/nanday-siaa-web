package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Rol;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.servicio.RolServicio;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Roles. URL: RolServlet
 * Operaciones (tipo): listar, listarTipos, listarCombo, Guardar, Modificar, Borrar, imprimir.
 */
public class RolServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        RolServicio servicio = RolServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("roles", servicio.listarTodos());
            vista(req, resp, "rol/filas.jsp");

        } else if ("listarTipos".equals(tipo)) {
            vista(req, resp, "rol/tipos.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo)) {
            boolean nuevo = "Guardar".equals(tipo);
            Rol r = new Rol(nuevo ? 0 : idValido(req.getParameter("pk")), req.getParameter("nombre"), req.getParameter("descripcion"),
                    Boolean.parseBoolean(req.getParameter("activo")), req.getParameter("codigo"));
            try {
                servicio.guardar(r, nuevo);
                exito(resp, nuevo ? "Rol guardado con éxito." : "Rol modificado con éxito. Los usuarios con este rol verán el cambio en menos de un minuto.");
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("Borrar".equals(tipo)) {
            int id = idValido(req.getParameter("pk"));
            if (id < 0) {
                falla(resp, "Rol no válido.");
                return;
            }
            try {
                servicio.eliminar(id);
                exito(resp, "Rol eliminado con éxito.");
            } catch (ReglaNegocioException e) {
                alerta(resp, "div", e.isAviso() ? "warning" : "danger", Html.esc(e.getMessage()));
            }

        } else if ("listarCombo".equals(tipo)) {
            req.setAttribute("roles", servicio.listarActivos());
            vista(req, resp, "rol/opciones.jsp");

        } else if ("imprimir".equals(tipo)) {
            imprimir(req, resp);
        }
    }

    private void imprimir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("TITULO", "Listado de Roles");
            p.put("FECHA", new Date());
            p.put("USUARIO", usuarioReporte(req));
            if (!ReportePdf.enviarCompilado(getServletContext(), resp, "roles_listado", p, conexion(req), "roles")) {
                escribir(resp, "Error: El archivo de reporte no existe en: " + ReportePdf.ruta(getServletContext(), "roles_listado"));
            }
        } catch (Exception e) {
            log("Error al generar el listado de roles", e);
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div class='alert alert-danger'>No se pudo generar el reporte.</div>");
        }
    }

    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        log("Error en rolcontrolador (tipo=" + req.getParameter("tipo") + ")", e);
        escribir(resp, "<div class='alert alert-danger'>Ocurrió un error al procesar la operación. Intente de nuevo.</div>");
    }
}
