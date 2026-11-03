<%-- Combo de insumos activos con su precio (valor "id,precio"). Atributo "insumos". --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.InsumoPrecio,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<InsumoPrecio> insumos = (List<InsumoPrecio>) request.getAttribute("insumos");
    for (InsumoPrecio i : insumos) {
%><option value='<%= i.id %>,<%= i.precio == null ? "0" : i.precio.toPlainString() %>'><%= Html.esc(i.descripcion) %></option><%
    }
%>
