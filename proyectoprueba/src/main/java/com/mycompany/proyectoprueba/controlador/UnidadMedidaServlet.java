package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.UnidadMedida;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.servicio.UnidadMedidaServicio;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unidades de medida. URL: UnidadMedidaServlet
 * Operaciones (tipo): listar, listarCombo, Guardar, Modificar, Borrar, imprimir.
 */
public class UnidadMedidaServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String etiquetaAlerta() {
        return "i";
    }

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        UnidadMedidaServicio servicio = UnidadMedidaServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("unidades", servicio.listarTodas());
            vista(req, resp, "unidadMedida/filas.jsp");

        } else if ("listarCombo".equals(tipo)) {
            req.setAttribute("unidades", servicio.listarActivas());
            vista(req, resp, "unidadMedida/opciones.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo) || "Borrar".equals(tipo)) {
            try {
                if ("Guardar".equals(tipo)) {
                    servicio.guardar(desdeFormulario(req, 0));
                    exito(resp, "Unidad guardada con éxito");
                } else if ("Modificar".equals(tipo)) {
                    servicio.modificar(desdeFormulario(req, entero(req.getParameter("pk"))));
                    exito(resp, "Unidad modificada con éxito");
                } else {
                    servicio.eliminar(entero(req.getParameter("pk")));
                    exito(resp, "Unidad eliminada con éxito");
                }
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("imprimir".equals(tipo)) {
            imprimirListado(req, resp, "unidades_listado", "Listado de Unidades de Medida", "unidades", "div");
        }
    }

    private static UnidadMedida desdeFormulario(HttpServletRequest req, int id) {
        return new UnidadMedida(id, req.getParameter("nombre"), req.getParameter("abreviatura"),
                Boolean.parseBoolean(req.getParameter("activo")));
    }
}
