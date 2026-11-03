<%-- Combo de insumos para suministrar a una OT: los que no tienen stock aparecen deshabilitados. Atributo "insumos" (List<InsumoDisponible>). --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.InsumoDisponible,com.mycompany.proyectoprueba.util.Html"%><option value=''>-- Seleccionar insumo --</option><%
    @SuppressWarnings("unchecked")
    List<InsumoDisponible> insumos = (List<InsumoDisponible>) request.getAttribute("insumos");
    for (InsumoDisponible i : insumos) {
        boolean sinStock = i.total <= 0;
%><option value='<%= i.id %>' data-codigo='<%= Html.esc(i.codigo) %>' data-nombre='<%= Html.esc(i.descripcion) %>'<%= sinStock ? " disabled" : "" %>><%= Html.esc(i.codigo) %> - <%= Html.esc(i.descripcion) %><%= sinStock ? " (sin stock)" : " (disponible: " + i.total + ")" %></option><%
    }
%>
