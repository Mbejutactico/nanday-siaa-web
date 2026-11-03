<%@include file="header.jsp" %>
<%-- ORDEN DE TRABAJO > Finalizar: una OT finalizada no se puede volver a usar.
     La finalización se puede anular (con motivo) para volver a usarla. --%>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <h1><i class="fas fa-check-double"></i> Finalizar Orden de Trabajo</h1>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">
            <div class="card card-primary card-outline card-outline-tabs">
                <div class="card-header p-0 border-bottom-0">
                    <ul class="nav nav-tabs" role="tablist">
                        <li class="nav-item"><a class="nav-link active" data-toggle="pill" href="#tabPorFinalizar"><i class="fas fa-cogs"></i> Por finalizar <span class="badge badge-warning" id="cantPor">0</span></a></li>
                        <li class="nav-item"><a class="nav-link" data-toggle="pill" href="#tabFinalizadas"><i class="fas fa-check-double"></i> Finalizadas <span class="badge badge-success" id="cantFin">0</span></a></li>
                    </ul>
                </div>
                <div class="card-body">
                    <div id="mensajeGeneral"></div>
                    <div class="tab-content">
                        <div class="tab-pane fade show active" id="tabPorFinalizar">
                            <p class="text-muted">Para finalizar, la orden debe tener al menos una <b>salida registrada</b> (de insumos o de activos) y ningún insumo pendiente de salida.
                               Al finalizarla queda cerrada: no se puede modificar ni recibir más insumos.</p>
                            <table class="table table-bordered table-striped" id="tablaPor" style="width:100%">
                                <thead><tr><th>N° OT</th><th>Fecha de creación</th><th>Título</th><th>Centro de costo</th><th>Solicitante</th><th>Insumos</th><th>Acción</th></tr></thead>
                                <tbody></tbody>
                            </table>
                        </div>
                        <div class="tab-pane fade" id="tabFinalizadas">
                            <p class="text-muted">Si una orden se finalizó por error, anule la finalización indicando el motivo para volver a usarla.</p>
                            <table class="table table-bordered table-striped" id="tablaFin" style="width:100%">
                                <thead><tr><th>N° OT</th><th>Título</th><th>Centro de costo</th><th>Solicitante</th><th>Insumos</th><th>Fecha de finalización</th><th>Acción</th></tr></thead>
                                <tbody></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Finalizar -->
<div class="modal fade" id="modalFinalizar" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Finalizar <span id="finNumero"></span></h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="finId">
                <p id="finTitulo" class="font-weight-bold"></p>
                <div class="form-group">
                    <label>Fecha y hora de finalización</label>
                    <input type="text" class="form-control" id="finFecha" readonly>
                    <small class="text-muted">Se registra automáticamente al confirmar.</small>
                </div>
                <h6>Insumos entregados</h6>
                <div id="finItems"></div>
                <div class="form-group">
                    <label>Observación <small class="text-muted">(opcional)</small></label>
                    <textarea class="form-control" id="finObservacion" rows="2" maxlength="2000" placeholder="Ej.: trabajo realizado y entregado al sector"></textarea>
                </div>
                <div class="alert alert-warning mb-0"><i class="fas fa-lock"></i> Una vez finalizada, la orden no podrá modificarse ni recibir insumos.</div>
                <div id="mensajeFinalizar" class="mt-2"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-success" id="btnConfirmarFinalizar"><i class="fas fa-check-double"></i> Finalizar orden</button>
            </div>
        </div>
    </div>
</div>

<!-- Anular finalización -->
<div class="modal fade" id="modalReabrir" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Anular finalización de <span id="reaNumero"></span></h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="reaId">
                <p>La orden volverá a estar <b>en proceso</b> y se podrá seguir usando. La fecha, el usuario y el motivo quedan registrados.</p>
                <div class="form-group">
                    <label>Fecha y hora de anulación</label>
                    <input type="text" class="form-control" id="reaFecha" readonly>
                    <small class="text-muted">Se registra automáticamente al confirmar.</small>
                </div>
                <div class="form-group">
                    <label>Motivo *</label>
                    <textarea class="form-control" id="reaMotivo" rows="3" maxlength="2000"></textarea>
                </div>
                <div id="mensajeReabrir"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-danger" id="btnConfirmarReabrir"><i class="fas fa-lock-open"></i> Anular finalización</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var URL_OT = "OrdenTrabajoServlet";
var tablaPor, tablaFin;

function fechaLegible(iso) {
    if (!iso) return '';
    var p = iso.substring(0, 10).split('-');
    return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] + (iso.length > 10 ? ' ' + iso.substring(11, 16) : '') : iso;
}

$(document).ready(function () {
    var opciones = { responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [], columnDefs: [{ orderable: false, targets: -1 }] };
    tablaPor = $('#tablaPor').DataTable($.extend({}, opciones, { language: { emptyTable: 'No hay órdenes para finalizar' } }));
    tablaFin = $('#tablaFin').DataTable($.extend({}, opciones, { language: { emptyTable: 'No hay órdenes finalizadas' } }));
    // Recalcula el ancho de la tabla al mostrar la pestaña
    $('a[data-toggle="pill"]').on('shown.bs.tab', function () { tablaPor.columns.adjust(); tablaFin.columns.adjust(); });

    cargar();
    $('#btnConfirmarFinalizar').click(confirmarFinalizar);
    $('#btnConfirmarReabrir').click(confirmarReabrir);
});

function cargar() {
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'listar', estado: 'ABIERTAS' },
        success: function (lista) {
            tablaPor.clear();
            var n = 0;
            $.each(lista, function (i, o) {
                var id = parseInt(o.id, 10);
                var boton;
                if (o.finalizable) {
                    boton = '<button class="btn btn-success btn-sm" onclick="finalizar(' + id + ')"><i class="fas fa-check-double"></i> Finalizar</button>';
                    n++;
                } else if (o.sin_salida > 0) {
                    boton = '<a href="salidaInsumos.jsp?ot=' + id + '" class="btn btn-outline-info btn-sm" title="Hay insumos sin salida"><i class="fas fa-truck-loading"></i> Registrar salida</a>';
                } else {
                    boton = '<a href="otSuministrar.jsp?ot=' + id + '" class="btn btn-outline-secondary btn-sm" title="Sin salida registrada: suministre insumos (o registre la salida de activos)"><i class="fas fa-dolly"></i> Suministrar</a>';
                }
                tablaPor.row.add([
                    '<strong>' + escHtml(o.numero) + '</strong>' +
                        (o.fecha_anulacion_fin ? '<br><small class="text-danger" title="Se anuló su finalización"><i class="fas fa-lock-open"></i> reabierta ' + escHtml(o.fecha_anulacion_fin) + '</small>' : ''),
                    escHtml(o.fecha_creacion || fechaLegible(o.fecha)), escHtml(o.titulo), escHtml(o.centro_costo), escHtml(o.solicitante),
                    (o.items > 0 ? escHtml(o.items) + ' <small class="text-muted">(' + escHtml(o.unidades) + ' u.)</small>' : '<span class="badge badge-secondary">sin insumos</span>') +
                    (o.sin_salida > 0 ? '<br><span class="badge badge-warning">' + o.sin_salida + ' sin salida</span>' : (o.salidas > 0 ? '<br><span class="badge badge-success">con salida</span>' : '')),
                    boton
                ]);
            });
            tablaPor.draw();
            $('#cantPor').text(n);
        }
    });
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'listar', estado: 'FINALIZADA' },
        success: function (lista) {
            tablaFin.clear();
            $.each(lista, function (i, o) {
                var id = parseInt(o.id, 10);
                tablaFin.row.add([
                    '<strong>' + escHtml(o.numero) + '</strong>', escHtml(o.titulo), escHtml(o.centro_costo), escHtml(o.solicitante),
                    escHtml(o.items) + ' <small class="text-muted">(' + escHtml(o.unidades) + ' u.)</small>',
                    escHtml(o.fecha_finalizacion),
                    // Anular la finalización: solo quien la finalizó o el administrador
                    (o.puede_reabrir
                        ? '<button class="btn btn-outline-danger btn-sm" onclick="reabrir(' + id + ',\'' + escHtml(o.numero) + '\')"><i class="fas fa-lock-open"></i> Anular finalización</button>'
                        : '<small class="text-muted" title="Solo quien la finalizó o el administrador pueden anular la finalización"><i class="fas fa-lock"></i> Finalizada por otro usuario</small>')
                ]);
            });
            tablaFin.draw();
            $('#cantFin').text(lista.length);
        }
    });
}

// Fecha y hora actual del servidor (solo lectura)
function mostrarAhora(campo) {
    $(campo).val('...');
    $.post(URL_OT, { tipo: 'ahora' }, function (r) { $(campo).val(r.fecha); }, 'json');
}

function finalizar(id) {
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'detalle', id: id },
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            $('#finId').val(f.id);
            $('#finNumero').text(f.numero);
            $('#finTitulo').text(f.titulo);
            var h = '<table class="table table-sm table-bordered"><thead><tr><th>Código</th><th>Insumo</th><th>Cantidad</th><th>Almacén</th></tr></thead><tbody>';
            $.each(f.items, function (i, it) {
                h += '<tr><td>' + escHtml(it.codigo) + '</td><td>' + escHtml(it.descripcion) + '</td><td>' + escHtml(it.cantidad) + ' ' + escHtml(it.unidad) + '</td><td>' + escHtml(it.almacen) + '</td></tr>';
            });
            $('#finItems').html(h + '</tbody></table>');
            $('#finObservacion').val('');
            mostrarAhora('#finFecha');
            $('#mensajeFinalizar').html('');
            $('#btnConfirmarFinalizar').prop('disabled', false);
            $('#modalFinalizar').modal('show');
        }
    });
}

function confirmarFinalizar() {
    var $b = $('#btnConfirmarFinalizar').prop('disabled', true);
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'finalizar', id: $('#finId').val(), observacion: $('#finObservacion').val() },
        success: function (r) {
            if (r.ok) {
                $('#modalFinalizar').modal('hide');
                $('#mensajeGeneral').html('<div class="alert alert-success alert-dismissible"><button type="button" class="close" data-dismiss="alert">&times;</button>' +
                    '<i class="fas fa-check-double"></i> Orden <b>' + escHtml($('#finNumero').text()) + '</b> finalizada el ' + escHtml(r.fecha) + '.</div>');
                cargar();
            }
            else { $('#mensajeFinalizar').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
        },
        error: function () { $b.prop('disabled', false); }
    });
}

function reabrir(id, numero) {
    $('#reaId').val(id);
    $('#reaNumero').text(numero);
    $('#reaMotivo').val('');
    mostrarAhora('#reaFecha');
    $('#mensajeReabrir').html('');
    $('#btnConfirmarReabrir').prop('disabled', false);
    $('#modalReabrir').modal('show');
}

function confirmarReabrir() {
    if (!$.trim($('#reaMotivo').val())) { alert('Indique el motivo.'); return; }
    var $b = $('#btnConfirmarReabrir').prop('disabled', true);
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'reabrir', id: $('#reaId').val(), motivo: $('#reaMotivo').val() },
        success: function (r) {
            if (r.ok) {
                $('#modalReabrir').modal('hide');
                $('#mensajeGeneral').html('<div class="alert alert-warning alert-dismissible"><button type="button" class="close" data-dismiss="alert">&times;</button>' +
                    '<i class="fas fa-lock-open"></i> Se anuló la finalización de <b>' + escHtml($('#reaNumero').text()) + '</b> el ' + escHtml(r.fecha) + '. La orden volvió a estar en proceso.</div>');
                cargar();
            }
            else { $('#mensajeReabrir').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
        },
        error: function () { $b.prop('disabled', false); }
    });
}
</script>
</body>
</html>
