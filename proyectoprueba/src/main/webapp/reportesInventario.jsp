<%@include file="header.jsp" %>
<%-- INVENTARIOS > Reportes: consumo por centro de costo, insumos más usados y valorización del stock --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-chart-bar"></i> Reportes de Inventario</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card"><div class="card-body">
            <div class="form-row align-items-end mb-2">
                <div class="col-md-3 form-group"><label>Desde</label><input type="date" class="form-control" id="desde"></div>
                <div class="col-md-3 form-group"><label>Hasta</label><input type="date" class="form-control" id="hasta"></div>
                <div class="col-md-2 form-group"><button class="btn btn-primary btn-block" id="btnActualizar"><i class="fas fa-sync"></i> Actualizar</button></div>
                <div class="col-md-4 form-group"><small class="text-muted">El período aplica a consumo e insumos más usados. Consumo = insumos entregados en OT + ajustes de consumo directo.</small></div>
            </div>
            <ul class="nav nav-tabs">
                <li class="nav-item"><a class="nav-link active" data-toggle="tab" href="#tabConsumo">Consumo por centro de costo</a></li>
                <li class="nav-item"><a class="nav-link" data-toggle="tab" href="#tabUsados">Insumos más usados</a></li>
                <li class="nav-item"><a class="nav-link" data-toggle="tab" href="#tabValor">Valorización del stock</a></li>
            </ul>
            <div class="tab-content pt-3">
                <div class="tab-pane fade show active" id="tabConsumo"><div id="repConsumo"></div></div>
                <div class="tab-pane fade" id="tabUsados"><div id="repUsados"></div></div>
                <div class="tab-pane fade" id="tabValor"><div id="repValor"></div></div>
            </div>
        </div></div>
    </div></div>
</div>
<%@include file="footer.jsp" %>
<script>
var URL_INV = "InventarioServlet";
function formatoGs(v) { var n = Math.round(parseFloat(v)); return isNaN(n) ? '0' : n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'); }
function tablaHtml(cols, filas, pie) {
    if (!filas.length) return '<p class="text-muted">Sin datos para el período seleccionado.</p>';
    var h = '<table class="table table-sm table-bordered table-striped"><thead class="thead-light"><tr>';
    $.each(cols, function (i, c) { h += '<th' + (c.num ? ' class="text-right"' : '') + '>' + c.t + '</th>'; });
    h += '</tr></thead><tbody>';
    $.each(filas, function (i, f) { h += '<tr>'; $.each(f, function (j, v) { h += '<td' + (cols[j].num ? ' class="text-right"' : '') + '>' + v + '</td>'; }); h += '</tr>'; });
    return h + '</tbody>' + (pie ? '<tfoot><tr class="font-weight-bold">' + pie + '</tr></tfoot>' : '') + '</table>';
}
$(document).ready(function () { actualizar(); $('#btnActualizar').click(actualizar); });
function actualizar() {
    var p = { desde: $('#desde').val(), hasta: $('#hasta').val() };
    $.post(URL_INV, $.extend({ tipo: 'reporteConsumo' }, p), function (l) {
        var tv = 0, tu = 0;
        var filas = $.map(l, function (x) { tv += parseFloat(x.valor); tu += x.unidades; return [[escHtml(x.centro), escHtml(x.centro_costo), escHtml(x.documentos), x.unidades, 'Gs. ' + formatoGs(x.valor)]]; });
        $('#repConsumo').html(tablaHtml([{ t: 'Centro' }, { t: 'Centro de costo' }, { t: 'Documentos', num: 1 }, { t: 'Unidades', num: 1 }, { t: 'Valor', num: 1 }], filas,
            '<td colspan="3">Total</td><td class="text-right">' + tu + '</td><td class="text-right">Gs. ' + formatoGs(tv) + '</td>'));
    }, 'json');
    $.post(URL_INV, $.extend({ tipo: 'reporteMasUsados' }, p), function (l) {
        var filas = $.map(l, function (x, i) { return [[i + 1, escHtml(x.codigo), escHtml(x.nombre), x.unidades + ' ' + escHtml(x.unidad), 'Gs. ' + formatoGs(x.valor)]]; });
        $('#repUsados').html(tablaHtml([{ t: '#' }, { t: 'Código' }, { t: 'Insumo' }, { t: 'Cantidad usada', num: 1 }, { t: 'Valor', num: 1 }], filas));
    }, 'json');
    $.post(URL_INV, { tipo: 'reporteValorizacion' }, function (l) {
        var tv = 0, tu = 0;
        var filas = $.map(l, function (x) { tv += parseFloat(x.valor); tu += x.unidades; return [[escHtml(x.centro), escHtml(x.almacen), x.insumos, x.unidades, 'Gs. ' + formatoGs(x.valor)]]; });
        $('#repValor').html(tablaHtml([{ t: 'Centro' }, { t: 'Almacén' }, { t: 'Insumos con stock', num: 1 }, { t: 'Unidades', num: 1 }, { t: 'Valor (precio de compra)', num: 1 }], filas,
            '<td colspan="3">Total</td><td class="text-right">' + tu + '</td><td class="text-right">Gs. ' + formatoGs(tv) + '</td>'));
    }, 'json');
}
</script>
</body>
</html>
