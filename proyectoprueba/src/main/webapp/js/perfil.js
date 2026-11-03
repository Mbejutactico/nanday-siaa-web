$(function () {

    var URL_CONTROLADOR = "PerfilServlet";

    // Guardar Nombre y Apellido
    $("#btnGuardarDatos").click(function () {
        var nombre = $("#pf_nombre").val().trim();
        var apellido = $("#pf_apellido").val().trim();

        if (nombre === "" || apellido === "") {
            $("#msgDatos").html("<div class='alert alert-danger'>Nombre y apellido son obligatorios</div>");
            return;
        }

        $.ajax({
            type: "POST",
            url: URL_CONTROLADOR,
            data: { tipo: "ActualizarDatos", nombre: nombre, apellido: apellido },
            success: function (response) {
                $("#msgDatos").html(response);
                if (response.indexOf("alert-success") !== -1) {
                    $("#lnkNombreSidebar").text(nombre + " " + apellido);
                }
            },
            error: function () {
                $("#msgDatos").html("<div class='alert alert-danger'>Error de comunicación con el servidor</div>");
            }
        });
    });

    // Vista previa de la foto al seleccionar archivo
    $("#pf_archivo").change(function () {
        var file = this.files[0];
        if (!file) {
            return;
        }
        var reader = new FileReader();
        reader.onload = function (e) {
            $("#pf_preview").attr("src", e.target.result);
        };
        reader.readAsDataURL(file);
    });

    // Subir Foto de Perfil
    $("#btnGuardarFoto").click(function () {
        var archivo = $("#pf_archivo")[0].files[0];
        if (!archivo) {
            $("#msgFoto").html("<div class='alert alert-danger'>Seleccione una imagen primero</div>");
            return;
        }
        if (archivo.size > 2 * 1024 * 1024) {
            $("#msgFoto").html("<div class='alert alert-danger'>La imagen supera el tamaño máximo permitido (2MB)</div>");
            return;
        }
        if (archivo.type !== "image/jpeg" && archivo.type !== "image/png") {
            $("#msgFoto").html("<div class='alert alert-danger'>Formato no permitido. Use JPG o PNG</div>");
            return;
        }

        var formData = new FormData();
        formData.append("tipo", "ActualizarFoto");
        formData.append("foto", archivo);

        $.ajax({
            type: "POST",
            url: URL_CONTROLADOR,
            data: formData,
            processData: false,
            contentType: false,
            success: function (response) {
                // Si la respuesta es una ruta (no un <div> de error), fue exitoso
                if (response.indexOf("<div") === -1) {
                    $("#imgPerfilSidebar").attr("src", response + "?t=" + new Date().getTime());
                    $("#pf_preview").attr("src", response + "?t=" + new Date().getTime());
                    $("#msgFoto").html("<div class='alert alert-success'>Foto actualizada con éxito</div>");
                } else {
                    $("#msgFoto").html(response);
                }
            },
            error: function () {
                $("#msgFoto").html("<div class='alert alert-danger'>Error de comunicación con el servidor</div>");
            }
        });
    });

    // Cambiar Contraseña
    $("#btnGuardarPassword").click(function () {
        var actual = $("#pf_pass_actual").val();
        var nueva = $("#pf_pass_nueva").val();
        var confirmar = $("#pf_pass_confirmar").val();

        if (!actual || !nueva || !confirmar) {
            $("#msgPassword").html("<div class='alert alert-danger'>Todos los campos son obligatorios</div>");
            return;
        }
        if (nueva.length < 6) {
            $("#msgPassword").html("<div class='alert alert-danger'>La nueva contraseña debe tener al menos 6 caracteres</div>");
            return;
        }
        if (nueva !== confirmar) {
            $("#msgPassword").html("<div class='alert alert-danger'>La confirmación no coincide</div>");
            return;
        }

        $.ajax({
            type: "POST",
            url: URL_CONTROLADOR,
            data: {
                tipo: "CambiarPassword",
                password_actual: actual,
                password_nueva: nueva,
                password_confirmar: confirmar
            },
            success: function (response) {
                $("#msgPassword").html(response);
                if (response.indexOf("alert-success") !== -1) {
                    $("#pf_pass_actual").val("");
                    $("#pf_pass_nueva").val("");
                    $("#pf_pass_confirmar").val("");
                }
            },
            error: function () {
                $("#msgPassword").html("<div class='alert alert-danger'>Error de comunicación con el servidor</div>");
            }
        });
    });

});
