package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Insumo;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.servicio.InsumoServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Insumos. URL: InsumoServlet
 * Operaciones (tipo): listar, Guardar, Modificar, Borrar (requiere INSUMOS_ELIMINAR), imprimir.
 */
public class InsumoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String etiquetaAlerta() {
        return "i";
    }

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        InsumoServicio servicio = InsumoServicio.con(conexion(req));

        if ("listar".equals(tipo)) {
            req.setAttribute("insumos", servicio.listarTodos());
            req.setAttribute("puedeEliminar", Boolean.valueOf(permisos(req).tiene(Permisos.INSUMOS_ELIMINAR)));
            vista(req, resp, "insumo/filas.jsp");

        } else if ("Guardar".equals(tipo) || "Modificar".equals(tipo) || "Borrar".equals(tipo)) {
            try {
                if ("Guardar".equals(tipo)) {
                    servicio.guardar(desdeFormulario(req, false));
                    exito(resp, "Insumo guardado con éxito");
                } else if ("Modificar".equals(tipo)) {
                    servicio.modificar(desdeFormulario(req, true));
                    exito(resp, "Insumo modificado con éxito");
                } else {
                    servicio.eliminar(entero(req.getParameter("pk")));
                    exito(resp, "Insumo eliminado con éxito");
                }
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("imprimir".equals(tipo)) {
            imprimirListado(req, resp, "insumos_listado", "Listado de Insumos", "insumos", "div");
        }
    }

    /** Precio vacío = 0 (como antes); primero precio, luego unidad y pk (mismo error si un número es inválido). */
    private static Insumo desdeFormulario(HttpServletRequest req, boolean conId) {
        String precio = req.getParameter("precio_compra");
        BigDecimal precioCompra = new BigDecimal(precio != null && !precio.isEmpty() ? precio : "0");
        Integer unidad = enteroOpcional(req.getParameter("id_unidad_medida"));
        int id = conId ? entero(req.getParameter("pk")) : 0;
        return new Insumo(id, req.getParameter("codigo"), req.getParameter("descripcion"), precioCompra, unidad,
                Boolean.parseBoolean(req.getParameter("activo")));
    }
}
