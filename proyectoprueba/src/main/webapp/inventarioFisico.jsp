<%@include file="header.jsp" %>
<%-- INVENTARIOS > Inventario físico: se carga lo contado y el sistema genera el ajuste por las diferencias --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-clipboard-check"></i> Inventario Físico</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card"><div class="card-body">
            <div class="form-row">
                <div class="col-md-6 form-group"><label>Almacén a contar</label><select class="form-control" id="almacen"></select></div>
                <div class="col-md-6 form-group"><label>Observación</label><input class="form-control" id="observacion" maxlength="500" placeholder="Ej.: conteo mensual de septiembre"></div>
            </div>
            <p class="text-muted mb-2">Cargue la cantidad <b>contada</b> de cada insumo. Las filas que deje vacías no se ajustan. Al registrar, el sistema crea un ajuste
               (corrección por conteo) solo por las diferencias, y queda en el Kardex.</p>
            <table class="table table-sm table-bordered" id="tablaConteo">
                <thead class="thead-light"><tr><th>Código</th><th>Insumo</th><th>Unidad</th><th class="text-right">Stock del sistema</th><th style="width:150px">Contado</th><th class="text-right">Diferencia</th></tr></thead>
                <tbody><tr><td colspan="6" class="text-center text-muted">Seleccione un almacén</td></tr></tbody>
            </table>
            <div class="d-flex justify-content-between align-items-center">
                <span id="resumen" class="text-muted"></span>
                <button class="btn btn-success" id="btnRegistrar" disabled><i class="fas fa-check"></i> Registrar conteo</button>
            </div>
            <div id="mensaje" class="mt-3"></div>
        </div></div>
    </div></div>
</div>
<%@include file="footer.jsp" %>
<script>
var URL_INV = "InventarioServlet";
$(document).ready(function () {
    $.post(URL_INV, { tipo: 'listarAlmacenes' }, function (h) { $('#almacen').html(h); });
    $('#almacen').change(function () { $('#mensaje').html(''); cargar(); });
    $('#tablaConteo').on('input', '.contado', recalcular);
    $('#btnRegistrar').click(registrar);
});
function cargar() {
    var a = $('#almacen').val();
    if (!a) { $('#tablaConteo tbody').html('<tr><td colspan="6" class="text-center text-muted">Seleccione un almacén</td></tr>'); $('#btnRegistrar').prop('disabled', true); return; }
    $.post(URL_INV, { tipo: 'stockAlmacen', id_almacen: a }, function (lista) {
        if (!lista.length) { $('#tablaConteo tbody').html('<tr><td colspan="6" class="text-center text-muted">Este almacén no tiene insumos registrados</td></tr>'); $('#btnRegistrar').prop('disabled', true); return; }
        var h = '';
        $.each(lista, function (i, x) {
            h += '<tr data-id="' + parseInt(x.id_insumo, 10) + '" data-sistema="' + parseInt(x.stock, 10) + '"><td>' + escHtml(x.codigo) + '</td><td>' + escHtml(x.nombre) + '</td><td>' + escHtml(x.unidad) +
                 '</td><td class="text-right">' + x.stock + '</td><td><input type="number" min="0" class="form-control form-control-sm contado"></td><td class="text-right dif">-</td></tr>';
        });
        $('#tablaConteo tbody').html(h); $('#btnRegistrar').prop('disabled', false); recalcular();
    }, 'json');
}
function recalcular() {
    var cargados = 0, conDif = 0;
    $('#tablaConteo tbody tr[data-id]').each(function () {
        var v = $.trim($(this).find('.contado').val()), $d = $(this).find('.dif');
        if (v === '') { $d.html('-'); return; }
        cargados++;
        var dif = parseInt(v, 10) - parseInt($(this).data('sistema'), 10);
        if (dif !== 0) conDif++;
        $d.html(dif === 0 ? '<span class="text-muted">0</span>' : (dif > 0 ? '<span class="text-success">+' + dif + '</span>' : '<span class="text-danger">' + dif + '</span>'));
    });
    $('#resumen').text(cargados + ' insumo(s) contados, ' + conDif + ' con diferencia');
}
function registrar() {
    var items = [], invalido = false;
    $('#tablaConteo tbody tr[data-id]').each(function () {
        var v = $.trim($(this).find('.contado').val());
        if (v === '') return;
        if (!/^[0-9]+$/.test(v)) { invalido = true; return false; }
        items.push({ id_insumo: $(this).data('id'), contado: parseInt(v, 10) });
    });
    if (invalido) { alert('Las cantidades contadas deben ser números enteros mayores o iguales a 0.'); return; }
    if (!items.length) { alert('Cargue al menos una cantidad contada.'); return; }
    if (!confirmado('¿Registrar el conteo? Se ajustará el stock por las diferencias.', this, arguments)) return;
    var $b = $('#btnRegistrar').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'registrarConteo', id_almacen: $('#almacen').val(), observacion: $('#observacion').val(), items: JSON.stringify(items) },
        success: function (r) {
            $b.prop('disabled', false);
            if (!r.ok) { $('#mensaje').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); return; }
            $('#mensaje').html(r.sin_diferencias
                ? '<div class="alert alert-info">Conteo sin diferencias: el stock coincide, no se generó ajuste.</div>'
                : '<div class="alert alert-success">Conteo registrado el ' + escHtml(r.fecha) + '. Se generó el ajuste <b>' + escHtml(r.numero) + '</b> por ' + r.diferencias +
                  ' diferencia(s). <a href="ajustesInventario.jsp" class="alert-link">Ver ajustes</a></div>');
            cargar();
        }, error: function () { $b.prop('disabled', false); } });
}
</script>
</body>
</html>
