package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.CentroCosto;
import com.mycompany.proyectoprueba.servicio.CentroCostoServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Centros de costo. URL: CentroCostoServlet
 * Operaciones (tipo): listar, Guardar, Modificar, Borrar, imprimir.
 */
public class CentroCostoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        CentroCostoServicio servicio = CentroCostoServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("centrosCosto", servicio.listarTodos());
            vista(req, resp, "centroCosto/filas.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo) || "Borrar".equals(tipo)) {
            try {
                if ("Guardar".equals(tipo)) {
                    servicio.guardar(desdeFormulario(req, false));
                    exito(resp, "Centro de costo guardado con éxito");
                } else if ("Modificar".equals(tipo)) {
                    servicio.modificar(desdeFormulario(req, true));
                    exito(resp, "Centro de costo modificado con éxito");
                } else {
                    servicio.eliminar(entero(req.getParameter("pk")));
                    exito(resp, "Centro de costo eliminado con éxito");
                }
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("imprimir".equals(tipo)) {
            imprimirListado(req, resp, "centros_costo_listado", "Listado de Centros de Costo", "centros_costo", "div");
        }
    }

    /** Como antes: primero el centro y después el pk (un número inválido da el mismo error). */
    private static CentroCosto desdeFormulario(HttpServletRequest req, boolean conId) {
        Integer idCentro = enteroOpcional(req.getParameter("id_centro"));
        int id = conId ? entero(req.getParameter("pk")) : 0;
        return new CentroCosto(id, req.getParameter("codigo"), req.getParameter("descripcion"), idCentro,
                Boolean.parseBoolean(req.getParameter("activo")));
    }
}
