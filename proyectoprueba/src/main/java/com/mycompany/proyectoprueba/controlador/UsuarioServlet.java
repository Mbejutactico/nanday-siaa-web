package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Usuario;
import com.mycompany.proyectoprueba.modelo.HistorialCentro;
import com.mycompany.proyectoprueba.modelo.UsuarioCentro;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.servicio.UsuarioServicio;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Usuarios. URL: UsuarioServlet
 * Operaciones (tipo): listar, Guardar, Modificar, Borrar, centrosUsuario, agregarCentroUsuario,
 * modificarCentroUsuario, quitarCentroUsuario, historialCentrosUsuario, imprimir.
 */
public class UsuarioServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        UsuarioServicio servicio = UsuarioServicio.con(conexion(req));
        try {
            if ("listar".equals(tipo)) {
                req.setAttribute("usuarios", servicio.listarTodos());
                vista(req, resp, "usuario/filas.jsp");

            } else if ("Guardar".equals(tipo)) {
                servicio.guardar(desdeFormulario(req, 0), req.getParameter("password"), idUsuario(req));
                exito(resp, "Usuario guardado con éxito");

            } else if ("Modificar".equals(tipo)) {
                Usuario u = desdeFormulario(req, entero(req.getParameter("pk")));
                servicio.modificar(u, req.getParameter("password"), idUsuario(req));
                exito(resp, "Usuario modificado con éxito");

            } else if ("Borrar".equals(tipo)) {
                servicio.eliminar(entero(req.getParameter("pk")), idUsuario(req));
                exito(resp, "Usuario eliminado con éxito");

            } else if ("centrosUsuario".equals(tipo)) {
                JSONArray lista = new JSONArray();
                for (UsuarioCentro c : servicio.listarCentros(entero(req.getParameter("pk")))) {
                    JSONObject o = new JSONObject();
                    o.put("id_centro", c.getIdCentro());
                    o.put("centro", c.getCentro() == null ? "" : c.getCentro());
                    o.put("desde", c.getDesde() == null ? "" : c.getDesde().toString());
                    o.put("hasta", c.getHasta() == null ? "" : c.getHasta().toString());
                    o.put("observacion", c.getObservacion() == null ? "" : c.getObservacion());
                    o.put("vigente", c.isVigente());
                    lista.put(o);
                }
                resp.setContentType("application/json; charset=UTF-8");
                escribir(resp, lista.toString());

            } else if ("agregarCentroUsuario".equals(tipo) || "modificarCentroUsuario".equals(tipo)) {
                String idCentro = req.getParameter("id_centro");
                if (idCentro == null || idCentro.trim().isEmpty()) {
                    falla(resp, "Seleccione el centro");
                    return;
                }
                if ("agregarCentroUsuario".equals(tipo)) {
                    servicio.agregarCentro(entero(req.getParameter("pk")), entero(idCentro.trim()), req.getParameter("desde"),
                            req.getParameter("hasta"), req.getParameter("observacion"), idUsuario(req));
                    exito(resp, "Centro adicional agregado con éxito");
                } else {
                    servicio.modificarCentro(entero(req.getParameter("pk")), entero(idCentro.trim()), req.getParameter("desde"),
                            req.getParameter("hasta"), req.getParameter("observacion"), idUsuario(req));
                    exito(resp, "Centro adicional modificado con éxito");
                }

            } else if ("quitarCentroUsuario".equals(tipo)) {
                servicio.quitarCentro(entero(req.getParameter("pk")), entero(req.getParameter("id_centro")), idUsuario(req));
                exito(resp, "Centro adicional quitado con éxito");

            } else if ("historialCentrosUsuario".equals(tipo)) {
                JSONArray lista = new JSONArray();
                for (HistorialCentro h : servicio.historialCentros(entero(req.getParameter("pk")))) {
                    JSONObject o = new JSONObject();
                    o.put("fecha", h.fechaRegistro == null ? "" : new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(h.fechaRegistro));
                    o.put("centro", h.centro == null ? "" : h.centro);
                    o.put("tipo", h.tipoAcceso);
                    o.put("accion", h.accion);
                    o.put("desde", h.desde == null ? "" : h.desde.toString());
                    o.put("hasta", h.hasta == null ? "" : h.hasta.toString());
                    o.put("observacion", h.observacion == null ? "" : h.observacion);
                    o.put("registra", h.registradoPor == null ? "Sistema" : h.registradoPor);
                    lista.put(o);
                }
                json(resp, lista);

            } else if ("imprimir".equals(tipo)) {
                imprimirListado(req, resp, "usuarioslista", "Listado de Usuarios", "usuarios_listado", "div", "Error al generar el reporte: ");
            }
        } catch (ReglaNegocioException e) {
            falla(resp, e.getMessage());
        }
    }

    private static Usuario desdeFormulario(HttpServletRequest req, int id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(req.getParameter("nombre"));
        u.setApellido(req.getParameter("apellido"));
        String email = req.getParameter("email");
        u.setEmail(email == null ? null : email.trim());
        u.setIdRol(enteroOpcional(req.getParameter("id_rol")));
        u.setActivo(Boolean.parseBoolean(req.getParameter("activo")));
        u.setCentroAsignado(enteroOpcional(req.getParameter("centro_asignado")));
        return u;
    }
}
