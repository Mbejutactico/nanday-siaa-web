<%@include file="header.jsp" %>
<%-- INVENTARIOS > Ajuste puntual: consumo sin OT, rotura/pérdida, vencimiento o corrección por
     conteo. Centro, centro de costo, almacén y motivo. Se anula con motivo (el stock se revierte). --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-sliders-h"></i> Ajuste Puntual de Inventario</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card">
            <div class="card-header"><button class="btn btn-success" id="btnNuevo"><i class="fas fa-plus"></i> Nuevo ajuste</button></div>
            <div class="card-body">
                <table class="table table-bordered table-striped" id="tablaAj" style="width:100%">
                    <thead><tr><th>N°</th><th>Fecha</th><th>Tipo</th><th>Centro / C. costo</th><th>Almacén</th><th>Motivo</th><th>Ítems</th><th>Estado</th><th>Acción</th></tr></thead>
                    <tbody></tbody>
                </table>
            </div>
        </div>
    </div></div>
</div>

<div class="modal fade" id="modalNuevo" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Nuevo ajuste puntual</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body">
        <div class="form-row">
            <div class="col-md-4 form-group"><label>Tipo de ajuste *</label>
                <select class="form-control" id="ajTipo">
                    <option value="CONSUMO">Consumo directo (sin OT)</option>
                    <option value="ROTURA_PERDIDA">Rotura / pérdida</option>
                    <option value="VENCIMIENTO">Vencimiento</option>
                    <option value="CORRECCION_CONTEO">Corrección por conteo (+/-)</option>
                </select></div>
            <div class="col-md-4 form-group"><label>Centro *</label><select class="form-control" id="ajCentro"></select></div>
            <div class="col-md-4 form-group"><label id="lblCC">Centro de costo *</label><select class="form-control" id="ajCC"><option value="">-- Elija el centro --</option></select></div>
            <div class="col-md-4 form-group"><label>Almacén *</label><select class="form-control" id="ajAlmacen"><option value="">-- Elija el centro --</option></select></div>
            <div class="col-md-8 form-group"><label>Motivo *</label><input type="text" class="form-control" id="ajMotivo" maxlength="500"></div>
        </div>
        <div class="form-row align-items-end">
            <div class="col-md-7 form-group"><label>Insumo</label><select class="form-control" id="ajInsumo"><option value="">-- Elija el almacén --</option></select></div>
            <div class="col-md-3 form-group"><label>Cantidad</label><input type="number" class="form-control" id="ajCantidad" value="1"><small class="text-muted" id="ayudaCantidad">Sale del stock</small></div>
            <div class="col-md-2 form-group"><button class="btn btn-outline-primary btn-block" id="btnAgregar"><i class="fas fa-plus"></i></button></div>
        </div>
        <table class="table table-sm table-bordered"><thead class="thead-light"><tr><th>Código</th><th>Nombre</th><th>Disponible</th><th>Cantidad</th><th></th></tr></thead><tbody id="cuerpoLineas"></tbody></table>
        <div id="mensajeNuevo"></div>
    </div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cancelar</button><button class="btn btn-success" id="btnGuardar"><i class="fas fa-save"></i> Registrar ajuste</button></div>
</div></div></div>

<div class="modal fade" id="modalDetalle" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Ajuste <span id="detNumero"></span></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body" id="detCuerpo"></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button></div>
</div></div></div>

<div class="modal fade" id="modalAnular" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Anular ajuste <span id="anuNumero"></span></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><input type="hidden" id="anuId"><p class="text-muted">El stock se revierte y queda registrado en el Kardex.</p>
        <div class="form-group"><label>Motivo *</label><textarea class="form-control" id="anuMotivo" rows="3" maxlength="2000"></textarea></div><div id="mensajeAnular"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cancelar</button><button class="btn btn-danger" id="btnAnular"><i class="fas fa-ban"></i> Anular</button></div>
</div></div></div>

<%@include file="footer.jsp" %>
<script>
var URL_INV = "InventarioServlet";
var tabla, lineas = [], stock = {}, ajustes = {};
var TIPOS = { CONSUMO: 'Consumo directo', ROTURA_PERDIDA: 'Rotura / pérdida', VENCIMIENTO: 'Vencimiento', CORRECCION_CONTEO: 'Corrección por conteo' };
function formatoGs(v) { var n = Math.round(parseFloat(v)); return isNaN(n) ? '' : n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'); }

$(document).ready(function () {
    tabla = $('#tablaAj').DataTable({ responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [], columnDefs: [{ orderable: false, targets: -1 }] });
    cargar();
    $.post(URL_INV, { tipo: 'listarCentros' }, function (h) { $('#ajCentro').html(h); });
    $('#btnNuevo').click(function () {
        lineas = []; dibujarLineas(); $('#mensajeNuevo').html(''); $('#ajMotivo').val(''); $('#btnGuardar').prop('disabled', false);
        $('#modalNuevo').modal('show');
    });
    $('#ajTipo').change(function () {
        var conteo = $(this).val() === 'CORRECCION_CONTEO';
        $('#lblCC').text(conteo ? 'Centro de costo (opcional)' : 'Centro de costo *');
        $('#ayudaCantidad').text(conteo ? 'Positiva suma, negativa resta' : 'Sale del stock');
        lineas = []; dibujarLineas();
    });
    $('#ajCentro').change(function () {
        var c = $(this).val();
        $.post(URL_INV, { tipo: 'listarCentrosCosto', id_centro: c }, function (h) { $('#ajCC').html(h); });
        $.post(URL_INV, { tipo: 'listarAlmacenes', id_centro: c || '-1' }, function (h) { $('#ajAlmacen').html(h); });
        lineas = []; dibujarLineas();
    });
    $('#ajAlmacen').change(cargarInsumosAlmacen);
    $('#btnAgregar').click(agregar);
    $('#btnGuardar').click(guardar);
    $('#btnAnular').click(anular);
});

function cargarInsumosAlmacen() {
    var a = $('#ajAlmacen').val(); stock = {}; lineas = []; dibujarLineas();
    if (!a) { $('#ajInsumo').html('<option value="">-- Elija el almacén --</option>'); return; }
    // Con corrección por conteo se puede sumar cualquier insumo; en los demás, solo los que tienen stock
    $.when($.post(URL_INV, { tipo: 'stockAlmacen', id_almacen: a }, null, 'json'), $.post(URL_INV, { tipo: 'listarInsumos' })).done(function (s, h) {
        $.each(s[0], function (i, x) { stock[x.id_insumo] = x.stock; });
        var $tmp = $('<select>').html(h[0]), conteo = $('#ajTipo').val() === 'CORRECCION_CONTEO';
        $tmp.find('option').each(function () {
            var id = $(this).val(); if (!id) return;
            var disp = stock[id] || 0;
            if (!conteo && disp <= 0) { $(this).remove(); return; }
            $(this).text($(this).text() + ' (disponible: ' + disp + ')');
        });
        $('#ajInsumo').html($tmp.html());
    });
}

function agregar() {
    var $o = $('#ajInsumo option:selected'), id = parseInt($('#ajInsumo').val(), 10), c = parseInt($('#ajCantidad').val(), 10);
    var conteo = $('#ajTipo').val() === 'CORRECCION_CONTEO';
    if (!id) { alert('Seleccione el insumo.'); return; }
    if (isNaN(c) || c === 0 || (!conteo && c < 0)) { alert(conteo ? 'La cantidad debe ser distinta de 0 (negativa resta, positiva suma).' : 'La cantidad debe ser mayor a 0.'); return; }
    var usado = 0; $.each(lineas, function (i, l) { if (l.id_insumo === id) usado += l.cantidad; });
    var salida = conteo ? -(usado + c) : (usado + c);
    if (salida > (stock[id] || 0)) { alert('La cantidad supera el stock disponible (' + (stock[id] || 0) + ').'); return; }
    lineas.push({ id_insumo: id, codigo: $o.data('codigo'), nombre: $o.data('nombre'), cantidad: c });
    $('#ajCantidad').val(1); dibujarLineas();
}
function quitarLinea(i) { lineas.splice(i, 1); dibujarLineas(); }
function dibujarLineas() {
    if (!lineas.length) { $('#cuerpoLineas').html('<tr><td colspan="5" class="text-center text-muted">Agregue insumos</td></tr>'); return; }
    var h = ''; $.each(lineas, function (i, l) {
        h += '<tr><td>' + escHtml(l.codigo) + '</td><td>' + escHtml(l.nombre) + '</td><td>' + (stock[l.id_insumo] || 0) + '</td><td>' + l.cantidad + '</td><td><button class="btn btn-outline-danger btn-sm" onclick="quitarLinea(' + i + ')"><i class="fas fa-times"></i></button></td></tr>';
    }); $('#cuerpoLineas').html(h);
}

function guardar() {
    var d = { tipo: 'crearAjuste', tipo_ajuste: $('#ajTipo').val(), id_centro: $('#ajCentro').val(), id_centro_costo: $('#ajCC').val(), id_almacen: $('#ajAlmacen').val(),
              motivo: $.trim($('#ajMotivo').val()), items: JSON.stringify($.map(lineas, function (l) { return { id_insumo: l.id_insumo, cantidad: l.cantidad }; })) };
    if (!d.id_centro || !d.id_almacen || !d.motivo || (!d.id_centro_costo && d.tipo_ajuste !== 'CORRECCION_CONTEO')) { alert('Complete centro, centro de costo, almacén y motivo.'); return; }
    if (!lineas.length) { alert('Agregue al menos un insumo.'); return; }
    var $b = $('#btnGuardar').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: d, success: function (r) {
        if (r.ok) { $('#modalNuevo').modal('hide'); alert('Ajuste ' + r.numero + ' registrado el ' + r.fecha + '.'); cargar(); }
        else { $('#mensajeNuevo').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
    }, error: function () { $b.prop('disabled', false); } });
}

function cargar() {
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'listarAjustes' }, success: function (lista) {
        tabla.clear(); ajustes = {};
        $.each(lista, function (i, a) {
            ajustes[a.id] = a; var id = parseInt(a.id, 10);
            var acc = '<button class="btn btn-info btn-sm" onclick="verDetalle(' + id + ')"><i class="fas fa-eye"></i></button> ';
            if (!a.anulado) acc += '<button class="btn btn-outline-danger btn-sm" onclick="abrirAnular(' + id + ')"><i class="fas fa-ban"></i></button>';
            tabla.row.add(['<strong>' + escHtml(a.numero) + '</strong>' + (a.origen === 'INVENTARIO_FISICO' ? '<br><small class="text-muted">inventario físico</small>' : ''),
                escHtml(a.fecha), escHtml(TIPOS[a.tipo] || a.tipo), escHtml(a.centro) + '<br><small>' + escHtml(a.centro_costo) + '</small>', escHtml(a.almacen), escHtml(a.motivo), escHtml(a.items),
                a.anulado ? '<span class="badge badge-danger" title="' + escHtml(a.motivo_anulacion) + '">Anulado ' + escHtml(a.fecha_anulacion) + '</span>' : '<span class="badge badge-success">Vigente</span>', acc]);
        }); tabla.draw();
    } });
}

function verDetalle(id) {
    var a = ajustes[id];
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'detalleAjuste', id: id }, success: function (lista) {
        var h = '<p><b>' + escHtml(TIPOS[a.tipo] || a.tipo) + '</b> - ' + escHtml(a.fecha) + ' por ' + escHtml(a.usuario) + '<br>Centro: ' + escHtml(a.centro) + ' | Centro de costo: ' + escHtml(a.centro_costo || '-') +
            ' | Almacén: ' + escHtml(a.almacen) + '<br>Motivo: ' + escHtml(a.motivo) + '</p>' +
            (a.anulado ? '<div class="alert alert-danger">Anulado el ' + escHtml(a.fecha_anulacion) + ': ' + escHtml(a.motivo_anulacion) + '</div>' : '') +
            '<table class="table table-sm table-bordered"><thead><tr><th>Código</th><th>Insumo</th><th>Movimiento</th><th>Valor (Gs.)</th></tr></thead><tbody>';
        $.each(lista, function (i, l) {
            h += '<tr><td>' + escHtml(l.codigo) + '</td><td>' + escHtml(l.nombre) + '</td><td>' + (l.cantidad > 0 ? '<span class="text-success">+' : '<span class="text-danger">') + l.cantidad + '</span> ' + escHtml(l.unidad) +
                 '</td><td>' + formatoGs(l.cantidad * parseFloat(l.costo)) + '</td></tr>';
        });
        $('#detNumero').text(a.numero); $('#detCuerpo').html(h + '</tbody></table>'); $('#modalDetalle').modal('show');
    } });
}

function abrirAnular(id) { $('#anuId').val(id); $('#anuNumero').text(ajustes[id].numero); $('#anuMotivo').val(''); $('#mensajeAnular').html(''); $('#btnAnular').prop('disabled', false); $('#modalAnular').modal('show'); }
function anular() {
    if (!$.trim($('#anuMotivo').val())) { alert('Indique el motivo.'); return; }
    var $b = $('#btnAnular').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'anularAjuste', id: $('#anuId').val(), motivo: $('#anuMotivo').val() }, success: function (r) {
        if (r.ok) { $('#modalAnular').modal('hide'); cargar(); } else { $('#mensajeAnular').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
    }, error: function () { $b.prop('disabled', false); } });
}
</script>
</body>
</html>
