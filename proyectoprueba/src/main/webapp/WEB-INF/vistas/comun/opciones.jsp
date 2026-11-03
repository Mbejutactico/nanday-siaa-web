<%-- Combo genérico: atributos "primera" (texto de la opción vacía; null = sin opción vacía) y "opciones" (List<Opcion>). --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.Opcion,com.mycompany.proyectoprueba.util.Html"%><%
    String primera = (String) request.getAttribute("primera");
    if (primera != null) {
%><option value=''><%= primera %></option><%
    }
    @SuppressWarnings("unchecked")
    List<Opcion> opciones = (List<Opcion>) request.getAttribute("opciones");
    for (Opcion o : opciones) {
%><option value='<%= o.getId() %>'><%= Html.esc(o.getTexto()) %></option><%
    }
%>
