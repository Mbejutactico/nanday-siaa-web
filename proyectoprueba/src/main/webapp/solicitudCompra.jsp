<%@include file="header.jsp" %>
<%@ page import="java.util.Date, java.text.SimpleDateFormat" %>
<%    Date fechaActual = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    String fechaFormateada = sdf.format(fechaActual);

    // Obtener el usuario logueado desde la sesión
    Integer idUsuarioSolicitante = (Integer) session.getAttribute("id_usuario");
    String nombreCompleto = (String) session.getAttribute("nombre_completo");

    if (idUsuarioSolicitante == null || nombreCompleto == null) {
        // Si no hay sesión, redirigir al login
        response.sendRedirect("index.jsp");
        return;
    }

%>
<div class="content-wrapper">
    <section class="content">
        <form id="formSolicitud" method="post" class="form-horizontal" onsubmit="return false;">
            <h3><i class="fa fa-shopping-cart"></i> Solicitud de Compra <small class="text-muted">(insumos y activos)</small></h3>
            <hr>

            <div class="row">
                <!-- Columna izquierda: datos de cabecera -->
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header"><strong>Datos de la Solicitud</strong></div>
                        <div class="card-body">
                            <div class="form-group">
                                <label>Proveedor *</label>
                                <select class="form-control" id="idProveedor" onchange="actualizarDatosProveedor(this.value)">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                                <input type="hidden" id="codProveedor" name="codProveedor">
                                <input type="text" class="form-control" id="ruc" placeholder="RUC" readonly>
                            </div>
                            <div class="form-group">
                                <label>Centro Destino *</label>
                                <select class="form-control" id="idCentroDestino">
                                    <option value="">-- Seleccionar --</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Usuario Solicitante *</label>
                                <input type="text" class="form-control" id="nombreUsuarioSolicitante"
                                       value="<%= Html.esc(nombreCompleto) %>" readonly>
                                <input type="hidden" id="idUsuarioSolicitante" name="idUsuarioSolicitante"
                                       value="<%= Html.esc(idUsuarioSolicitante) %>">
                            </div>
                            <div class="form-group">
                                <label>Fecha de Solicitud</label>
                                <input type="text" class="form-control" id="fechaSolicitud" value="<%= Html.esc(fechaFormateada) %>" readonly>
                            </div>
                            <div class="form-group">
                                <label>Observaciones</label>
                                <textarea class="form-control" id="observaciones" rows="2" maxlength="1000"></textarea>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Columna derecha: detalle de artículos -->
                <div class="col-md-8">
                    <div class="card card-primary">
                        <div class="card-header"><strong>Detalle de Artículos</strong></div>
                        <div class="card-body">
                            <!-- Fila para agregar: primero se elige si es INSUMO o ACTIVO.
                                 En la misma solicitud se pueden mezclar ambos tipos. -->
                            <div class="row">
                                <div class="col-md-2">
                                    <div class="form-group">
                                        <label>Tipo *</label>
                                        <select class="form-control" id="tipoItem">
                                            <option value="">-- Elegir --</option>
                                            <option value="INSUMO">Insumo</option>
                                            <option value="EQUIPO">Activo</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="form-group">
                                        <label id="lblArticulo">Artículo (Insumo) *</label>
                                        <select class="form-control" id="idInsumo" onchange="actualizarPrecio(this.value)">
                                            <option value="">-- Seleccionar --</option>
                                        </select>
                                        <input type="hidden" id="codInsumo" name="codInsumo">
                                        <small class="form-text text-muted" id="precioSugerido"></small>
                                    </div>
                                </div>
                                <div class="col-md-2">
                                    <div class="form-group">
                                        <label id="lblPrecio">Precio Unitario *</label>
                                        <input type="number" step="0.01" min="0.01" class="form-control" id="precioUnitario" readonly>
                                    </div>
                                </div>
                                <div class="col-md-2">
                                    <div class="form-group">
                                        <label>Cantidad *</label>
                                        <input type="number" class="form-control" id="cantidad" value="1" min="1">
                                    </div>
                                </div>
                                <div class="col-md-2" style="padding-top: 25px;">
                                    <button type="button" class="btn btn-success" id="btnAgregar"><i class="fa fa-plus"></i> Agregar</button>
                                </div>
                            </div>
                            <small class="form-text text-muted" id="ayudaTipo">Elija el tipo de artículo para comenzar.</small>
                            <div id="mensaje"></div>
                            <hr>

                            <!-- Tabla del carrito -->
                            <div class="table-responsive">
                                <table class="table table-bordered table-striped" id="tablaCarrito">
                                    <thead>
                                        <tr>
                                            <th>Acción</th>
                                            <th>Tipo</th>
                                            <th>Código</th>
                                            <th>Descripción</th>
                                            <th>Precio</th>
                                            <th>Cantidad</th>
                                            <th>Subtotal</th>
                                        </tr>
                                    </thead>
                                    <tbody id="cuerpoCarrito">
                                        <tr><td colspan="7" class="text-center text-muted">No hay artículos agregados</td></tr>
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <th colspan="6" class="text-right">TOTAL</th>
                                            <th id="totalCompra">0.00</th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>

                            <!-- Botones de acción -->
                            <div class="text-right">
                                <button type="button" class="btn btn-danger" id="btnCancelar"><i class="fa fa-times"></i> Cancelar Solicitud</button>
                                <button type="button" class="btn btn-primary" id="btnGuardar"><i class="fa fa-save"></i> Guardar Solicitud (OC)</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </section>
</div>

<%@include file="footer.jsp" %>

<!-- Script con toda la lógica, colocado DESPUÉS del footer para asegurar que jQuery esté cargado -->
<script>
    var URL_SOLICITUD = "SolicitudCompraServlet";
    var tipoActual = '';

    $(document).ready(function () {
        $('#tipoItem').change(function () {
            setTipo($(this).val());
        });
        setTipo('');

        // Primero los combos, después se restaura la cabecera del borrador (si existe)
        $.when(cargarProveedores(), cargarCentros(), cargarInsumos()).always(function () {
            cargarCabecera();
        });
        cargarDetalle();
    });

    // Cambio de tipo: INSUMO (precio del catálogo) / ACTIVO (precio manual)
    function setTipo(tipo) {
        tipoActual = (tipo === 'EQUIPO' || tipo === 'INSUMO') ? tipo : '';
        $('#tipoItem').val(tipoActual);
        // Sin tipo elegido no se puede cargar nada
        $('#idInsumo, #cantidad, #btnAgregar').prop('disabled', tipoActual === '');

        if (tipoActual === 'EQUIPO') {
            $('#lblArticulo').text('Activo (modelo) *');
            $('#lblPrecio').html('Precio * <small class="text-muted">(manual)</small>');
            $('#precioUnitario').prop('readonly', false).attr('placeholder', 'Ingrese el precio');
            $('#ayudaTipo').text('Activo: cada unidad se registra por separado; el número de serie se carga en la recepción de la OC.');
        } else if (tipoActual === 'INSUMO') {
            $('#lblArticulo').text('Artículo (Insumo) *');
            $('#lblPrecio').text('Precio *');
            $('#precioUnitario').prop('readonly', true).attr('placeholder', '');
            $('#ayudaTipo').text('Insumo: ingresa al stock del almacén al recibir la OC.');
        } else {
            $('#lblArticulo').text('Artículo *');
            $('#lblPrecio').text('Precio *');
            $('#precioUnitario').prop('readonly', true).attr('placeholder', '');
            $('#ayudaTipo').text('Elija el tipo de artículo para comenzar.');
        }
        // Recalcular precio según el artículo que esté seleccionado
        actualizarPrecio($('#idInsumo').val());
    }

    function cargarProveedores() {
        return $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'listarProveedores'},
            success: function (data) {
                $('#idProveedor').append(data);
            },
            error: function (xhr) {
                alert("Error al cargar proveedores: " + xhr.responseText);
            }
        });
    }

    function cargarCentros() {
        return $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'listarCentros'},
            success: function (data) {
                $('#idCentroDestino').append(data);
            },
            error: function (xhr) {
                alert("Error al cargar centros: " + xhr.responseText);
            }
        });
    }

    // Mismo catálogo para insumos y para modelos de equipo
    function cargarInsumos() {
        return $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'listarInsumos'},
            success: function (data) {
                $('#idInsumo').append(data);
            },
            error: function (xhr) {
                alert("Error al cargar artículos: " + xhr.responseText);
            }
        });
    }

    // Si hay un borrador guardado, vuelve a mostrar su proveedor, centro y observaciones
    function cargarCabecera() {
        $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'cargarCabecera'},
            dataType: 'json',
            success: function (cab) {
                if (!cab || !cab.id_compra) return;
                var valorProv = cab.id_proveedor + ',' + cab.ruc;
                if ($('#idProveedor option').filter(function () { return this.value === valorProv; }).length) {
                    $('#idProveedor').val(valorProv);
                    actualizarDatosProveedor(valorProv);
                }
                $('#idCentroDestino').val(String(cab.id_centro_destino));
                $('#observaciones').val(cab.observaciones);
            }
        });
    }

    function actualizarDatosProveedor(valor) {
        if (valor) {
            var partes = valor.split(',');
            $('#codProveedor').val(partes[0]);
            $('#ruc').val(partes[1]);
        } else {
            $('#codProveedor').val('');
            $('#ruc').val('');
        }
    }

    function actualizarPrecio(valor) {
        if (valor) {
            var partes = valor.split(',');
            $('#codInsumo').val(partes[0]);
            if (tipoActual === 'EQUIPO') {
                // Equipo: el precio se ingresa a mano; el del catálogo es solo referencia
                $('#precioSugerido').text('Precio de referencia: ' + partes[1]);
                $('#precioUnitario').val('');
            } else {
                $('#precioSugerido').text('');
                $('#precioUnitario').val(partes[1]);
            }
        } else {
            $('#codInsumo').val('');
            $('#precioUnitario').val('');
            $('#precioSugerido').text('');
        }
    }

// Agregar detalle (insumo o equipo según el tipo elegido)
    $('#btnAgregar').click(function () {
        var idProveedor = $('#codProveedor').val();
        var idCentro = $('#idCentroDestino').val();
        var observaciones = $('#observaciones').val();
        var idInsumo = $('#codInsumo').val();
        var precio = $('#precioUnitario').val();
        var cantidad = $('#cantidad').val();

        if (!tipoActual) {
            alert('Seleccione si el artículo es un Insumo o un Activo.');
            return;
        }
        if (!idProveedor || !idCentro || !idInsumo || !precio || !cantidad) {
            alert('Debe completar todos los campos obligatorios.');
            return;
        }
        if (!/^[0-9]+$/.test(cantidad) || parseInt(cantidad, 10) <= 0) {
            alert('La cantidad debe ser un número entero mayor a 0.');
            return;
        }
        if (tipoActual === 'EQUIPO' && !(parseFloat(precio) > 0)) {
            alert('El precio debe ser mayor a 0.');
            return;
        }

        var $btn = $(this).prop('disabled', true);
        $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {
                tipo: (tipoActual === 'EQUIPO') ? 'agregarDetalleActivo' : 'agregarDetalle',
                idProveedor: idProveedor,
                idCentroDestino: idCentro,
                observaciones: observaciones,
                codInsumo: idInsumo,
                precio: precio,
                cantidad: cantidad
            },
            success: function (response) {
                $('#mensaje').html(response);
                cargarDetalle();
                $('#cantidad').val(1);
                if (tipoActual === 'EQUIPO') {
                    $('#precioUnitario').val('');
                }
            },
            error: function (xhr) {
                $('#mensaje').html(xhr.responseText || '<div class="alert alert-danger">Error al agregar.</div>');
            },
            complete: function () {
                $btn.prop('disabled', false);
            }
        });
    });

    function cargarDetalle() {
        $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'cargarDetalle'},
            success: function (data) {
                $('#cuerpoCarrito').html(data);
                calcularTotal();
            },
            error: function (xhr) {
                alert("Error al cargar detalle: " + xhr.responseText);
            }
        });
    }

    function eliminarDetalle(idDetalle) {
        if (!confirmado('¿Eliminar este artículo?', this, arguments))
            return;
        $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'eliminarDetalle', id: idDetalle},
            success: function (response) {
                $('#mensaje').html(response);
                cargarDetalle();
            },
            error: function (xhr) {
                alert("Error al eliminar: " + xhr.responseText);
            }
        });
    }

    function calcularTotal() {
        var total = 0;
        $('#cuerpoCarrito td.subtotal').each(function () {
            total += parseFloat($(this).text()) || 0;
        });
        $('#totalCompra').text(total.toFixed(2));
    }

// Guardar solicitud
    $('#btnGuardar').click(function () {
        var idProveedor = $('#codProveedor').val();
        var idCentro = $('#idCentroDestino').val();
        if (!idProveedor || !idCentro) {
            alert('Seleccione proveedor y centro destino.');
            return;
        }
        if (!confirmado('¿Confirmar la solicitud? Se generará el número de OC automáticamente.', this, arguments))
            return;
        var $btn = $(this).prop('disabled', true);
        $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {
                tipo: 'guardar',
                idProveedor: idProveedor,
                idCentroDestino: idCentro,
                observaciones: $('#observaciones').val()
            },
            success: function (response) {
                if (response.trim() === 'ok') {
                    alert('Solicitud guardada correctamente. Número de OC generado.');
                    location.href = 'solicitudCompra.jsp';
                } else {
                    alert('Error: ' + $('<div>').html(response).text());
                    $btn.prop('disabled', false);
                }
            },
            error: function (xhr) {
                alert("Error al guardar: " + $('<div>').html(xhr.responseText).text());
                $btn.prop('disabled', false);
            }
        });
    });

// Cancelar solicitud
    $('#btnCancelar').click(function () {
        if (!confirmado('¿Cancelar la solicitud actual? Se eliminarán todos los datos en borrador.', this, arguments))
            return;
        $.ajax({
            type: "post",
            url: URL_SOLICITUD,
            data: {tipo: 'cancelarSolicitud'},
            success: function (response) {
                $('#mensaje').html(response);
                cargarDetalle();
                $('#idProveedor').val('');
                $('#codProveedor').val('');
                $('#ruc').val('');
                $('#idCentroDestino').val('');
                $('#observaciones').val('');
            },
            error: function (xhr) {
                alert("Error al cancelar: " + xhr.responseText);
            }
        });
    });
</script>
</body>
</html>
