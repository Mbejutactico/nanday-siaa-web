<%@include file="header.jsp" %>
<%-- ORDEN DE TRABAJO > Suministrar: insumos (código, nombre, cantidad, almacén de salida).
     El stock se descuenta al confirmar y vuelve si se quita una línea. --%>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <h1><i class="fas fa-dolly"></i> Suministrar Orden de Trabajo</h1>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-8 form-group mb-0">
                            <label>Orden de trabajo (pendientes y en proceso)</label>
                            <select class="form-control" id="comboOT">
                                <option value="">-- Seleccionar orden de trabajo --</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            <div id="panelOT" style="display:none;">
                <div class="card card-outline card-info">
                    <div class="card-header"><h3 class="card-title" id="otCabecera"></h3></div>
                    <div class="card-body" id="otDatos"></div>
                </div>

                <div class="row">
                    <div class="col-lg-6">
                        <div class="card card-outline card-primary">
                            <div class="card-header"><h3 class="card-title"><i class="fas fa-plus-circle"></i> Agregar insumos</h3></div>
                            <div class="card-body">
                                <div class="form-group">
                                    <label>Insumo (código - nombre) *</label>
                                    <select class="form-control" id="comboInsumo"></select>
                                </div>
                                <div class="form-row">
                                    <div class="col-md-8 form-group">
                                        <label>Almacén de salida *</label>
                                        <select class="form-control" id="comboAlmacen" disabled>
                                            <option value="">-- Primero elija el insumo --</option>
                                        </select>
                                    </div>
                                    <div class="col-md-4 form-group">
                                        <label>Cantidad *</label>
                                        <input type="number" class="form-control" id="cantidad" min="1" value="1">
                                    </div>
                                </div>
                                <button type="button" class="btn btn-outline-primary" id="btnAgregar"><i class="fas fa-plus"></i> Agregar a la lista</button>

                                <h6 class="mt-3">Insumos por entregar</h6>
                                <table class="table table-sm table-bordered">
                                    <thead><tr><th>Código</th><th>Nombre</th><th>Cantidad</th><th>Almacén</th><th></th></tr></thead>
                                    <tbody id="cuerpoPendientes"><tr><td colspan="5" class="text-center text-muted">Todavía no agregó insumos</td></tr></tbody>
                                </table>
                                <div id="mensajeSuministro"></div>
                                <button type="button" class="btn btn-success btn-block" id="btnConfirmar" disabled><i class="fas fa-check"></i> Confirmar suministro (descuenta stock)</button>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card card-outline card-success">
                            <div class="card-header"><h3 class="card-title"><i class="fas fa-check-circle"></i> Insumos ya suministrados</h3></div>
                            <div class="card-body" id="suministrados"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var URL_OT = "OrdenTrabajoServlet";
var pendientes = [];      // [{id_insumo, codigo, nombre, id_almacen, almacen, cantidad}]
var stockAlmacenes = {};  // id_almacen -> stock disponible del insumo elegido

function fechaLegible(iso) {
    if (!iso) return '';
    var p = iso.substring(0, 10).split('-');
    return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] + (iso.length > 10 ? ' ' + iso.substring(11, 16) : '') : iso;
}

$(document).ready(function () {
    var m = /[?&]ot=([0-9]+)/.exec(location.search);
    cargarOTs(m ? m[1] : '');
    $.post(URL_OT, { tipo: 'listarInsumos' }, function (html) { $('#comboInsumo').html(html); });

    $('#comboOT').change(function () { pendientes = []; dibujarPendientes(); cargarOT($(this).val()); });
    $('#comboInsumo').change(cargarAlmacenes);
    $('#btnAgregar').click(agregar);
    $('#btnConfirmar').click(confirmar);
});

function cargarOTs(seleccionar) {
    $.post(URL_OT, { tipo: 'listarAbiertas' }, function (html) {
        $('#comboOT').html(html);
        if (seleccionar && $('#comboOT option[value="' + seleccionar + '"]').length) {
            $('#comboOT').val(seleccionar).change();
        }
    });
}

function cargarOT(id) {
    if (!id) { $('#panelOT').hide(); return; }
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'detalle', id: id },
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            $('#otCabecera').html('<strong>' + escHtml(f.numero) + '</strong> - ' + escHtml(f.titulo));
            $('#otDatos').html('<div class="row">' +
                '<div class="col-md-3"><small class="text-muted">Solicitante</small><br>' + escHtml(f.solicitante) + '</div>' +
                '<div class="col-md-3"><small class="text-muted">Centro</small><br>' + escHtml(f.centro) + '</div>' +
                '<div class="col-md-3"><small class="text-muted">Centro de costo</small><br>' + escHtml(f.centro_costo) + '</div>' +
                '<div class="col-md-3"><small class="text-muted">Fecha</small><br>' + escHtml(fechaLegible(f.fecha)) + '</div>' +
                (f.descripcion ? '<div class="col-12 mt-2"><small class="text-muted">Descripción</small><br>' + escHtml(f.descripcion).replace(/\n/g, '<br>') + '</div>' : '') +
                '</div>');
            dibujarSuministrados(f.items || []);
            $('#panelOT').show();
        }
    });
}

function dibujarSuministrados(items) {
    if (items.length === 0) {
        $('#suministrados').html('<p class="text-muted">Todavía no se entregaron insumos para esta orden.</p>');
        return;
    }
    var h = '<table class="table table-sm table-striped"><thead><tr><th>Código</th><th>Nombre</th><th>Cant.</th><th>Almacén</th><th>Fecha</th><th></th></tr></thead><tbody>';
    $.each(items, function (i, it) {
        h += '<tr><td>' + escHtml(it.codigo) + '</td><td>' + escHtml(it.descripcion) + '</td><td><strong>' + escHtml(it.cantidad) + '</strong> ' + escHtml(it.unidad) +
            '</td><td>' + escHtml(it.almacen) + '</td><td><small>' + escHtml(fechaLegible(it.fecha)) + '<br>' + escHtml(it.suministrado_por) + '</small></td>' +
            '<td>' + (it.remision
                ? '<span class="badge badge-success" title="Ya salió con esta remisión; para quitarlo, anule la remisión en Salida de insumos">' + escHtml(it.remision) + '</span>'
                : '<button class="btn btn-outline-danger btn-sm" title="Quitar (el stock vuelve al almacén)" onclick="quitar(' + parseInt(it.id, 10) + ')"><i class="fas fa-undo"></i></button>') + '</td></tr>';
    });
    $('#suministrados').html(h + '</tbody></table>');
}

// Vuelve a cargar los insumos (con su disponible) conservando el elegido
function recargarInsumos() {
    var elegido = $('#comboInsumo').val();
    $.post(URL_OT, { tipo: 'listarInsumos' }, function (html) {
        $('#comboInsumo').html(html);
        if (elegido) $('#comboInsumo').val(elegido);
        cargarAlmacenes();
    });
}

function cargarAlmacenes() {
    var idInsumo = $('#comboInsumo').val();
    stockAlmacenes = {};
    if (!idInsumo) {
        $('#comboAlmacen').html('<option value="">-- Primero elija el insumo --</option>').prop('disabled', true);
        return;
    }
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'almacenesConStock', id_insumo: idInsumo },
        success: function (lista) {
            if (lista.length === 0) {
                $('#comboAlmacen').html('<option value="">Sin stock en ningún almacén</option>').prop('disabled', true);
                return;
            }
            var h = '<option value="">-- Seleccionar almacén --</option>';
            $.each(lista, function (i, a) {
                // Descuenta lo que ya está en la lista por entregar
                var reservado = 0;
                $.each(pendientes, function (j, p) { if (p.id_insumo == idInsumo && p.id_almacen == a.id) reservado += p.cantidad; });
                var disp = a.stock - reservado;
                stockAlmacenes[a.id] = disp;
                h += '<option value="' + parseInt(a.id, 10) + '"' + (disp <= 0 ? ' disabled' : '') + '>' + escHtml(a.almacen) + ' (' + escHtml(a.centro) + ') - disponible: ' + disp + '</option>';
            });
            $('#comboAlmacen').html(h).prop('disabled', false);
        }
    });
}

function agregar() {
    var $ins = $('#comboInsumo option:selected');
    var idInsumo = $('#comboInsumo').val(), idAlmacen = $('#comboAlmacen').val();
    var cant = $.trim($('#cantidad').val());
    if (!idInsumo) { alert('Seleccione el insumo.'); return; }
    if (!idAlmacen) { alert('Seleccione el almacén de salida.'); return; }
    if (!/^[0-9]+$/.test(cant) || parseInt(cant, 10) <= 0) { alert('La cantidad debe ser un número entero mayor a 0.'); return; }
    cant = parseInt(cant, 10);
    if (cant > (stockAlmacenes[idAlmacen] || 0)) { alert('La cantidad supera lo disponible en ese almacén (' + (stockAlmacenes[idAlmacen] || 0) + ').'); return; }

    var existente = null;
    $.each(pendientes, function (i, p) { if (p.id_insumo == idInsumo && p.id_almacen == idAlmacen) existente = p; });
    if (existente) {
        existente.cantidad += cant;
    } else {
        pendientes.push({
            id_insumo: parseInt(idInsumo, 10), codigo: $ins.data('codigo'), nombre: $ins.data('nombre'),
            id_almacen: parseInt(idAlmacen, 10), almacen: $('#comboAlmacen option:selected').text().split(' - disponible')[0], cantidad: cant
        });
    }
    $('#cantidad').val(1);
    dibujarPendientes();
    cargarAlmacenes();   // actualiza lo disponible
}

function quitarPendiente(i) {
    pendientes.splice(i, 1);
    dibujarPendientes();
    cargarAlmacenes();
}

function dibujarPendientes() {
    if (pendientes.length === 0) {
        $('#cuerpoPendientes').html('<tr><td colspan="5" class="text-center text-muted">Todavía no agregó insumos</td></tr>');
        $('#btnConfirmar').prop('disabled', true);
        return;
    }
    var h = '';
    $.each(pendientes, function (i, p) {
        h += '<tr><td>' + escHtml(p.codigo) + '</td><td>' + escHtml(p.nombre) + '</td><td>' + p.cantidad + '</td><td>' + escHtml(p.almacen) +
            '</td><td><button class="btn btn-outline-danger btn-sm" onclick="quitarPendiente(' + i + ')"><i class="fas fa-times"></i></button></td></tr>';
    });
    $('#cuerpoPendientes').html(h);
    $('#btnConfirmar').prop('disabled', false);
}

function confirmar() {
    var idOT = $('#comboOT').val();
    if (!idOT || pendientes.length === 0) return;
    if (!confirmado('¿Confirmar la entrega de ' + pendientes.length + ' insumo(s)? Se descontará el stock de los almacenes.', this, arguments)) return;
    var $b = $('#btnConfirmar').prop('disabled', true);
    var items = $.map(pendientes, function (p) { return { id_insumo: p.id_insumo, id_almacen: p.id_almacen, cantidad: p.cantidad }; });
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'suministrar', id_ot: idOT, items: JSON.stringify(items) },
        success: function (r) {
            if (r.ok) {
                $('#mensajeSuministro').html('<div class="alert alert-success">Suministro registrado. El stock fue descontado.</div>');
                pendientes = [];
                dibujarPendientes();
                recargarInsumos();
                cargarOT(idOT);
            } else {
                $('#mensajeSuministro').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>');
                $b.prop('disabled', false);
            }
        },
        error: function () {
            $('#mensajeSuministro').html('<div class="alert alert-danger">Error al registrar el suministro.</div>');
            $b.prop('disabled', false);
        }
    });
}

function quitar(idDetalle) {
    if (!confirmado('¿Quitar este insumo de la orden? La cantidad vuelve al stock del almacén.', this, arguments)) return;
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json', data: { tipo: 'quitarSuministro', id_detalle: idDetalle },
        success: function (r) {
            if (r.ok) {
                cargarOT($('#comboOT').val());
                recargarInsumos();
            } else {
                alert(r.error);
            }
        }
    });
}
</script>
</body>
</html>
