<%-- Filas de la tabla de Insumos (atributos "insumos" y "puedeEliminar"). Vista de InsumoServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Insumo,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
    boolean puedeEliminar = Boolean.TRUE.equals(request.getAttribute("puedeEliminar"));
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (Insumo i : insumos) {
        int unidad = i.getIdUnidadMedida() == null ? 0 : i.getIdUnidadMedida().intValue();
%><tr><td><%= i.getId() %></td><td><%= Html.esc(i.getCodigo()) %></td><td><%= Html.esc(i.getDescripcion()) %></td><td><%= Html.esc(i.getUnidadMostrar()) %></td><td><%= i.getPrecioCompra() %></td><td><%= i.isActivo() ? "Sí" : "No" %></td><td><button class='btn btn-info' onclick='imprimirInsumo(<%= i.getId() %>)' title='Imprimir insumo'><i class='fa fa-print'></i></button> <button class='btn btn-warning' onclick="abrirModalEditarInsumo('<%= i.getId() %>','<%= Html.js(i.getCodigo()) %>','<%= Html.js(i.getDescripcion()) %>','<%= unidad %>','<%= i.getPrecioCompra() %>',<%= i.isActivo() %>)"><i class='fa fa-edit'></i></button> <% if (puedeEliminar) { %><button class='btn btn-danger' onclick="abrirModalEliminarInsumo('<%= i.getId() %>')"><i class='fa fa-trash'></i></button><% } %> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=insumos&amp;id=<%= i.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
