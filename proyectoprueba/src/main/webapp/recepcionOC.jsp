<%@include file="header.jsp" %>
<%@ page import="java.util.*, java.sql.*" %>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-8">
                    <h1><i class="fas fa-boxes"></i> Recepción de OC <small class="text-muted">(insumos y activos)</small></h1>
                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">
            <!-- Selección de OC -->
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Seleccionar Orden de Compra</h3>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>OC Pendiente</label>
                                <select class="form-control" id="comboOC">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <button class="btn btn-primary" id="btnCargarOC"><i class="fa fa-search"></i> Cargar Detalle</button>
                        </div>
                    </div>
                    <div id="mensajeOC"></div>
                </div>
            </div>

            <!-- Detalle de la OC -->
            <div class="card" id="cardDetalle" style="display:none;">
                <div class="card-header">
                    <h3 class="card-title" id="tituloOC">OC #</h3>
                    <div class="card-tools">
                        <span class="badge badge-info" id="estadoOC"></span>
                    </div>
                </div>
                <div class="card-body">
                    <p class="text-muted mb-2">
                        <b>Insumos:</b> indique la cantidad que llegó (0 = no se recibe ahora) y el almacén.
                        <b>Activos:</b> marque los que llegaron y elija el centro de costo (sector) donde quedan; el
                        <b>número de activo se genera automáticamente</b> y el número de serie del fabricante es opcional.
                        Lo que quede pendiente se puede recibir más adelante.
                    </p>
                    <form id="formRecepcion" onsubmit="return false;">
                        <input type="hidden" id="idCompra" name="id_compra">
                        <div class="form-row align-items-end mb-2">
                            <div class="col-md-4" id="grpAlmacenTodos">
                                <label class="mb-0"><small>Almacén para todos los insumos</small></label>
                                <select class="form-control form-control-sm" id="almacenTodos">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                            </div>
                            <div class="col-md-4" id="grpCcTodos">
                                <label class="mb-0"><small>Centro de costo para todos los activos</small></label>
                                <select class="form-control form-control-sm" id="ccTodos">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                            </div>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-bordered table-striped" id="tablaDetalleRecepcion">
                                <thead>
                                    <tr>
                                        <th>Tipo</th>
                                        <th>Código</th>
                                        <th>Descripción</th>
                                        <th>Pedido</th>
                                        <th>Recibido</th>
                                        <th>Pendiente</th>
                                        <th>A recibir *</th>
                                        <th>N° de serie <small class="text-muted">(opcional)</small></th>
                                        <th>Destino * <small class="text-muted">(almacén / centro de costo)</small></th>
                                    </tr>
                                </thead>
                                <tbody id="cuerpoDetalleRecepcion">
                                    <tr><td colspan="9" class="text-center">Seleccione una OC</td></tr>
                                </tbody>
                            </table>
                        </div>
                        <div id="mensajeRecepcion"></div>
                        <div class="text-right">
                            <button type="button" class="btn btn-success" id="btnConfirmarRecepcion"><i class="fa fa-check"></i> Confirmar Recepción</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var URL_OC = "OrdenCompraServlet";

$(document).ready(function() {
    cargarOCPendientes();

    $('#btnCargarOC').click(function() {
        var idCompra = $('#comboOC').val();
        if (!idCompra) {
            alert('Seleccione una OC.');
            return;
        }
        cargarDetalleRecepcion(idCompra);
    });

    $('#btnConfirmarRecepcion').click(function() {
        confirmarRecepcion();
    });

    $('#comboOC').change(function() {
        $('#cardDetalle').hide();
        $('#mensajeRecepcion').html('');
    });

    // Copiar el almacén elegido a todos los insumos / el centro de costo a todos los activos
    $('#almacenTodos').change(function() {
        var v = $(this).val();
        if (v) {
            $('.combo-almacen').val(v);
        }
    });
    $('#ccTodos').change(function() {
        var v = $(this).val();
        if (v) {
            $('.combo-cc:not(:disabled)').val(v);
        }
    });

    // Equipo: al marcar "recibir" se habilita el número de serie
    $('#cuerpoDetalleRecepcion').on('change', '.recibir-equipo', function() {
        var tr = $(this).closest('tr');
        tr.find('.serie-equipo, .combo-cc').prop('disabled', !this.checked);
    });
});

function cargarOCPendientes() {
    $.ajax({
        type: "post",
        url: URL_OC,
        data: { tipo: 'listarOCPendientes' },
        success: function(data) {
            $('#comboOC').html(data);
        },
        error: function() {
            alert('Error al cargar OC pendientes.');
        }
    });
}

function cargarDetalleRecepcion(idCompra) {
    $.ajax({
        type: "post",
        url: URL_OC,
        data: { tipo: 'obtenerDetalleRecepcion', id: idCompra },
        dataType: 'json',
        success: function(data) {
            if (data.error) {
                alert(data.error);
                return;
            }
            $('#cardDetalle').show();
            $('#idCompra').val(idCompra);
            $('#tituloOC').text('OC: ' + data.numero_oc);
            $('#estadoOC').text(data.estado);
            $('#mensajeRecepcion').html('');
            $('#btnConfirmarRecepcion').prop('disabled', false).html('<i class="fa fa-check"></i> Confirmar Recepción');

            var html = '';
            if (data.detalle && data.detalle.length > 0) {
                $.each(data.detalle, function(i, item) {
                    var pendiente = item.cantidad - item.recibido;
                    var esEquipo = (item.tipo_item === 'EQUIPO');
                    html += '<tr data-id="' + escHtml(item.id_detalle) + '" data-tipo="' + (esEquipo ? 'EQUIPO' : 'INSUMO') + '">' +
                        '<td>' + (esEquipo ? '<span class="badge badge-info">Activo</span>' : '<span class="badge badge-secondary">Insumo</span>') + '</td>' +
                        '<td>' + escHtml(item.codigo) + '</td>' +
                        '<td>' + escHtml(item.descripcion) + '</td>' +
                        '<td>' + escHtml(item.cantidad) + '</td>' +
                        '<td>' + escHtml(item.recibido) + '</td>' +
                        '<td>' + escHtml(pendiente) + '</td>';
                    if (esEquipo) {
                        html += '<td class="text-center"><div class="custom-control custom-checkbox">' +
                            '<input type="checkbox" class="custom-control-input recibir-equipo" id="rec_' + escHtml(item.id_detalle) + '" checked>' +
                            '<label class="custom-control-label" for="rec_' + escHtml(item.id_detalle) + '">Recibir</label></div></td>' +
                            '<td><input type="text" class="form-control form-control-sm serie-equipo" maxlength="100" placeholder="Opcional"></td>';
                    } else {
                        html += '<td><input type="number" class="form-control form-control-sm cantidad-recibir" min="0" max="' + escHtml(pendiente) + '" value="' + escHtml(pendiente) + '"></td>' +
                            '<td class="text-muted text-center">-</td>';
                    }
                    // Destino: los insumos van a un almacén; los activos a un centro de costo
                    html += '<td>' +
                        '<select class="form-control form-control-sm ' + (esEquipo ? 'combo-cc' : 'combo-almacen') + '">' +
                        '<option value="">-- Seleccionar --</option>' +
                        '</select>' +
                        '</td>' +
                        '</tr>';
                });
            } else {
                html = '<tr><td colspan="9" class="text-center">Esta OC no tiene artículos pendientes de recepción.</td></tr>';
            }
            $('#cuerpoDetalleRecepcion').html(html);

            // Almacenes (insumos) y centros de costo (activos) del centro destino de la OC
            $('#grpAlmacenTodos').toggle($('.combo-almacen').length > 0);
            $('#grpCcTodos').toggle($('.combo-cc').length > 0);
            cargarCombosAlmacenes(data.id_centro_destino);
            cargarCombosCentrosCosto(data.id_centro_destino);
        },
        error: function() {
            alert('Error al cargar detalle de la OC.');
        }
    });
}

function cargarCombosAlmacenes(idCentro) {
    $.ajax({
        type: "post",
        url: URL_OC,
        data: { tipo: 'listarAlmacenesPorCentro', id_centro: idCentro },
        success: function(data) {
            $('#almacenTodos').html(data);
            $('.combo-almacen').each(function() {
                $(this).html(data);
            });
        }
    });
}

function cargarCombosCentrosCosto(idCentro) {
    $.ajax({
        type: "post",
        url: URL_OC,
        data: { tipo: 'listarCentrosCostoPorCentro', id_centro: idCentro },
        success: function(data) {
            $('#ccTodos').html(data);
            $('.combo-cc').each(function() {
                $(this).html(data);
            });
        }
    });
}

function confirmarRecepcion() {
    var idCompra = $('#idCompra').val();
    if (!idCompra) {
        alert('No hay OC seleccionada.');
        return;
    }

    var datos = [];
    var valido = true;
    $('#cuerpoDetalleRecepcion tr').each(function() {
        var idDetalle = $(this).data('id');
        if (!idDetalle) return;
        var esEquipo = ($(this).data('tipo') === 'EQUIPO');
        var cantidad, serie = '';
        var destino = esEquipo ? $(this).find('.combo-cc').val() : $(this).find('.combo-almacen').val();

        if (esEquipo) {
            cantidad = $(this).find('.recibir-equipo').is(':checked') ? 1 : 0;
            serie = $.trim($(this).find('.serie-equipo').val());   // opcional
        } else {
            var txt = $.trim($(this).find('.cantidad-recibir').val());
            var max = parseInt($(this).find('.cantidad-recibir').attr('max'), 10);
            if (!/^[0-9]+$/.test(txt)) {
                alert('La cantidad a recibir debe ser un número entero mayor o igual a 0.');
                valido = false;
                return false;
            }
            cantidad = parseInt(txt, 10);
            if (cantidad > max) {
                alert('La cantidad a recibir no puede superar lo pendiente (' + max + ').');
                valido = false;
                return false;
            }
        }

        if (cantidad === 0) return;   // esta línea no se recibe ahora
        if (!destino) {
            alert(esEquipo ? 'Seleccione el centro de costo de todos los activos que va a recibir.'
                           : 'Seleccione el almacén de todos los insumos que va a recibir.');
            valido = false;
            return false;
        }
        var item = { id_detalle: idDetalle, cantidad: cantidad };
        if (esEquipo) {
            item.id_centro_costo = parseInt(destino, 10);
            item.serie = serie;
        } else {
            item.id_almacen = parseInt(destino, 10);
        }
        datos.push(item);
    });

    if (!valido) return;
    if (datos.length === 0) {
        alert('No hay nada para recibir: todas las cantidades están en 0.');
        return;
    }

    $('#btnConfirmarRecepcion').prop('disabled', true).text('Procesando...');

    $.ajax({
        type: "post",
        url: URL_OC,
        data: {
            tipo: 'procesarRecepcion',
            id_compra: idCompra,
            datos: JSON.stringify(datos)
        },
        success: function(response) {
            $('#mensajeRecepcion').html(response);
            if (response.indexOf('correctamente') !== -1) {
                cargarOCPendientes();
                setTimeout(function() {
                    $('#cardDetalle').hide();
                    $('#cuerpoDetalleRecepcion').html('<tr><td colspan="9" class="text-center">Seleccione una OC</td></tr>');
                    $('#mensajeOC').html('<div class="alert alert-success">Recepción completada. La OC ha sido actualizada.</div>');
                }, 2000);
            } else {
                $('#btnConfirmarRecepcion').prop('disabled', false).html('<i class="fa fa-check"></i> Confirmar Recepción');
            }
        },
        error: function(xhr) {
            $('#mensajeRecepcion').html('<div class="alert alert-danger">Error al procesar la recepción: ' + escHtml(xhr.status + ' - ' + xhr.statusText) + '</div>');
            $('#btnConfirmarRecepcion').prop('disabled', false).html('<i class="fa fa-check"></i> Confirmar Recepción');
        }
    });
}
</script>
</body>
</html>
