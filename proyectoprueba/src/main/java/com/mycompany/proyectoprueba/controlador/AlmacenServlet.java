package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Almacen;
import com.mycompany.proyectoprueba.servicio.AlmacenServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Almacenes. URL: AlmacenServlet
 * Operaciones (tipo): listar, Guardar, Modificar, Borrar, imprimir.
 */
public class AlmacenServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String etiquetaAlerta() {
        return "i";
    }

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AlmacenServicio servicio = AlmacenServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("almacenes", servicio.listarTodos());
            vista(req, resp, "almacen/filas.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo) || "Borrar".equals(tipo)) {
            try {
                if ("Guardar".equals(tipo)) {
                    servicio.guardar(desdeFormulario(req, false));
                    exito(resp, "Almacén guardado con éxito");
                } else if ("Modificar".equals(tipo)) {
                    servicio.modificar(desdeFormulario(req, true));
                    exito(resp, "Almacén modificado con éxito");
                } else {
                    servicio.eliminar(entero(req.getParameter("pk")));
                    exito(resp, "Almacén eliminado con éxito");
                }
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("imprimir".equals(tipo)) {
            imprimirListado(req, resp, "almacenes_listado", "Listado de Almacenes", "almacenes", "div");
        }
    }

    private static Almacen desdeFormulario(HttpServletRequest req, boolean conId) {
        Integer idCentro = enteroOpcional(req.getParameter("id_centro"));
        int id = conId ? entero(req.getParameter("pk")) : 0;
        return new Almacen(id, req.getParameter("nombre"), req.getParameter("descripcion"), req.getParameter("ubicacion"),
                idCentro, Boolean.parseBoolean(req.getParameter("activo")));
    }
}
