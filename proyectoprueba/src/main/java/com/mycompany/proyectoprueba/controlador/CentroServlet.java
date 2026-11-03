package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Centro;
import com.mycompany.proyectoprueba.servicio.CentroServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Centros. URL: CentroServlet
 * Operaciones (tipo): listar, listarCombo, Guardar, Modificar, Borrar, imprimir.
 */
public class CentroServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String etiquetaAlerta() {
        return "i";
    }

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        CentroServicio servicio = CentroServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("centros", servicio.listarTodos());
            vista(req, resp, "centro/filas.jsp");

        } else if ("listarCombo".equals(tipo)) {
            req.setAttribute("centros", servicio.listarActivos());
            vista(req, resp, "centro/opciones.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo) || "Borrar".equals(tipo)) {
            try {
                if ("Guardar".equals(tipo)) {
                    servicio.guardar(desdeFormulario(req, 0));
                    exito(resp, "Centro guardado con éxito");
                } else if ("Modificar".equals(tipo)) {
                    servicio.modificar(desdeFormulario(req, entero(req.getParameter("pk"))));
                    exito(resp, "Centro modificado con éxito");
                } else {
                    servicio.eliminar(entero(req.getParameter("pk")));
                    exito(resp, "Centro eliminado con éxito");
                }
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("imprimir".equals(tipo)) {
            imprimirListado(req, resp, "centros", "Listado de Centros", "centros", "i");
        }
    }

    private static Centro desdeFormulario(HttpServletRequest req, int id) {
        return new Centro(id, req.getParameter("codigo"), req.getParameter("nombre"), req.getParameter("direccion"),
                Boolean.parseBoolean(req.getParameter("activo")));
    }
}
