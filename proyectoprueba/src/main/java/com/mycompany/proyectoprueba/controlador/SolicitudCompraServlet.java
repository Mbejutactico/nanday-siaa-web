package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.BorradorCompra;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.servicio.CentroServicio;
import com.mycompany.proyectoprueba.servicio.ProveedorServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.servicio.SolicitudCompraServicio;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Html;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Solicitud de compra (borrador de cada usuario). URL: SolicitudCompraServlet
 * Operaciones (tipo): listarProveedores, listarCentros, listarInsumos, cargarCabecera, cargarDetalle,
 * agregarDetalle, agregarDetalleActivo, eliminarDetalle, guardar, cancelarSolicitud.
 */
public class SolicitudCompraServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer idUsuario = idUsuario(req);
        if (idUsuario == null) {
            escribir(resp, "error: Usuario no logueado");
            return;
        }
        int usuario = idUsuario.intValue();
        Connection conn = conexion(req);
        SolicitudCompraServicio servicio = SolicitudCompraServicio.con(conn);

        if ("listarProveedores".equals(tipo)) {
            req.setAttribute("proveedores", ProveedorServicio.con(conn).listarActivos());
            vista(req, resp, "solicitudCompra/proveedores.jsp");

        } else if ("listarCentros".equals(tipo)) {
            List<Opcion> centros = new ArrayList<Opcion>();
            for (com.mycompany.proyectoprueba.modelo.Centro c : CentroServicio.con(conn).listarActivos()) {
                centros.add(new Opcion(c.getId(), c.getNombre()));
            }
            opciones(req, resp, null, centros);

        } else if ("listarInsumos".equals(tipo)) {
            req.setAttribute("insumos", servicio.insumosActivos());
            vista(req, resp, "solicitudCompra/insumos.jsp");

        } else if ("cargarCabecera".equals(tipo)) {
            JSONObject cab = new JSONObject();
            BorradorCompra b = servicio.borrador(usuario);
            if (b != null) {
                cab.put("id_compra", b.idCompra);
                cab.put("id_proveedor", b.idProveedor);
                cab.put("ruc", b.ruc == null ? "" : b.ruc);
                cab.put("id_centro_destino", b.idCentroDestino);
                cab.put("observaciones", b.observaciones == null ? "" : b.observaciones);
            }
            json(resp, cab);

        } else if ("cargarDetalle".equals(tipo)) {
            int idCompra = servicio.idBorrador(usuario);
            if (idCompra == 0) {
                escribir(resp, "<tr><td colspan='7' class='text-center text-muted'>No hay artículos agregados.</td></tr>");
                return;
            }
            req.getSession().setAttribute("id_compra_actual", idCompra);
            req.setAttribute("detalle", servicio.detalle(idCompra));
            vista(req, resp, "solicitudCompra/detalle.jsp");

        } else if ("agregarDetalle".equals(tipo)) {
            agregarInsumo(servicio, usuario, req, resp);

        } else if ("agregarDetalleActivo".equals(tipo)) {
            agregarActivos(servicio, usuario, req, resp);

        } else if ("eliminarDetalle".equals(tipo)) {
            int idDetalle = Integer.parseInt(req.getParameter("id"));
            try {
                servicio.eliminarLinea(usuario, idDetalle);
                exito(resp, "Artículo eliminado.");
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            } catch (Exception e) {
                falla(resp, "Error al eliminar: " + mensaje(e));
            }

        } else if ("guardar".equals(tipo)) {
            try {
                servicio.emitir(usuario, req.getParameter("idProveedor"), req.getParameter("idCentroDestino"), req.getParameter("observaciones"));
                escribir(resp, "ok");
            } catch (Exception e) {
                escribir(resp, "error: " + Html.esc(mensaje(e)));
            }

        } else if ("cancelarSolicitud".equals(tipo)) {
            try {
                if (servicio.cancelar(usuario)) {
                    exito(resp, "Solicitud cancelada correctamente.");
                } else {
                    alerta(resp, "div", "info", "No hay solicitud en curso.");
                }
            } catch (Exception e) {
                falla(resp, "Error al cancelar: " + mensaje(e));
            }

        } else {
            falla(resp, "Operación no reconocida.");
        }
    }

    private void agregarInsumo(SolicitudCompraServicio servicio, int usuario, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String idProveedor = req.getParameter("idProveedor");
        String idCentro = req.getParameter("idCentroDestino");
        String codInsumo = req.getParameter("codInsumo");
        String cantidadStr = req.getParameter("cantidad");
        if (Comun.vacio(idProveedor) || Comun.vacio(idCentro) || Comun.vacio(codInsumo) || Comun.vacio(cantidadStr)) {
            falla(resp, "Faltan datos obligatorios.");
            return;
        }
        int proveedor = Integer.parseInt(idProveedor.trim());
        int centro = Integer.parseInt(idCentro.trim());
        int insumo = Integer.parseInt(codInsumo.trim());
        int cantidad = Integer.parseInt(cantidadStr.trim());
        BigDecimal precio;
        try {
            precio = servicio.validarInsumo(insumo, cantidad);
        } catch (ReglaNegocioException e) {
            falla(resp, e.getMessage());
            return;
        }
        try {
            servicio.agregarInsumo(usuario, proveedor, centro, req.getParameter("observaciones"), insumo, cantidad, precio);
            exito(resp, "Insumo agregado correctamente.");
        } catch (Exception e) {
            falla(resp, "Error al agregar insumo: " + mensaje(e));
        }
    }

    private void agregarActivos(SolicitudCompraServicio servicio, int usuario, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String idProveedor = req.getParameter("idProveedor");
        String idCentro = req.getParameter("idCentroDestino");
        String idInsumoBase = req.getParameter("codInsumo");   // insumo que describe el modelo del equipo
        String precioStr = req.getParameter("precio");
        String cantidadStr = req.getParameter("cantidad");
        if (Comun.vacio(idProveedor) || Comun.vacio(idCentro) || Comun.vacio(idInsumoBase) || Comun.vacio(precioStr) || Comun.vacio(cantidadStr)) {
            falla(resp, "Faltan datos obligatorios.");
            return;
        }
        int proveedor = Integer.parseInt(idProveedor.trim());
        int centro = Integer.parseInt(idCentro.trim());
        int insumoBase = Integer.parseInt(idInsumoBase.trim());
        BigDecimal precio = new BigDecimal(precioStr.trim());
        int cantidad = Integer.parseInt(cantidadStr.trim());
        try {
            servicio.validarActivos(cantidad, precio);
        } catch (ReglaNegocioException e) {
            falla(resp, e.getMessage());
            return;
        }
        try {
            servicio.agregarActivos(usuario, proveedor, centro, req.getParameter("observaciones"), insumoBase, cantidad, precio);
            exito(resp, "Activo(s) agregado(s) correctamente.");
        } catch (Exception e) {
            falla(resp, "Error al agregar activo: " + mensaje(e));
        }
    }

    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof NumberFormatException) {
            falla(resp, "Dato numérico inválido.");
        } else {
            falla(resp, "Error: " + mensaje(e));
        }
    }
}
