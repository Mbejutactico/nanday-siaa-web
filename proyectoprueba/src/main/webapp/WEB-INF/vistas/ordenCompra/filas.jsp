<%-- Filas del listado de OC (atributos "ordenes" y "puedeModificar"). Vista de OrdenCompraServlet. --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.OrdenCompra,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<OrdenCompra> ordenes = (List<OrdenCompra>) request.getAttribute("ordenes");
    boolean puedeModificar = Boolean.TRUE.equals(request.getAttribute("puedeModificar"));
    for (OrdenCompra o : ordenes) {
        String estado = o.getEstado();
        int id = o.getId();
%><tr data-id='<%= id %>' style='cursor:pointer;'><td><span class='fila-oc' data-id='<%= id %>'></span><strong><%= Html.esc(o.getNumero()) %></strong></td><td><%= Html.esc(o.getFecha()) %></td><td><%= Html.esc(o.getProveedor()) %></td><td><%= Html.esc(o.getCentro()) %></td><td><%= Html.esc(o.getSolicitante()) %></td><td><span class='badge <%= OrdenCompra.colorEstado(estado) %>'><%= Html.esc(estado) %></span></td><td><%= o.getTotal().toPlainString() %></td><td class='text-nowrap'><button type='button' class='btn btn-secondary btn-sm' onclick='verFichaOC(<%= id %>)' title='Ver detalle'><i class='fa fa-eye'></i></button> <button type='button' class='btn btn-info btn-sm' onclick='imprimirOC(<%= id %>)' title='Imprimir OC'><i class='fa fa-print'></i></button> <%
        if (!"ANULADO".equals(estado) && puedeModificar) {
%><button type='button' class='btn btn-warning btn-sm' onclick='editarOC(<%= id %>)'><i class='fa fa-edit'></i></button> <%
        } else {
%><span class='text-muted'><i class='fa fa-edit'></i></span> <%
        }
        if (!"ANULADO".equals(estado) && !"RECIBIDO".equals(estado) && puedeModificar) {
%><button type='button' class='btn btn-danger btn-sm' onclick='anularOC(<%= id %>)'><i class='fa fa-ban'></i></button><%
        } else {
%><span class='text-muted'><i class='fa fa-ban'></i></span><%
        }
%></td></tr><%
    }
%>
