<%-- Combo de insumos activos con código y nombre en atributos data-. Atributo "insumos" (List<Insumo>). --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Insumo,com.mycompany.proyectoprueba.util.Html"%><option value=''>-- Seleccionar insumo --</option><%
    @SuppressWarnings("unchecked")
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
    for (Insumo i : insumos) {
%><option value='<%= i.getId() %>' data-codigo='<%= Html.esc(i.getCodigo()) %>' data-nombre='<%= Html.esc(i.getDescripcion()) %>'><%= Html.esc(i.getCodigo()) %> - <%= Html.esc(i.getDescripcion()) %></option><%
    }
%>
