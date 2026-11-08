<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    HttpSession sesion = request.getSession();
    if (sesion.getAttribute("logueado") == null || sesion.getAttribute("logueado").equals("0")) {
%>
<script>alert('Ud. debe de identificarse..!!');</script>
<%
        response.sendRedirect("index.jsp");
        return;
    }
%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.proyectoprueba.util.Html"%>
<%@page import="com.mycompany.proyectoprueba.seguridad.Permisos"%>
<%@page import="com.mycompany.proyectoprueba.seguridad.Csrf"%>
<%@ include file="perfilmodal.jspf" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <%-- Token CSRF: footer.jsp lo envía en todas las peticiones AJAX --%>
        <meta name="csrf-token" content="<%= Html.esc(Csrf.token(sesion))%>">
        <title>Ñanday SIAA - Gestión de Activos</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/imagenes/favicon.ico">
        <link rel="icon" type="image/png" sizes="32x32" href="${pageContext.request.contextPath}/imagenes/favicon-32.png">
        <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/imagenes/apple-touch-icon.png">
        <link rel="stylesheet" href="dist/css/source-sans-pro.css">
        <link rel="stylesheet" href="plugins/fontawesome-free/css/all.min.css">
        <link rel="stylesheet" href="plugins/datatables-bs4/css/dataTables.bootstrap4.min.css">
        <link rel="stylesheet" href="plugins/datatables-responsive/css/responsive.bootstrap4.min.css">
        <link rel="stylesheet" href="plugins/datatables-buttons/css/buttons.bootstrap4.min.css">
        <link rel="stylesheet" href="dist/css/adminlte.min.css">
        <link rel="stylesheet" href="plugins/sweetalert2-theme-bootstrap-4/bootstrap-4.min.css">
        <%-- ?v= cambia cuando se modifica el CSS, así el navegador no usa una copia vieja --%>
        <%
            String rutaCssNanday = application.getRealPath("/dist/css/nanday-bold.css");
            long versionCssNanday = rutaCssNanday == null ? 0L : new java.io.File(rutaCssNanday).lastModified();
        %>
        <link rel="stylesheet" href="dist/css/nanday-bold.css?v=<%= versionCssNanday%>">
        <style>
            table.dataTable thead .sorting:before, table.dataTable thead .sorting_asc:before,
            table.dataTable thead .sorting_desc:before {
                content: "";
                position: absolute;
                color: #2E7D32;
                right: 0.6em;
                bottom: 0.9em;
                font-size: 0.65em;
                font-family: "Font Awesome 5 Free";
                font-weight: 900;
            }
            table.dataTable thead .sorting:before {
                content: "\f0de";
                opacity: .4;
            }
            table.dataTable thead .sorting_asc:before {
                content: "\f0de";
            }
            table.dataTable thead .sorting:after, table.dataTable thead .sorting_asc:after,
            table.dataTable thead .sorting_desc:after {
                content: "";
                position: absolute;
                color: #2E7D32;
                right: 0.6em;
                top: 0.9em;
                font-size: 0.65em;
                font-family: "Font Awesome 5 Free";
                font-weight: 900;
            }
            table.dataTable thead .sorting:after {
                content: "\f0dd";
                opacity: .4;
            }
            table.dataTable thead .sorting_desc:after {
                content: "\f0dd";
            }
            .content-wrapper .nav-pills .nav-link.active, .content-wrapper .nav-pills .show > .nav-link {
                background-color: #2E7D32;
            }
            .content-wrapper .nav-pills .nav-link:not(.active) {
                color: #1B4D2E;
            }
            .tipo-salida.activo {
                border-color: #2E7D32;
                background: rgba(46,125,50,.08);
            }</style>
    </head>
    <body class="hold-transition sidebar-mini">
        <div class="wrapper">

            <!-- Navbar superior -->
            <nav class="main-header navbar navbar-expand navbar-dark">
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <a class="nav-link" data-widget="pushmenu" href="#" role="button"><i class="fas fa-bars"></i></a>
                    </li>
                    <li class="nav-item d-none d-sm-inline-block">
                        <a href="dashboard.jsp" class="nav-link">Inicio</a>
                    </li>
                    <li class="nav-item dropdown d-none d-sm-inline-block">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarModulos" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Referenciales
                        </a>
                        <div class="dropdown-menu" aria-labelledby="navbarModulos">
                            <a class="dropdown-item" href="proveedorvista.jsp">Proveedores</a>
                            <a class="dropdown-item" href="centrosvista.jsp">Centros</a>
                            <a class="dropdown-item" href="centro_costovista.jsp">Centros de Costo</a>
                            <a class="dropdown-item" href="almacenesvista.jsp">Almacenes</a>
                            <div class="dropdown-divider"></div>
                            <a class="dropdown-item" href="usuariosvista.jsp">Usuarios</a>
                            <a class="dropdown-item" href="insumosvista.jsp">Insumos</a>
                            <a class="dropdown-item" href="rolesvista.jsp">Roles</a>
                            <a class="dropdown-item" href="unidad_medidavista.jsp">Unidades de Medida</a>
                        </div>
                    </li>
                    <!-- Menú desplegable de Compras -->
                    <li class="nav-item dropdown d-none d-sm-inline-block">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarCompras" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Compras
                        </a>
                        <div class="dropdown-menu" aria-labelledby="navbarCompras">
                            <a class="dropdown-item" href="solicitudCompra.jsp">Solicitud de Compra</a>
                            <a class="dropdown-item" href="listarOC.jsp">Orden de compra</a>
                            <a class="dropdown-item" href="recepcionOC.jsp">Recepcion OC</a>
                        </div>
                    </li>
                    <!-- Órdenes de Trabajo -->
                    <li class="nav-item dropdown d-none d-sm-inline-block">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarOT" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Órdenes de Trabajo
                        </a>
                        <div class="dropdown-menu" aria-labelledby="navbarOT">
                            <a class="dropdown-item" href="otCrear.jsp">Crear OT</a>
                            <a class="dropdown-item" href="otListado.jsp">Órdenes de Trabajo</a>
                            <a class="dropdown-item" href="otSuministrar.jsp">Suministrar OT</a>
                            <a class="dropdown-item" href="otFinalizar.jsp">Finalizar OT</a>
                        </div>
                    </li>
                    <!-- Activos Fijos -->
                    <li class="nav-item dropdown d-none d-sm-inline-block">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarActivos" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Activos Fijos
                        </a>
                        <div class="dropdown-menu" aria-labelledby="navbarActivos">
                            <a class="dropdown-item" href="activosFijosVista.jsp">Listar Activos Fijos</a>
                            <a class="dropdown-item" href="activosSalida.jsp">Salida de Activos Fijos</a>
                            <a class="dropdown-item" href="activosAprobaciones.jsp">Aprobaciones</a>
                            <a class="dropdown-item" href="activosBaja.jsp">Baja de Activos</a>
                        </div>
                    </li>
                    <!-- Informes -->
                    <li class="nav-item d-none d-sm-inline-block">
                        <a href="informes.jsp" class="nav-link">Informes</a>
                    </li>
                    <!-- Inventarios -->
                    <li class="nav-item dropdown d-none d-sm-inline-block">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarInventarios" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Inventarios
                        </a>
                        <div class="dropdown-menu" aria-labelledby="navbarInventarios">
                            <a class="dropdown-item" href="stockVista.jsp">Stock</a>
                            <a class="dropdown-item" href="kardex.jsp">Kardex</a>
                            <a class="dropdown-item" href="salidaInsumos.jsp">Salida de insumos</a>
                            <a class="dropdown-item" href="ajustesInventario.jsp">Ajuste puntual</a>
                            <a class="dropdown-item" href="transferencias.jsp">Transferencias</a>
                            <a class="dropdown-item" href="inventarioFisico.jsp">Inventario físico</a>
                            <a class="dropdown-item" href="reportesInventario.jsp">Reportes de inventario</a>
                        </div>
                    </li>
                </ul>

                <ul class="navbar-nav ml-auto">
                    <%-- Campana de notificaciones: pendientes y novedades según el rol (la completa js/nanday-ui.js) --%>
                    <% Permisos permisosCampana = Permisos.de(request);
                       if (permisosCampana != null && com.mycompany.proyectoprueba.servicio.NotificacionServicio.tieneAvisos(permisosCampana)) { %>
                    <li class="nav-item dropdown" id="campanaNotif">
                        <a class="nav-link" data-toggle="dropdown" href="#" title="Notificaciones" aria-label="Notificaciones">
                            <i class="far fa-bell"></i>
                            <span class="badge badge-warning navbar-badge" id="campanaCantidad" style="display:none;"></span>
                        </a>
                        <div class="dropdown-menu dropdown-menu-lg dropdown-menu-right campana-menu">
                            <span class="dropdown-item dropdown-header" id="campanaTitulo">Notificaciones</span>
                            <div class="dropdown-divider"></div>
                            <div id="campanaLista"><span class="dropdown-item text-muted small">Cargando...</span></div>
                            <% boolean campanaVeStock = permisosCampana.puedeVer("/stockVista.jsp");
                               boolean campanaPdf = com.mycompany.proyectoprueba.servicio.NotificacionServicio.veStock(permisosCampana);
                               boolean campanaCompra = permisosCampana.puedeVer("/solicitudCompra.jsp");
                               if (campanaVeStock || campanaPdf || campanaCompra) { %>
                            <div class="dropdown-divider"></div>
                            <% } %>
                            <% if (campanaVeStock) { %>
                            <a href="stockVista.jsp" class="dropdown-item"><i class="fas fa-boxes mr-2"></i> Ver el stock</a>
                            <% } %>
                            <% if (campanaPdf) { %>
                            <a href="#" class="dropdown-item" onclick="window.open('StockServlet?tipo=imprimirReponer', '_blank'); return false;"><i class="fas fa-file-pdf mr-2"></i> Insumos a reponer (PDF)</a>
                            <% } %>
                            <% if (campanaCompra) { %>
                            <a href="solicitudCompra.jsp" class="dropdown-item"><i class="fas fa-shopping-cart mr-2"></i> Solicitar compra</a>
                            <% } %>
                        </div>
                    </li>
                    <% } %>
                    <li class="nav-item">
                        <a class="nav-link" data-widget="fullscreen" href="#" role="button"><i class="fas fa-expand-arrows-alt"></i></a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link js-logout" href="logout.jsp" role="button" title="Cerrar sesión"><i class="fas fa-sign-out-alt"></i><span class="d-none d-md-inline ml-1">Salir</span></a>
                    </li>
                </ul>
            </nav>
            <!-- fin de la barra superior -->

            <!-- Menú lateral -->
            <aside class="main-sidebar sidebar-dark-primary elevation-4">
                <a href="dashboard.jsp" class="brand-link">
                    <img src="imagenes/logo_ave.png" alt="Ñanday SIAA" class="brand-image nd-logo">
                    <span class="brand-text nd-marca">
                        <span class="nd-marca-nombre">Ñanday SIAA</span>
                        <span class="nd-marca-sub">Gestión de activos e insumos</span>
                    </span>
                </a>

                <div class="sidebar">
                    <%                        String fotoPerfilSidebar = (String) session.getAttribute("foto_perfil");
                        if (fotoPerfilSidebar == null || fotoPerfilSidebar.isEmpty()) {
                            fotoPerfilSidebar = "dist/img/perfil_1.jpg";
                        }
                    %>
                    <div class="user-panel mt-3 pb-3 mb-3 d-flex align-items-center">
                        <div class="image">
                            <img id="imgPerfilSidebar" src="<%= Html.esc(fotoPerfilSidebar)%>" class="img-circle elevation-2"
                                 style="width:45px; height:45px; object-fit:cover;" alt="User Image">
                        </div>
                        <div class="info">
                            <a href="#" class="d-block" data-toggle="modal" data-target="#modalPerfil" id="lnkNombreSidebar">
                                <%= Html.esc(session.getAttribute("nombre_completo") != null ? session.getAttribute("nombre_completo") : "Usuario")%>
                            </a>
                        </div>
                    </div>
                    <div class="form-inline">
                        <div class="input-group" data-widget="sidebar-search">
                            <input class="form-control form-control-sidebar" type="search" placeholder="Buscar..." aria-label="Search">
                            <div class="input-group-append">
                                <button class="btn btn-sidebar"><i class="fas fa-search fa-fw"></i></button>
                            </div>
                        </div>
                    </div>

                    <nav class="mt-2">
                        <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="true">
                            <!-- Referenciales -->
                            <li class="nav-item">
                                <a href="#" class="nav-link">
                                    <i class="nav-icon fas fa-cog"></i>
                                    <p>Referenciales <i class="right fas fa-angle-left"></i></p>
                                </a>
                                <ul class="nav nav-treeview">
                                    <li class="nav-item"><a href="proveedorvista.jsp" class="nav-link"><i class="fas fa-truck nav-icon"></i><p>Proveedores</p></a></li>
                                    <li class="nav-item"><a href="centrosvista.jsp" class="nav-link"><i class="fas fa-building nav-icon"></i><p>Centros</p></a></li>
                                    <li class="nav-item"><a href="centro_costovista.jsp" class="nav-link"><i class="fas fa-coins nav-icon"></i><p>Centros de Costo</p></a></li>
                                    <li class="nav-item"><a href="almacenesvista.jsp" class="nav-link"><i class="fas fa-warehouse nav-icon"></i><p>Almacenes</p></a></li>
                                    <li class="nav-item"><a href="usuariosvista.jsp" class="nav-link"><i class="fas fa-users nav-icon"></i><p>Usuarios</p></a></li>
                                    <li class="nav-item"><a href="insumosvista.jsp" class="nav-link"><i class="fas fa-box-open nav-icon"></i><p>Insumos</p></a></li>
                                    <li class="nav-item"><a href="rolesvista.jsp" class="nav-link"><i class="fas fa-user-shield nav-icon"></i><p>Roles</p></a></li>
                                    <li class="nav-item"><a href="unidad_medidavista.jsp" class="nav-link"><i class="fas fa-ruler-combined nav-icon"></i><p>Unidades de Medida</p></a></li>
                                </ul>
                            </li>

                            <!-- Compras -->
                            <li class="nav-item">
                                <a href="#" class="nav-link">
                                    <i class="nav-icon fas fa-shopping-cart"></i>
                                    <p>Compras <i class="right fas fa-angle-left"></i></p>
                                </a>
                                <ul class="nav nav-treeview">
                                    <li class="nav-item"><a href="solicitudCompra.jsp" class="nav-link"><i class="fas fa-cart-plus nav-icon"></i><p>Solicitud de Compra</p></a></li>
                                    <li class="nav-item">
                                        <a href="listarOC.jsp" class="nav-link">
                                            <i class="fas fa-file-invoice nav-icon"></i>
                                            <p>Órdenes de Compra</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="recepcionOC.jsp" class="nav-link">
                                            <i class="fas fa-boxes nav-icon"></i>
                                            <p>Recepción OC</p>
                                        </a>
                                    </li>
                                </ul>
                            </li>

                            <!-- Órdenes de Trabajo -->
                            <li class="nav-item">
                                <a href="#" class="nav-link">
                                    <i class="nav-icon fas fa-clipboard-list"></i>
                                    <p>Órdenes de Trabajo <i class="right fas fa-angle-left"></i></p>
                                </a>
                                <ul class="nav nav-treeview">
                                    <li class="nav-item">
                                        <a href="otCrear.jsp" class="nav-link">
                                            <i class="fas fa-plus-circle nav-icon"></i>
                                            <p>Crear OT</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="otListado.jsp" class="nav-link">
                                            <i class="fas fa-list nav-icon"></i>
                                            <p>Órdenes de Trabajo</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="otSuministrar.jsp" class="nav-link">
                                            <i class="fas fa-dolly nav-icon"></i>
                                            <p>Suministrar OT</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="otFinalizar.jsp" class="nav-link">
                                            <i class="fas fa-check-double nav-icon"></i>
                                            <p>Finalizar OT</p>
                                        </a>
                                    </li>
                                </ul>
                            </li>

                            <!-- Activos Fijos -->
                            <li class="nav-item">
                                <a href="#" class="nav-link">
                                    <i class="nav-icon fas fa-laptop"></i>
                                    <p>Activos Fijos <i class="right fas fa-angle-left"></i></p>
                                </a>
                                <ul class="nav nav-treeview">
                                    <li class="nav-item">
                                        <a href="activosFijosVista.jsp" class="nav-link">
                                            <i class="fas fa-list nav-icon"></i>
                                            <p>Listar Activos Fijos</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="activosSalida.jsp" class="nav-link">
                                            <i class="fas fa-dolly-flatbed nav-icon"></i>
                                            <p>Salida de Activos Fijos</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="activosAprobaciones.jsp" class="nav-link">
                                            <i class="fas fa-stamp nav-icon"></i>
                                            <p>Aprobaciones</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="activosBaja.jsp" class="nav-link">
                                            <i class="fas fa-ban nav-icon"></i>
                                            <p>Baja de Activos</p>
                                        </a>
                                    </li>
                                </ul>
                            </li>

                            <!-- Inventarios -->
                            <li class="nav-item">
                                <a href="#" class="nav-link">
                                    <i class="nav-icon fas fa-warehouse"></i>
                                    <p>Inventarios <i class="right fas fa-angle-left"></i></p>
                                </a>
                                <ul class="nav nav-treeview">
                                    <li class="nav-item">
                                        <a href="stockVista.jsp" class="nav-link">
                                            <i class="fas fa-boxes nav-icon"></i>
                                            <p>Stock</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="kardex.jsp" class="nav-link">
                                            <i class="fas fa-book nav-icon"></i>
                                            <p>Kardex</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="salidaInsumos.jsp" class="nav-link">
                                            <i class="fas fa-truck-loading nav-icon"></i>
                                            <p>Salida de insumos</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="ajustesInventario.jsp" class="nav-link">
                                            <i class="fas fa-sliders-h nav-icon"></i>
                                            <p>Ajuste puntual</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="transferencias.jsp" class="nav-link">
                                            <i class="fas fa-exchange-alt nav-icon"></i>
                                            <p>Transferencias</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="inventarioFisico.jsp" class="nav-link">
                                            <i class="fas fa-clipboard-check nav-icon"></i>
                                            <p>Inventario físico</p>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a href="reportesInventario.jsp" class="nav-link">
                                            <i class="fas fa-chart-bar nav-icon"></i>
                                            <p>Reportes de inventario</p>
                                        </a>
                                    </li>
                                </ul>
                            </li>

                            <li class="nav-item">
                                <a href="informes.jsp" class="nav-link">
                                    <i class="nav-icon fas fa-chart-line"></i>
                                    <p>Informes</p>
                                </a>
                            </li>

                            <%-- Seguridad: bitácora del sistema (solo administrador) --%>
                            <% Permisos permisosMenuBitacora = Permisos.de(request);
                               if (permisosMenuBitacora != null && permisosMenuBitacora.esAdministrador()) { %>
                            <li class="nav-header">SEGURIDAD</li>
                            <li class="nav-item">
                                <a href="bitacora.jsp" class="nav-link">
                                    <i class="nav-icon fas fa-history"></i>
                                    <p>Bitácora</p>
                                </a>
                            </li>
                            <% } %>

                            <li class="nav-item">
                                <a href="logout.jsp" class="nav-link js-logout">
                                    <i class="nav-icon fas fa-sign-out-alt"></i>
                                    <p>Cerrar sesión</p>
                                </a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </aside>

            <%-- Cierre de sesión: logout.jsp solo acepta POST --%>
            <form id="frmLogout" action="logout.jsp" method="post" style="display:none"></form>
            <script>
                document.addEventListener('click', function (e) {
                    var a = e.target.closest ? e.target.closest('.js-logout') : null;
                    if (!a)
                        return;
                    e.preventDefault();
                    document.getElementById('frmLogout').submit();
                });
            </script>

            <%-- Menú según el rol: el servidor calcula qué pantallas puede abrir el usuario.
                 (Solo oculta opciones; el control real lo hace el FiltroSeguridad.) --%>
            <%
                Permisos permisosUsuario = Permisos.de(request);
                String[] paginasMenu = {"informes.jsp", "activosAprobaciones.jsp", "activosBaja.jsp", "activosFijosVista.jsp", "activosSalida.jsp", "ajustesInventario.jsp", "almacenesvista.jsp", "centro_costovista.jsp", "centrosvista.jsp", "insumosvista.jsp", "inventarioFisico.jsp", "kardex.jsp", "listarOC.jsp", "otCrear.jsp", "otFinalizar.jsp", "otListado.jsp", "otSuministrar.jsp", "proveedorvista.jsp", "recepcionOC.jsp", "reportesInventario.jsp", "rolesvista.jsp", "salidaInsumos.jsp", "solicitudCompra.jsp", "stockVista.jsp", "transferencias.jsp", "unidad_medidavista.jsp", "usuariosvista.jsp"};
                StringBuilder permitidas = new StringBuilder();
                for (int ip = 0; ip < paginasMenu.length; ip++) {
                    if (permisosUsuario.puedeVer("/" + paginasMenu[ip])) {
                        permitidas.append(permitidas.length() == 0 ? "" : ",").append('"').append(paginasMenu[ip]).append('"');
                    }
                }
            %>
            <script>
                // Permisos del usuario para las pantallas (mostrar u ocultar botones)
                var PERMISOS = <%= permisosUsuario.comoJson()%>;
                (function () {
                    var permitidas = [<%= permitidas.toString()%>];
                    var controladas = ["informes.jsp", "activosAprobaciones.jsp", "activosBaja.jsp", "activosFijosVista.jsp", "activosSalida.jsp", "ajustesInventario.jsp", "almacenesvista.jsp", "centro_costovista.jsp", "centrosvista.jsp", "insumosvista.jsp", "inventarioFisico.jsp", "kardex.jsp", "listarOC.jsp", "otCrear.jsp", "otFinalizar.jsp", "otListado.jsp", "otSuministrar.jsp", "proveedorvista.jsp", "recepcionOC.jsp", "reportesInventario.jsp", "rolesvista.jsp", "salidaInsumos.jsp", "solicitudCompra.jsp", "stockVista.jsp", "transferencias.jsp", "unidad_medidavista.jsp", "usuariosvista.jsp"];
                    function quitar(el) {
                        if (el && el.parentNode)
                            el.parentNode.removeChild(el);
                    }
                    // Enlaces a pantallas no permitidas
                    var enlaces = document.querySelectorAll('.main-header a[href], .main-sidebar a[href]');
                    for (var i = 0; i < enlaces.length; i++) {
                        var href = enlaces[i].getAttribute('href');
                        if (controladas.indexOf(href) >= 0 && permitidas.indexOf(href) < 0) {
                            var li = enlaces[i].closest('.main-sidebar li.nav-item');
                            quitar(li && li.parentNode.classList.contains('nav-treeview') ? li : enlaces[i]);
                        }
                    }
                    // Menús que quedaron vacíos
                    var dropdowns = document.querySelectorAll('.main-header li.dropdown');
                    for (var j = 0; j < dropdowns.length; j++) {
                        if (dropdowns[j].id !== 'campanaNotif' && !dropdowns[j].querySelector('.dropdown-menu a'))
                            quitar(dropdowns[j]);
                    }
                    var arboles = document.querySelectorAll('.main-sidebar ul.nav-treeview');
                    for (var k = 0; k < arboles.length; k++) {
                        if (!arboles[k].querySelector('li'))
                            quitar(arboles[k].closest('li.nav-item'));
                    }
                    // Aviso cuando se intentó abrir una pantalla sin permiso
                    if (/[?&]acceso=denegado/.test(location.search)) {
                        document.addEventListener('DOMContentLoaded', function () {
                            var cw = document.querySelector('.content-wrapper');
                            if (!cw)
                                return;
                            var d = document.createElement('div');
                            d.className = 'alert alert-warning m-3';
                            d.innerHTML = '<i class="fas fa-lock"></i> Su rol no tiene permiso para abrir esa pantalla.';
                            cw.insertBefore(d, cw.firstChild);
                        });
                    }
                })();
            </script>

            <!-- Script para marcar la opción activa en el menú lateral -->
            <script>
                (function () {
                    var currentPage = window.location.pathname.split("/").pop();
                    if (currentPage === "" || currentPage === "dashboard.jsp") {
                        return;
                    }
                    var links = document.querySelectorAll('.sidebar .nav-link');
                    for (var i = 0; i < links.length; i++) {
                        var link = links[i];
                        var href = link.getAttribute('href');
                        if (href && href === currentPage) {
                            link.classList.add('active');
                            var parentLi = link.closest('li.nav-item');
                            if (parentLi) {
                                var parentUl = parentLi.parentElement.closest('li.nav-item');
                                if (parentUl)
                                    parentUl.classList.add('menu-open');
                            }
                            break;
                        }
                    }
                })();
            </script>
