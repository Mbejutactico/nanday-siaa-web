<%-- Opciones del combo de tipos de permisos (código, nombre y explicación). Vista de RolServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.Map,com.mycompany.proyectoprueba.modelo.TipoRol,com.mycompany.proyectoprueba.util.Html"%><option value=''>-- Seleccione el tipo de permisos --</option><%
    for (Map.Entry<String, String[]> t : TipoRol.todos().entrySet()) {
%><option value='<%= t.getKey() %>' data-ayuda='<%= Html.esc(t.getValue()[1]) %>'><%= Html.esc(t.getValue()[0]) %></option><%
    }
%>
