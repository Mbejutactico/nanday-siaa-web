<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Iniciar Sesión | Ñanday SIAA</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/imagenes/favicon.ico">
        <link rel="icon" type="image/png" sizes="32x32" href="${pageContext.request.contextPath}/imagenes/favicon-32.png">
        <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/imagenes/apple-touch-icon.png">

        <!-- Fuente Source Sans Pro -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/dist/css/source-sans-pro.css">
        <!-- Font Awesome -->
        <link rel="stylesheet" href="plugins/fontawesome-free/css/all.min.css">
        <!-- Casillas de verificación (icheck) -->
        <link rel="stylesheet" href="plugins/icheck-bootstrap/icheck-bootstrap.min.css">
        <!-- Estilos de AdminLTE -->
        <link rel="stylesheet" href="dist/css/adminlte.min.css">
        <script src="plugins/jquery/jquery.min.js"></script>
        <style>
            @import url("dist/css/nanday-bold.css");
            body {
                background: #0E2216 url("imagenes/fondos/deposito.svg") center bottom / cover no-repeat fixed;
            }
            .login-logo {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 12px;
                margin-bottom: 20px;
                border-bottom: 2px solid #C2410C;
                padding-bottom: 15px;
            }
            .login-logo img {
                height: 80px;
                width: auto;
                object-fit: contain;
            }
            .login-logo a {
                color: #FFFFFF;
                font-weight: bold;
                letter-spacing: 1px;
                font-size: 1.8rem;
                text-decoration: none;
            }
            .login-logo a b {
                color: #FB923C;
                font-size: 2rem;
            }
            .card {
                border-radius: 12px;
                border: none;
                border-top: 4px solid #C2410C;
                background-color: #FFFFFF;
                box-shadow: 0 18px 40px rgba(0, 0, 0, 0.55);
                overflow: hidden;
            }
            .card-body {
                background-color: #FFFFFF;
                padding: 1.5rem;
            }
            .btn-primary {
                background-color: #2E7D32;
                border-color: #2E7D32;
                transition: background-color 0.2s ease;
            }
            .btn-primary:hover, .btn-primary:focus {
                background-color: #256B29;
                border-color: #256B29;
            }
            .input-group-text {
                background-color: #E8F2EA;
                color: #1B4D2E;
                border-color: #D5DED7;
            }
            .form-control:focus {
                border-color: #2E7D32;
                box-shadow: 0 0 0 0.2rem rgba(46, 125, 50, 0.2);
            }
            .alert-danger {
                background-color: #FDECEA;
                border-color: #F5C2C0;
                color: #7F1D1D;
            }
            .login-box-msg {
                color: #1B4D2E;
                font-weight: bold;
                border-bottom: 1px solid #E8F2EA;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }

            /* Campos del login: cada grupo es una sola caja (borde y redondeo en el grupo),
               con línea divisoria entre el ícono, el campo y el botón para ver la contraseña */
            .login-page .login-card-body .input-group {
                flex-wrap: nowrap;
                border: 1px solid #D5DED7;
                border-radius: .5rem;
                background-color: #FFFFFF;
                overflow: hidden;
                transition: border-color .15s ease, box-shadow .15s ease;
            }
            .login-page .login-card-body .input-group .input-group-text,
            .login-page .login-card-body .input-group .form-control,
            .login-page .login-card-body .input-group .btn-ojo {
                border: 0;
                border-radius: 0;
                box-shadow: none;
                background-color: #FFFFFF;
            }
            .login-page .login-card-body .input-group-prepend,
            .login-page .login-card-body .input-group-append { margin: 0; }
            .login-page .login-card-body .input-group .input-group-text {
                color: #2E7D32;
                border-right: 1px solid #D5DED7;
                width: 2.75rem;
                justify-content: center;
            }
            .login-page .login-card-body .input-group .btn-ojo {
                color: #4B5563;
                border-left: 1px solid #D5DED7;
                width: 2.75rem;
            }
            .login-page .login-card-body .input-group .btn-ojo:hover { color: #2E7D32; }
            /* Al escribir: solo el contorno del grupo se pone verde */
            .login-page .login-card-body .input-group:focus-within {
                border-color: #2E7D32;
                box-shadow: 0 0 0 0.2rem rgba(46, 125, 50, 0.2);
            }
        </style>
    </head>
    <body class="hold-transition login-page">
        <div class="login-box">
            <div class="login-logo">
                <!-- Logo -->
                <img src="imagenes/logo_ave.png" alt="Logo Ñanday SIAA">
                <a href="#"><b>Ñanday</b> SIAA</a>
            </div>

            <div class="card">
                <div class="card-body login-card-body">
                    <p class="login-box-msg">Acceder al sistema</p>

                    <form id="formdata">
                        <input type="hidden" id="tipo" name="tipo" value="login">
                        <div class="input-group mb-3">
                            <div class="input-group-prepend">
                                <div class="input-group-text"><span class="fas fa-envelope"></span></div>
                            </div>
                            <input type="text" class="form-control" placeholder="Correo" id="usuario" name="usuario">
                        </div>
                        <div class="input-group mb-3">
                            <div class="input-group-prepend">
                                <div class="input-group-text"><span class="fas fa-lock"></span></div>
                            </div>
                            <input type="password" class="form-control" placeholder="Contraseña" id="pass" name="pass">
                            <div class="input-group-append">
                                <button type="button" class="btn btn-ojo" id="btnVerPass" title="Mostrar contraseña" aria-label="Mostrar contraseña">
                                    <i class="fas fa-eye"></i>
                                </button>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-8">
                                <div class="icheck-primary">
                                </div>
                            </div>
                            <div class="col-4">
                                <button type="button" id="btnacceder" class="btn btn-primary btn-block">Ingresar</button>
                            </div>
                        </div>
                    </form>
                    <div id="mensaje"></div>
                </div>
            </div>
        </div>
        <script src="plugins/bootstrap/js/bootstrap.bundle.min.js"></script>
        <script>
            // Enter en el correo o la contraseña hace lo mismo que el botón Ingresar
            $("#usuario, #pass").on("keydown", function (e) {
                if (e.key === "Enter" || e.keyCode === 13) {
                    e.preventDefault();
                    $("#btnacceder").click();
                }
            });
            $("#btnacceder").click(function () {
                var $btn = $(this);
                if ($btn.prop("disabled")) return;
                datosformulario = $("#formdata").serialize();
                // Mientras se verifica (un intento fallido demora 1 segundo) no se puede volver a enviar
                $btn.prop("disabled", true).html('<span class="spinner-border spinner-border-sm"></span> Verificando...');
                $("#mensaje").html("");
                $.ajax({
                    type: "post",
                    url: "LoginServlet",
                    data: datosformulario,
                    success: function (response) {
                        if (response.trim() === "true") {
                            window.location.href = 'dashboard.jsp';
                            return;
                        }
                        $("#mensaje").html(response);
                        $btn.prop("disabled", false).text("Ingresar");
                    },
                    error: function () {
                        $("#mensaje").html('<div class="alert alert-danger" role="alert">Error de conexión. Intente de nuevo.</div>');
                        $btn.prop("disabled", false).text("Ingresar");
                    }
                });
            });
        </script>
        <script>
            // Mostrar u ocultar la contraseña
            $('#btnVerPass').on('click', function () {
                var campo = $('#pass');
                var mostrar = campo.attr('type') === 'password';
                campo.attr('type', mostrar ? 'text' : 'password');
                $(this).find('i').toggleClass('fa-eye', !mostrar).toggleClass('fa-eye-slash', mostrar);
                $(this).attr({title: mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña', 'aria-label': mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña'});
                campo.trigger('focus');
            });
        </script>
    </body>
</html>