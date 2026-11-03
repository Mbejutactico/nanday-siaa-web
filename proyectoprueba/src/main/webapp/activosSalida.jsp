<%@include file="header.jsp" %>
<%-- ACTIVOS FIJOS > Salida: 1) datos (tipo, destino/proveedor, transportista) 2) selección múltiple de activos.
     Queda como solicitud pendiente hasta que se apruebe en Aprobaciones (ahí se genera la remisión). --%>
<style>
    .tipo-salida { cursor: pointer; border: 2px solid #dee2e6; transition: all .15s; }
    .tipo-salida:hover { border-color: #80bdff; }
    .tipo-salida.activo { border-color: #2E7D32; background: rgba(46,125,50,.08); }
    #tablaActivos tr.no-sel td { color: #999; }
</style>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-dolly-flatbed"></i> Salida de Activos Fijos</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card card-primary card-outline" id="paso1">
            <div class="card-header"><h3 class="card-title"><span class="badge badge-primary">1</span> Datos de la salida</h3></div>
            <div class="card-body">
                <label>Tipo de salida *</label>
                <div class="row mb-3">
                    <div class="col-md-4"><div class="card tipo-salida mb-2" data-tipo="REPARACION"><div class="card-body text-center py-3">
                        <i class="fas fa-tools fa-2x text-warning"></i><h6 class="mt-2 mb-0">Reparación</h6><small class="text-muted">Al proveedor; sigue en el inventario como "en reparación"</small></div></div></div>
                    <div class="col-md-4"><div class="card tipo-salida mb-2" data-tipo="TRANSFERENCIA"><div class="card-body text-center py-3">
                        <i class="fas fa-truck fa-2x text-primary"></i><h6 class="mt-2 mb-0">Transferencia entre sucursales</h6><small class="text-muted">A otro centro y centro de costo</small></div></div></div>
                    <div class="col-md-4"><div class="card tipo-salida mb-2" data-tipo="ENTREGA_OT"><div class="card-body text-center py-3">
                        <i class="fas fa-clipboard-list fa-2x text-success"></i><h6 class="mt-2 mb-0">Entrega por Orden de Trabajo</h6><small class="text-muted">Al sector que pidió la OT</small></div></div></div>
                </div>
                <div class="form-row">
                    <div class="col-md-6 form-group grp grp-REPARACION"><label>Proveedor que repara *</label><select class="form-control" id="proveedor"></select></div>
                    <div class="col-md-6 form-group grp grp-TRANSFERENCIA"><label>Centro de destino *</label><select class="form-control" id="centroDestino"></select></div>
                    <div class="col-md-6 form-group grp grp-TRANSFERENCIA"><label>Centro de costo de destino *</label><select class="form-control" id="ccDestino"><option value="">-- Elija el centro --</option></select></div>
                    <div class="col-md-12 form-group grp grp-ENTREGA_OT"><label>Orden de trabajo *</label><select class="form-control" id="ot"></select>
                        <small class="text-muted">Los activos se entregan al centro y centro de costo de la OT. La salida aprobada permite finalizar la OT.</small></div>
                </div>
                <h6><i class="fas fa-user"></i> Transportista</h6>
                <div class="form-row">
                    <div class="col-md-4 form-group"><label>Nombre *</label><input class="form-control" id="tNombre" maxlength="100"></div>
                    <div class="col-md-4 form-group"><label>Apellido *</label><input class="form-control" id="tApellido" maxlength="100"></div>
                    <div class="col-md-4 form-group"><label>Cédula de identidad *</label><input class="form-control" id="tCi" maxlength="20"></div>
                    <div class="col-md-4 form-group"><label>Vehículo: marca</label><input class="form-control" id="vMarca" maxlength="50"></div>
                    <div class="col-md-4 form-group"><label>Modelo</label><input class="form-control" id="vModelo" maxlength="50"></div>
                    <div class="col-md-4 form-group"><label>N° de chapa</label><input class="form-control" id="vChapa" maxlength="20"></div>
                </div>
                <div class="form-group"><label>Observación</label><textarea class="form-control" id="observacion" rows="2" maxlength="2000"></textarea></div>
                <div class="text-right"><button class="btn btn-primary" id="btnContinuar">Continuar: seleccionar activos <i class="fas fa-arrow-right"></i></button></div>
            </div>
        </div>
        <div class="card card-primary card-outline" id="paso2" style="display:none;">
            <div class="card-header"><h3 class="card-title"><span class="badge badge-primary">2</span> Seleccione los activos <span class="text-muted" id="resumenPaso1"></span></h3></div>
            <div class="card-body">
                <div class="form-row align-items-center mb-2">
                    <div class="col-md-6"><input class="form-control" id="buscar" placeholder="Buscar por N° de activo, serie, descripción, centro..."></div>
                    <div class="col-md-6 text-right"><span class="badge badge-info p-2" id="contador">0 seleccionados</span></div>
                </div>
                <div class="table-responsive" style="max-height:520px; overflow-y:auto;">
                    <table class="table table-sm table-bordered table-hover" id="tablaActivos">
                        <thead class="thead-light"><tr><th style="width:40px"><input type="checkbox" id="todos" title="Seleccionar todos los visibles"></th><th>N° activo</th><th>N° serie</th><th>Descripción</th><th>Estado</th><th>Centro</th><th>Centro de costo</th><th>Observación</th></tr></thead>
                        <tbody></tbody>
                    </table>
                </div>
                <div id="mensaje" class="mt-2"></div>
                <div class="d-flex justify-content-between mt-2">
                    <button class="btn btn-secondary" id="btnVolver"><i class="fas fa-arrow-left"></i> Volver a los datos</button>
                    <button class="btn btn-success" id="btnEnviar"><i class="fas fa-paper-plane"></i> Enviar a aprobación</button>
                </div>
            </div>
        </div>
    </div></div>
</div>
<%@include file="footer.jsp" %>
<script>
var URL_AM = "SolicitudActivoServlet", tipoSalida = '', activos = [];
var ESTADOS = { DISPONIBLE: ['Disponible', 'badge-success'], ASIGNADO: ['Con responsable', 'badge-primary'], EN_TRANSITO: ['En tránsito', 'badge-warning'], EN_REPARACION: ['En reparación', 'badge-warning'] };
$(document).ready(function () {
    $('.grp').hide();
    $.post(URL_AM, { tipo: 'listarProveedores' }, function (h) { $('#proveedor').html(h); });
    $.post(URL_AM, { tipo: 'listarCentros' }, function (h) { $('#centroDestino').html(h); });
    $.post(URL_AM, { tipo: 'listarOTAbiertas' }, function (h) { $('#ot').html(h); });
    $('.tipo-salida').click(function () {
        tipoSalida = $(this).data('tipo');
        $('.tipo-salida').removeClass('activo'); $(this).addClass('activo');
        $('.grp').hide(); $('.grp-' + tipoSalida).show();
    });
    $('#centroDestino').change(function () { $.post(URL_AM, { tipo: 'listarCentrosCosto', id_centro: $(this).val() }, function (h) { $('#ccDestino').html(h); }); });
    $('#btnContinuar').click(continuar);
    $('#btnVolver').click(function () { $('#paso2').hide(); $('#paso1').show(); });
    $('#buscar').on('input', filtrar);
    $('#todos').change(function () { var c = this.checked; $('#tablaActivos tbody tr:visible .sel:not(:disabled)').prop('checked', c); contar(); });
    $('#tablaActivos').on('change', '.sel', contar);
    $('#btnEnviar').click(enviar);
});
function continuar() {
    if (!tipoSalida) { alert('Seleccione el tipo de salida.'); return; }
    if (tipoSalida === 'REPARACION' && !$('#proveedor').val()) { alert('Seleccione el proveedor.'); return; }
    if (tipoSalida === 'TRANSFERENCIA' && (!$('#centroDestino').val() || !$('#ccDestino').val())) { alert('Seleccione el centro y el centro de costo de destino.'); return; }
    if (tipoSalida === 'ENTREGA_OT' && !$('#ot').val()) { alert('Seleccione la orden de trabajo.'); return; }
    if (!$.trim($('#tNombre').val()) || !$.trim($('#tApellido').val()) || !$.trim($('#tCi').val())) { alert('Complete nombre, apellido y cédula de identidad del transportista.'); return; }
    var destino = tipoSalida === 'REPARACION' ? 'a ' + $('#proveedor option:selected').text()
        : (tipoSalida === 'TRANSFERENCIA' ? 'a ' + $('#centroDestino option:selected').text() : 'por ' + $('#ot option:selected').text().split(' (')[0]);
    $('#resumenPaso1').text('- ' + $('.tipo-salida.activo h6').text() + ' ' + destino);
    $('#mensaje').html('');
    $.ajax({ type: 'post', url: URL_AM, dataType: 'json', data: { tipo: 'activos' }, success: function (lista) {
        activos = lista; var destinoCentro = tipoSalida === 'TRANSFERENCIA' ? parseInt($('#centroDestino').val(), 10) : 0, h = '';
        $.each(lista, function (i, a) {
            var motivo = a.motivo, sel = a.seleccionable;
            if (sel && destinoCentro && a.id_centro === destinoCentro) { sel = false; motivo = 'Ya está en el centro de destino'; }
            var e = ESTADOS[a.estado] || [a.estado, 'badge-light'];
            h += '<tr class="' + (sel ? '' : 'no-sel') + '" data-texto="' + escHtml((a.numero_activo + ' ' + a.numero_serie + ' ' + a.descripcion + ' ' + a.codigo + ' ' + a.centro + ' ' + a.centro_costo).toLowerCase()) + '">' +
                '<td class="text-center"><input type="checkbox" class="sel" value="' + parseInt(a.id, 10) + '"' + (sel ? '' : ' disabled') + '></td>' +
                '<td><strong>' + escHtml(a.numero_activo) + '</strong></td><td>' + escHtml(a.numero_serie || '-') + '</td><td>' + escHtml(a.descripcion) + '</td>' +
                '<td><span class="badge ' + e[1] + '">' + escHtml(e[0]) + '</span></td><td>' + escHtml(a.centro) + '</td><td>' + escHtml(a.centro_costo) + '</td>' +
                '<td><small class="text-muted">' + escHtml(motivo) + '</small></td></tr>';
        });
        $('#tablaActivos tbody').html(h || '<tr><td colspan="8" class="text-center text-muted">No hay activos</td></tr>');
        $('#buscar').val(''); $('#todos').prop('checked', false); contar();
        $('#paso1').hide(); $('#paso2').show();
    } });
}
function filtrar() {
    var q = $.trim($('#buscar').val()).toLowerCase();
    $('#tablaActivos tbody tr').each(function () { $(this).toggle(!q || String($(this).data('texto')).indexOf(q) >= 0); });
}
function contar() { $('#contador').text($('.sel:checked').length + ' seleccionados'); }
function enviar() {
    var ids = $('.sel:checked').map(function () { return parseInt(this.value, 10); }).get();
    if (!ids.length) { alert('Seleccione al menos un activo.'); return; }
    if (!confirmado('¿Enviar la solicitud de salida de ' + ids.length + ' activo(s) a aprobación?', this, arguments)) return;
    var $b = $('#btnEnviar').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_AM, dataType: 'json', data: {
        tipo: 'crearSolicitud', tipo_solicitud: tipoSalida, activos: JSON.stringify(ids), observacion: $('#observacion').val(),
        id_proveedor: $('#proveedor').val(), id_centro_destino: $('#centroDestino').val(), id_centro_costo_destino: $('#ccDestino').val(), id_ot: $('#ot').val(),
        transportista_nombre: $('#tNombre').val(), transportista_apellido: $('#tApellido').val(), transportista_ci: $('#tCi').val(),
        vehiculo_marca: $('#vMarca').val(), vehiculo_modelo: $('#vModelo').val(), vehiculo_chapa: $('#vChapa').val() },
        success: function (r) {
            $b.prop('disabled', false);
            if (r.ok) {
                $('#mensaje').html('<div class="alert alert-success"><i class="fas fa-check-circle"></i> Solicitud <b>' + escHtml(r.numero) + '</b> registrada el ' + escHtml(r.fecha) +
                    ' y enviada a aprobación. La remisión se genera cuando se aprueba. <a href="activosAprobaciones.jsp" class="alert-link">Ir a Aprobaciones</a></div>');
                $('#btnEnviar').hide();
                $('.sel').prop('disabled', true);
            } else { $('#mensaje').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); }
        }, error: function () { $b.prop('disabled', false); } });
}
</script>
</body>
</html>
