<%@include file="header.jsp" %>
<%-- INVENTARIOS > Transferencias entre almacenes, con remisión. Se anulan con motivo (el stock vuelve al origen). --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-exchange-alt"></i> Transferencias entre Almacenes</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card">
            <div class="card-header"><button class="btn btn-success" id="btnNueva"><i class="fas fa-plus"></i> Nueva transferencia</button></div>
            <div class="card-body">
                <table class="table table-bordered table-striped" id="tablaTr" style="width:100%">
                    <thead><tr><th>N°</th><th>Fecha</th><th>Origen</th><th>Destino</th><th>Ítems</th><th>Remisión</th><th>Estado</th><th>Acción</th></tr></thead>
                    <tbody></tbody>
                </table>
            </div>
        </div>
    </div></div>
</div>

<div class="modal fade" id="modalNueva" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Nueva transferencia</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body">
        <div class="form-row">
            <div class="col-md-6 form-group"><label>Almacén de origen *</label><select class="form-control" id="trOrigen"></select></div>
            <div class="col-md-6 form-group"><label>Almacén de destino *</label><select class="form-control" id="trDestino"></select></div>
        </div>
        <div class="form-row align-items-end">
            <div class="col-md-7 form-group"><label>Insumo</label><select class="form-control" id="trInsumo"><option value="">-- Elija el origen --</option></select></div>
            <div class="col-md-3 form-group"><label>Cantidad</label><input type="number" class="form-control" id="trCantidad" min="1" value="1"></div>
            <div class="col-md-2 form-group"><button class="btn btn-outline-primary btn-block" id="btnAgregar"><i class="fas fa-plus"></i></button></div>
        </div>
        <table class="table table-sm table-bordered"><thead class="thead-light"><tr><th>Código</th><th>Nombre</th><th>Disponible</th><th>Cantidad</th><th></th></tr></thead><tbody id="cuerpoLineas"></tbody></table>
        <h6><i class="fas fa-user"></i> Transportista y vehículo <small class="text-muted">(opcional)</small></h6>
        <div class="form-row">
            <div class="col-md-4 form-group"><input class="form-control" id="tNombre" placeholder="Nombre" maxlength="100"></div>
            <div class="col-md-4 form-group"><input class="form-control" id="tApellido" placeholder="Apellido" maxlength="100"></div>
            <div class="col-md-4 form-group"><input class="form-control" id="tCi" placeholder="Cédula de identidad" maxlength="20"></div>
            <div class="col-md-4 form-group"><input class="form-control" id="vMarca" placeholder="Marca" maxlength="50"></div>
            <div class="col-md-4 form-group"><input class="form-control" id="vModelo" placeholder="Modelo" maxlength="50"></div>
            <div class="col-md-4 form-group"><input class="form-control" id="vChapa" placeholder="N° de chapa" maxlength="20"></div>
        </div>
        <div class="form-group"><label>Observación</label><input class="form-control" id="trObs" maxlength="500"></div>
        <div id="mensajeNueva"></div>
    </div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cancelar</button><button class="btn btn-success" id="btnGuardar"><i class="fas fa-save"></i> Registrar transferencia</button></div>
</div></div></div>

<div class="modal fade" id="modalDetalle" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Transferencia <span id="detNumero"></span></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body" id="detCuerpo"></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button></div>
</div></div></div>

<div class="modal fade" id="modalAnular" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Anular transferencia <span id="anuNumero"></span></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><input type="hidden" id="anuId"><p class="text-muted">El stock vuelve al almacén de origen y la remisión queda anulada.</p>
        <div class="form-group"><label>Motivo *</label><textarea class="form-control" id="anuMotivo" rows="3" maxlength="2000"></textarea></div><div id="mensajeAnular"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cancelar</button><button class="btn btn-danger" id="btnAnular"><i class="fas fa-ban"></i> Anular</button></div>
</div></div></div>

<%@include file="footer.jsp" %>
<script>
var URL_INV = "InventarioServlet";
var tabla, lineas = [], stock = {}, datos = {};

$(document).ready(function () {
    tabla = $('#tablaTr').DataTable({ responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [], columnDefs: [{ orderable: false, targets: -1 }] });
    cargar();
    $.post(URL_INV, { tipo: 'listarAlmacenes' }, function (h) { $('#trOrigen').html(h); });
    $.post(URL_INV, { tipo: 'listarAlmacenes', destino: 'true' }, function (h) { $('#trDestino').html(h); });
    $('#btnNueva').click(function () { lineas = []; dibujarLineas(); $('#mensajeNueva').html(''); $('#btnGuardar').prop('disabled', false); $('#modalNueva').modal('show'); });
    $('#trOrigen').change(cargarStockOrigen);
    $('#btnAgregar').click(agregar);
    $('#btnGuardar').click(guardar);
    $('#btnAnular').click(anular);
});

function cargarStockOrigen() {
    var a = $('#trOrigen').val(); stock = {}; lineas = []; dibujarLineas();
    if (!a) { $('#trInsumo').html('<option value="">-- Elija el origen --</option>'); return; }
    $.post(URL_INV, { tipo: 'stockAlmacen', id_almacen: a }, function (lista) {
        var h = '<option value="">-- Seleccionar insumo --</option>';
        $.each(lista, function (i, x) {
            stock[x.id_insumo] = { disp: x.stock, codigo: x.codigo, nombre: x.nombre };
            if (x.stock > 0) h += '<option value="' + parseInt(x.id_insumo, 10) + '">' + escHtml(x.codigo) + ' - ' + escHtml(x.nombre) + ' (disponible: ' + x.stock + ')</option>';
        });
        $('#trInsumo').html(h);
    }, 'json');
}
function agregar() {
    var id = parseInt($('#trInsumo').val(), 10), c = parseInt($('#trCantidad').val(), 10);
    if (!id) { alert('Seleccione el insumo.'); return; }
    if (isNaN(c) || c <= 0) { alert('La cantidad debe ser mayor a 0.'); return; }
    var usado = 0; $.each(lineas, function (i, l) { if (l.id_insumo === id) usado += l.cantidad; });
    if (usado + c > stock[id].disp) { alert('Supera lo disponible en el origen (' + stock[id].disp + ').'); return; }
    lineas.push({ id_insumo: id, codigo: stock[id].codigo, nombre: stock[id].nombre, cantidad: c }); $('#trCantidad').val(1); dibujarLineas();
}
function quitarLinea(i) { lineas.splice(i, 1); dibujarLineas(); }
function dibujarLineas() {
    if (!lineas.length) { $('#cuerpoLineas').html('<tr><td colspan="5" class="text-center text-muted">Agregue insumos</td></tr>'); return; }
    var h = ''; $.each(lineas, function (i, l) {
        h += '<tr><td>' + escHtml(l.codigo) + '</td><td>' + escHtml(l.nombre) + '</td><td>' + stock[l.id_insumo].disp + '</td><td>' + l.cantidad + '</td><td><button class="btn btn-outline-danger btn-sm" onclick="quitarLinea(' + i + ')"><i class="fas fa-times"></i></button></td></tr>';
    }); $('#cuerpoLineas').html(h);
}
function guardar() {
    var d = { tipo: 'crearTransferencia', id_almacen_origen: $('#trOrigen').val(), id_almacen_destino: $('#trDestino').val(), observacion: $('#trObs').val(),
        transportista_nombre: $('#tNombre').val(), transportista_apellido: $('#tApellido').val(), transportista_ci: $('#tCi').val(),
        vehiculo_marca: $('#vMarca').val(), vehiculo_modelo: $('#vModelo').val(), vehiculo_chapa: $('#vChapa').val(),
        items: JSON.stringify($.map(lineas, function (l) { return { id_insumo: l.id_insumo, cantidad: l.cantidad }; })) };
    if (!d.id_almacen_origen || !d.id_almacen_destino) { alert('Seleccione origen y destino.'); return; }
    if (d.id_almacen_origen === d.id_almacen_destino) { alert('El destino debe ser distinto al origen.'); return; }
    if (!lineas.length) { alert('Agregue al menos un insumo.'); return; }
    var $b = $('#btnGuardar').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: d, success: function (r) {
        if (r.ok) {
            $('#modalNueva').modal('hide');
            preguntar('Transferencia ' + r.numero + ' registrada el ' + r.fecha + ' con la remisión ' + r.remision + '.\n¿Imprimir la remisión ahora?', function () { imprimir(r.id_remision); }, { titulo: 'Transferencia registrada', icono: 'success', boton: 'Imprimir remisión', cancelar: 'Ahora no' });
            cargar();
        } else { $('#mensajeNueva').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
    }, error: function () { $b.prop('disabled', false); } });
}
function imprimir(id) { window.open(URL_INV + '?tipo=imprimirRemision&id=' + encodeURIComponent(id), '_blank'); }
function cargar() {
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'listarTransferencias' }, success: function (lista) {
        tabla.clear(); datos = {};
        $.each(lista, function (i, t) {
            datos[t.id] = t; var id = parseInt(t.id, 10);
            var acc = '<button class="btn btn-info btn-sm" onclick="verDetalle(' + id + ')"><i class="fas fa-eye"></i></button> ' +
                      '<button class="btn btn-secondary btn-sm" onclick="imprimir(' + parseInt(t.id_remision, 10) + ')" title="Imprimir remisión"><i class="fas fa-print"></i></button> ';
            if (!t.anulada) acc += '<button class="btn btn-outline-danger btn-sm" onclick="abrirAnular(' + id + ')"><i class="fas fa-ban"></i></button>';
            tabla.row.add(['<strong>' + escHtml(t.numero) + '</strong>', escHtml(t.fecha), escHtml(t.origen), escHtml(t.destino), escHtml(t.items), escHtml(t.remision),
                t.anulada ? '<span class="badge badge-danger" title="' + escHtml(t.motivo_anulacion) + '">Anulada ' + escHtml(t.fecha_anulacion) + '</span>' : '<span class="badge badge-success">Vigente</span>', acc]);
        }); tabla.draw();
    } });
}
function verDetalle(id) {
    var t = datos[id];
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'detalleTransferencia', id: id }, success: function (lista) {
        var h = '<p>' + escHtml(t.fecha) + ' por ' + escHtml(t.usuario) + '<br>' + escHtml(t.origen) + ' &rarr; ' + escHtml(t.destino) + (t.observacion ? '<br>' + escHtml(t.observacion) : '') + '</p>' +
            (t.anulada ? '<div class="alert alert-danger">Anulada el ' + escHtml(t.fecha_anulacion) + ': ' + escHtml(t.motivo_anulacion) + '</div>' : '') +
            '<table class="table table-sm table-bordered"><thead><tr><th>Código</th><th>Insumo</th><th>Cantidad</th></tr></thead><tbody>';
        $.each(lista, function (i, l) { h += '<tr><td>' + escHtml(l.codigo) + '</td><td>' + escHtml(l.nombre) + '</td><td>' + l.cantidad + ' ' + escHtml(l.unidad) + '</td></tr>'; });
        $('#detNumero').text(t.numero); $('#detCuerpo').html(h + '</tbody></table>'); $('#modalDetalle').modal('show');
    } });
}
function abrirAnular(id) { $('#anuId').val(id); $('#anuNumero').text(datos[id].numero); $('#anuMotivo').val(''); $('#mensajeAnular').html(''); $('#btnAnular').prop('disabled', false); $('#modalAnular').modal('show'); }
function anular() {
    if (!$.trim($('#anuMotivo').val())) { alert('Indique el motivo.'); return; }
    var $b = $('#btnAnular').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'anularTransferencia', id: $('#anuId').val(), motivo: $('#anuMotivo').val() }, success: function (r) {
        if (r.ok) { $('#modalAnular').modal('hide'); cargar(); } else { $('#mensajeAnular').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
    }, error: function () { $b.prop('disabled', false); } });
}
</script>
</body>
</html>
