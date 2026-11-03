package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.util.Uploads;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sirve las fotos de perfil guardadas fuera del webapp en la misma URL de
 * siempre: uploads/perfiles/perfil_ID.jpg (las rutas ya guardadas en la base
 * siguen funcionando). Si la foto no esta en la carpeta externa, busca en la
 * ubicacion antigua dentro del proyecto desplegado.
 * Mapeado en WEB-INF/web.xml. El FiltroSeguridad exige sesion.
 */
public class FotoPerfilServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String info = req.getPathInfo();
        String nombre = (info == null) ? null : info.substring(1);
        if (!Uploads.nombreValido(nombre)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File archivo = new File(Uploads.carpetaPerfiles(getServletContext()), nombre);
        if (!archivo.isFile()) {
            String antigua = getServletContext().getRealPath("/uploads/perfiles/" + nombre);
            archivo = (antigua == null) ? null : new File(antigua);
        }
        if (archivo == null || !archivo.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setContentType(nombre.endsWith(".png") ? "image/png" : "image/jpeg");
        resp.setHeader("Cache-Control", "private, no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentLength((int) archivo.length());   // Servlet 3.0 (Tomcat 7); las fotos pesan 2 MB como máximo
        Files.copy(archivo.toPath(), resp.getOutputStream());
    }
}
