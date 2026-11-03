package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.servicio.PerfilServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.Uploads;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * Perfil del usuario de la sesión. URL: PerfilServlet
 * (acepta multipart; límites en web.xml). Operaciones (tipo): ActualizarDatos, ActualizarFoto, CambiarPassword.
 */
public class PerfilServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HttpSession sesion = req.getSession();
        if (sesion.getAttribute("logueado") == null || sesion.getAttribute("id_usuario") == null) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div class='alert alert-danger'>Sesión no válida. Vuelva a iniciar sesión.</div>");
            return;
        }
        int idUsuario = ((Integer) sesion.getAttribute("id_usuario")).intValue();
        PerfilServicio servicio = PerfilServicio.con(conexion(req));
        try {
            if ("ActualizarDatos".equals(tipo)) {
                String nombre = req.getParameter("nombre");
                String apellido = req.getParameter("apellido");
                servicio.actualizarDatos(idUsuario, nombre, apellido);
                // El encabezado refleja el cambio sin volver a iniciar sesión
                sesion.setAttribute("nombre", nombre.trim());
                sesion.setAttribute("apellido", apellido.trim());
                sesion.setAttribute("nombre_completo", nombre.trim() + " " + apellido.trim());
                exito(resp, "Datos actualizados con éxito");

            } else if ("ActualizarFoto".equals(tipo)) {
                final Part parte;
                try {
                    parte = req.getPart("foto");
                } catch (IllegalStateException muyGrande) {
                    // Tomcat rechaza el archivo antes de llegar aquí si supera el máximo de web.xml (2 MB)
                    falla(resp, "La imagen supera el tamaño máximo permitido (2MB)");
                    return;
                }
                if (parte == null || parte.getSize() == 0) {
                    falla(resp, "No se seleccionó ninguna imagen");
                    return;
                }
                String ruta = servicio.actualizarFoto(idUsuario, parte.getContentType(), parte.getSize(), parte::getInputStream,
                        Uploads.carpetaPerfiles(getServletContext()));
                sesion.setAttribute("foto_perfil", ruta);
                escribir(resp, ruta);   // el JS actualiza la imagen al instante

            } else if ("CambiarPassword".equals(tipo)) {
                servicio.cambiarPassword(idUsuario, req.getParameter("password_actual"), req.getParameter("password_nueva"),
                        req.getParameter("password_confirmar"));
                exito(resp, "Contraseña actualizada con éxito");

            } else if (tipo == null && req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/")) {
                // Si el archivo supera el máximo de web.xml, Tomcat descarta todo el formulario (incluido "tipo")
                falla(resp, "La imagen supera el tamaño máximo permitido (2MB)");
            } else {
                falla(resp, "Operación no reconocida");
            }
        } catch (ReglaNegocioException e) {
            alerta(resp, "div", "danger", Html.esc(e.getMessage()));
        }
    }
}
