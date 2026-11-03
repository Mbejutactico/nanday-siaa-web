<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%
    // Cerrar sesión SOLO por POST (formulario #frmLogout de header.jsp):
    // así un link o imagen de otro sitio no puede cerrar la sesión (CSRF).
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        // Un GET (marcador, URL escrita a mano) no cierra nada: se vuelve al sistema.
        response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
        return;
    }
    HttpSession sesion = request.getSession(false);
    if (sesion != null && sesion.getAttribute("id_usuario") != null) {
        // Bitácora: cierre de sesión (si falla, igual se cierra la sesión)
        try {
            com.mycompany.proyectoprueba.seguridad.Bitacora.registrar(request,
                    com.mycompany.proyectoprueba.util.Conexion.obtener(application, request),
                    "SEGURIDAD", "LOGOUT", "usuarios", (Integer) sesion.getAttribute("id_usuario"), "Cierre de sesión");
        } catch (Exception sinBitacora) {
            // no impide cerrar la sesión
        }
    }
    if (sesion != null) {
        try {
            sesion.invalidate();
        } catch (IllegalStateException yaInvalidada) {
            // la sesión ya había expirado: no hay nada que cerrar
        }
    }
    response.sendRedirect(request.getContextPath() + "/index.jsp");
%>
