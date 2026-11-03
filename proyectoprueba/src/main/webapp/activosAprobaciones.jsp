<%@include file="header.jsp" %>
<%-- ACTIVOS FIJOS > Aprobaciones: salidas y bajas pendientes (al aprobar se genera la remisión / el acta),
     Recepción (activos en tránsito y en reparación) e Historial (imprimir y anular salidas). --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-stamp"></i> Aprobaciones de Activos Fijos</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div id="mensaje"></div>
        <div class="card card-primary card-outline card-outline-tabs">
            <div class="card-header p-0 border-bottom-0">
                <ul class="nav nav-tabs">
                    <li class="nav-item"><a class="nav-link active" data-toggle="pill" href="#tabPend"><i class="fas fa-hourglass-half"></i> Pendientes de aprobación <span class="badge badge-warning" id="cPend">0</span></a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="pill" href="#tabRec"><i class="fas fa-inbox"></i> Recepción <span class="badge badge-info" id="cRec">0</span></a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="pill" href="#tabHist"><i class="fas fa-history"></i> Historial</a></li>
                </ul>
            </div>
            <div class="card-body"><div class="tab-content">
                <div class="tab-pane fade show active" id="tabPend">
                    <table class="table table-bordered table-striped" id="tablaPend" style="width:100%">
                        <thead><tr><th>N°</th><th>Tipo</th><th>Fecha</th><th>Solicitante</th><th>Destino</th><th>Transportista</th><th>Activos</th><th>Acción</th></tr></thead><tbody></tbody>
                    </table>
                </div>
                <div class="tab-pane fade" id="tabRec">
                    <p class="text-muted">Activos enviados a otro centro (o por una OT) que el destino debe <b>recibir</b> o <b>rechazar</b> con justificación, y activos en reparación que vuelven.</p>
                    <div class="mb-2">
                        <button class="btn btn-success btn-sm" id="btnRecibir"><i class="fas fa-check"></i> Recibir seleccionados</button>
                        <button class="btn btn-danger btn-sm" id="btnRechazarRec"><i class="fas fa-times"></i> Rechazar seleccionados</button>
                    </div>
                    <table class="table table-sm table-bordered table-hover" id="tablaRec">
                        <thead class="thead-light"><tr><th style="width:40px"><input type="checkbox" id="todosRec"></th><th>Tipo</th><th>Solicitud / remisión</th><th>N° activo</th><th>Descripción</th><th>Origen</th><th>Destino</th><th>Salió</th></tr></thead><tbody></tbody>
                    </table>
                </div>
                <div class="tab-pane fade" id="tabHist">
                    <table class="table table-bordered table-striped" id="tablaHist" style="width:100%">
                        <thead><tr><th>N°</th><th>Tipo</th><th>Fecha</th><th>Destino</th><th>Activos</th><th>Estado</th><th>Remisión</th><th>Acción</th></tr></thead><tbody></tbody>
                    </table>
                </div>
            </div></div>
        </div>
    </div></div>
</div>

<div class="modal fade" id="modalDetalle" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title" id="detTitulo"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body" id="detCuerpo"></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button></div>
</div></div></div>

<div class="modal fade" id="modalMotivo" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title" id="motTitulo"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><p class="text-muted" id="motAyuda"></p><div class="form-group"><label id="motLabel">Motivo *</label><textarea class="form-control" id="motTexto" rows="3" maxlength="2000"></textarea></div><div id="motMensaje"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cancelar</button><button class="btn btn-danger" id="motConfirmar">Confirmar</button></div>
</div></div></div>

<%@include file="footer.jsp" %>
<script>
var URL_AM = "SolicitudActivoServlet", tPend, tHist, sols = {}, accionMotivo = null;
var TIPOS = { REPARACION: ['Reparación', 'badge-warning'], TRANSFERENCIA: ['Transferencia', 'badge-primary'], ENTREGA_OT: ['Entrega por OT', 'badge-success'], BAJA: ['Baja', 'badge-dark'] };
var EST = { PENDIENTE: 'badge-warning', APROBADA: 'badge-success', RECHAZADA: 'badge-danger', ANULADA: 'badge-secondary' };
var LINEA = { PENDIENTE: 'Pendiente', EN_CURSO: 'En curso', RECIBIDO: 'Recibido', RECHAZADO_DESTINO: 'Rechazado en destino', FINALIZADO: 'Dado de baja', ANULADO: 'Anulado' };
function badgeTipo(t) { var x = TIPOS[t] || [t, 'badge-light']; return '<span class="badge ' + x[1] + '">' + escHtml(x[0]) + '</span>'; }
function mensaje(tipo, html) { $('#mensaje').html('<div class="alert alert-' + tipo + ' alert-dismissible"><button type="button" class="close" data-dismiss="alert">&times;</button>' + html + '</div>'); }

$(document).ready(function () {
    var op = { responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [], columnDefs: [{ orderable: false, targets: -1 }] };
    tPend = $('#tablaPend').DataTable($.extend({}, op, { language: { emptyTable: 'No hay solicitudes pendientes' } }));
    tHist = $('#tablaHist').DataTable($.extend({}, op, { language: { emptyTable: 'Sin solicitudes' } }));
    $('a[data-toggle="pill"]').on('shown.bs.tab', function () { tPend.columns.adjust(); tHist.columns.adjust(); });
    refrescar();
    // Desde la campana: activosAprobaciones.jsp#tabRec abre la pestaña Recepción
    function pestanaDeLaDireccion() { if (location.hash === '#tabRec') $('a[href="#tabRec"]').tab('show'); }
    pestanaDeLaDireccion();
    $(window).on('hashchange', pestanaDeLaDireccion);
    $('#todosRec').change(function () { $('#tablaRec .sel').prop('checked', this.checked); });
    $('#btnRecibir').click(function () {
        var ids = seleccion(); if (!ids.length) { alert('Seleccione al menos un activo.'); return; }
        var obs = prompt('Observación de la recepción (opcional):', ''); if (obs === null) return;
        $.post(URL_AM, { tipo: 'recibir', ids: JSON.stringify(ids), observacion: obs }, function (r) {
            if (r.ok) { mensaje('success', r.cantidad + ' activo(s) recibido(s) el ' + escHtml(r.fecha) + '.'); refrescar(); } else alert(r.error);
        }, 'json');
    });
    $('#btnRechazarRec').click(function () {
        var ids = seleccion(); if (!ids.length) { alert('Seleccione al menos un activo.'); return; }
        abrirMotivo('Rechazar recepción', 'Los activos vuelven a su centro y centro de costo de origen. Los que vuelven de reparación no se rechazan.', 'Justificación *',
            function (texto) { return { tipo: 'rechazarRecepcion', ids: JSON.stringify(ids), observacion: texto }; }, 'Recepción rechazada; los activos volvieron al origen.');
    });
    $('#motConfirmar').click(confirmarMotivo);
});
function seleccion() { return $('#tablaRec .sel:checked').map(function () { return parseInt(this.value, 10); }).get(); }
function refrescar() { cargarPendientes(); cargarRecepcion(); cargarHistorial(); }

function cargarPendientes() {
    $.post(URL_AM, { tipo: 'listarSolicitudes', estado: 'PENDIENTE' }, function (l) {
        tPend.clear();
        $.each(l, function (i, s) {
            sols[s.id] = s; var id = parseInt(s.id, 10);
            tPend.row.add(['<strong>' + escHtml(s.numero) + '</strong>', badgeTipo(s.tipo), escHtml(s.fecha), escHtml(s.solicitante), escHtml(s.destino),
                escHtml(s.transportista || '-'), escHtml(s.activos),
                '<button class="btn btn-info btn-sm" onclick="ver(' + id + ')" title="Ver activos"><i class="fas fa-eye"></i></button> ' +
                '<button class="btn btn-success btn-sm" onclick="aprobar(' + id + ')"><i class="fas fa-check"></i> Aprobar</button> ' +
                '<button class="btn btn-outline-danger btn-sm" onclick="rechazar(' + id + ')"><i class="fas fa-times"></i></button>']);
        });
        tPend.draw(); $('#cPend').text(l.length);
    }, 'json');
}
function cargarRecepcion() {
    $.post(URL_AM, { tipo: 'enRecepcion' }, function (l) {
        var h = '';
        $.each(l, function (i, a) {
            h += '<tr><td class="text-center"><input type="checkbox" class="sel" value="' + parseInt(a.id, 10) + '"></td><td>' + badgeTipo(a.tipo) + '</td><td>' + escHtml(a.solicitud) +
                 '<br><small>' + escHtml(a.remision) + '</small></td><td><strong>' + escHtml(a.numero_activo) + '</strong>' + (a.numero_serie ? '<br><small>' + escHtml(a.numero_serie) + '</small>' : '') +
                 '</td><td>' + escHtml(a.descripcion) + '</td><td>' + escHtml(a.origen) + '</td><td>' + escHtml(a.destino) + '</td><td><small>' + escHtml(a.fecha) + '</small></td></tr>';
        });
        $('#tablaRec tbody').html(h || '<tr><td colspan="8" class="text-center text-muted">No hay activos por recibir</td></tr>');
        $('#todosRec').prop('checked', false); $('#cRec').text(l.length);
    }, 'json');
}
function cargarHistorial() {
    $.post(URL_AM, { tipo: 'listarSolicitudes' }, function (l) {
        tHist.clear();
        $.each(l, function (i, s) {
            sols[s.id] = s; var id = parseInt(s.id, 10), acc = '<button class="btn btn-info btn-sm" onclick="ver(' + id + ')"><i class="fas fa-eye"></i></button> ';
            if (s.id_remision) acc += '<button class="btn btn-secondary btn-sm" onclick="imprimirRemision(' + parseInt(s.id_remision, 10) + ')" title="Imprimir remisión"><i class="fas fa-print"></i></button> ';
            if (s.tipo === 'BAJA') acc += '<button class="btn btn-dark btn-sm" onclick="imprimirActa(' + id + ')" title="Acta de baja"><i class="fas fa-file-pdf"></i></button> ';
            if (s.estado === 'APROBADA' && s.tipo !== 'BAJA' && s.recibidos === 0) acc += '<button class="btn btn-outline-danger btn-sm" onclick="anularSalida(' + id + ')" title="Anular salida y remisión"><i class="fas fa-ban"></i></button>';
            var estado = '<span class="badge ' + (EST[s.estado] || 'badge-light') + '">' + escHtml(s.estado) + '</span>' +
                (s.motivo_rechazo ? '<br><small>' + escHtml(s.motivo_rechazo) + '</small>' : '') + (s.motivo_anulacion ? '<br><small>' + escHtml(s.motivo_anulacion) + '</small>' : '');
            tHist.row.add(['<strong>' + escHtml(s.numero) + '</strong>', badgeTipo(s.tipo), escHtml(s.fecha), escHtml(s.destino),
                s.tipo === 'BAJA' || s.tipo === 'REPARACION' ? escHtml(s.activos) : escHtml(s.recibidos) + ' / ' + escHtml(s.activos) + ' recibidos', estado, escHtml(s.remision), acc]);
        });
        tHist.draw();
    }, 'json');
}
function ver(id) {
    var s = sols[id];
    $.post(URL_AM, { tipo: 'detalleSolicitud', id: id }, function (l) {
        var h = '<p>' + badgeTipo(s.tipo) + ' ' + escHtml(s.fecha) + ' - Solicitó: ' + escHtml(s.solicitante) + '<br><b>' + escHtml(s.destino) + '</b>' +
            (s.transportista ? '<br>Transportista: ' + escHtml(s.transportista) : '') + (s.observacion ? '<br>Observación: ' + escHtml(s.observacion) : '') + '</p>' +
            '<table class="table table-sm table-bordered"><thead><tr><th>N° activo</th><th>Serie</th><th>Descripción</th><th>Origen</th><th>Estado</th></tr></thead><tbody>';
        $.each(l, function (i, a) {
            h += '<tr><td><strong>' + escHtml(a.numero_activo) + '</strong></td><td>' + escHtml(a.numero_serie || '-') + '</td><td>' + escHtml(a.descripcion) + '</td><td>' + escHtml(a.origen) +
                 '</td><td>' + escHtml(LINEA[a.estado_linea] || a.estado_linea) + (a.fecha_recepcion ? '<br><small>' + escHtml(a.fecha_recepcion) + '</small>' : '') +
                 (a.observacion_recepcion ? '<br><small>' + escHtml(a.observacion_recepcion) + '</small>' : '') + '</td></tr>';
        });
        $('#detTitulo').text('Solicitud ' + s.numero); $('#detCuerpo').html(h + '</tbody></table>'); $('#modalDetalle').modal('show');
    }, 'json');
}
function aprobar(id) {
    var s = sols[id];
    if (!confirmado('¿Aprobar la solicitud ' + s.numero + '? ' + (s.tipo === 'BAJA' ? 'La baja es definitiva.' : 'Se generará la remisión.'), this, arguments)) return;
    $.post(URL_AM, { tipo: 'aprobar', id: id }, function (r) {
        if (!r.ok) { alert(r.error); return; }
        mensaje('success', 'Solicitud <b>' + escHtml(s.numero) + '</b> aprobada el ' + escHtml(r.fecha) + '. ' + (s.tipo === 'BAJA'
            ? '<button class="btn btn-light btn-sm" onclick="imprimirActa(' + id + ')"><i class="fas fa-file-pdf"></i> Imprimir acta de baja</button>'
            : 'Remisión <b>' + escHtml(r.remision) + '</b> <button class="btn btn-light btn-sm" onclick="imprimirRemision(' + parseInt(r.id_remision, 10) + ')"><i class="fas fa-print"></i> Imprimir</button>'));
        refrescar();
    }, 'json');
}
function rechazar(id) {
    abrirMotivo('Rechazar ' + sols[id].numero, 'Los activos quedan liberados y no se genera remisión.', 'Motivo *',
        function (texto) { return { tipo: 'rechazar', id: id, motivo: texto }; }, 'Solicitud rechazada.');
}
function anularSalida(id) {
    abrirMotivo('Anular salida ' + sols[id].numero, 'La remisión queda anulada y los activos vuelven a su estado y ubicación de origen. Solo es posible si ningún activo fue recibido.', 'Motivo *',
        function (texto) { return { tipo: 'anularSalida', id: id, motivo: texto }; }, 'Salida y remisión anuladas.');
}
function abrirMotivo(titulo, ayuda, label, datos, exito) {
    accionMotivo = { datos: datos, exito: exito };
    $('#motTitulo').text(titulo); $('#motAyuda').text(ayuda); $('#motLabel').text(label); $('#motTexto').val(''); $('#motMensaje').html('');
    $('#motConfirmar').prop('disabled', false); $('#modalMotivo').modal('show');
}
function confirmarMotivo() {
    var texto = $.trim($('#motTexto').val()); if (!texto) { alert('Este dato es obligatorio.'); return; }
    var $b = $('#motConfirmar').prop('disabled', true);
    $.post(URL_AM, accionMotivo.datos(texto), function (r) {
        if (r.ok) { $('#modalMotivo').modal('hide'); mensaje('warning', accionMotivo.exito + ' (' + escHtml(r.fecha) + ')'); refrescar(); }
        else { $('#motMensaje').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); $b.prop('disabled', false); }
    }, 'json').fail(function () { $b.prop('disabled', false); });
}
function imprimirRemision(id) { window.open('InventarioServlet?tipo=imprimirRemision&id=' + encodeURIComponent(id), '_blank'); }
function imprimirActa(id) { window.open(URL_AM + '?tipo=imprimirActaBaja&id=' + encodeURIComponent(id), '_blank'); }
</script>
</body>
</html>
