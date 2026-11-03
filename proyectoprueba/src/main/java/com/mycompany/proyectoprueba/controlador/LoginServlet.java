package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Usuario;
import com.mycompany.proyectoprueba.seguridad.Bitacora;
import com.mycompany.proyectoprueba.servicio.SesionServicio;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Inicio de sesión. URL: LoginServlet (tipo=login, usuario=email, pass).
 * Responde "true" si entra o un mensaje HTML de error.
 */
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("text/plain; charset=UTF-8");
        if (!"login".equals(tipo)) {
            return;
        }
        String email = req.getParameter("usuario");   // el campo "usuario" del formulario contiene el email
        String password = req.getParameter("pass");
        email = email == null ? "" : email.trim();
        if (email.isEmpty() || password == null || password.isEmpty()) {
            escribir(resp, "<div class=\"alert alert-danger\" role=\"alert\">Ingrese email y contraseña</div>");
            return;
        }
        SesionServicio.Resultado r = SesionServicio.con(conexion(req)).iniciar(email, password, req.getRemoteAddr());
        if (r.minutosBloqueo > 0) {
            Bitacora.inicioFallido(req, conexion(req), email, "Cuenta o IP bloqueada por intentos fallidos");
            escribir(resp, "<div class=\"alert alert-danger\" role=\"alert\">Demasiados intentos fallidos. Espere "
                    + r.minutosBloqueo + (r.minutosBloqueo == 1 ? " minuto" : " minutos") + " y vuelva a intentar.</div>");
            return;
        }
        if (r.usuario == null) {
            Bitacora.inicioFallido(req, conexion(req), email, "Email o contraseña incorrectos, o usuario inactivo");
            // Mismo mensaje para email inexistente y contraseña incorrecta: no se revela qué emails existen
            escribir(resp, "<div class=\"alert alert-danger\" role=\"alert\">Email o contraseña incorrectos, o usuario inactivo</div>");
            return;
        }
        // Nueva sesión tras el login (evita fijación de sesión)
        HttpSession anterior = req.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        Usuario u = r.usuario;
        HttpSession sesion = req.getSession(true);
        sesion.setAttribute("logueado", "1");
        sesion.setAttribute("id_usuario", u.getId());
        sesion.setAttribute("nombre_completo", u.getNombre() + " " + u.getApellido());
        sesion.setAttribute("email", u.getEmail());
        sesion.setAttribute("id_rol", u.getIdRol());
        sesion.setAttribute("nombre", u.getNombre());
        sesion.setAttribute("apellido", u.getApellido());
        sesion.setAttribute("foto_perfil", u.getFotoPerfil());
        Bitacora.registrar(req, conexion(req), "SEGURIDAD", "LOGIN", "usuarios", u.getId(), "Inicio de sesión");
        escribir(resp, "true");
    }

    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        log("Error en login", e);
        escribir(resp, "<div class=\"alert alert-danger\" role=\"alert\">Error al iniciar sesión. Intente de nuevo.</div>");
    }
}
