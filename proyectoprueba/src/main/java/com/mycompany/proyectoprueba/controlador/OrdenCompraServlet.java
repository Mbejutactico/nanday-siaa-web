package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Centro;
import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.InsumoPrecio;
import com.mycompany.proyectoprueba.modelo.ItemRecepcion;
import com.mycompany.proyectoprueba.modelo.LineaRecepcion;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenCompra;
import com.mycompany.proyectoprueba.modelo.Proveedor;
import com.mycompany.proyectoprueba.modelo.RecepcionOC;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.servicio.CentroServicio;
import com.mycompany.proyectoprueba.servicio.OrdenCompraServicio;
import com.mycompany.proyectoprueba.servicio.ProveedorServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.servicio.SolicitudCompraServicio;
import com.mycompany.proyectoprueba.util.Html;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Órdenes de compra y su recepción. URL: OrdenCompraServlet
 * Operaciones (tipo): listar, cargarCombos, cargarInsumos, obtenerOC, fichaOC, anularRecepcion,
 * obtenerDetalle, actualizarOC, actualizarDetalleOC, eliminarDetalleOC, agregarDetalleOC, anularOC,
 * listarOCPendientes, listarCentrosCostoPorCentro, listarAlmacenesPorCentro, obtenerDetalleRecepcion,
 * procesarRecepcion.
 */
public class OrdenCompraServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final String SELECCIONAR = "<option value=''>-- Seleccionar --</option>";

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Connection conn = conexion(req);
        Permisos permisos = permisos(req);
        OrdenCompraServicio servicio = OrdenCompraServicio.con(conn, permisos);

        if ("listar".equals(tipo)) {
            req.setAttribute("ordenes", servicio.listar());
            req.setAttribute("puedeModificar", Boolean.valueOf(permisos.tiene(Permisos.OC_MODIFICAR)));
            vista(req, resp, "ordenCompra/filas.jsp");

        } else if ("cargarCombos".equals(tipo)) {
            StringBuilder prov = new StringBuilder(SELECCIONAR);
            for (Proveedor p : ProveedorServicio.con(conn).listarActivos()) {
                prov.append("<option value='").append(p.getId()).append("'>").append(Html.esc(p.getRazonSocial())).append("</option>");
            }
            StringBuilder cent = new StringBuilder(SELECCIONAR);
            for (Centro c : CentroServicio.con(conn).listarActivos()) {
                cent.append("<option value='").append(c.getId()).append("'>").append(Html.esc(c.getNombre())).append("</option>");
            }
            JSONObject combos = new JSONObject();
            combos.put("proveedores", prov.toString());
            combos.put("centros", cent.toString());
            escribir(resp, combos.toString());

        } else if ("cargarInsumos".equals(tipo)) {
            StringBuilder html = new StringBuilder(SELECCIONAR);
            for (InsumoPrecio i : SolicitudCompraServicio.con(conn).insumosActivos()) {
                html.append("<option value='").append(i.id).append("' data-precio='")
                    .append(i.precio == null ? "0" : i.precio.toPlainString()).append("'>")
                    .append(Html.esc(i.descripcion)).append("</option>");
            }
            escribir(resp, html.toString());

        } else if ("obtenerOC".equals(tipo)) {
            int idCompra = Integer.parseInt(req.getParameter("id"));
            JSONObject data = new JSONObject();
            OrdenCompra oc = servicio.cabecera(idCompra);   // solo OC de los centros del usuario
            if (oc == null) {
                data.put("error", "OC no encontrada");
                escribir(resp, data.toString());
                return;
            }
            data.put("id_compra", idCompra);
            data.put("id_proveedor", oc.getIdProveedor());
            data.put("id_centro_destino", oc.getIdCentroDestino());
            data.put("observaciones", oc.getObservaciones() == null ? "" : oc.getObservaciones());
            data.put("estado", oc.getEstado());
            data.put("detalle", lineasJson(servicio.lineasEditables(idCompra)));
            escribir(resp, data.toString());

        } else if ("fichaOC".equals(tipo)) {
            int idFicha = idValido(req.getParameter("id"));
            if (idFicha <= 0) {
                escribir(resp, new JSONObject().put("error", "Indique la orden de compra a consultar").toString());
            } else {
                ficha(servicio, idFicha, resp);
            }

        } else if ("anularRecepcion".equals(tipo)) {
            anularRecepcion(servicio, req, resp);

        } else if ("obtenerDetalle".equals(tipo)) {
            List<DetalleCompra> lineas = servicio.lineasEditablesSiVisible(Integer.parseInt(req.getParameter("id")));
            escribir(resp, lineas == null ? "[]" : lineasJson(lineas).toString());

        } else if ("actualizarOC".equals(tipo)) {
            int idCompra = Integer.parseInt(req.getParameter("id_compra"));
            String idProv = req.getParameter("id_proveedor");
            String idCent = req.getParameter("id_centro_destino");
            if (idProv == null || idProv.trim().isEmpty() || idCent == null || idCent.trim().isEmpty()) {
                falla(resp, "Seleccione proveedor y centro destino");
                return;
            }
            int idProveedor = Integer.parseInt(idProv.trim());
            int idCentro = Integer.parseInt(idCent.trim());
            try {
                servicio.actualizarCabecera(idCompra, idProveedor, idCentro, req.getParameter("observaciones"));
                exito(resp, "OC actualizada correctamente");
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
            }

        } else if ("actualizarDetalleOC".equals(tipo)) {
            int idDetalle = Integer.parseInt(req.getParameter("id_detalle"));
            int cantidad = Integer.parseInt(req.getParameter("cantidad").trim());
            BigDecimal precio = new BigDecimal(req.getParameter("precio").trim());
            try {
                servicio.actualizarLinea(idDetalle, cantidad, precio);
                escribir(resp, "ok");
            } catch (Exception e) {
                escribir(resp, "error: " + Html.esc(mensaje(e)));
            }

        } else if ("eliminarDetalleOC".equals(tipo)) {
            int idDetalle = Integer.parseInt(req.getParameter("id"));
            try {
                servicio.eliminarLinea(idDetalle);
                escribir(resp, "Artículo eliminado con éxito");
            } catch (Exception e) {
                escribir(resp, "error: " + Html.esc(mensaje(e)));
            }

        } else if ("agregarDetalleOC".equals(tipo)) {
            int idCompra = Integer.parseInt(req.getParameter("id_compra"));
            int idInsumo = Integer.parseInt(req.getParameter("id_insumo"));
            int cantidad = Integer.parseInt(req.getParameter("cantidad").trim());
            BigDecimal precio = new BigDecimal(req.getParameter("precio").trim());
            try {
                servicio.validarAgregado(idCompra, cantidad, precio);
            } catch (ReglaNegocioException e) {
                falla(resp, e.getMessage());
                return;
            }
            try {
                servicio.agregarInsumo(idCompra, idInsumo, cantidad, precio);
                escribir(resp, "Artículo agregado con éxito");
            } catch (Exception e) {
                falla(resp, "Error: " + mensaje(e));
            }

        } else if ("anularOC".equals(tipo)) {
            int idCompra = Integer.parseInt(req.getParameter("id"));
            try {
                servicio.validarAnulacion(idCompra);
            } catch (ReglaNegocioException e) {
                alerta(resp, "div", e.isAviso() ? "warning" : "danger", Html.esc(e.getMessage()));
                return;
            }
            try {
                servicio.anular(idCompra);
                exito(resp, "OC anulada correctamente");
            } catch (Exception e) {
                falla(resp, "Error al anular: " + mensaje(e));
            }

        } else if ("listarOCPendientes".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar --", servicio.pendientesDeRecibir());

        } else if ("listarCentrosCostoPorCentro".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar --", servicio.centrosCostoDelCentro(Integer.parseInt(req.getParameter("id_centro"))));

        } else if ("listarAlmacenesPorCentro".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar --", servicio.almacenesActivosDelCentro(Integer.parseInt(req.getParameter("id_centro"))));

        } else if ("obtenerDetalleRecepcion".equals(tipo)) {
            detalleRecepcion(servicio, Integer.parseInt(req.getParameter("id")), resp);

        } else if ("procesarRecepcion".equals(tipo)) {
            procesarRecepcion(servicio, req, resp);

        } else {
            escribir(resp, "error: Operación no reconocida");
        }
    }

    /** Líneas de la OC para la edición (JSON). */
    private static JSONArray lineasJson(List<DetalleCompra> lineas) {
        JSONArray detalle = new JSONArray();
        for (DetalleCompra d : lineas) {
            JSONObject item = new JSONObject();
            item.put("id_detalle", d.getId());
            item.put("tipo_item", d.getTipoItem());
            item.put("codigo", d.getCodigo());
            item.put("descripcion", d.getDescripcion());
            item.put("precio_unitario", d.getPrecioUnitario().toPlainString());
            item.put("cantidad", d.getCantidad());
            item.put("recibido", d.getCantidadRecibida());
            item.put("subtotal", d.getSubtotal().toPlainString());
            String serial = d.getSerialActivo();
            item.put("serial", (d.isEquipo() && serial != null && !serial.startsWith("PEND-")) ? serial : "");
            detalle.put(item);
        }
        return detalle;
    }

    /** Ficha de la OC: cabecera, líneas con lo recibido y recepciones (vigentes y anuladas). */
    private void ficha(OrdenCompraServicio servicio, int idCompra, HttpServletResponse resp) throws Exception {
        JSONObject data = new JSONObject();
        OrdenCompra oc = servicio.ficha(idCompra);
        if (oc == null) {
            data.put("error", "OC no encontrada");
            escribir(resp, data.toString());
            return;
        }
        if (!servicio.veCentro(oc.getIdCentroDestino())) {
            data.put("error", "Sin acceso a este centro");
            escribir(resp, data.toString());
            return;
        }
        data.put("numero_oc", nn(oc.getNumero()));
        data.put("fecha", oc.getFecha() == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(oc.getFecha()));
        data.put("estado", nn(oc.getEstado()));
        data.put("observaciones", nn(oc.getObservaciones()));
        data.put("centro", nn(oc.getCentro()));
        data.put("solicitante", nn(oc.getSolicitante()));
        data.put("comprador", nn(oc.getComprador()));
        data.put("proveedor", nn(oc.getProveedor()));
        data.put("ruc", nn(oc.getRuc()));
        data.put("telefono", nn(oc.getTelefono()));
        data.put("prov_email", nn(oc.getEmailProveedor()));
        data.put("direccion", nn(oc.getDireccionProveedor()));

        JSONArray lineas = new JSONArray();
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompra d : servicio.lineasFicha(idCompra)) {
            JSONObject ln = new JSONObject();
            ln.put("tipo", nn(d.getTipoItem()));
            ln.put("codigo", nn(d.getCodigo()));
            ln.put("descripcion", nn(d.getDescripcion()));
            ln.put("cantidad", d.getCantidad());
            ln.put("recibido", d.getCantidadRecibida());
            ln.put("precio", d.getPrecioUnitario() == null ? "0" : d.getPrecioUnitario().toPlainString());
            ln.put("subtotal", d.getSubtotal() == null ? "0" : d.getSubtotal().toPlainString());
            String serial = d.getSerialActivo();
            ln.put("serial", serial == null || serial.startsWith("PEND-") ? "" : serial);
            if (d.getSubtotal() != null) {
                total = total.add(d.getSubtotal());
            }
            lineas.put(ln);
        }
        data.put("lineas", lineas);
        data.put("total", total.toPlainString());

        boolean puedeAnular = servicio.puedeAnularRecepciones(data.getString("estado"));
        boolean faltaAlmacen = false;
        JSONArray recepciones = new JSONArray();
        SimpleDateFormat fh = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (RecepcionOC r : servicio.recepciones(idCompra)) {
            JSONObject x = new JSONObject();
            x.put("id", r.id);
            x.put("numero", r.numero);
            x.put("historica", r.historica);
            x.put("fecha", r.historica ? new SimpleDateFormat("dd/MM/yyyy").format(r.fecha) : fh.format(r.fecha));
            x.put("usuario", nn(r.usuario));
            x.put("anulada", r.anulada);
            x.put("fecha_anulacion", r.fechaAnulacion == null ? "" : fh.format(r.fechaAnulacion));
            x.put("anulado_por", nn(r.anuladoPor));
            x.put("motivo_anulacion", nn(r.motivoAnulacion));
            x.put("puede_anular", puedeAnular && !r.anulada);
            JSONArray ls = new JSONArray();
            for (LineaRecepcion l : r.lineas) {
                JSONObject y = new JSONObject();
                y.put("id", l.id);
                y.put("tipo", l.tipoItem);
                y.put("codigo", nn(l.codigo));
                y.put("descripcion", nn(l.descripcion));
                y.put("cantidad", l.cantidad);
                y.put("almacen", nn(l.almacen));
                y.put("centro_costo", l.ccCodigo == null ? "" : l.ccCodigo
                        + (l.ccDescripcion == null || l.ccDescripcion.trim().isEmpty() ? "" : " - " + l.ccDescripcion));
                y.put("numero_activo", l.numeroActivo == null || l.numeroActivo.startsWith("PEND-") ? "" : l.numeroActivo);
                boolean sinAlmacen = "INSUMO".equals(l.tipoItem) && l.almacen == null;
                y.put("sin_almacen", sinAlmacen);
                if (sinAlmacen && !r.anulada) {
                    faltaAlmacen = true;
                }
                ls.put(y);
            }
            x.put("lineas", ls);
            recepciones.put(x);
        }
        data.put("recepciones", recepciones);

        // Recepciones antiguas sin almacén registrado: al anularlas hay que indicar de qué almacén sale el stock
        if (faltaAlmacen && puedeAnular) {
            StringBuilder alm = new StringBuilder("<option value=''>-- Almacén --</option>");
            for (Opcion o : servicio.almacenesDelCentro(oc.getIdCentroDestino())) {
                alm.append("<option value='").append(o.getId()).append("'>").append(Html.esc(o.getTexto())).append("</option>");
            }
            data.put("almacenes", alm.toString());
        }
        escribir(resp, data.toString());
    }

    private void anularRecepcion(OrdenCompraServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idRecepcion = idValido(req.getParameter("id_recepcion"));
        if (idRecepcion <= 0) {
            falla(resp, "Indique la recepción que desea anular");
            return;
        }
        String motivo = req.getParameter("motivo") == null ? "" : req.getParameter("motivo").trim();
        String alm = req.getParameter("almacenes");
        JSONObject elegidos;
        try {
            elegidos = new JSONObject(alm == null || alm.trim().isEmpty() ? "{}" : alm);
        } catch (org.json.JSONException e) {
            falla(resp, "Los almacenes elegidos no son válidos. Recargue la página e intente de nuevo.");
            return;
        }
        Map<String, Integer> almacenes = new HashMap<String, Integer>();
        for (Iterator<String> it = elegidos.keys(); it.hasNext();) {
            String k = it.next();
            almacenes.put(k, Integer.valueOf(elegidos.optInt(k, 0)));
        }
        try {
            OrdenCompraServicio.Anulacion a = servicio.anularRecepcion(idRecepcion, motivo, almacenes, idUsuario(req).intValue());
            escribir(resp, "<div class='alert alert-success'>Recepción " + Html.esc(a.numero) + " anulada. "
                    + (a.insumos > 0 ? "Se descontó del stock lo recibido de " + a.insumos + (a.insumos == 1 ? " insumo" : " insumos") + ". " : "")
                    + (a.activos > 0 ? a.activos + (a.activos == 1 ? " activo volvió" : " activos volvieron") + " a SOLICITADO. " : "")
                    + "La OC quedó en estado <strong>" + Html.esc(a.estadoOC) + "</strong>.</div>");
        } catch (IllegalArgumentException e) {
            falla(resp, mensaje(e));
        } catch (Exception e) {
            log("Error al anular la recepción " + idRecepcion, e);
            escribir(resp, "<div class='alert alert-danger'>No se pudo anular la recepción. Intente de nuevo.</div>");
        }
    }

    private void detalleRecepcion(OrdenCompraServicio servicio, int idCompra, HttpServletResponse resp) throws Exception {
        JSONObject data = new JSONObject();
        OrdenCompra oc = servicio.paraRecibir(idCompra);
        if (oc == null) {
            data.put("error", "OC no encontrada");
            escribir(resp, data.toString());
            return;
        }
        if (!servicio.veCentro(oc.getIdCentroDestino())) {
            data = new JSONObject();
            data.put("error", "La OC es de un centro al que no tiene acceso");
            escribir(resp, data.toString());
            return;
        }
        data.put("numero_oc", oc.getNumero());
        data.put("estado", oc.getEstado());
        data.put("id_centro_destino", oc.getIdCentroDestino());
        JSONArray detalle = new JSONArray();
        for (DetalleCompra d : servicio.lineasPendientes(idCompra)) {
            JSONObject item = new JSONObject();
            item.put("id_detalle", d.getId());
            item.put("tipo_item", d.getTipoItem());
            item.put("codigo", d.getCodigo());
            item.put("descripcion", d.getDescripcion());
            item.put("cantidad", d.getCantidad());
            item.put("recibido", d.getCantidadRecibida());
            item.put("pendiente", d.getPendiente());
            detalle.put(item);
        }
        data.put("detalle", detalle);
        escribir(resp, data.toString());
    }

    /** datos = [{id_detalle, cantidad, id_almacen, id_centro_costo, serie}]; cantidad 0 = no se recibe ahora. */
    private void procesarRecepcion(OrdenCompraServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idCompra = Integer.parseInt(req.getParameter("id_compra"));
        JSONArray datos = new JSONArray(req.getParameter("datos"));
        List<ItemRecepcion> items = new ArrayList<ItemRecepcion>();
        for (int i = 0; i < datos.length(); i++) {
            ItemRecepcion it = new ItemRecepcion();
            try {
                JSONObject obj = datos.getJSONObject(i);
                it.idDetalle = obj.getInt("id_detalle");
                it.cantidad = obj.getInt("cantidad");
                it.idCentroCosto = obj.optInt("id_centro_costo", 0);
                it.idAlmacen = obj.optInt("id_almacen", 0);
                it.serie = obj.optString("serie", "");
            } catch (RuntimeException e) {
                it.errorLectura = e;   // se informa al llegar a esta línea, como antes
            }
            items.add(it);
        }
        try {
            OrdenCompraServicio.Recepcion r = servicio.recibir(idCompra, items, idUsuario(req).intValue());
            StringBuilder msg = new StringBuilder("<div class='alert alert-success'>Recepción " + Html.esc(r.numero)
                    + " procesada correctamente. Estado actual: " + Html.esc(r.estadoOC));
            if (!r.activosGenerados.isEmpty()) {
                msg.append("<br><strong>Números de activo generados:</strong><ul class='mb-0'>");
                for (String act : r.activosGenerados) {
                    msg.append("<li>").append(Html.esc(act)).append("</li>");
                }
                msg.append("</ul>");
            }
            msg.append("</div>");
            escribir(resp, msg.toString());
        } catch (IllegalArgumentException e) {
            falla(resp, mensaje(e));
        } catch (Exception e) {
            falla(resp, "Error al procesar la recepción: " + mensaje(e));
        }
    }

    private static String nn(String s) {
        return s == null ? "" : s;
    }

    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof NumberFormatException) {
            escribir(resp, "error: Dato numérico inválido");
        } else {
            escribir(resp, "error: " + Html.esc(mensaje(e)));
        }
    }
}
