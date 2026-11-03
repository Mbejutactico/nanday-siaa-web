package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Proveedor;
import com.mycompany.proyectoprueba.servicio.ProveedorServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proveedores. URL: ProveedorServlet
 * Operaciones (tipo): listar, listarCombo, Guardar, Modificar, Borrar, imprimir.
 */
public class ProveedorServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String etiquetaAlerta() {
        return "i";
    }

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ProveedorServicio servicio = ProveedorServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("proveedores", servicio.listarTodos());
            vista(req, resp, "proveedor/filas.jsp");

        } else if ("listarCombo".equals(tipo)) {
            req.setAttribute("proveedores", servicio.listarActivos());
            vista(req, resp, "proveedor/opciones.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo) || "Borrar".equals(tipo)) {
            try {
                if ("Guardar".equals(tipo)) {
                    servicio.guardar(desdeFormulario(req, 0));
                    exito(resp, "Proveedor guardado con éxito");
                } else if ("Modificar".equals(tipo)) {
                    servicio.modificar(desdeFormulario(req, entero(req.getParameter("pk"))));
                    exito(resp, "Proveedor modificado con éxito");
                } else {
                    servicio.eliminar(entero(req.getParameter("pk")));
                    exito(resp, "Proveedor eliminado con éxito");
                }
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("imprimir".equals(tipo)) {
            imprimirListado(req, resp, "proveedores", "Listado de Proveedores", "proveedores", "i");
        }
    }

    private static Proveedor desdeFormulario(HttpServletRequest req, int id) {
        return new Proveedor(id, req.getParameter("razon_social"), req.getParameter("ruc"), req.getParameter("direccion"),
                req.getParameter("telefono"), req.getParameter("email"), Boolean.parseBoolean(req.getParameter("activo")));
    }
}
