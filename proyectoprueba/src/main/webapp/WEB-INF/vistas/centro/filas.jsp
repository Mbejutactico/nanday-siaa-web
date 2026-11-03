<%-- Filas de la tabla de Centros (atributo "centros"). Vista de CentroServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Centro,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Centro> centros = (List<Centro>) request.getAttribute("centros");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (Centro c : centros) {
%><tr><td><%= c.getId() %></td><td><%= Html.esc(c.getCodigo()) %></td><td><%= Html.esc(c.getNombre()) %></td><td><%= Html.esc(c.getDireccion()) %></td><td><%= c.isActivo() ? "Sí" : "No" %></td><td><button type='button' class='btn btn-info' onclick='imprimirCentro(<%= c.getId() %>)' title='Imprimir centro'><i class='fa fa-print'></i></button> <button class='btn btn-warning' onclick="abrirModalEditarCentro('<%= c.getId() %>','<%= Html.js(c.getCodigo()) %>','<%= Html.js(c.getNombre()) %>','<%= Html.js(c.getDireccion()) %>',<%= c.isActivo() %>)"><i class='fa fa-edit'></i></button> <button class='btn btn-danger' onclick="abrirModalEliminarCentro('<%= c.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=centros&amp;id=<%= c.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
