<%-- Combo de proveedores activos para la solicitud (valor "id,ruc"). Atributo "proveedores". --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Proveedor,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    for (Proveedor p : proveedores) {
%><option value='<%= p.getId() %>,<%= Html.esc(p.getRuc()) %>'><%= Html.esc(p.getRazonSocial()) %></option><%
    }
%>
