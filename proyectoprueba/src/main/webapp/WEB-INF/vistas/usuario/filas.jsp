<%-- Filas de la tabla de Usuarios (atributo "usuarios"). Vista de UsuarioServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Usuario,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (Usuario u : usuarios) {
        int idRol = u.getIdRol() == null ? 0 : u.getIdRol().intValue();
        String centro = u.getCentroAsignado() == null || u.getCentroAsignado().intValue() == 0 ? "null" : u.getCentroAsignado().toString();
%><tr><td><%= u.getId() %></td><td><%= Html.esc(u.getNombre()) %></td><td><%= Html.esc(u.getApellido()) %></td><td><%= Html.esc(u.getEmail()) %></td><td><%= Html.esc(u.getRolNombre()) %></td><td><%= u.isActivo() ? "Sí" : "No" %></td><td><%= Html.esc(u.getCentroNombre()) %></td><td><button type='button' class='btn btn-info' onclick='imprimirUsuario(<%= u.getId() %>)' title='Imprimir usuario'><i class='fa fa-print'></i></button> <button type='button' class='btn btn-warning' onclick="abrirModalEditarUsuario('<%= u.getId() %>','<%= Html.js(u.getNombre()) %>','<%= Html.js(u.getApellido()) %>','<%= Html.js(u.getEmail()) %>','<%= idRol %>',<%= u.isActivo() %>,<%= centro %>)"><i class='fa fa-edit'></i></button> <button type='button' class='btn btn-info' title='Centros adicionales e historial de accesos' onclick="abrirCentrosUsuario('<%= u.getId() %>','<%= Html.js(u.getNombre() + " " + u.getApellido()) %>')"><i class='fa fa-building'></i></button> <button type='button' class='btn btn-danger' onclick="abrirModalEliminarUsuario('<%= u.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=usuarios&amp;id=<%= u.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
