<%-- Opciones del combo de roles activos (atributo "roles"). Vista de RolServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Rol,com.mycompany.proyectoprueba.util.Html"%><option value=''>Seleccione</option><%
    @SuppressWarnings("unchecked")
    List<Rol> roles = (List<Rol>) request.getAttribute("roles");
    for (Rol r : roles) {
%><option value='<%= r.getId() %>'><%= Html.esc(r.getNombre()) %> (<%= Html.esc(r.getNombreTipo()) %>)</option><%
    }
%>
