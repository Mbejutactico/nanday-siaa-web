<%-- Filas de la tabla de Almacenes (atributo "almacenes"). Vista de AlmacenServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Almacen,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Almacen> almacenes = (List<Almacen>) request.getAttribute("almacenes");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (Almacen a : almacenes) {
        int idCentro = a.getIdCentro() == null ? 0 : a.getIdCentro().intValue();
%><tr><td><%= a.getId() %></td><td><%= Html.esc(a.getNombre()) %></td><td><%= Html.esc(a.getDescripcion()) %></td><td><%= Html.esc(a.getUbicacion()) %></td><td><%= Html.esc(a.getCentroNombre()) %></td><td><%= a.isActivo() ? "Sí" : "No" %></td><td><button class='btn btn-info' onclick='imprimirAlmacen(<%= a.getId() %>)' title='Imprimir almacén'><i class='fa fa-print'></i></button> <button class='btn btn-warning' onclick="abrirModalEditarAlmacen('<%= a.getId() %>','<%= Html.js(a.getNombre()) %>','<%= Html.js(a.getDescripcion()) %>','<%= Html.js(a.getUbicacion()) %>',<%= idCentro %>,<%= a.isActivo() %>)"><i class='fa fa-edit'></i></button> <button class='btn btn-danger' onclick="abrirModalEliminarAlmacen('<%= a.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=almacenes&amp;id=<%= a.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
