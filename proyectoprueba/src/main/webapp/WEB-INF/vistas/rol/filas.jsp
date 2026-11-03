<%-- Filas de la tabla de Roles (atributo "roles"). Vista de RolServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Rol,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Rol> roles = (List<Rol>) request.getAttribute("roles");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (Rol r : roles) {
        String badge = "ADMINISTRADOR".equals(r.getCodigo()) ? "badge-danger" : "badge-info";
%><tr><td><%= r.getId() %></td><td><%= Html.esc(r.getNombre()) %></td><td><span class='badge <%= badge %>'><%= Html.esc(r.getNombreTipo()) %></span></td><td><%= Html.esc(r.getDescripcion()) %></td><td><%= r.getUsuarios() %></td><td><%= r.isActivo() ? "Sí" : "No" %></td><td class='text-nowrap'><button class='btn btn-info btn-sm' onclick='imprimirRol(<%= r.getId() %>)' title='Imprimir rol'><i class='fa fa-print'></i></button> <button class='btn btn-warning btn-sm' title='Editar' onclick="abrirModalEditarRol('<%= r.getId() %>','<%= Html.js(r.getNombre()) %>','<%= Html.js(r.getDescripcion()) %>',<%= r.isActivo() %>,'<%= Html.js(r.getCodigo()) %>')"><i class='fa fa-edit'></i></button> <button class='btn btn-danger btn-sm' title='Eliminar' onclick="abrirModalEliminarRol('<%= r.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=roles&amp;id=<%= r.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
