<%-- Opciones del combo de centros activos (atributo "centros"). Vista de CentroServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Centro,com.mycompany.proyectoprueba.util.Html"%><option value=''>Seleccione</option><%
    @SuppressWarnings("unchecked")
    List<Centro> centros = (List<Centro>) request.getAttribute("centros");
    for (Centro c : centros) {
%><option value='<%= c.getId() %>'><%= Html.esc(c.getNombre()) %></option><%
    }
%>
