<%-- Filas del carrito de la solicitud (insumos y activos). Atributo "detalle". --%><%@page trimDirectiveWhitespaces="true" import="java.util.List,com.mycompany.proyectoprueba.modelo.DetalleCompra,com.mycompany.proyectoprueba.util.Html"%><%
    @SuppressWarnings("unchecked")
    List<DetalleCompra> detalle = (List<DetalleCompra>) request.getAttribute("detalle");
    for (DetalleCompra d : detalle) {
%><tr><td><button type='button' class='btn btn-danger btn-sm' onclick='eliminarDetalle(<%= d.getId() %>)'><i class='fa fa-trash'></i></button></td><td><%= d.isEquipo() ? "<span class='badge badge-info'>Activo</span>" : "<span class='badge badge-secondary'>Insumo</span>" %></td><td><%= Html.esc(d.getCodigo()) %></td><td><%= Html.esc(d.getDescripcion()) %></td><td><%= d.getPrecioUnitario().toPlainString() %></td><td><%= d.getCantidad() %></td><td class='subtotal'><%= d.getSubtotal().toPlainString() %></td></tr><%
    }
    if (detalle.isEmpty()) {
%><tr><td colspan='7' class='text-center text-muted'>No hay artículos en el detalle.</td></tr><%
    }
%>
