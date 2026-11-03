<%-- Filas de la tabla de Proveedores (atributo "proveedores"). Vista de ProveedorServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Proveedor,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (Proveedor p : proveedores) {
%><tr><td><%= p.getId() %></td><td><%= Html.esc(p.getRazonSocial()) %></td><td><%= Html.esc(p.getRuc()) %></td><td><%= Html.esc(p.getTelefono()) %></td><td><%= Html.esc(p.getEmail()) %></td><td><%= p.isActivo() ? "Sí" : "No" %></td><td><button type='button' class='btn btn-info' onclick='imprimirProveedor(<%= p.getId() %>)' title='Imprimir proveedor'><i class='fa fa-print'></i></button> <button type='button' class='btn btn-warning' onclick="abrirModalEditarProveedor('<%= p.getId() %>','<%= Html.js(p.getRazonSocial()) %>','<%= Html.js(p.getRuc()) %>','<%= Html.js(p.getDireccion()) %>','<%= Html.js(p.getTelefono()) %>','<%= Html.js(p.getEmail()) %>',<%= p.isActivo() %>)"><i class='fa fa-edit'></i></button> <button type='button' class='btn btn-danger' onclick="abrirModalEliminarProveedor('<%= p.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=proveedores&amp;id=<%= p.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
