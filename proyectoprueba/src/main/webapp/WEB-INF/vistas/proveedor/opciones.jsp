<%-- Opciones del combo de proveedores activos (atributo "proveedores"). Vista de ProveedorServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Proveedor,com.mycompany.proyectoprueba.util.Html"%><option value=''>Seleccione</option><%
    @SuppressWarnings("unchecked")
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    for (Proveedor p : proveedores) {
%><option value='<%= p.getId() %>'><%= Html.esc(p.getRazonSocial()) %></option><%
    }
%>
