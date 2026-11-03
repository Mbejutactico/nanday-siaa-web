<%-- Filas de la tabla de Centros de costo (atributo "centrosCosto"). Vista de CentroCostoServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.CentroCosto,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<CentroCosto> lista = (List<CentroCosto>) request.getAttribute("centrosCosto");
    // Botón Historial (bitácora): solo el administrador
    com.mycompany.proyectoprueba.seguridad.Permisos permisosFila = com.mycompany.proyectoprueba.seguridad.Permisos.de(request);
    boolean verHistorial = permisosFila != null && permisosFila.esAdministrador();
    for (CentroCosto cc : lista) {
        int idCentro = cc.getIdCentro() == null ? 0 : cc.getIdCentro().intValue();
%><tr><td><%= cc.getId() %></td><td><%= Html.esc(cc.getCodigo()) %></td><td><%= Html.esc(cc.getDescripcion()) %></td><td><%= Html.esc(cc.getCentroNombre()) %></td><td><%= cc.isActivo() ? "Sí" : "No" %></td><td><button class='btn btn-info btn-sm' onclick='imprimirCentroCosto(<%= cc.getId() %>)' title='Imprimir centro de costo'><i class='fa fa-print'></i></button> <button class='btn btn-warning btn-sm' onclick="abrirModalEditarCC('<%= cc.getId() %>','<%= Html.js(cc.getCodigo()) %>','<%= Html.js(cc.getDescripcion()) %>',<%= idCentro %>,<%= cc.isActivo() %>)"><i class='fa fa-edit'></i></button> <button class='btn btn-danger btn-sm' onclick="abrirModalEliminarCC('<%= cc.getId() %>')"><i class='fa fa-trash'></i></button> <% if (verHistorial) { %><a class='btn btn-secondary' href='bitacora.jsp?tabla=centros_costo&amp;id=<%= cc.getId() %>' title='Historial (bitácora)'><i class='fa fa-history'></i></a><% } %></td></tr><%
    }
%>
