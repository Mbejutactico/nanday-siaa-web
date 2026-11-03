<%@include file="header.jsp" %>
<%-- INVENTARIOS > Salida de insumos: despacho de lo suministrado a una OT, con datos del
     transportista; genera la remisión. Pestaña de remisiones: imprimir y anular con motivo. --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-truck-loading"></i> Salida de Insumos</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card card-primary card-outline card-outline-tabs">
            <div class="card-header p-0 border-bottom-0">
                <ul class="nav nav-tabs">
                    <li class="nav-item"><a class="nav-link active" data-toggle="pill" href="#tabSalida"><i class="fas fa-truck-loading"></i> Registrar salida</a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="pill" href="#tabRemisiones"><i class="fas fa-file-alt"></i> Remisiones emitidas</a></li>
                </ul>
            </div>
            <div class="card-body"><div class="tab-content">
                <!-- Registrar salida -->
                <div class="tab-pane fade show active" id="tabSalida">
                    <div class="form-group">
                        <label>Orden de trabajo con insumos por despachar</label>
                        <select class="form-control" id="comboOT"></select>
                        <small class="text-muted">Los insumos y cantidades se cargan en <a href="otSuministrar.jsp">Suministrar OT</a>; aquí solo se registra su salida.</small>
                    </div>
                    <div id="panelSalida" style="display:none;">
                        <div class="callout callout-info" id="datosOT"></div>
                        <h6>Insumos que salen <small class="text-muted">(no se pueden modificar aquí)</small></h6>
                        <table class="table table-sm table-bordered">
                            <thead class="thead-light"><tr><th>Código</th><th>Nombre</th><th>Cantidad</th><th>Almacén de salida</th></tr></thead>
                            <tbody id="cuerpoItems"></tbody>
                        </table>
                        <h6 class="mt-3"><i class="fas fa-user"></i> Datos del transportista</h6>
                        <div class="form-row">
                            <div class="col-md-4 form-group"><label>Nombre *</label><input type="text" class="form-control" id="trNombre" maxlength="100"></div>
                            <div class="col-md-4 form-group"><label>Apellido *</label><input type="text" class="form-control" id="trApellido" maxlength="100"></div>
                            <div class="col-md-4 form-group"><label>Cédula de identidad *</label><input type="text" class="form-control" id="trCi" maxlength="20"></div>
                        </div>
                        <h6><i class="fas fa-car"></i> Vehículo <small class="text-muted">(opcional, si retira a pie dejar vacío)</small></h6>
                        <div class="form-row">
                            <div class="col-md-4 form-group"><label>Marca</label><input type="text" class="form-control" id="vhMarca" maxlength="50"></div>
                            <div class="col-md-4 form-group"><label>Modelo</label><input type="text" class="form-control" id="vhModelo" maxlength="50"></div>
                            <div class="col-md-4 form-group"><label>N° de chapa</label><input type="text" class="form-control" id="vhChapa" maxlength="20"></div>
                        </div>
                        <div class="form-group"><label>Observación</label><textarea class="form-control" id="observacion" rows="2" maxlength="2000"></textarea></div>
                        <button class="btn btn-success" id="btnEmitir"><i class="fas fa-file-signature"></i> Registrar salida y generar remisión</button>
                    </div>
                    <div id="mensajeSalida" class="mt-3"></div>
                </div>
                <!-- Remisiones -->
                <div class="tab-pane fade" id="tabRemisiones">
                    <div class="form-row justify-content-end mb-2">
                        <div class="col-sm-auto mb-1">
                            <select class="form-control" id="filtroTipo" title="Tipo de remisión">
                                <option value="">Insumos y activos</option>
                                <optgroup label="Insumos">
                                    <option value="INSUMOS">Solo insumos (salidas y transferencias)</option>
                                    <option value="SALIDA_INSUMOS">Salidas de insumos (OT)</option>
                                    <option value="TRANSFERENCIA">Transferencias entre almacenes</option>
                                </optgroup>
                                <optgroup label="Activos fijos">
                                    <option value="SALIDA_ACTIVOS">Solo activos</option>
                                </optgroup>
                            </select>
                        </div>
                        <div class="col-sm-auto mb-1">
                            <select class="form-control" id="filtroEstadoRem" title="Estado de la remisión">
                                <option value="">Vigentes y anuladas</option>
                                <option value="VIGENTE">Solo vigentes</option>
                                <option value="ANULADA">Solo anuladas</option>
                            </select>
                        </div>
                    </div>
                    <table class="table table-bordered table-striped" id="tablaRem" style="width:100%">
                        <thead><tr><th>N° remisión</th><th>Tipo</th><th>Fecha</th><th>Referencia</th><th>Transportista</th><th>Ítems</th><th>Estado</th><th>Acción</th></tr></thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div></div>
        </div>
    </div></div>
</div>

<div class="modal fade" id="modalDetalleRem" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title"><i class="fas fa-list"></i> Detalle de la remisión <span id="detRemNumero"></span></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body">
        <p class="text-muted small mb-2" id="detRemInfo"></p>
        <table class="table table-sm table-bordered mb-0">
            <thead class="thead-light"><tr><th>#</th><th>Código</th><th>Descripción</th><th class="text-right">Cantidad</th><th>Origen</th></tr></thead>
            <tbody id="detRemLista"></tbody>
        </table>
    </div>
    <div class="modal-footer">
        <button class="btn btn-info" id="detRemImprimir"><i class="fas fa-print"></i> Imprimir</button>
        <button class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
    </div>
</div></div></div>

<div class="modal fade" id="modalAnular" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title">Anular remisión <span id="anuNumero"></span></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body">
        <input type="hidden" id="anuId">
        <p id="anuAyuda" class="text-muted"></p>
        <div class="form-group"><label>Motivo *</label><textarea class="form-control" id="anuMotivo" rows="3" maxlength="2000"></textarea></div>
        <div id="mensajeAnular"></div>
    </div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cancelar</button><button class="btn btn-danger" id="btnAnular"><i class="fas fa-ban"></i> Anular remisión</button></div>
</div></div></div>

<%@include file="footer.jsp" %>
<script>
var URL_INV = "InventarioServlet";
var tablaRem;
var TIPOS_REM = { SALIDA_INSUMOS: 'Salida de insumos', TRANSFERENCIA: 'Transferencia', SALIDA_ACTIVOS: 'Salida de activos' };

$(document).ready(function () {
    tablaRem = $('#tablaRem').DataTable({ responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [], columnDefs: [{ orderable: false, targets: -1 }] });
    $('a[data-toggle="pill"]').on('shown.bs.tab', function () { tablaRem.columns.adjust(); });
    var m = /[?&]ot=([0-9]+)/.exec(location.search);
    cargarOTs(m ? m[1] : '');
    cargarRemisiones();
    $('#comboOT').change(function () { cargarPendientes($(this).val()); });
    $('#filtroTipo, #filtroEstadoRem').change(cargarRemisiones);
    $('#btnEmitir').click(emitir);
    $('#btnAnular').click(anular);
});

function cargarOTs(sel) {
    $.post(URL_INV, { tipo: 'otsConPendientes' }, function (html) {
        $('#comboOT').html(html);
        if (sel && $('#comboOT option[value="' + sel + '"]').length) { $('#comboOT').val(sel).change(); }
    });
}

function cargarPendientes(id) {
    if (!id) { $('#panelSalida').hide(); return; }
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'pendientesSalida', id_ot: id },
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            $('#datosOT').html('<strong>' + escHtml(f.numero) + '</strong> - ' + escHtml(f.titulo) + '<br><small>Centro: ' + escHtml(f.centro) +
                ' | Centro de costo: ' + escHtml(f.centro_costo) + ' | Solicitante: ' + escHtml(f.solicitante) + '</small>');
            var h = '';
            $.each(f.items, function (i, it) {
                h += '<tr><td>' + escHtml(it.codigo) + '</td><td>' + escHtml(it.nombre) + '</td><td><strong>' + escHtml(it.cantidad) + '</strong> ' + escHtml(it.unidad) + '</td><td>' + escHtml(it.almacen) + '</td></tr>';
            });
            $('#cuerpoItems').html(h);
            $('#panelSalida').show();
        } });
}

function emitir() {
    var d = { tipo: 'emitirSalida', id_ot: $('#comboOT').val(),
        transportista_nombre: $.trim($('#trNombre').val()), transportista_apellido: $.trim($('#trApellido').val()), transportista_ci: $.trim($('#trCi').val()),
        vehiculo_marca: $('#vhMarca').val(), vehiculo_modelo: $('#vhModelo').val(), vehiculo_chapa: $('#vhChapa').val(), observacion: $('#observacion').val() };
    if (!d.id_ot) { alert('Seleccione la orden de trabajo.'); return; }
    if (!d.transportista_nombre || !d.transportista_apellido || !d.transportista_ci) { alert('Complete nombre, apellido y cédula de identidad del transportista.'); return; }
    if (!confirmado('¿Registrar la salida y generar la remisión?', this, arguments)) return;
    var $b = $('#btnEmitir').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: d,
        success: function (r) {
            $b.prop('disabled', false);
            if (r.ok) {
                $('#mensajeSalida').html('<div class="alert alert-success"><i class="fas fa-check-circle"></i> Salida registrada el ' + escHtml(r.fecha) +
                    '. Remisión <b>' + escHtml(r.numero) + '</b>. <button class="btn btn-light btn-sm ml-2" onclick="imprimir(' + parseInt(r.id_remision, 10) + ')"><i class="fas fa-print"></i> Imprimir remisión</button></div>');
                $('#panelSalida').hide();
                $('#trNombre, #trApellido, #trCi, #vhMarca, #vhModelo, #vhChapa, #observacion').val('');
                cargarOTs('');
                cargarRemisiones();
            } else {
                $('#mensajeSalida').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>');
            }
        }, error: function () { $b.prop('disabled', false); } });
}

function cargarRemisiones() {
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'listarRemisiones', tipo_remision: $('#filtroTipo').val(), estado_remision: $('#filtroEstadoRem').val() },
        success: function (lista) {
            tablaRem.clear();
            $.each(lista, function (i, r) {
                var id = parseInt(r.id, 10);
                var acc = '<button class="btn btn-outline-primary btn-sm rem-detalle" data-i="' + i + '" title="Ver detalle"><i class="fas fa-list"></i></button> ' +
                    '<button class="btn btn-info btn-sm" onclick="imprimir(' + id + ')" title="Imprimir"><i class="fas fa-print"></i></button> ';
                if (!r.anulada && r.tipo !== 'SALIDA_ACTIVOS') {
                    acc += '<button class="btn btn-outline-danger btn-sm" onclick="abrirAnular(' + id + ',\'' + escHtml(r.numero) + '\',\'' + r.tipo + '\')" title="Anular"><i class="fas fa-ban"></i></button>';
                }
                tablaRem.row.add([
                    '<strong>' + escHtml(r.numero) + '</strong>', escHtml(TIPOS_REM[r.tipo] || r.tipo), escHtml(r.fecha), escHtml(r.referencia),
                    escHtml(r.transportista) + (r.ci ? ' <small class="text-muted">CI ' + escHtml(r.ci) + '</small>' : ''), escHtml(r.items),
                    r.anulada ? '<span class="badge badge-danger" title="' + escHtml(r.motivo_anulacion) + '">Anulada ' + escHtml(r.fecha_anulacion) + '</span>' : '<span class="badge badge-success">Vigente</span>',
                    acc]);
            });
            tablaRem.draw();
            remisionesCargadas = lista;
        } });
}

var remisionesCargadas = [];
$(document).on('click', '#tablaRem .rem-detalle', function () { verDetalleRemision(remisionesCargadas[$(this).data('i')]); });

function verDetalleRemision(rem) {
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'detalleRemision', id: rem.id },
        success: function (lista) {
            if (lista.error) { alert(lista.error); return; }
            $('#detRemNumero').text(rem.numero);
            $('#detRemInfo').text((TIPOS_REM[rem.tipo] || rem.tipo) + ' - ' + rem.fecha + (rem.referencia ? ' - ' + rem.referencia : '') +
                (rem.anulada ? ' (ANULADA)' : ''));
            var h = '';
            $.each(lista, function (i, l) {
                h += '<tr><td>' + escHtml(l.orden) + '</td><td>' + escHtml(l.codigo) + '</td><td>' + escHtml(l.descripcion) +
                    '</td><td class="text-right">' + escHtml(l.cantidad) + ' ' + escHtml(l.unidad) + '</td><td>' + escHtml(l.origen) + '</td></tr>';
            });
            $('#detRemLista').html(h || '<tr><td colspan="5" class="text-center text-muted">La remisión no tiene ítems registrados</td></tr>');
            $('#detRemImprimir').off('click').on('click', function () { imprimir(rem.id); });
            $('#modalDetalleRem').modal('show');
        },
        error: function () { alert('No se pudo obtener el detalle de la remisión.'); } });
}

function imprimir(id) { window.open(URL_INV + '?tipo=imprimirRemision&id=' + encodeURIComponent(id), '_blank'); }

function abrirAnular(id, numero, tipoRem) {
    $('#anuId').val(id);
    $('#anuNumero').text(numero);
    $('#anuMotivo').val('');
    $('#mensajeAnular').html('');
    $('#anuAyuda').text(tipoRem === 'TRANSFERENCIA'
        ? 'Se anula la transferencia: el stock vuelve al almacén de origen.'
        : 'Los insumos de la OT vuelven a quedar pendientes de salida (el stock no cambia). Si la OT está finalizada, primero anule su finalización.');
    $('#btnAnular').prop('disabled', false);
    $('#modalAnular').modal('show');
}

function anular() {
    if (!$.trim($('#anuMotivo').val())) { alert('Indique el motivo.'); return; }
    var $b = $('#btnAnular').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INV, dataType: 'json', data: { tipo: 'anularRemision', id: $('#anuId').val(), motivo: $('#anuMotivo').val() },
        success: function (r) {
            if (r.ok) { $('#modalAnular').modal('hide'); cargarRemisiones(); cargarOTs(''); }
            else { $('#mensajeAnular').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
        }, error: function () { $b.prop('disabled', false); } });
}
</script>
</body>
</html>
