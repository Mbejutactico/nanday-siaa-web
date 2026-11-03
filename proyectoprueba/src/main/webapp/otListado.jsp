<%@include file="header.jsp" %>
<%-- ORDEN DE TRABAJO > Visualizar, modificar y eliminar --%>
<style>
    #tablaOT tbody tr {
        cursor: pointer;
    }
    #tablaOT tbody tr:hover td {
        background-color: rgba(0, 123, 255, 0.08);
    }
    #modalEditarOT, #modalEliminarOT {
        z-index: 1060;
    }
</style>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <h1><i class="fas fa-clipboard-list"></i> Órdenes de Trabajo</h1>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-info"><div class="inner"><h3 id="resTotal">0</h3><p>Total de OT</p></div><div class="icon"><i class="fas fa-clipboard-list"></i></div></div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-secondary"><div class="inner"><h3 id="resPendiente">0</h3><p>Pendientes (sin insumos)</p></div><div class="icon"><i class="fas fa-hourglass-start"></i></div></div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-warning"><div class="inner"><h3 id="resProceso">0</h3><p>En proceso</p></div><div class="icon"><i class="fas fa-cogs"></i></div></div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-success"><div class="inner"><h3 id="resFinalizada">0</h3><p>Finalizadas</p></div><div class="icon"><i class="fas fa-check-double"></i></div></div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <a href="otCrear.jsp" class="btn btn-success"><i class="fas fa-plus"></i> Crear orden de trabajo</a>
                    <div class="float-right ml-2" style="min-width:220px;">
                        <select class="form-control" id="filtroMias" title="Filtrar por su participación">
                            <option value="">Todas las que puedo ver</option>
                            <option value="SOLICITADAS">Pedidas por mí</option>
                            <option value="A_CARGO">A mi cargo (soy responsable)</option>
                        </select>
                    </div>
                    <div class="float-right" style="min-width:220px;">
                        <select class="form-control" id="filtroEstado">
                            <option value="">Todos los estados</option>
                            <option value="PENDIENTE">Pendientes</option>
                            <option value="EN_PROCESO">En proceso</option>
                            <option value="FINALIZADA">Finalizadas</option>
                        </select>
                    </div>
                </div>
                <div class="card-body">
                    <small class="text-muted"><i class="fas fa-mouse-pointer"></i> Haga clic en una orden para ver su detalle.</small>
                    <table id="tablaOT" class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>N° OT</th>
                                <th>Fecha de creación</th>
                                <th>Título</th>
                                <th>Centro destino</th>
                                <th>Centro de costo</th>
                                <th>Solicitante</th>
                                <th>Responsable</th>
                                <th>Insumos</th>
                                <th>Estado</th>
                                <th>Acción</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Ficha de la OT -->
<div class="modal fade" id="modalFichaOT" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="tituloFichaOT">Orden de trabajo</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div class="mb-2" id="accionesFichaOT"></div>
                <ul class="nav nav-tabs" role="tablist">
                    <li class="nav-item"><a class="nav-link active" data-toggle="tab" href="#fichaDatos">Datos</a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="tab" href="#fichaInsumos">Insumos suministrados <span class="badge badge-info" id="cantInsumos">0</span></a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="tab" href="#fichaHistorial">Historial</a></li>
                </ul>
                <div class="tab-content pt-3">
                    <div class="tab-pane fade show active" id="fichaDatos"></div>
                    <div class="tab-pane fade" id="fichaInsumos"></div>
                    <div class="tab-pane fade" id="fichaHistorial"></div>
                </div>
            </div>
            <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button></div>
        </div>
    </div>
</div>

<!-- Modificar OT -->
<div class="modal fade" id="modalEditarOT" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Modificar orden de trabajo <span id="editNumero" class="text-muted"></span></h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editId">
                <div class="form-group">
                    <label>Centro destino *</label>
                    <select class="form-control" id="editCentro"></select>
                    <small class="text-muted" id="editCentroAyuda"></small>
                </div>
                <div class="form-group">
                    <label>Centro de costo *</label>
                    <select class="form-control" id="editCentroCosto"></select>
                </div>
                <div class="form-group">
                    <label>Responsable <small class="text-muted">(opcional)</small></label>
                    <select class="form-control" id="editResponsable"></select>
                </div>
                <div class="form-group">
                    <label>Título *</label>
                    <input type="text" class="form-control" id="editTitulo" maxlength="200">
                </div>
                <div class="form-group">
                    <label>Descripción <small class="text-muted">(opcional)</small></label>
                    <textarea class="form-control" id="editDescripcion" rows="3" maxlength="2000"></textarea>
                </div>
                <div id="mensajeEditar"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-warning" id="btnGuardarEdicion"><i class="fas fa-save"></i> Guardar cambios</button>
            </div>
        </div>
    </div>
</div>

<!-- Eliminar OT -->
<div class="modal fade" id="modalEliminarOT" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Eliminar orden de trabajo</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="eliminarId">
                <p>¿Seguro que desea eliminar la orden <b id="eliminarNumero"></b>? Esta acción no se puede deshacer.</p>
                <div id="mensajeEliminar"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-danger" id="btnConfirmarEliminar"><i class="fas fa-trash"></i> Eliminar</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
    var URL_OT = "OrdenTrabajoServlet";
    var tablaOT, fichaOT = null;

    var ESTADOS_OT = {
        PENDIENTE: ['Pendiente', 'badge-secondary'],
        EN_PROCESO: ['En proceso', 'badge-warning'],
        FINALIZADA: ['Finalizada', 'badge-success'],
        ANULADA: ['Anulada', 'badge-danger']
    };
    var EVENTOS_OT = {
        CREACION: ['Creación', 'fa-plus', 'bg-info'],
        MODIFICACION: ['Modificación', 'fa-edit', 'bg-warning'],
        SUMINISTRO: ['Suministro de insumos', 'fa-dolly', 'bg-primary'],
        QUITAR_SUMINISTRO: ['Insumo quitado', 'fa-undo', 'bg-secondary'],
        FINALIZACION: ['Finalización', 'fa-check-double', 'bg-success'],
        REAPERTURA: ['Finalización anulada', 'fa-lock-open', 'bg-danger'],
        RESPONSABLE: ['Responsable', 'fa-user-check', 'bg-purple'],
        CAMBIO_CENTRO: ['Cambio de centro destino', 'fa-exchange-alt', 'bg-orange']
    };

    function badgeOT(e) {
        var x = ESTADOS_OT[e] || [e, 'badge-light'];
        return '<span class="badge ' + x[1] + '">' + escHtml(x[0]) + '</span>';
    }
    function fechaLegible(iso) {
        if (!iso)
            return '';
        var p = iso.substring(0, 10).split('-');
        return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] + (iso.length > 10 ? ' ' + iso.substring(11, 16) : '') : iso;
    }
    function abierta(e) {
        return e === 'PENDIENTE' || e === 'EN_PROCESO';
    }

    $(document).ready(function () {
        tablaOT = $('#tablaOT').DataTable({
            responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [],
            columnDefs: [{orderable: false, targets: -1}],
            language: {info: 'Mostrando _START_ a _END_ de _TOTAL_ órdenes', emptyTable: 'No hay órdenes de trabajo'}
        });
        refrescar();
        $('#filtroEstado, #filtroMias').change(cargarTabla);
        $('#editCentro').change(function () {
            cargarCombosEdicion($('#editCentro').val(), '', '');
        });
        var otUrl = /[?&]ot=(\d+)/.exec(location.search);
        if (otUrl)
            verOT(parseInt(otUrl[1], 10));

        $('#tablaOT tbody').on('click', 'tr', function (ev) {
            if ($(ev.target).closest('button').length)
                return;
            var id = $(this).find('.fila-ot').data('id');
            if (id)
                verOT(id);
        });

        $('#btnGuardarEdicion').click(guardarEdicion);
        $('#btnConfirmarEliminar').click(confirmarEliminar);

        // Modal sobre la ficha
        $('#modalEditarOT, #modalEliminarOT').on('shown.bs.modal', function () {
            if ($('#modalFichaOT').hasClass('show'))
                $('.modal-backdrop').last().css('z-index', 1055);
        }).on('hidden.bs.modal', function () {
            if ($('#modalFichaOT').hasClass('show'))
                $('body').addClass('modal-open');
        });
    });

    function refrescar() {
        cargarResumen();
        cargarTabla();
    }

    function cargarResumen() {
        $.post(URL_OT, {tipo: 'resumen'}, function (r) {
            $('#resTotal').text(r.TOTAL || 0);
            $('#resPendiente').text(r.PENDIENTE || 0);
            $('#resProceso').text(r.EN_PROCESO || 0);
            $('#resFinalizada').text(r.FINALIZADA || 0);
        }, 'json');
    }

    function cargarTabla() {
        $.ajax({
            type: 'post', url: URL_OT, dataType: 'json', data: {tipo: 'listar', estado: $('#filtroEstado').val(), mias: $('#filtroMias').val()},
            success: function (lista) {
                tablaOT.clear();
                $.each(lista, function (i, o) {
                    var id = parseInt(o.id, 10);
                    var acciones = '<span class="fila-ot" data-id="' + id + '"></span>' +
                            '<button class="btn btn-info btn-sm" onclick="verOT(' + id + ')" title="Ver"><i class="fas fa-eye"></i></button> ';
                    if (abierta(o.estado) && PERMISOS.OT_GESTION && o.puede_gestionar) {
                        acciones += '<button class="btn btn-warning btn-sm" onclick="editarOT(' + id + ')" title="Modificar"><i class="fas fa-edit"></i></button> ';
                    }
                    // Eliminar: solo quien creó la OT o el administrador
                    if (abierta(o.estado) && o.items === 0 && PERMISOS.OT_GESTION && o.puede_eliminar) {
                        acciones += '<button class="btn btn-danger btn-sm" onclick="eliminarOT(' + id + ',\'' + escHtml(o.numero) + '\')" title="Eliminar"><i class="fas fa-trash"></i></button>';
                    }
                    tablaOT.row.add([
                        '<strong>' + escHtml(o.numero) + '</strong>',
                        escHtml(o.fecha_creacion || fechaLegible(o.fecha)),
                        escHtml(o.titulo),
                        escHtml(o.centro),
                        escHtml(o.centro_costo),
                        escHtml(o.solicitante) + (o.es_mia ? ' <span class="badge badge-light">yo</span>' : ''),
                        o.responsable ? escHtml(o.responsable) + (o.a_mi_cargo ? ' <span class="badge badge-primary">yo</span>' : '') : '<span class="text-muted">-</span>',
                        o.items > 0 ? escHtml(o.items) + ' <small class="text-muted">(' + escHtml(o.unidades) + ' u.)</small>' : '<span class="text-muted">-</span>',
                        badgeOT(o.estado),
                        acciones
                    ]);
                });
                tablaOT.draw();
            },
            error: function () {
                alert('Error al cargar las órdenes de trabajo.');
            }
        });
    }

    function filaFicha(etq, val) {
        return '<tr><th style="width:35%">' + escHtml(etq) + '</th><td>' + (val === '' ? '<span class="text-muted">-</span>' : val) + '</td></tr>';
    }

    function verOT(id, silencioso) {
        $.ajax({
            type: 'post', url: URL_OT, dataType: 'json', data: {tipo: 'detalle', id: id},
            success: function (f) {
                if (f.error) {
                    if (silencioso) {
                        $('#modalFichaOT').modal('hide');
                    } else {
                        alert(f.error);
                    }
                    return;
                }
                fichaOT = f;
                $('#tituloFichaOT').html('<i class="fas fa-clipboard-list"></i> ' + escHtml(f.numero) + ' ' + badgeOT(f.estado) +
                        (f.a_mi_cargo ? ' <span class="badge badge-primary">A su cargo</span>' : '') + (f.es_mia ? ' <span class="badge badge-light">Pedida por usted</span>' : ''));

                var acc = '';
                if (abierta(f.estado)) {
                    if (PERMISOS.OT_SUMINISTRAR && f.puede_operar)
                        acc += '<a href="otSuministrar.jsp?ot=' + f.id + '" class="btn btn-primary btn-sm mr-1"><i class="fas fa-dolly"></i> Suministrar insumos</a>';
                    if (PERMISOS.OT_GESTION && f.puede_gestionar)
                        acc += '<button class="btn btn-warning btn-sm mr-1" onclick="editarOT(' + f.id + ')"><i class="fas fa-edit"></i> Modificar</button>';
                    if (f.items.length === 0 && PERMISOS.OT_GESTION && f.puede_eliminar)
                        acc += '<button class="btn btn-danger btn-sm mr-1" onclick="eliminarOT(' + f.id + ',\'' + escHtml(f.numero) + '\')"><i class="fas fa-trash"></i> Eliminar</button>';
                }
                if (f.pendientes_salida && abierta(f.estado) && PERMISOS.INVENTARIO && f.puede_operar) {
                    acc += '<a href="salidaInsumos.jsp?ot=' + f.id + '" class="btn btn-info btn-sm mr-1"><i class="fas fa-truck-loading"></i> Registrar salida</a>';
                }
                if ((f.finalizable || f.estado === 'FINALIZADA') && PERMISOS.OT_FINALIZAR && f.puede_operar) {
                    acc += '<a href="otFinalizar.jsp" class="btn btn-success btn-sm mr-1"><i class="fas fa-check-double"></i> Ir a Finalizar OT</a>';
                }
                if (!f.puede_operar && abierta(f.estado)) {
                    acc += '<span class="text-muted small ml-1"><i class="fas fa-info-circle"></i> La resuelve: ' + escHtml(f.centro) + '.</span>';
                }
                $('#accionesFichaOT').html(acc);

                $('#fichaDatos').html('<div class="row"><div class="col-md-6"><table class="table table-sm table-bordered">' +
                        filaFicha('N° OT', '<strong>' + escHtml(f.numero) + '</strong>') +
                        filaFicha('Fecha y hora de creación', escHtml(f.fecha_creacion || fechaLegible(f.fecha))) +
                        filaFicha('Estado', badgeOT(f.estado)) +
                        filaFicha('Título', escHtml(f.titulo)) +
                        filaFicha('Descripción', f.descripcion ? escHtml(f.descripcion).replace(/\n/g, '<br>') : '') +
                        '</table></div><div class="col-md-6"><table class="table table-sm table-bordered">' +
                        filaFicha('Solicitante', escHtml(f.solicitante) + (f.solicitante_email ? ' <small class="text-muted">' + escHtml(f.solicitante_email) + '</small>' : '') +
                                (f.centro_solicitante ? '<br><small class="text-muted">de ' + escHtml(f.centro_solicitante) + '</small>' : '')) +
                        filaFicha('Centro destino', '<strong>' + escHtml(f.centro) + '</strong>') +
                        filaFicha('Responsable', f.responsable ? escHtml(f.responsable) + (f.responsable_email ? ' <small class="text-muted">' + escHtml(f.responsable_email) + '</small>' : '') : '') +
                        filaFicha('Centro de costo', escHtml(f.centro_costo)) +
                        (f.estado === 'FINALIZADA'
                                ? filaFicha('Fecha y hora de finalización', escHtml(f.fecha_finalizacion) + ' <small class="text-muted">por ' + escHtml(f.finalizado_por) + '</small>') +
                                filaFicha('Observación', escHtml(f.observacion_finalizacion))
                                : '') +
                        (f.fecha_anulacion_fin
                                ? filaFicha('Última anulación de finalización', escHtml(f.fecha_anulacion_fin) + ' <small class="text-muted">por ' + escHtml(f.anulado_por) + '</small>' +
                                        (f.motivo_anulacion_fin ? '<br><small>Motivo: ' + escHtml(f.motivo_anulacion_fin) + '</small>' : ''))
                                : '') +
                        '</table></div></div>');

                $('#cantInsumos').text(f.items.length);
                if (f.items.length === 0) {
                    $('#fichaInsumos').html('<p class="text-muted">Todavía no se suministraron insumos a esta orden.</p>');
                } else {
                    var h = '<div class="table-responsive"><table class="table table-sm table-striped"><thead><tr><th>Código</th><th>Insumo</th><th>Cantidad</th><th>Almacén de salida</th><th>Fecha</th><th>Entregó</th><th>Salida</th></tr></thead><tbody>';
                    $.each(f.items, function (i, it) {
                        h += '<tr><td>' + escHtml(it.codigo) + '</td><td>' + escHtml(it.descripcion) + '</td><td><strong>' + escHtml(it.cantidad) + '</strong> ' + escHtml(it.unidad) +
                                '</td><td>' + escHtml(it.almacen) + ' <small class="text-muted">(' + escHtml(it.centro_almacen) + ')</small></td><td>' + escHtml(fechaLegible(it.fecha)) +
                                '</td><td>' + escHtml(it.suministrado_por) + '</td><td>' +
                                (it.remision ? '<span class="badge badge-success">' + escHtml(it.remision) + '</span>' : '<span class="badge badge-warning">pendiente</span>') + '</td></tr>';
                    });
                    $('#fichaInsumos').html(h + '</tbody></table></div>');
                }
                if (f.remisiones && f.remisiones.length) {
                    var rh = '<h6 class="mt-3">Remisiones</h6><table class="table table-sm table-bordered"><thead><tr><th>N°</th><th>Tipo</th><th>Fecha</th><th>Transportista</th><th>Estado</th></tr></thead><tbody>';
                    $.each(f.remisiones, function (i, r) {
                        rh += '<tr><td><a href="InventarioServlet?tipo=imprimirRemision&id=' + parseInt(r.id, 10) + '" target="_blank">' + escHtml(r.numero) + '</a></td><td>' +
                                (r.tipo === 'SALIDA_ACTIVOS' ? 'Salida de activos' : 'Salida de insumos') + '</td><td>' + escHtml(r.fecha) + '</td><td>' + escHtml(r.transportista) + '</td><td>' +
                                (r.anulada ? '<span class="badge badge-danger" title="' + escHtml(r.motivo_anulacion) + '">Anulada</span>' : '<span class="badge badge-success">Vigente</span>') + '</td></tr>';
                    });
                    $('#fichaInsumos').append(rh + '</tbody></table>');
                }

                if (f.eventos.length === 0) {
                    $('#fichaHistorial').html('<p class="text-muted">Sin eventos registrados.</p>');
                } else {
                    var t = '<div class="timeline">';
                    $.each(f.eventos, function (i, ev) {
                        var e = EVENTOS_OT[ev.tipo] || [ev.tipo, 'fa-circle', 'bg-gray'];
                        t += '<div><i class="fas ' + e[1] + ' ' + e[2] + '"></i><div class="timeline-item">' +
                                '<span class="time"><i class="fas fa-clock"></i> ' + escHtml(fechaLegible(ev.fecha)) + '</span>' +
                                '<h3 class="timeline-header"><strong>' + escHtml(e[0]) + '</strong> <small class="text-muted">por ' + escHtml(ev.usuario) + '</small></h3>' +
                                (ev.detalle ? '<div class="timeline-body">' + escHtml(ev.detalle) + '</div>' : '') + '</div></div>';
                    });
                    $('#fichaHistorial').html(t + '<div><i class="fas fa-clock bg-gray"></i></div></div>');
                }
                $('#modalFichaOT .nav-tabs a:first').tab('show');
                $('#modalFichaOT').modal('show');
            },
            error: function () {
                alert('Error al obtener la orden de trabajo.');
            }
        });
    }

    function editarOT(id) {
        $.ajax({
            type: 'post', url: URL_OT, dataType: 'json', data: {tipo: 'detalle', id: id},
            success: function (f) {
                if (f.error) {
                    alert(f.error);
                    return;
                }
                $('#editId').val(f.id);
                $('#editNumero').text(f.numero);
                $('#editTitulo').val(f.titulo);
                $('#editDescripcion').val(f.descripcion);
                $('#mensajeEditar').html('');
                $('#btnGuardarEdicion').prop('disabled', false);
                // El centro destino solo se puede cambiar mientras la OT no tenga insumos entregados
                var conInsumos = f.items && f.items.length > 0;
                $.post(URL_OT, {tipo: 'contextoUsuario'}, function (u) {
                    var h = '';
                    $.each(u.centros || [], function (i, c) {
                        h += '<option value="' + parseInt(c.id, 10) + '">' + escHtml(c.nombre) + '</option>';
                    });
                    $('#editCentro').html(h).val(String(f.id_centro)).prop('disabled', conInsumos);
                    $('#editCentroAyuda').text(conInsumos ? 'No se puede cambiar: la OT ya tiene insumos entregados.' : 'Si la cambia a otro centro, la OT pasa a resolverla ese centro.');
                }, 'json');
                cargarCombosEdicion(f.id_centro, f.id_centro_costo, f.id_responsable);
                $('#modalEditarOT').modal('show');
            }
        });
    }

    function cargarCombosEdicion(idCentro, cc, responsable) {
        $.post(URL_OT, {tipo: 'listarCentrosCosto', id_centro: idCentro}, function (html) {
            $('#editCentroCosto').html(html);
            if (cc)
                $('#editCentroCosto').val(String(cc));
        });
        $.post(URL_OT, {tipo: 'listarResponsables', id_centro: idCentro}, function (lista) {
            var h = '<option value="">-- Sin responsable --</option>';
            $.each(lista || [], function (i, r) {
                h += '<option value="' + parseInt(r.id, 10) + '">' + escHtml(r.nombre) + ' - ' + escHtml(r.rol) + '</option>';
            });
            $('#editResponsable').html(h);
            if (responsable)
                $('#editResponsable').val(String(responsable));
        }, 'json');
    }

    function guardarEdicion() {
        var $b = $('#btnGuardarEdicion').prop('disabled', true);
        $.ajax({
            type: 'post', url: URL_OT, dataType: 'json',
            data: {tipo: 'modificar', id: $('#editId').val(), titulo: $('#editTitulo').val(), descripcion: $('#editDescripcion').val(),
                id_centro: $('#editCentro').val(), id_centro_costo: $('#editCentroCosto').val(), id_responsable: $('#editResponsable').val()},
            success: function (r) {
                if (r.ok) {
                    $('#modalEditarOT').modal('hide');
                    refrescar();
                    if ($('#modalFichaOT').hasClass('show'))
                        verOT($('#editId').val(), true);
                } else {
                    $('#mensajeEditar').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>');
                    $b.prop('disabled', false);
                }
            },
            error: function () {
                $b.prop('disabled', false);
            }
        });
    }

    function eliminarOT(id, numero) {
        $('#eliminarId').val(id);
        $('#eliminarNumero').text(numero);
        $('#mensajeEliminar').html('');
        $('#btnConfirmarEliminar').prop('disabled', false);
        $('#modalEliminarOT').modal('show');
    }

    function confirmarEliminar() {
        var $b = $('#btnConfirmarEliminar').prop('disabled', true);
        $.ajax({
            type: 'post', url: URL_OT, dataType: 'json', data: {tipo: 'eliminar', id: $('#eliminarId').val()},
            success: function (r) {
                if (r.ok) {
                    $('#modalEliminarOT').modal('hide');
                    $('#modalFichaOT').modal('hide');
                    refrescar();
                } else {
                    $('#mensajeEliminar').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>');
                    $b.prop('disabled', false);
                }
            },
            error: function () {
                $b.prop('disabled', false);
            }
        });
    }
</script>
</body>
</html>
