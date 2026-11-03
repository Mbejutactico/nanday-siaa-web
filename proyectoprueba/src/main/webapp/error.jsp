<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Ñanday SIAA</title>
<style>body{font-family:sans-serif;background:#f4f6f9;color:#333;text-align:center;padding:80px 20px}h1{font-size:22px}a{color:#1a7f37}</style>
</head>
<body>
<h1>Ocurrió un problema al procesar la solicitud.</h1>
<p>Intente de nuevo. Si el problema continúa, contacte al administrador del sistema.</p>
<p><a href="<%= request.getContextPath() %>/index.jsp">Volver al inicio</a></p>
</body>
</html>
