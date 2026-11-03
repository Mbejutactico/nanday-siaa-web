# Ñanday SIAA – Sistema web

Sistema web de administración de recursos y activos: compras, recepción, órdenes de trabajo, inventario, activos fijos, informes y bitácora.
Desarrollado con Java EE (Servlets y JSP), PostgreSQL y Apache Tomcat.

## Requisitos

| Componente | Versión |
|---|---|
| JDK | OpenJDK 17 |
| PostgreSQL | 16 (con pgAdmin 4, opcional) |
| Apache Tomcat | 9 (ver nota para Tomcat 10 u 11) |
| Driver JDBC | postgresql-42.7.10.jar, copiado en la carpeta `lib` de Tomcat |
| IDE (para compilar) | Apache NetBeans con Maven |

> **Tomcat 10 u 11:** el sistema usa Java EE (`javax`). En esas versiones, el `.war` debe copiarse en la carpeta `webapps-javaee` (no en `webapps`), y Tomcat lo convierte automáticamente al iniciar.

## Base de datos

El script completo está en `proyectoprueba/db/proyecto_final_completo.sql`. Crea el usuario `nanday_app` con el que se conecta el sistema.

1. Crear la base de datos `proyecto_final` en PostgreSQL.
2. Ejecutar el script sobre esa base (pgAdmin → Query Tool, o `psql -d proyecto_final -f proyecto_final_completo.sql`).

Los datos de conexión (servidor, base, usuario y contraseña) están en `src/main/webapp/META-INF/context.xml`. Si la base está en otra PC, se cambia ahí.

## Compilación y despliegue

1. Abrir el proyecto en Apache NetBeans.
2. Hacer **Clean and Build**; se genera el `.war` en la carpeta `target`.
3. Copiar el `.war` en Tomcat (`webapps`, o `webapps-javaee` en Tomcat 10 u 11) e iniciar Tomcat.
4. Abrir en el navegador `http://localhost:8080/` seguido del nombre de la aplicación.
