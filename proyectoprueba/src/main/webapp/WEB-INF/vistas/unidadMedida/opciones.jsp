<%-- Opciones del combo de unidades activas (atributo "unidades"). Vista de UnidadMedidaServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.UnidadMedida,com.mycompany.proyectoprueba.util.Html"%><option value=''>Seleccione una unidad</option><%
    @SuppressWarnings("unchecked")
    List<UnidadMedida> unidades = (List<UnidadMedida>) request.getAttribute("unidades");
    for (UnidadMedida u : unidades) {
%><option value='<%= u.getId() %>'><%= Html.esc(u.getNombre()) %> (<%= Html.esc(u.getAbreviatura()) %>)</option><%
    }
%>
