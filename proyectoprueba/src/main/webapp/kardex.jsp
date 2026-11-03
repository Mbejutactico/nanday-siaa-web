<%@include file="header.jsp" %>
<%-- INVENTARIOS > Kardex: entradas, salidas y saldo de un insumo (impresión en JRXML) --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-book"></i> Kardex de Insumos</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card"><div class="card-body">
            <div class="form-row">
                <div class="col-md-4 form-group"><label>Insumo *</label><select class="form-control" id="insumo"></select></div>
                <div class="col-md-3 form-group"><label>Almacén</label><select class="form-control" id="almacen"></select></div>
                <div class="col-md-2 form-group"><label>Desde</label><input type="date" class="form-control" id="desde"></div>
                <div class="col-md-2 form-group"><label>Hasta</label><input type="date" class="form-control" id="hasta"></div>
                <div class="col-md-1 form-group" style="padding-top:32px"><button class="btn btn-primary btn-block" id="btnBuscar" title="Buscar"><i class="fas fa-search"></i></button></div>
            </div>
            <button class="btn btn-outline-secondary btn-sm mb-2" id="btnImprimir"><i class="fas fa-print"></i> Imprimir Kardex</button>
            <table class="table table-bordered table-striped table-sm" id="tablaK" style="width:100%">
                <thead><tr><th>Fecha</th><th>Movimiento</th><th>Documento</th><th>Almacén</th><th class="text-right">Entrada</th><th class="text-right">Salida</th><th class="text-right">Saldo</th><th>Usuario</th><th>Observación</th></tr></thead>
                <tbody></tbody>
            </table>
            <div id="totales" class="text-right font-weight-bold"></div>
        </div></div>
    </div></div>
</div>
<%@include file="footer.jsp" %>
<script>
var URL_INV = "InventarioServlet", tabla;
var TIPOS = { SALDO_INICIAL: 'Saldo inicial', ENTRADA_COMPRA: 'Entrada por compra', SALIDA_OT: 'Salida por OT', DEVOLUCION_OT: 'Devolución de OT', AJUSTE: 'Ajuste',
    ANULACION_AJUSTE: 'Anulación de ajuste', TRANSFERENCIA_SALIDA: 'Transferencia (sale)', TRANSFERENCIA_ENTRADA: 'Transferencia (entra)', ANULACION_TRANSFERENCIA: 'Anulación de transferencia', ANULACION_RECEPCION: 'Anulación de recepción de OC' };
$(document).ready(function () {
    tabla = $('#tablaK').DataTable({ responsive: true, lengthChange: false, autoWidth: false, pageLength: 50, ordering: false,
        language: { emptyTable: 'Seleccione un insumo y presione buscar' } });
    $.post(URL_INV, { tipo: 'listarInsumos' }, function (h) { $('#insumo').html(h); });
    $.post(URL_INV, { tipo: 'listarAlmacenes', todos: 'true' }, function (h) { $('#almacen').html(h); });
    $('#btnBuscar').click(buscar);
    $('#insumo, #almacen').change(function () { if ($('#insumo').val()) buscar(); });
    $('#btnImprimir').click(function () {
        if (!$('#insumo').val()) { alert('Seleccione el insumo.'); return; }
        window.open(URL_INV + '?' + $.param({ tipo: 'imprimirKardex', id_insumo: $('#insumo').val(), id_almacen: $('#almacen').val() || '', desde: $('#desde').val(), hasta: $('#hasta').val() }), '_blank');
    });
});
function buscar() {
    if (!$('#insumo').val()) { alert('Seleccione el insumo.'); return; }
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json',
        data: { tipo: 'kardex', id_insumo: $('#insumo').val(), id_almacen: $('#almacen').val(), desde: $('#desde').val(), hasta: $('#hasta').val() },
        success: function (lista) {
            tabla.clear(); var ent = 0, sal = 0;
            $.each(lista, function (i, m) {
                var e = m.cantidad > 0 ? m.cantidad : 0, s = m.cantidad < 0 ? -m.cantidad : 0; ent += e; sal += s;
                tabla.row.add([escHtml(m.fecha), escHtml(TIPOS[m.tipo] || m.tipo), escHtml(m.documento), escHtml(m.almacen),
                    e ? '<span class="text-success">' + e + '</span>' : '', s ? '<span class="text-danger">' + s + '</span>' : '', '<strong>' + m.saldo + '</strong>',
                    escHtml(m.usuario), escHtml(m.observacion)]);
            });
            tabla.draw();
            $('#totales').text(lista.length ? 'Movimientos: ' + lista.length + '   |   Entradas: ' + ent + '   |   Salidas: ' + sal : '');
        } });
}
</script>
</body>
</html>
