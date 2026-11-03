<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Insumos</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" data-toggle="modal" data-target="#modalInsumo" onclick="abrirModalNuevoInsumo()">Nuevo Insumo</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
    <div class="card-body"><table id="tablaInsumos" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Código</th><th>Descripción</th><th>Unidad</th><th>Precio Compra</th><th>Activo</th><th>Acción</th></tr></thead><tbody id="listaInsumos"></tbody><tfoot><tr><th>ID</th><th>Código</th><th>Descripción</th><th>Unidad</th><th>Precio Compra</th><th>Activo</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal Insumo -->
<div class="modal fade" id="modalInsumo" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 id="modalInsumoLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formInsumo"><input type="hidden" name="tipo" id="tipoInsumo"><input type="hidden" name="pk" id="pkInsumo">
        <div class="row"><div class="col-md-6"><div class="form-group"><label>Código</label><input type="text" class="form-control" name="codigo" id="codigoInsumo" required></div></div>
        <div class="col-md-6"><div class="form-group"><label>Unidad Medida</label><select class="form-control" name="id_unidad_medida" id="unidadSelect" required></select></div></div></div>
        <div class="form-group"><label>Descripción</label><textarea class="form-control" name="descripcion" id="descripcionInsumo"></textarea></div>
        <div class="row">
            <div class="col-md-6"><div class="form-group"><label>Precio de Compra</label><input type="number" step="0.01" class="form-control" name="precio_compra" id="precioCompra" placeholder="0.00"></div></div>
            <div class="col-md-6"><div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div></div>
        </div>
    </form><div id="mensajeInsumo"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarInsumo"></button></div>
</div></div></div>
<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarInsumo" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Insumo</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarInsumo"><h5>¿Seguro que desea eliminar este insumo?</h5><div id="mensajeEliminarInsumo"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarInsumo">Eliminar</button></div></div></div></div>

<%@include file="footer.jsp" %>

<script>
var dataTableInsumos;

$(document).ready(function() {
    dataTableInsumos = $('#tablaInsumos').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarTablaInsumos();
});

function cargarTablaInsumos() {
    $.ajax({
        type: "post",
        url: "InsumoServlet",
        data: { tipo: "listar" },
        success: function(r) {
            dataTableInsumos.clear();
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableInsumos.row.add(rowData);
            });
            dataTableInsumos.draw();
        }
    });
}

function cargarUnidadesMedida(selectedId) {
    $.ajax({
        type: "post",
        url: "UnidadMedidaServlet",
        data: { tipo: "listarCombo" },
        success: function(html) {
            $("#unidadSelect").html(html);
            if (selectedId) $("#unidadSelect").val(selectedId);
        }
    });
}

function abrirModalNuevoInsumo() {
    $("#modalInsumoLabel").html("Nuevo Insumo");
    $("#tipoInsumo").val("Guardar");
    $("#btnGuardarInsumo").html("Guardar");
    $("#formInsumo")[0].reset();
    $("#pkInsumo").val("");
    $("#mensajeInsumo").html("");
    cargarUnidadesMedida(null);
    $('#modalInsumo').modal('show');
}

function abrirModalEditarInsumo(id, codigo, descripcion, id_unidad, precio_compra, activo) {
    $("#modalInsumoLabel").html("Editar Insumo");
    $("#tipoInsumo").val("Modificar");
    $("#btnGuardarInsumo").html("Modificar");
    $("#pkInsumo").val(id);
    $("#codigoInsumo").val(codigo);
    $("#descripcionInsumo").val(descripcion);
    $("#precioCompra").val(precio_compra);
    $("select[name='activo']").val(activo.toString());
    cargarUnidadesMedida(id_unidad);
    $("#mensajeInsumo").html("");
    $('#modalInsumo').modal('show');
}

function abrirModalEliminarInsumo(id) {
    $("#pkEliminarInsumo").val(id);
    $("#mensajeEliminarInsumo").html("");
    $('#modalEliminarInsumo').modal('show');
}

function imprimirReporte() {
    window.open('InsumoServlet?tipo=imprimir', '_blank');
}
function imprimirInsumo(id) {
    window.open('ReporteIndividualServlet?reporte=insumo&cod=' + id, '_blank');
}
$("#btnGuardarInsumo").click(function() {
    $.ajax({
        type: "post",
        url: "InsumoServlet",
        data: $("#formInsumo").serialize(),
        success: function(r) {
            $("#mensajeInsumo").html(r);
            if (r.includes("éxito")) cargarTablaInsumos();
            setTimeout(() => $('#modalInsumo').modal('hide'), 1500);
        }
    });
});

$("#btnConfirmarEliminarInsumo").click(function() {
    var id = $("#pkEliminarInsumo").val();
    $.ajax({
        type: "post",
        url: "InsumoServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarInsumo").html(r);
            if (r.includes("éxito")) cargarTablaInsumos();
            setTimeout(() => $('#modalEliminarInsumo').modal('hide'), 1500);
        }
    });
});
</script>
</body>
</html>
