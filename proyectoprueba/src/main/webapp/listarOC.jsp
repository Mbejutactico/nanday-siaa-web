<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-6">
                    <h1><i class="fas fa-file-invoice"></i> Órdenes de Compra</h1>
                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Listado de OC generadas</h3>
                </div>
                <div class="card-body">
                    <table id="tablaOC" class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>N° OC</th>
                                <th>Fecha</th>
                                <th>Proveedor</th>
                                <th>Centro Destino</th>
                                <th>Usuario Solicitante</th>
                                <th>Estado</th>
                                <th>Total (Gs.)</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="listaOC"></tbody>
                        <tfoot>
                            <tr>
                                <th>N° OC</th>
                                <th>Fecha</th>
                                <th>Proveedor</th>
                                <th>Centro Destino</th>
                                <th>Usuario Solicitante</th>
                                <th>Estado</th>
                                <th>Total (Gs.)</th>
                                <th>Acciones</th>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Editar OC -->
<div class="modal fade" id="modalEditarOC" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Editar Orden de Compra</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <form id="formEditarOC">
                    <input type="hidden" id="edit_id_compra" name="id_compra">
                    <input type="hidden" name="tipo" value="actualizarOC">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Proveedor *</label>
                                <select class="form-control" id="edit_idProveedor" name="id_proveedor">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Centro Destino *</label>
                                <select class="form-control" id="edit_idCentroDestino" name="id_centro_destino">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label>Observaciones</label>
                                <textarea class="form-control" id="edit_observaciones" name="observaciones" rows="2"></textarea>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-md-8"><h5>Detalle de artículos</h5></div>
                        <div class="col-md-4 text-right">
                            <button type="button" class="btn btn-success btn-sm" id="btnAgregarDetalleEdit"><i class="fa fa-plus"></i> Agregar</button>
                        </div>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-sm table-bordered" id="tablaDetalleEdit">
                            <thead>
                                <tr>
                                    <th>Acción</th>
                                    <th>Código</th>
                                    <th>Descripción</th>
                                    <th>Precio</th>
                                    <th>Cantidad</th>
                                    <th>Subtotal</th>
                                </tr>
                            </thead>
                            <tbody id="edit_detalle"></tbody>
                            <tfoot>
                                <tr>
                                    <th colspan="5" class="text-right">TOTAL</th>
                                    <th id="edit_total">0.00</th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </form>
                <div id="mensajeEditarOC"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
                <button class="btn btn-primary" id="btnGuardarEdicion">Guardar Cambios</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Agregar Detalle -->
<div class="modal fade" id="modalAgregarDetalleOC" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5>Agregar Artículo</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <form id="formAgregarDetalleOC">
                    <input type="hidden" id="agregar_id_compra">
                    <div class="form-group">
                        <label>Insumo *</label>
                        <select class="form-control" id="agregar_id_insumo">
                            <option value="">-- Seleccionar --</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Cantidad *</label>
                        <input type="number" class="form-control" id="agregar_cantidad" value="1" min="1">
                    </div>
                    <div class="form-group">
                        <label>Precio Unitario *</label>
                        <input type="number" class="form-control" id="agregar_precio" step="0.01">
                    </div>
                </form>
                <div id="mensajeAgregarDetalle"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-primary" id="btnConfirmarAgregarDetalle">Agregar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Anular OC -->
<div class="modal fade" id="modalAnularOC" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5>Anular Orden de Compra</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="anular_id_compra">
                <h5>¿Está seguro de anular esta OC?</h5>
                <p>La OC pasará a estado <strong>ANULADO</strong> y no podrá ser modificada.</p>
                <div id="mensajeAnularOC"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-danger" id="btnConfirmarAnular">Anular</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var dataTableOC;
var timeoutDetalle;

$(document).ready(function() {
    dataTableOC = $('#tablaOC').DataTable({
        responsive: true,
        lengthChange: false,
        autoWidth: false,
        buttons: []
    });

    // Los combos de edición solo hacen falta a quien puede modificar OC
    if (PERMISOS.OC_MODIFICAR) cargarCombosEdicion();
    cargarTablaOC();

    // Evento guardar edición
    $('#btnGuardarEdicion').click(function() {
        guardarEdicionOC();
    });

    // Evento confirmar anulación
    $('#btnConfirmarAnular').click(function() {
        confirmarAnularOC();
    });

    // Evento para abrir modal de agregar detalle
    $('#btnAgregarDetalleEdit').click(function() {
        var idCompra = $('#edit_id_compra').val();
        if (!idCompra) {
            alert('Primero debe guardar la cabecera.');
            return;
        }
        abrirModalAgregarDetalle(idCompra);
    });

    // Evento confirmar agregar detalle
    $('#btnConfirmarAgregarDetalle').click(function() {
        confirmarAgregarDetalle();
    });

    // Autocompletar precio al seleccionar insumo
    $('#agregar_id_insumo').on('change', function() {
        var precio = $(this).find('option:selected').data('precio');
        if (precio) {
            $('#agregar_precio').val(precio);
        } else {
            $('#agregar_precio').val('');
        }
    });

    // Evento input para actualizar detalle (con delegación)
    $('#edit_detalle').on('input', '.cantidad-edit, .precio-edit', function() {
        clearTimeout(timeoutDetalle);
        var $this = $(this);
        timeoutDetalle = setTimeout(function() {
            actualizarDetalleEdicion($this);
        }, 500);
    });

    // Eliminar detalle
    $('#edit_detalle').on('click', '.eliminar-detalle-edit', function() {
        var idDetalle = $(this).data('id');
        preguntar('¿Eliminar este artículo?', function () {
            eliminarDetalleOC(idDetalle);
        });
    });

    // Al cerrar modal de edición, resetear botón
    $('#modalEditarOC').on('hidden.bs.modal', function () {
        $('#btnGuardarEdicion').prop('disabled', false).text('Guardar Cambios');
        $('#mensajeEditarOC').html('');
    });

    // Al cerrar modal de anular, resetear botón
    $('#modalAnularOC').on('hidden.bs.modal', function () {
        $('#btnConfirmarAnular').prop('disabled', false).text('Anular');
        $('#mensajeAnularOC').html('');
    });
});

function cargarCombosEdicion() {
    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'cargarCombos' },
        success: function(data) {
            var obj = JSON.parse(data);
            $('#edit_idProveedor').html(obj.proveedores);
            $('#edit_idCentroDestino').html(obj.centros);
            cargarComboInsumos();
        }
    });
}

function cargarComboInsumos() {
    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'cargarInsumos' },
        success: function(data) {
            $('#agregar_id_insumo').html(data);
        }
    });
}

function cargarTablaOC() {
    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'listar' },
        success: function(data) {
            dataTableOC.clear();
            var tempDiv = $('<div>').html(data);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() {
                    rowData.push($(this).html());
                });
                if (rowData.length) dataTableOC.row.add(rowData);
            });
            dataTableOC.draw();
        }
    });
}

function editarOC(id_compra) {
    // Resetear botón guardar
    $('#btnGuardarEdicion').prop('disabled', false).text('Guardar Cambios');
    $('#mensajeEditarOC').html('');

    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'obtenerOC', id: id_compra },
        dataType: 'json',
        success: function(data) {
            if (data.error) {
                alert(data.error);
                return;
            }
            $('#edit_id_compra').val(data.id_compra);
            $('#edit_idProveedor').val(data.id_proveedor);
            $('#edit_idCentroDestino').val(data.id_centro_destino);
            $('#edit_observaciones').val(data.observaciones);
            cargarDetalleEditable(id_compra);

            if (data.estado === 'ANULADO' || data.estado === 'RECIBIDO') {
                $('#btnGuardarEdicion').prop('disabled', true);
                $('#mensajeEditarOC').html('<div class="alert alert-warning">Esta OC no se puede modificar porque está ' + data.estado + '.</div>');
                $('#btnAgregarDetalleEdit').prop('disabled', true);
            } else {
                $('#btnGuardarEdicion').prop('disabled', false);
                $('#mensajeEditarOC').html('');
                $('#btnAgregarDetalleEdit').prop('disabled', false);
            }

            $('#modalEditarOC').modal('show');
        },
        error: function() {
            alert('Error al obtener datos de la OC.');
        }
    });
}
function imprimirOC(id_compra) {
    // Abre una nueva ventana o pestaña con el reporte individual
    window.open('ReporteIndividualServlet?reporte=oc&cod=' + id_compra, '_blank');
}
function cargarDetalleEditable(idCompra) {
    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'obtenerDetalle', id: idCompra },
        dataType: 'json',
        success: function(data) {
            var html = '';
            if (data && data.length > 0) {
                $.each(data, function(i, item) {
                    var esEquipo = (item.tipo_item === 'EQUIPO');
                    var recibido = parseInt(item.recibido, 10) || 0;
                    // Una línea con unidades recibidas no se puede borrar; un equipo siempre es 1 unidad
                    var btnEliminar = (recibido > 0)
                        ? '<span class="text-muted" title="Tiene unidades recibidas"><i class="fa fa-lock"></i></span>'
                        : '<button type="button" class="btn btn-danger btn-sm eliminar-detalle-edit" data-id="' + escHtml(item.id_detalle) + '"><i class="fa fa-trash"></i></button>';
                    var descripcion = escHtml(item.descripcion) +
                        (esEquipo ? ' <span class="badge badge-info">Activo</span>' : '') +
                        (item.serial ? '<br><small class="text-muted">N° activo: ' + escHtml(item.serial) + '</small>' : '') +
                        (recibido > 0 && !esEquipo ? '<br><small class="text-muted">Recibido: ' + recibido + '</small>' : '');
                    html += '<tr data-id="' + escHtml(item.id_detalle) + '">' +
                        '<td>' + btnEliminar + '</td>' +
                        '<td>' + escHtml(item.codigo) + '</td>' +
                        '<td>' + descripcion + '</td>' +
                        '<td><input type="number" class="form-control form-control-sm precio-edit" value="' + escHtml(item.precio_unitario) + '" step="0.01" min="0"></td>' +
                        '<td><input type="number" class="form-control form-control-sm cantidad-edit" value="' + escHtml(item.cantidad) + '" min="' + Math.max(1, recibido) + '"' + (esEquipo ? ' readonly title="Cada activo es una unidad"' : '') + '></td>' +
                        '<td class="subtotal-edit">' + escHtml(item.subtotal) + '</td>' +
                        '</tr>';
                });
            } else {
                html = '<tr><td colspan="6" class="text-center">Sin artículos</td></tr>';
            }
            $('#edit_detalle').html(html);
            calcularTotalesEdicion();
        }
    });
}

function calcularTotalesEdicion() {
    var total = 0;
    $('#edit_detalle tr').each(function() {
        var precio = parseFloat($(this).find('.precio-edit').val()) || 0;
        var cantidad = parseInt($(this).find('.cantidad-edit').val()) || 0;
        var subtotal = precio * cantidad;
        $(this).find('.subtotal-edit').text(subtotal.toFixed(2));
        total += subtotal;
    });
    $('#edit_total').text(total.toFixed(2));
}

function actualizarDetalleEdicion($element) {
    var tr = $element.closest('tr');
    var idDetalle = tr.data('id');
    if (!idDetalle) return;
    var cantidad = tr.find('.cantidad-edit').val();
    var precio = tr.find('.precio-edit').val();

    if (!cantidad || parseInt(cantidad) < 1) {
        alert('La cantidad debe ser mayor a 0');
        tr.find('.cantidad-edit').val(1);
        cantidad = 1;
    }
    if (!precio || parseFloat(precio) < 0) {
        alert('El precio debe ser mayor o igual a 0');
        tr.find('.precio-edit').val(0);
        precio = 0;
    }

    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: {
            tipo: 'actualizarDetalleOC',
            id_detalle: idDetalle,
            cantidad: cantidad,
            precio: precio
        },
        success: function(response) {
            if (response.trim() === 'ok') {
                calcularTotalesEdicion();
            } else {
                alert('Error al actualizar: ' + $('<div>').html(response).text());
                cargarDetalleEditable($('#edit_id_compra').val());
            }
        }
    });
}

function eliminarDetalleOC(idDetalle) {
    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'eliminarDetalleOC', id: idDetalle },
        success: function(response) {
            if (response.indexOf('éxito') !== -1 || response.indexOf('eliminado') !== -1) {
                var idCompra = $('#edit_id_compra').val();
                cargarDetalleEditable(idCompra);
            } else {
                alert($('<div>').html(response).text());
            }
        }
    });
}

function abrirModalAgregarDetalle(idCompra) {
    $('#agregar_id_compra').val(idCompra);
    $('#agregar_cantidad').val(1);
    $('#agregar_precio').val('');
    $('#mensajeAgregarDetalle').html('');
    $('#modalAgregarDetalleOC').modal('show');
}

function confirmarAgregarDetalle() {
    var idCompra = $('#agregar_id_compra').val();
    var idInsumo = $('#agregar_id_insumo').val();
    var cantidad = $('#agregar_cantidad').val();
    var precio = $('#agregar_precio').val();

    if (!idInsumo || !cantidad || !precio) {
        $('#mensajeAgregarDetalle').html('<div class="alert alert-danger">Complete todos los campos.</div>');
        return;
    }
    if (!/^[0-9]+$/.test(cantidad) || parseInt(cantidad, 10) <= 0) {
        $('#mensajeAgregarDetalle').html('<div class="alert alert-danger">La cantidad debe ser un entero mayor a 0.</div>');
        return;
    }
    if (parseFloat(precio) < 0) {
        $('#mensajeAgregarDetalle').html('<div class="alert alert-danger">El precio no puede ser negativo.</div>');
        return;
    }

    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: {
            tipo: 'agregarDetalleOC',
            id_compra: idCompra,
            id_insumo: idInsumo,
            cantidad: cantidad,
            precio: precio
        },
        success: function(response) {
            if (response.indexOf('éxito') !== -1) {
                $('#mensajeAgregarDetalle').html('<div class="alert alert-success">Artículo agregado.</div>');
                cargarDetalleEditable(idCompra);
                setTimeout(function() {
                    $('#modalAgregarDetalleOC').modal('hide');
                }, 1000);
            } else {
                $('#mensajeAgregarDetalle').html(response);
            }
        }
    });
}

function guardarEdicionOC() {
    // Deshabilitar botón y mostrar "Procesando..."
    $('#btnGuardarEdicion').prop('disabled', true).text('Procesando...');
    $('#mensajeEditarOC').html(''); // limpiar mensajes previos

    var formData = $('#formEditarOC').serialize();
    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: formData,
        success: function(response) {
            // Mostrar mensaje de respuesta
            $('#mensajeEditarOC').html(response);

            // Verificar si la respuesta contiene "correctamente" o "éxito" (mensaje de éxito)
            if (response.indexOf('correctamente') !== -1 || response.indexOf('éxito') !== -1) {
                cargarTablaOC(); // Actualizar listado
                setTimeout(function() {
                    $('#modalEditarOC').modal('hide');
                }, 1500);
            } else {
                // Si hubo error, habilitar botón nuevamente
                $('#btnGuardarEdicion').prop('disabled', false).text('Guardar Cambios');
            }
        },
        error: function() {
            $('#mensajeEditarOC').html('<div class="alert alert-danger">Error de comunicación con el servidor.</div>');
            $('#btnGuardarEdicion').prop('disabled', false).text('Guardar Cambios');
        }
    });
}

function anularOC(id_compra) {
    $('#anular_id_compra').val(id_compra);
    $('#mensajeAnularOC').html('');
    // Restablecer botón por si quedó deshabilitado
    $('#btnConfirmarAnular').prop('disabled', false).text('Anular');
    $('#modalAnularOC').modal('show');
}

function confirmarAnularOC() {
    var id = $('#anular_id_compra').val();
    // Deshabilitar botón para evitar doble clic
    $('#btnConfirmarAnular').prop('disabled', true).text('Procesando...');
    $('#mensajeAnularOC').html('');

    $.ajax({
        type: "post",
        url: "OrdenCompraServlet",
        data: { tipo: 'anularOC', id: id },
        success: function(response) {
            $('#mensajeAnularOC').html(response);
            // Verificar si la respuesta contiene "correctamente" o "éxito"
            if (response.indexOf('correctamente') !== -1 || response.indexOf('éxito') !== -1) {
                cargarTablaOC(); // Actualizar listado
                setTimeout(function() {
                    $('#modalAnularOC').modal('hide');
                }, 1500);
            } else {
                // Si hubo error, habilitar botón nuevamente
                $('#btnConfirmarAnular').prop('disabled', false).text('Anular');
            }
        },
        error: function() {
            $('#mensajeAnularOC').html('<div class="alert alert-danger">Error de comunicación con el servidor.</div>');
            $('#btnConfirmarAnular').prop('disabled', false).text('Anular');
        }
    });
}
</script>

<!-- Modal ficha de OC (se abre con clic en la fila o con el botón del ojo) -->
<div class="modal fade" id="modalFichaOC" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="fichaOCTitulo"><i class="fas fa-file-invoice"></i> Orden de Compra</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div class="mb-2" id="accionesFichaOC"></div>
                <ul class="nav nav-tabs" role="tablist">
                    <li class="nav-item"><a class="nav-link active" data-toggle="tab" href="#fichaOCDatos">Datos</a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="tab" href="#fichaOCArticulos">Artículos <span class="badge badge-secondary" id="cantArticulosOC">0</span></a></li>
                    <li class="nav-item"><a class="nav-link" data-toggle="tab" href="#fichaOCRecepciones">Recepciones <span class="badge badge-info" id="cantRecepcionesOC">0</span></a></li>
                </ul>
                <div class="tab-content pt-3">
                    <div class="tab-pane fade show active" id="fichaOCDatos"></div>
                    <div class="tab-pane fade" id="fichaOCArticulos"></div>
                    <div class="tab-pane fade" id="fichaOCRecepciones"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal anular recepción -->
<div class="modal fade" id="modalAnularRecepcion" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-danger">
                <h5 class="modal-title"><i class="fas fa-undo"></i> Anular recepción <span id="anrNumero"></span></h5>
                <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="anrId">
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle"></i> Al anular esta recepción:
                    <ul class="mb-0">
                        <li>Los insumos recibidos se <strong>descuentan del stock</strong> del almacén donde entraron (queda registrado en el Kardex).</li>
                        <li>Los activos recibidos vuelven a estado <strong>SOLICITADO</strong> y pierden su número de activo.</li>
                        <li>La OC vuelve a <strong>PARCIAL</strong> si le quedan otras recepciones vigentes, o a <strong>SOLICITADO</strong> si no.</li>
                        <li>La recepción no se borra: queda como <strong>anulada</strong> con su justificación.</li>
                    </ul>
                </div>
                <div class="table-responsive"><table class="table table-sm table-bordered">
                    <thead class="thead-light"><tr><th>Artículo</th><th class="text-right">Cantidad</th><th>Destino</th></tr></thead>
                    <tbody id="anrLineas"></tbody>
                </table></div>
                <div class="form-group">
                    <label for="anrMotivo">Justificación *</label>
                    <textarea class="form-control" id="anrMotivo" rows="3" maxlength="1000" placeholder="¿Por qué se anula esta recepción?"></textarea>
                    <small class="form-text text-muted"><span id="anrContador">0</span>/1000 caracteres (mínimo 10)</small>
                </div>
                <div id="mensajeAnularRecepcion"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-danger" id="btnConfirmarAnularRecepcion"><i class="fas fa-undo"></i> Anular recepción</button>
            </div>
        </div>
    </div>
</div>

<style>
    #tablaOC tbody tr { cursor: pointer; }
    .recepcion-oc.anulada { opacity: .75; }
    .recepcion-oc.anulada .card-header { background: #fdecea; }
</style>

<script>
// Ficha de OC al hacer clic en la fila
var ocFichaId = null, ocFicha = null;
var URL_OC = 'OrdenCompraServlet';

function gs(v) {
    if (!v) return '0';
    var n = parseFloat(v);
    return isNaN(n) ? v : n.toLocaleString('es-PY');
}
function badgeOC(e) {
    var m = {'SOLICITADO':'badge-primary','PARCIAL':'badge-warning','RECIBIDO':'badge-success','ANULADO':'badge-danger'};
    return '<span class="badge ' + (m[e]||'badge-light') + '">' + escHtml(e) + '</span>';
}
function filaFichaOC(etq, val) {
    return '<tr><th style="width:40%">' + escHtml(etq) + '</th><td>' + (val === '' ? '<span class="text-muted">-</span>' : val) + '</td></tr>';
}

function verFichaOC(id, pestana) {
    ocFichaId = id;
    $.ajax({
        type: 'post', url: URL_OC, dataType: 'json', data: { tipo: 'fichaOC', id: id },
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            ocFicha = f;
            $('#fichaOCTitulo').html('<i class="fas fa-file-invoice"></i> ' + escHtml(f.numero_oc) + ' ' + badgeOC(f.estado));

            // Acciones según estado y permisos
            var acc = '<button class="btn btn-info btn-sm mr-1" onclick="imprimirOC(' + parseInt(id, 10) + ')"><i class="fas fa-print"></i> Imprimir OC</button>';
            var enCurso = f.estado === 'SOLICITADO' || f.estado === 'PARCIAL';
            if (enCurso && PERMISOS.OC_RECEPCION) acc += '<a href="recepcionOC.jsp" class="btn btn-primary btn-sm mr-1"><i class="fas fa-boxes"></i> Ir a Recepción OC</a>';
            if (enCurso && PERMISOS.OC_MODIFICAR) {
                acc += '<button class="btn btn-warning btn-sm mr-1" onclick="$(\'#modalFichaOC\').modal(\'hide\'); editarOC(' + parseInt(id, 10) + ')"><i class="fas fa-edit"></i> Modificar</button>';
                acc += '<button class="btn btn-danger btn-sm mr-1" onclick="$(\'#modalFichaOC\').modal(\'hide\'); anularOC(' + parseInt(id, 10) + ')"><i class="fas fa-ban"></i> Anular OC</button>';
            }
            $('#accionesFichaOC').html(acc);

            // Pestaña Datos
            $('#fichaOCDatos').html('<div class="row"><div class="col-lg-6"><h6 class="text-primary"><i class="fas fa-file-invoice"></i> Datos de la orden</h6><table class="table table-sm table-bordered">' +
                filaFichaOC('N° OC', '<strong>' + escHtml(f.numero_oc) + '</strong>') +
                filaFichaOC('Fecha', escHtml(f.fecha)) +
                filaFichaOC('Estado', badgeOC(f.estado)) +
                filaFichaOC('Centro destino', escHtml(f.centro)) +
                filaFichaOC('Solicitante', escHtml(f.solicitante)) +
                filaFichaOC('Registrada por', escHtml(f.comprador)) +
                filaFichaOC('Observaciones', f.observaciones ? escHtml(f.observaciones).replace(/\n/g, '<br>') : '') +
                filaFichaOC('Total', '<strong>Gs. ' + gs(f.total) + '</strong>') +
                '</table></div><div class="col-lg-6"><h6 class="text-primary"><i class="fas fa-truck"></i> Proveedor</h6><table class="table table-sm table-bordered">' +
                filaFichaOC('Razón social', escHtml(f.proveedor)) +
                filaFichaOC('RUC', escHtml(f.ruc)) +
                filaFichaOC('Teléfono', escHtml(f.telefono)) +
                filaFichaOC('Email', escHtml(f.prov_email)) +
                filaFichaOC('Dirección', escHtml(f.direccion)) +
                '</table></div></div>');

            // Pestaña Artículos
            var lineas = f.lineas || [];
            $('#cantArticulosOC').text(lineas.length);
            var h = '<div class="table-responsive"><table class="table table-sm table-bordered table-striped">' +
                '<thead class="thead-light"><tr><th>Tipo</th><th>Código</th><th>Descripción</th><th class="text-right">Cantidad</th><th class="text-right">Recibido</th><th class="text-right">Precio unit.</th><th class="text-right">Subtotal</th><th>N° Activo</th></tr></thead><tbody>';
            $.each(lineas, function (i, ln) {
                var porcentaje = ln.cantidad > 0 ? Math.round(ln.recibido * 100 / ln.cantidad) : 0;
                var colRec = porcentaje === 100 ? 'text-success font-weight-bold' : (porcentaje > 0 ? 'text-warning font-weight-bold' : 'text-danger');
                h += '<tr><td><span class="badge ' + (ln.tipo === 'EQUIPO' ? 'badge-info' : 'badge-secondary') + '">' + (ln.tipo === 'EQUIPO' ? 'Activo' : 'Insumo') + '</span></td>' +
                    '<td>' + escHtml(ln.codigo) + '</td><td>' + escHtml(ln.descripcion) + '</td>' +
                    '<td class="text-right">' + escHtml(ln.cantidad) + '</td>' +
                    '<td class="text-right ' + colRec + '">' + escHtml(ln.recibido) + ' <small>(' + porcentaje + '%)</small></td>' +
                    '<td class="text-right">Gs. ' + gs(ln.precio) + '</td><td class="text-right">Gs. ' + gs(ln.subtotal) + '</td>' +
                    '<td>' + (ln.serial ? '<span class="badge badge-success">' + escHtml(ln.serial) + '</span>' : '') + '</td></tr>';
            });
            h += '</tbody><tfoot><tr><td colspan="6" class="text-right font-weight-bold">Total</td><td class="text-right font-weight-bold">Gs. ' + gs(f.total) + '</td><td></td></tr></tfoot></table></div>';
            $('#fichaOCArticulos').html(h);

            // Pestaña Recepciones
            var recs = f.recepciones || [];
            var vigentes = $.grep(recs, function (r) { return !r.anulada; }).length;
            $('#cantRecepcionesOC').text(vigentes + (recs.length > vigentes ? ' + ' + (recs.length - vigentes) + ' anul.' : ''));
            if (recs.length === 0) {
                $('#fichaOCRecepciones').html('<p class="text-muted">Esta orden todavía no tiene recepciones.</p>');
            } else {
                var r = '';
                $.each(recs, function (i, rec) {
                    var estadoRec = rec.anulada ? '<span class="badge badge-danger">Anulada</span>' : '<span class="badge badge-success">Vigente</span>';
                    if (rec.historica) estadoRec += ' <span class="badge badge-light" title="Recepción registrada antes de que el sistema guardara cada recepción por separado">Anterior</span>';
                    r += '<div class="card recepcion-oc mb-3' + (rec.anulada ? ' anulada' : '') + '"><div class="card-header py-2 d-flex align-items-center">' +
                        '<div><strong>' + escHtml(rec.numero) + '</strong> ' + estadoRec +
                        ' <small class="text-muted ml-2"><i class="fas fa-clock"></i> ' + escHtml(rec.fecha) + (rec.usuario ? ' &middot; por ' + escHtml(rec.usuario) : '') + '</small></div>' +
                        (rec.puede_anular ? '<button class="btn btn-outline-danger btn-sm ml-auto" onclick="abrirAnularRecepcion(' + parseInt(rec.id, 10) + ')"><i class="fas fa-undo"></i> Anular recepción</button>' : '') +
                        '</div><div class="card-body p-2"><table class="table table-sm mb-0"><thead><tr><th>Artículo</th><th class="text-right">Cantidad</th><th>Destino</th></tr></thead><tbody>';
                    $.each(rec.lineas, function (j, l) {
                        r += '<tr><td>' + escHtml(l.codigo) + ' - ' + escHtml(l.descripcion) + '</td><td class="text-right">' + escHtml(l.cantidad) + '</td><td>' + destinoLinea(l) + '</td></tr>';
                    });
                    r += '</tbody></table>';
                    if (rec.anulada) {
                        r += '<div class="alert alert-light border mt-2 mb-0 py-2"><i class="fas fa-ban text-danger"></i> Anulada el ' + escHtml(rec.fecha_anulacion) +
                            (rec.anulado_por ? ' por <strong>' + escHtml(rec.anulado_por) + '</strong>' : '') +
                            '<br><strong>Motivo:</strong> ' + escHtml(rec.motivo_anulacion) + '</div>';
                    }
                    r += '</div></div>';
                });
                $('#fichaOCRecepciones').html(r);
            }

            $('#modalFichaOC .nav-tabs a[href="' + (pestana || '#fichaOCDatos') + '"]').tab('show');
            $('#modalFichaOC').modal('show');
        },
        error: function () { alert('Error al obtener la orden de compra.'); }
    });
}

function destinoLinea(l) {
    if (l.tipo === 'EQUIPO') {
        return (l.numero_activo ? '<span class="badge badge-success">' + escHtml(l.numero_activo) + '</span> ' : '') + escHtml(l.centro_costo);
    }
    return l.almacen ? escHtml(l.almacen) : '<span class="text-muted">sin almacén registrado</span>';
}

function buscarRecepcion(id) {
    var rec = null;
    $.each((ocFicha && ocFicha.recepciones) || [], function (i, r) { if (r.id === id) rec = r; });
    return rec;
}

function abrirAnularRecepcion(id) {
    var rec = buscarRecepcion(id);
    if (!rec) return;
    $('#anrId').val(id);
    $('#anrNumero').text(rec.numero);
    var h = '';
    $.each(rec.lineas, function (i, l) {
        var destino = destinoLinea(l);
        if (l.sin_almacen) {
            destino = '<select class="form-control form-control-sm anr-almacen" data-linea="' + parseInt(l.id, 10) + '">' + (ocFicha.almacenes || '') + '</select>' +
                '<small class="text-muted">¿De qué almacén se descuenta?</small>';
        }
        h += '<tr><td>' + escHtml(l.codigo) + ' - ' + escHtml(l.descripcion) + '</td><td class="text-right">' + escHtml(l.cantidad) + '</td><td>' + destino + '</td></tr>';
    });
    $('#anrLineas').html(h);
    $('#anrMotivo').val('');
    $('#anrContador').text('0');
    $('#mensajeAnularRecepcion').html('');
    $('#btnConfirmarAnularRecepcion').prop('disabled', false);
    $('#modalAnularRecepcion').modal('show');
}

function confirmarAnularRecepcion() {
    var motivo = $.trim($('#anrMotivo').val());
    if (motivo.length < 10) {
        $('#mensajeAnularRecepcion').html('<div class="alert alert-danger">Escriba la justificación de la anulación (al menos 10 caracteres).</div>');
        $('#anrMotivo').focus();
        return;
    }
    var almacenes = {}, falta = false;
    $('.anr-almacen').each(function () {
        if (!$(this).val()) falta = true;
        almacenes[$(this).data('linea')] = parseInt($(this).val(), 10);
    });
    if (falta) {
        $('#mensajeAnularRecepcion').html('<div class="alert alert-danger">Indique el almacén de cada insumo.</div>');
        return;
    }
    var $b = $('#btnConfirmarAnularRecepcion').prop('disabled', true);
    $.ajax({
        type: 'post', url: URL_OC,
        data: { tipo: 'anularRecepcion', id_recepcion: $('#anrId').val(), motivo: motivo, almacenes: JSON.stringify(almacenes) },
        success: function (r) {
            $('#mensajeAnularRecepcion').html(r);
            if (r.indexOf('alert-success') !== -1) {
                cargarTablaOC();
                setTimeout(function () {
                    $('#modalAnularRecepcion').modal('hide');
                    verFichaOC(ocFichaId, '#fichaOCRecepciones');
                }, 1800);
            } else {
                $b.prop('disabled', false);   // error: queda abierto para corregir
            }
        },
        error: function () {
            $('#mensajeAnularRecepcion').html('<div class="alert alert-danger">Error de comunicación con el servidor.</div>');
            $b.prop('disabled', false);
        }
    });
}

$(document).ready(function() {
    // Clic en fila: si no se hizo clic en un botón de acción, abrir la ficha.
    // (DataTables arma las filas de nuevo: el id viaja en el span .fila-oc de la primera celda)
    $('#tablaOC tbody').on('click', 'tr', function(e) {
        if ($(e.target).closest('button, a').length) return;
        var id = $(this).find('.fila-oc').data('id');
        if (id) verFichaOC(id);
    });
    $('#btnConfirmarAnularRecepcion').click(confirmarAnularRecepcion);
    $('#anrMotivo').on('input', function () { $('#anrContador').text($(this).val().length); });

    // Ventana de anulación encima de la ficha
    $('#modalAnularRecepcion').on('shown.bs.modal', function () {
        if ($('#modalFichaOC').hasClass('show')) $('.modal-backdrop').last().css('z-index', 1055);
        $('#anrMotivo').focus();
    }).on('hidden.bs.modal', function () {
        if ($('#modalFichaOC').hasClass('show')) $('body').addClass('modal-open');
    });
    $('#modalAnularRecepcion').css('z-index', 1060);
});
</script>
</body>
</html>
