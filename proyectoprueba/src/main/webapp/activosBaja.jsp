<%@include file="header.jsp" %>
<%-- ACTIVOS FIJOS > Baja: motivo + selección múltiple. Pasa por Aprobaciones; al aprobarse la baja es definitiva y se imprime el acta. --%>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-ban"></i> Baja de Activos Fijos</h1></div></div>
    <div class="content"><div class="container-fluid">
        <div class="card card-danger card-outline"><div class="card-body">
            <div class="form-row">
                <div class="col-md-4 form-group"><label>Motivo de la baja *</label>
                    <select class="form-control" id="motivo">
                        <option value="">-- Seleccionar --</option>
                        <option value="OBSOLESCENCIA">Obsolescencia</option>
                        <option value="ROTURA_IRREPARABLE">Rotura irreparable</option>
                        <option value="ROBO_PERDIDA">Robo / pérdida</option>
                        <option value="VENTA">Venta</option>
                        <option value="DONACION">Donación</option>
                        <option value="OTRO">Otro</option>
                    </select></div>
                <div class="col-md-8 form-group"><label>Descripción / justificación *</label><input class="form-control" id="observacion" maxlength="2000" placeholder="Ej.: equipo sin reparación posible según informe técnico N° 123"></div>
            </div>
            <div class="alert alert-warning py-2"><i class="fas fa-exclamation-triangle"></i> La baja es <b>definitiva</b> una vez aprobada. Solo se pueden dar de baja activos disponibles o con responsable
                (si está en reparación, primero registre su retorno en Aprobaciones &rarr; Recepción).</div>
            <div class="form-row align-items-center mb-2">
                <div class="col-md-6"><input class="form-control" id="buscar" placeholder="Buscar por N° de activo, serie, descripción, centro..."></div>
                <div class="col-md-6 text-right"><span class="badge badge-danger p-2" id="contador">0 seleccionados</span></div>
            </div>
            <div class="table-responsive" style="max-height:480px; overflow-y:auto;">
                <table class="table table-sm table-bordered table-hover" id="tablaActivos">
                    <thead class="thead-light"><tr><th style="width:40px"></th><th>N° activo</th><th>N° serie</th><th>Descripción</th><th>Centro</th><th>Centro de costo</th><th>Observación</th></tr></thead>
                    <tbody></tbody>
                </table>
            </div>
            <div id="mensaje" class="mt-2"></div>
            <div class="text-right mt-2"><button class="btn btn-danger" id="btnEnviar"><i class="fas fa-paper-plane"></i> Enviar baja a aprobación</button></div>
        </div></div>
    </div></div>
</div>
<%@include file="footer.jsp" %>
<script>
var URL_AM = "SolicitudActivoServlet";
$(document).ready(function () {
    $.ajax({ type: 'post', url: URL_AM, dataType: 'json', data: { tipo: 'activos', baja: 'true' }, success: function (lista) {
        var h = '';
        $.each(lista, function (i, a) {
            h += '<tr' + (a.seleccionable ? '' : ' class="text-muted"') + ' data-texto="' + escHtml((a.numero_activo + ' ' + a.numero_serie + ' ' + a.descripcion + ' ' + a.centro + ' ' + a.centro_costo).toLowerCase()) + '">' +
                '<td class="text-center"><input type="checkbox" class="sel" value="' + parseInt(a.id, 10) + '"' + (a.seleccionable ? '' : ' disabled') + '></td>' +
                '<td><strong>' + escHtml(a.numero_activo) + '</strong></td><td>' + escHtml(a.numero_serie || '-') + '</td><td>' + escHtml(a.descripcion) + '</td><td>' + escHtml(a.centro) +
                '</td><td>' + escHtml(a.centro_costo) + '</td><td><small>' + escHtml(a.motivo) + '</small></td></tr>';
        });
        $('#tablaActivos tbody').html(h || '<tr><td colspan="7" class="text-center text-muted">No hay activos</td></tr>');
    } });
    $('#buscar').on('input', function () {
        var q = $.trim($(this).val()).toLowerCase();
        $('#tablaActivos tbody tr').each(function () { $(this).toggle(!q || String($(this).data('texto')).indexOf(q) >= 0); });
    });
    $('#tablaActivos').on('change', '.sel', function () { $('#contador').text($('.sel:checked').length + ' seleccionados'); });
    $('#btnEnviar').click(function () {
        var ids = $('.sel:checked').map(function () { return parseInt(this.value, 10); }).get();
        if (!$('#motivo').val() || !$.trim($('#observacion').val())) { alert('Seleccione el motivo y escriba la justificación.'); return; }
        if (!ids.length) { alert('Seleccione al menos un activo.'); return; }
        if (!confirmado('¿Enviar la baja de ' + ids.length + ' activo(s) a aprobación?', this, arguments)) return;
        var $b = $(this).prop('disabled', true);
        $.ajax({ type: 'post', url: URL_AM, dataType: 'json', data: { tipo: 'crearSolicitud', tipo_solicitud: 'BAJA', motivo_baja: $('#motivo').val(), observacion: $('#observacion').val(), activos: JSON.stringify(ids) },
            success: function (r) {
                $b.prop('disabled', false);
                if (r.ok) {
                    $('#mensaje').html('<div class="alert alert-success">Solicitud de baja <b>' + escHtml(r.numero) + '</b> registrada el ' + escHtml(r.fecha) +
                        ' y enviada a aprobación. <a href="activosAprobaciones.jsp" class="alert-link">Ir a Aprobaciones</a></div>');
                    $b.hide(); $('.sel').prop('disabled', true);
                } else { $('#mensaje').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>'); }
            }, error: function () { $b.prop('disabled', false); } });
    });
});
</script>
</body>
</html>
