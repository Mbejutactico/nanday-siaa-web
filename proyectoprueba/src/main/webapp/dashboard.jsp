<%@include file="header.jsp" %>

<!-- Estilos específicos para el dashboard (coherentes con el login y la paleta Ñanday) -->
<style>
    /* Paleta Ñanday (ver header.jsp): fondo claro, verdes, azul ala y naranja como acento */
    .info-box {
        border-radius: 12px;
        box-shadow: 0 1px 3px rgba(30, 36, 32, 0.12);
        transition: transform 0.2s;
        background-color: #FFFFFF;
        border: 1px solid #D5DED7;
    }
    .info-box:hover {
        transform: translateY(-2px);
    }
    .info-box .bg-info { background-color: #1F5F8B !important; }
    .info-box .bg-success { background-color: #2E7D32 !important; }
    .info-box .bg-warning { background-color: #F9A825 !important; color: #3D2E00 !important; }
    .info-box .bg-danger { background-color: #C62828 !important; }
    .info-box .bg-secondary { background-color: #3E7F5A !important; }
    .info-box .bg-primary { background-color: #1B4D2E !important; }
    .info-box-number { font-weight: bold; color: #1E2420; }
    .info-box-text { color: #5F6B63; }

    /* Tarjetas */
    .card {
        border-radius: 12px;
        border: 1px solid #D5DED7;
        box-shadow: 0 1px 3px rgba(30, 36, 32, 0.08);
        overflow: hidden;
    }
    /* En el tablero todas las tarjetas llevan encabezado claro; el tipo se marca con una franja superior */
    .card > .card-header {
        background-color: #E8F2EA !important;
        border-bottom: 1px solid #D5DED7;
        color: #1E2420 !important;
    }
    .card > .card-header .card-title {
        color: #1B4D2E !important;
        font-weight: bold;
    }
    .card-info { border-top: 3px solid #1F5F8B; }
    .card-secondary { border-top: 3px solid #5F6B63; }
    .card-success, .card-primary { border-top: 3px solid #2E7D32; }
    .card-footer {
        background-color: #FFFFFF;
        border-top: 1px solid #D5DED7;
    }
    .card .card-body {
        background-color: #FFFFFF;
        border-radius: 0;
    }
    /* Accesos rápidos */
    .btn-app {
        border-radius: 12px;
        background-color: #FFFFFF;
        color: #1B4D2E;
        border: 1px solid #D5DED7;
        transition: all 0.2s;
    }
    .btn-app:hover {
        transform: translateY(-2px);
        background-color: #E8F2EA;
        border-color: #2E7D32;
        color: #1B4D2E;
    }
    .btn-app.bg-info { background-color: #1F5F8B !important; color: #FFFFFF; border: none; }
    .btn-app.bg-success { background-color: #2E7D32 !important; color: #FFFFFF; border: none; }
    .btn-app.bg-warning { background-color: #F9A825 !important; color: #3D2E00; border: none; }
    .btn-app.bg-primary { background-color: #1B4D2E !important; color: #FFFFFF; border: none; }
    .btn-app.bg-danger { background-color: #C62828 !important; color: #FFFFFF; border: none; }
    .btn-app.bg-secondary { background-color: #3E7F5A !important; color: #FFFFFF; border: none; }

    /* Últimos registros: el naranja se reserva para esta marca lateral */
    .list-group-item {
        border-left: 3px solid #C2410C;
        margin-bottom: 5px;
        border-radius: 0;
    }
    /* Bienvenida */
    .alert-info {
        background-color: #E8F2EA;
        border-color: #D5DED7;
        color: #1E2420;
    }
    .alert-info .icon { color: #2E7D32; }
    .close { color: #1E2420; }
    .breadcrumb {
        background-color: transparent;
    }
    /* Panel de órdenes de trabajo */
    .ot-kpi { display: block; border: 1px solid #D5DED7; border-radius: 10px; padding: .5rem .25rem; color: #1E2420; }
    .ot-kpi:hover { background: #E8F2EA; text-decoration: none; color: #1E2420; }
    .ot-kpi-num { display: block; font-size: 1.8rem; font-weight: 700; line-height: 1.1; }
    .ot-kpi-txt { display: block; font-size: .75rem; color: #5F6B63; }
    #listaOTAbiertas .list-group-item { border-left: 3px solid #2E7D32; padding: .5rem .75rem; }
    #listaOTAbiertas .list-group-item.ot-demorada { border-left-color: #C62828; }
    #listaOTAbiertas a { color: inherit; text-decoration: none; }
    #listaOTAbiertas .list-group-item:hover { background: #F4F8F5; }
    .breadcrumb-item.active { color: #5F6B63; }
    .content-header h1 {
        color: #1E2420;
    }
</style>

<!-- Contenido de la página -->
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-6">
                    <h1 class="m-0">Accesos Rápidos</h1>
                </div>
                <div class="col-sm-6">
                    <ol class="breadcrumb float-sm-right">
                        <li class="breadcrumb-item active">Inicio</li>
                    </ol>
                </div>
            </div>
        </div>
    </div>

    <div class="content">
        <div class="container-fluid">
            <!-- Mensaje de bienvenida -->
            <div class="row">
                <div class="col-lg-12">
                    <div class="alert alert-info alert-dismissible">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                        <h5><i class="icon fas fa-info"></i> Bienvenido a Ñanday S.A</h5>
                        Sistema de gestión de activos e insumos. Utilice el menú lateral para administrar los diferentes módulos.
                    </div>
                </div>
            </div>

            <!-- Alertas de stock mínimo (solo para roles de inventario o compras, y si hay insumos bajo el mínimo) -->
            <% if (com.mycompany.proyectoprueba.servicio.NotificacionServicio.veStock(permisosUsuario)) { %>
            <div class="row" id="filaAlertasStock" style="display:none;">
                <div class="col-12">
                    <div class="card card-outline card-warning">
                        <div class="card-header">
                            <h3 class="card-title"><i class="fas fa-exclamation-triangle text-warning"></i> Insumos en o por debajo del stock mínimo <span class="badge badge-warning" id="cantAlertas"></span></h3>
                            <div class="card-tools">
                                <% if (permisosUsuario.puedeVer("/solicitudCompra.jsp")) { %>
                                <a href="solicitudCompra.jsp" class="btn btn-sm btn-warning"><i class="fas fa-shopping-cart"></i> Solicitar compra</a>
                                <% } %>
                                <button type="button" class="btn btn-tool" data-card-widget="collapse"><i class="fas fa-minus"></i></button>
                            </div>
                        </div>
                        <div class="card-body p-0">
                            <table class="table table-sm mb-0">
                                <thead><tr><th>Código</th><th>Insumo</th><th>Almacén</th><th class="text-right">Stock</th><th class="text-right">Mínimo</th></tr></thead>
                                <tbody id="cuerpoAlertas"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <% } %>

            <!-- Tarjetas de referenciales: solo las pantallas que el rol puede abrir -->
            <% boolean hayTarjetas = false;
               for (String pagTarjeta : new String[]{"proveedorvista", "almacenesvista", "centrosvista", "usuariosvista", "insumosvista", "rolesvista"}) {
                   if (permisosUsuario.puedeVer("/" + pagTarjeta + ".jsp")) { hayTarjetas = true; }
               } %>
            <div class="row"<%= hayTarjetas ? "" : " style=\"display:none;\"" %>>
                <% if (permisosUsuario.puedeVer("/proveedorvista.jsp")) { %>
                <div class="col-md-2 col-sm-4 col-6">
                    <a href="proveedorvista.jsp" class="hero">
                        <span class="hero-body">
                            <span class="hero-label">Proveedores</span>
                            <span class="hero-number" id="totalProveedores">0</span>
                            <span class="hero-sub">registrados</span>
                        </span>
                        <span class="hero-icon"><i class="fas fa-truck"></i></span>
                    </a>
                </div>
                <% } %>
                <% if (permisosUsuario.puedeVer("/almacenesvista.jsp")) { %>
                <div class="col-md-2 col-sm-4 col-6">
                    <a href="almacenesvista.jsp" class="hero">
                        <span class="hero-body">
                            <span class="hero-label">Almacenes</span>
                            <span class="hero-number" id="totalAlmacenes">0</span>
                            <span class="hero-sub">activos</span>
                        </span>
                        <span class="hero-icon"><i class="fas fa-warehouse"></i></span>
                    </a>
                </div>
                <% } %>
                <% if (permisosUsuario.puedeVer("/centrosvista.jsp")) { %>
                <div class="col-md-2 col-sm-4 col-6">
                    <a href="centrosvista.jsp" class="hero hero-amber">
                        <span class="hero-body">
                            <span class="hero-label">Centros</span>
                            <span class="hero-number" id="totalCentros">0</span>
                            <span class="hero-sub">operativos</span>
                        </span>
                        <span class="hero-icon"><i class="fas fa-building"></i></span>
                    </a>
                </div>
                <% } %>
                <% if (permisosUsuario.puedeVer("/usuariosvista.jsp")) { %>
                <div class="col-md-2 col-sm-4 col-6">
                    <a href="usuariosvista.jsp" class="hero hero-red">
                        <span class="hero-body">
                            <span class="hero-label">Usuarios</span>
                            <span class="hero-number" id="totalUsuarios">0</span>
                            <span class="hero-sub">con acceso</span>
                        </span>
                        <span class="hero-icon"><i class="fas fa-users"></i></span>
                    </a>
                </div>
                <% } %>
                <% if (permisosUsuario.puedeVer("/insumosvista.jsp")) { %>
                <div class="col-md-2 col-sm-4 col-6">
                    <a href="insumosvista.jsp" class="hero hero-teal">
                        <span class="hero-body">
                            <span class="hero-label">Insumos</span>
                            <span class="hero-number" id="totalInsumos">0</span>
                            <span class="hero-sub">en catálogo</span>
                        </span>
                        <span class="hero-icon"><i class="fas fa-boxes"></i></span>
                    </a>
                </div>
                <% } %>
                <% if (permisosUsuario.puedeVer("/rolesvista.jsp")) { %>
                <div class="col-md-2 col-sm-4 col-6">
                    <a href="rolesvista.jsp" class="hero hero-dark">
                        <span class="hero-body">
                            <span class="hero-label">Roles</span>
                            <span class="hero-number" id="totalRoles">0</span>
                            <span class="hero-sub">definidos</span>
                        </span>
                        <span class="hero-icon"><i class="fas fa-tag"></i></span>
                    </a>
                </div>
                <% } %>
            </div>

            <!-- Órdenes de trabajo y últimas compras -->
            <div class="row">
                <div class="col-lg-7" id="colOT">
                    <div class="card card-success">
                        <div class="card-header">
                            <h3 class="card-title"><i class="fas fa-clipboard-list"></i> Órdenes de Trabajo</h3>
                            <div class="card-tools"><span class="badge badge-primary mr-1" id="otACargo" style="display:none;" title="Órdenes abiertas en las que usted es el responsable"></span><span class="badge badge-danger" id="otDemoradas" style="display:none;" title="Abiertas hace más de 15 días"></span></div>
                        </div>
                        <div class="card-body">
                            <div class="row text-center mb-3">
                                <div class="col-4"><a href="otListado.jsp" class="ot-kpi"><span class="ot-kpi-num text-secondary" id="otPendientes">-</span><span class="ot-kpi-txt">Pendientes</span></a></div>
                                <div class="col-4"><a href="otListado.jsp" class="ot-kpi"><span class="ot-kpi-num text-warning" id="otEnProceso">-</span><span class="ot-kpi-txt">En proceso</span></a></div>
                                <div class="col-4"><a href="otListado.jsp" class="ot-kpi"><span class="ot-kpi-num text-success" id="otFinalizadasMes">-</span><span class="ot-kpi-txt">Finalizadas este mes</span></a></div>
                            </div>
                            <h6 class="text-muted mb-2">Abiertas más antiguas</h6>
                            <ul class="list-group" id="listaOTAbiertas">
                                <li class="list-group-item">Cargando...</li>
                            </ul>
                        </div>
                        <div class="card-footer">
                            <a href="otCrear.jsp" class="btn btn-sm btn-success" id="btnNuevaOT" style="display:none;"><i class="fas fa-plus"></i> Nueva OT</a>
                            <a href="otListado.jsp" class="btn btn-sm btn-primary float-right">Ver todas</a>
                        </div>
                    </div>
                </div>
                <div class="col-lg-5" id="colOC">
                    <div class="card card-secondary">
                        <div class="card-header">
                            <h3 class="card-title"><i class="fas fa-file-invoice"></i> Últimas Órdenes de Compra</h3>
                        </div>
                        <div class="card-body p-0">
                            <ul class="list-group list-group-unbordered" id="ultimasCompras">
                                <li class="list-group-item">Cargando...</li>
                            </ul>
                        </div>
                        <div class="card-footer">
                            <a href="listarOC.jsp" class="btn btn-sm btn-primary float-right">Ver todas</a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Gráficos: cada uno aparece solo si el rol puede ver esos datos -->
            <div class="row" id="filaGraficos" style="display:none;">
                <div class="col-lg-4 col-md-6" id="colGraficoOT" style="display:none;">
                    <div class="card card-success">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-chart-pie"></i> OT por estado <small class="text-muted">(últimos 12 meses)</small></h3></div>
                        <div class="card-body"><div class="grafico-caja"><canvas id="graficoOT"></canvas></div></div>
                    </div>
                </div>
                <div class="col-lg-4 col-md-6" id="colGraficoOC" style="display:none;">
                    <div class="card card-info">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-chart-bar"></i> Órdenes de compra por mes</h3></div>
                        <div class="card-body"><div class="grafico-caja"><canvas id="graficoOC"></canvas></div></div>
                    </div>
                </div>
                <div class="col-lg-4 col-md-12" id="colGraficoConsumo" style="display:none;">
                    <div class="card card-secondary">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-chart-bar"></i> Consumo por centro de costo <small class="text-muted">(90 días)</small></h3></div>
                        <div class="card-body"><div class="grafico-caja"><canvas id="graficoConsumo"></canvas></div></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

            <%@include file="footer.jsp" %>

<script src="plugins/chart.js/Chart.min.js"></script>
<script>
// Alertas de stock mínimo (la misma consulta que usa la campana de la barra superior)
$(function () {
    if (!$('#filaAlertasStock').length) return;
    var consulta = window.Nanday ? Nanday.alertasStock() : $.post('InventarioServlet', { tipo: 'alertasMinimo' }, null, 'json');
    consulta.done(function (lista) {
        if (!lista || !lista.length) return;
        var h = '';
        $.each(lista, function (i, a) {
            h += '<tr><td>' + escHtml(a.codigo) + '</td><td>' + escHtml(a.nombre) + '</td><td>' + escHtml(a.almacen) + '</td><td class="text-right ' +
                 (a.stock <= 0 ? 'text-danger' : 'text-warning') + ' font-weight-bold">' + a.stock + '</td><td class="text-right">' + a.minimo + '</td></tr>';
        });
        $('#cuerpoAlertas').html(h);
        $('#cantAlertas').text(lista.length);
        $('#filaAlertasStock').show();
    });
});
</script>

<script>
// Gráficos del tablero (Chart.js local, sin internet)
$(function () {
    if (!window.Chart) return;
    Chart.defaults.global.defaultFontFamily = $('body').css('font-family');
    Chart.defaults.global.defaultFontColor = '#5F6B63';

    function guaranies(v) { return 'Gs. ' + Math.round(Number(v) || 0).toLocaleString('es-PY'); }
    function sinDatos(idCanvas, texto) {
        $('#' + idCanvas).closest('.grafico-caja').replaceWith('<div class="grafico-vacio"><span><i class="fas fa-info-circle"></i> ' + escHtml(texto) + '</span></div>');
    }
    function suma(serie, campo) {
        var t = 0;
        $.each(serie, function (i, p) { t += Number(p[campo]) || 0; });
        return t;
    }

    $.post('DashboardServlet', { tipo: 'graficos' }, function (g) {
        if (!g || g.error) return;
        var hay = false;

        if (g.ot_estado) {
            hay = true;
            $('#colGraficoOT').show();
            var nombres = { PENDIENTE: 'Pendiente', EN_PROCESO: 'En proceso', FINALIZADA: 'Finalizada', ANULADA: 'Anulada' };
            if (suma(g.ot_estado, 'cantidad') === 0) {
                sinDatos('graficoOT', 'No hay órdenes de trabajo en los últimos 12 meses.');
            } else {
                new Chart(document.getElementById('graficoOT'), {
                    type: 'doughnut',
                    data: {
                        labels: $.map(g.ot_estado, function (p) { return nombres[p.etiqueta] || p.etiqueta; }),
                        datasets: [{ data: $.map(g.ot_estado, function (p) { return p.cantidad; }),
                            backgroundColor: ['#6C757D', '#F9A825', '#2E7D32', '#C62828'], borderWidth: 2 }]
                    },
                    options: { maintainAspectRatio: false, cutoutPercentage: 60, legend: { position: 'bottom' } }
                });
            }
        }

        if (g.oc_mes) {
            hay = true;
            $('#colGraficoOC').show();
            if (suma(g.oc_mes, 'cantidad') === 0) {
                sinDatos('graficoOC', 'No hay órdenes de compra en los últimos 6 meses.');
            } else {
                new Chart(document.getElementById('graficoOC'), {
                    type: 'bar',
                    data: {
                        labels: $.map(g.oc_mes, function (p) { return p.etiqueta; }),
                        datasets: [{ label: 'Órdenes de compra', data: $.map(g.oc_mes, function (p) { return p.cantidad; }),
                            backgroundColor: '#1F5F8B' }]
                    },
                    options: {
                        maintainAspectRatio: false, legend: { display: false },
                        scales: { yAxes: [{ ticks: { beginAtZero: true, precision: 0 } }] },
                        tooltips: { callbacks: { afterLabel: function (item) { return 'Monto: ' + guaranies(g.oc_mes[item.index].valor); } } }
                    }
                });
            }
        }

        if (g.consumo_cc) {
            hay = true;
            $('#colGraficoConsumo').show();
            if (!g.consumo_cc.length) {
                sinDatos('graficoConsumo', 'No hubo salidas de insumos en los últimos 90 días.');
            } else {
                new Chart(document.getElementById('graficoConsumo'), {
                    type: 'horizontalBar',
                    data: {
                        labels: $.map(g.consumo_cc, function (p) { return p.etiqueta; }),
                        datasets: [{ label: 'Consumo', data: $.map(g.consumo_cc, function (p) { return Number(p.valor); }),
                            backgroundColor: '#2E7D32' }]
                    },
                    options: {
                        maintainAspectRatio: false, legend: { display: false },
                        scales: { xAxes: [{ ticks: { beginAtZero: true, callback: function (v) { return Number(v).toLocaleString('es-PY'); } } }] },
                        tooltips: { callbacks: {
                            label: function (item) { return guaranies(item.xLabel); },
                            afterLabel: function (item) { return g.consumo_cc[item.index].cantidad + ' unidades'; } } }
                    }
                });
            }
        }

        // Con uno o dos gráficos, cada uno ocupa la mitad del ancho
        var $visibles = $('#filaGraficos > div').filter(function () { return this.style.display !== 'none'; });
        if ($visibles.length < 3) $visibles.removeClass('col-lg-4 col-md-12').addClass('col-lg-6 col-md-6');
        if (hay) $('#filaGraficos').show();
    }, 'json');
});
</script>

<script>
var ESTADO_OT = { PENDIENTE: ['Pendiente', 'badge-secondary'], EN_PROCESO: ['En proceso', 'badge-warning'] };
var ESTADO_OC = { SOLICITADO: 'badge-primary', PARCIAL: 'badge-warning', RECIBIDO: 'badge-success' };

$(document).ready(function () {
    // Conteos de los accesos rápidos
    $.post('DashboardServlet', { tipo: 'contar' }, function (d) {
        if (!d || d.error) return;
        $('#totalProveedores').text(d.TOTAL_PROVEEDORES || 0);
        $('#totalAlmacenes').text(d.TOTAL_ALMACENES || 0);
        $('#totalCentros').text(d.TOTAL_CENTROS || 0);
        $('#totalUsuarios').text(d.TOTAL_USUARIOS || 0);
        $('#totalInsumos').text(d.TOTAL_INSUMOS || 0);
        $('#totalRoles').text(d.TOTAL_ROLES || 0);
    }, 'json').fail(function() {
        $('#totalProveedores,#totalAlmacenes,#totalCentros,#totalUsuarios,#totalInsumos,#totalRoles').text('?');
    });

    // Órdenes de trabajo
    $.post('DashboardServlet', { tipo: 'resumenOT' }, function (r) {
        if (!r || r.sin_permiso) { $('#colOT').remove(); $('#colOC').removeClass('col-lg-5').addClass('col-lg-12'); return; }
        if (r.error) { $('#listaOTAbiertas').html('<li class="list-group-item text-danger">' + escHtml(r.error) + '</li>'); return; }
        $('#otPendientes').text(r.pendientes);
        $('#otEnProceso').text(r.en_proceso);
        $('#otFinalizadasMes').text(r.finalizadas_mes);
        if (r.demoradas > 0) $('#otDemoradas').text(r.demoradas + (r.demoradas === 1 ? ' demorada' : ' demoradas')).show();
        if (r.a_mi_cargo > 0) $('#otACargo').html('<a href="otListado.jsp" class="text-white">' + r.a_mi_cargo + ' a su cargo</a>').show();
        if (PERMISOS.OT_GESTION) $('#btnNuevaOT').show();
        r.abiertas = r.abiertas || [];
        if (!r.abiertas.length) {
            $('#listaOTAbiertas').html('<li class="list-group-item text-muted"><i class="fas fa-check text-success"></i> No hay órdenes de trabajo abiertas.</li>');
            return;
        }
        var h = '';
        $.each(r.abiertas, function (i, o) {
            var e = ESTADO_OT[o.estado] || [o.estado, 'badge-light'];
            var antig = o.dias === 0 ? 'hoy' : (o.dias === 1 ? 'hace 1 día' : 'hace ' + o.dias + ' días');
            h += '<li class="list-group-item ot-item' + (o.dias > 15 ? ' ot-demorada' : '') + '">' +
                '<a href="otListado.jsp?ot=' + parseInt(o.id, 10) + '" class="d-flex justify-content-between align-items-start">' +
                '<span><strong>' + escHtml(o.numero) + '</strong> <span class="badge ' + e[1] + '">' + escHtml(e[0]) + '</span><br>' +
                '<span class="text-dark">' + escHtml(o.titulo) + '</span><br><small class="text-muted">' + escHtml(o.centro) +
                (o.centro_costo ? ' &middot; ' + escHtml(o.centro_costo) : '') + ' &middot; ' + o.items + (o.items === 1 ? ' insumo' : ' insumos') +
                (o.responsable ? '<br><i class="fas fa-user-check"></i> ' + escHtml(o.responsable) : '<br><span class="text-warning"><i class="fas fa-user-slash"></i> sin responsable</span>') + '</small></span>' +
                '<small class="text-nowrap ml-2 ' + (o.dias > 15 ? 'text-danger font-weight-bold' : 'text-muted') + '">' + antig + '</small></a></li>';
        });
        $('#listaOTAbiertas').html(h);
    }, 'json').fail(function() { $('#listaOTAbiertas').html('<li class="list-group-item text-danger">Error al cargar</li>'); });

    // Últimas órdenes de compra
    $.post('DashboardServlet', { tipo: 'ultimasCompras' }, function (r) {
        if (!r || r.sin_permiso) { $('#colOC').remove(); $('#colOT').removeClass('col-lg-7').addClass('col-lg-12'); return; }
        if (r.error) { $('#ultimasCompras').html('<li class="list-group-item text-danger">' + escHtml(r.error) + '</li>'); return; }
        r.compras = r.compras || [];
        if (!r.compras.length) { $('#ultimasCompras').html('<li class="list-group-item">No hay órdenes de compra registradas</li>'); return; }
        var h = '';
        $.each(r.compras, function (i, c) {
            h += '<li class="list-group-item"><strong>' + escHtml(c.numero) + '</strong> <span class="badge ' + (ESTADO_OC[c.estado] || 'badge-light') + '">' + escHtml(c.estado) + '</span>' +
                '<span class="badge badge-info float-right">' + escHtml(c.fecha) + '</span><br><small>' + escHtml(c.proveedor) + '</small></li>';
        });
        $('#ultimasCompras').html(h);
    }, 'json').fail(function() { $('#ultimasCompras').html('<li class="list-group-item text-danger">Error al cargar</li>'); });
});
</script>
</body>
</html>