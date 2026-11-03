<%-- Filas de la tabla de Unidades de medida (atributo "unidades"). Vista de UnidadMedidaServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.UnidadMedida,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<UnidadMedida> unidades = (List<UnidadMedida>) request.getAttribute("unidades");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (UnidadMedida u : unidades) {
%><tr><td><%= u.getId() %></td><td><%= Html.esc(u.getNombre()) %></td><td><%= Html.esc(u.getAbreviatura()) %></td><td><%= u.isActivo() ? "Sí" : "No" %></td><td><button class='btn btn-info' onclick='imprimirUnidad(<%= u.getId() %>)' title='Imprimir unidad'><i class='fa fa-print'></i></button> <button class='btn btn-warning' onclick="abrirModalEditarUnidad('<%= u.getId() %>','<%= Html.js(u.getNombre()) %>','<%= Html.js(u.getAbreviatura()) %>',<%= u.isActivo() %>)"><i class='fa fa-edit'></i></button> <button class='btn btn-danger' onclick="abrirModalEliminarUnidad('<%= u.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=unidad_medida&amp;id=<%= u.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
