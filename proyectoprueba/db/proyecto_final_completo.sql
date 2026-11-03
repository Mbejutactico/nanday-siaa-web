-- Ñanday SIAA - Base de datos proyecto_final completa (estructura + datos), con el usuario nanday_app
--
-- Generado a partir del respaldo bd.backup (PostgreSQL 18.6) del 26/10/2026.
-- A diferencia de un respaldo hecho con pg_dump en formato texto, este archivo NO usa
-- bloques COPY: los datos van como INSERT, por eso se puede ejecutar directamente en el
-- Query Tool de pgAdmin, en cualquier PostgreSQL 13 o superior.
--
-- Contenido: 33 tablas con sus datos, secuencias, claves, índices, restricciones,
-- la extensión pgcrypto y los permisos del usuario nanday_app.
--
-- Cómo restaurar en otra PC:
--   1) En pgAdmin: clic derecho en Databases > Create > Database...
--        Database: proyecto_final   (pestaña Definition) Encoding: UTF8
--   2) Clic derecho en la base nueva proyecto_final > Query Tool.
--   3) Abrir este archivo y ejecutar con F5. Al final se muestra un resumen.
--   La base debe estar VACÍA: si ya tiene las tablas, el script da error.
--
-- Usuario de la aplicación: nanday_app, contraseña Nanday.App-2026 (la misma que trae
-- META-INF/context.xml). Si en Tomcat se usa otra contraseña, cambiala abajo.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'nanday_app') THEN
        CREATE ROLE nanday_app LOGIN PASSWORD 'Nanday.App-2026'
            NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    ELSE
        ALTER ROLE nanday_app LOGIN PASSWORD 'Nanday.App-2026';
    END IF;
END $$;
DO $$ BEGIN EXECUTE format('GRANT CONNECT ON DATABASE %I TO nanday_app', current_database()); END $$;
--
-- PostgreSQL database dump
--


-- Dumped from database version 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ajustes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ajustes (
    id_ajuste integer NOT NULL,
    numero character varying(30) NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo character varying(30) NOT NULL,
    origen character varying(20) DEFAULT 'MANUAL'::character varying NOT NULL,
    id_centro integer NOT NULL,
    id_centro_costo integer,
    id_almacen integer NOT NULL,
    motivo text NOT NULL,
    id_usuario integer,
    anulado boolean DEFAULT false NOT NULL,
    fecha_anulacion timestamp without time zone,
    id_usuario_anula integer,
    motivo_anulacion text,
    CONSTRAINT ajustes_origen_check CHECK (((origen)::text = ANY (ARRAY[('MANUAL'::character varying)::text, ('INVENTARIO_FISICO'::character varying)::text]))),
    CONSTRAINT ajustes_tipo_check CHECK (((tipo)::text = ANY (ARRAY[('CONSUMO'::character varying)::text, ('ROTURA_PERDIDA'::character varying)::text, ('VENCIMIENTO'::character varying)::text, ('CORRECCION_CONTEO'::character varying)::text])))
);


ALTER TABLE public.ajustes OWNER TO postgres;

--
-- Name: ajustes_detalle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ajustes_detalle (
    id_detalle integer NOT NULL,
    id_ajuste integer NOT NULL,
    id_insumo integer NOT NULL,
    cantidad integer NOT NULL,
    costo_unitario numeric(12,2),
    CONSTRAINT ajustes_detalle_cantidad_check CHECK ((cantidad <> 0))
);


ALTER TABLE public.ajustes_detalle OWNER TO postgres;

--
-- Name: ajustes_detalle_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ajustes_detalle_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ajustes_detalle_id_detalle_seq OWNER TO postgres;

--
-- Name: ajustes_detalle_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ajustes_detalle_id_detalle_seq OWNED BY public.ajustes_detalle.id_detalle;


--
-- Name: ajustes_id_ajuste_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ajustes_id_ajuste_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ajustes_id_ajuste_seq OWNER TO postgres;

--
-- Name: ajustes_id_ajuste_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ajustes_id_ajuste_seq OWNED BY public.ajustes.id_ajuste;


--
-- Name: almacenes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.almacenes (
    id_almacen integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    ubicacion character varying(200),
    id_centro integer NOT NULL,
    activo boolean DEFAULT true
);


ALTER TABLE public.almacenes OWNER TO postgres;

--
-- Name: almacenes_id_almacen_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.almacenes_id_almacen_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.almacenes_id_almacen_seq OWNER TO postgres;

--
-- Name: almacenes_id_almacen_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.almacenes_id_almacen_seq OWNED BY public.almacenes.id_almacen;


--
-- Name: bitacora; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bitacora (
    id_bitacora integer NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_usuario integer,
    usuario_email character varying(150),
    ip character varying(45),
    modulo character varying(40) NOT NULL,
    accion character varying(20) NOT NULL,
    tabla character varying(60),
    id_registro integer,
    descripcion character varying(300),
    CONSTRAINT chk_bitacora_accion CHECK (((accion)::text = ANY (ARRAY[('LOGIN'::character varying)::text, ('LOGIN_FALLIDO'::character varying)::text, ('LOGOUT'::character varying)::text, ('ALTA'::character varying)::text, ('MODIFICACION'::character varying)::text, ('BAJA'::character varying)::text, ('ANULACION'::character varying)::text, ('APROBACION'::character varying)::text, ('RECHAZO'::character varying)::text, ('FINALIZACION'::character varying)::text, ('REAPERTURA'::character varying)::text, ('RECEPCION'::character varying)::text]))),
    CONSTRAINT chk_bitacora_modulo CHECK (((modulo)::text = ANY (ARRAY[('SEGURIDAD'::character varying)::text, ('REFERENCIALES'::character varying)::text, ('COMPRAS'::character varying)::text, ('OT'::character varying)::text, ('INVENTARIO'::character varying)::text, ('ACTIVOS'::character varying)::text])))
);


ALTER TABLE public.bitacora OWNER TO postgres;

--
-- Name: TABLE bitacora; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.bitacora IS 'Bitácora del sistema: quién hizo qué, cuándo y desde qué IP. El sistema solo puede agregar y consultar.';


--
-- Name: COLUMN bitacora.usuario_email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.bitacora.usuario_email IS 'Copia fija del email: identifica al usuario aunque se elimine, y registra el email escrito en un inicio de sesión fallido.';


--
-- Name: bitacora_detalle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bitacora_detalle (
    id_detalle integer NOT NULL,
    id_bitacora integer NOT NULL,
    campo character varying(60) NOT NULL,
    valor_anterior text,
    valor_nuevo text
);


ALTER TABLE public.bitacora_detalle OWNER TO postgres;

--
-- Name: TABLE bitacora_detalle; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.bitacora_detalle IS 'Campos que cambiaron en cada modificación registrada en la bitácora (valor anterior y nuevo).';


--
-- Name: bitacora_detalle_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.bitacora_detalle_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.bitacora_detalle_id_detalle_seq OWNER TO postgres;

--
-- Name: bitacora_detalle_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.bitacora_detalle_id_detalle_seq OWNED BY public.bitacora_detalle.id_detalle;


--
-- Name: bitacora_id_bitacora_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.bitacora_id_bitacora_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.bitacora_id_bitacora_seq OWNER TO postgres;

--
-- Name: bitacora_id_bitacora_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.bitacora_id_bitacora_seq OWNED BY public.bitacora.id_bitacora;


--
-- Name: centros; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.centros (
    id_centro integer NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(150) NOT NULL,
    direccion text,
    activo boolean DEFAULT true
);


ALTER TABLE public.centros OWNER TO postgres;

--
-- Name: centros_costo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.centros_costo (
    id_centro_costo integer NOT NULL,
    codigo character varying(50) NOT NULL,
    descripcion text,
    id_centro integer NOT NULL,
    activo boolean DEFAULT true
);


ALTER TABLE public.centros_costo OWNER TO postgres;

--
-- Name: centros_costo_id_centro_costo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.centros_costo_id_centro_costo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.centros_costo_id_centro_costo_seq OWNER TO postgres;

--
-- Name: centros_costo_id_centro_costo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.centros_costo_id_centro_costo_seq OWNED BY public.centros_costo.id_centro_costo;


--
-- Name: centros_id_centro_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.centros_id_centro_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.centros_id_centro_seq OWNER TO postgres;

--
-- Name: centros_id_centro_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.centros_id_centro_seq OWNED BY public.centros.id_centro;


--
-- Name: compras; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.compras (
    id_compra integer NOT NULL,
    fecha_compra date DEFAULT CURRENT_DATE NOT NULL,
    id_proveedor integer NOT NULL,
    id_comprador integer NOT NULL,
    estado character varying(50) NOT NULL,
    numero_oc character varying(50),
    observaciones text,
    id_usuario_solicitante integer,
    id_centro_destino integer,
    CONSTRAINT chk_compras_estado CHECK (((estado)::text = ANY (ARRAY[('BORRADOR'::character varying)::text, ('SOLICITADO'::character varying)::text, ('PARCIAL'::character varying)::text, ('RECIBIDO'::character varying)::text, ('ANULADO'::character varying)::text])))
);


ALTER TABLE public.compras OWNER TO postgres;

--
-- Name: compras_id_compra_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.compras_id_compra_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.compras_id_compra_seq OWNER TO postgres;

--
-- Name: compras_id_compra_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.compras_id_compra_seq OWNED BY public.compras.id_compra;


--
-- Name: correlativo_oc; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.correlativo_oc (
    fecha date NOT NULL,
    ultimo integer NOT NULL,
    CONSTRAINT correlativo_oc_ultimo_check CHECK ((ultimo >= 0))
);


ALTER TABLE public.correlativo_oc OWNER TO postgres;

--
-- Name: correlativo_ot; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.correlativo_ot (
    fecha date NOT NULL,
    ultimo integer NOT NULL,
    CONSTRAINT correlativo_ot_ultimo_check CHECK ((ultimo >= 0))
);


ALTER TABLE public.correlativo_ot OWNER TO postgres;

--
-- Name: detalle_compra; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detalle_compra (
    id_detalle_compra integer NOT NULL,
    id_compra integer NOT NULL,
    tipo_item character varying(10) NOT NULL,
    id_insumo integer,
    id_equipo integer,
    cantidad integer NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    id_almacen_destino integer,
    cantidad_recibida integer DEFAULT 0,
    id_centro_asignado integer,
    id_almacen_asignado integer,
    id_equipo_generado integer,
    id_centro_costo_asignado integer,
    CONSTRAINT chk_detalle_equipo_unidad CHECK ((((tipo_item)::text <> 'EQUIPO'::text) OR (cantidad = 1))),
    CONSTRAINT chk_detalle_precio CHECK ((precio_unitario >= (0)::numeric)),
    CONSTRAINT chk_detalle_recibida CHECK (((cantidad_recibida >= 0) AND (cantidad_recibida <= cantidad))),
    CONSTRAINT chk_insumo_o_equipo CHECK (((((tipo_item)::text = 'INSUMO'::text) AND (id_insumo IS NOT NULL) AND (id_equipo IS NULL)) OR (((tipo_item)::text = 'EQUIPO'::text) AND (id_equipo IS NOT NULL) AND (id_insumo IS NULL)))),
    CONSTRAINT detalle_compra_cantidad_check CHECK ((cantidad > 0)),
    CONSTRAINT detalle_compra_tipo_item_check CHECK (((tipo_item)::text = ANY (ARRAY[('INSUMO'::character varying)::text, ('EQUIPO'::character varying)::text])))
);


ALTER TABLE public.detalle_compra OWNER TO postgres;

--
-- Name: detalle_compra_id_detalle_compra_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detalle_compra_id_detalle_compra_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detalle_compra_id_detalle_compra_seq OWNER TO postgres;

--
-- Name: detalle_compra_id_detalle_compra_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_compra_id_detalle_compra_seq OWNED BY public.detalle_compra.id_detalle_compra;


--
-- Name: detalle_ot; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detalle_ot (
    id_detalle_ot integer NOT NULL,
    id_ot integer NOT NULL,
    id_insumo integer,
    cantidad integer NOT NULL,
    id_almacen integer,
    precio_unitario numeric(12,2) NOT NULL,
    subtotal numeric(12,2) GENERATED ALWAYS AS (((cantidad)::numeric * precio_unitario)) STORED,
    fecha_suministro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_usuario_suministra integer,
    id_remision integer,
    CONSTRAINT detalle_ot_cantidad_check CHECK ((cantidad > 0))
);


ALTER TABLE public.detalle_ot OWNER TO postgres;

--
-- Name: detalle_ot_id_detalle_ot_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detalle_ot_id_detalle_ot_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detalle_ot_id_detalle_ot_seq OWNER TO postgres;

--
-- Name: detalle_ot_id_detalle_ot_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_ot_id_detalle_ot_seq OWNED BY public.detalle_ot.id_detalle_ot;


--
-- Name: equipos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.equipos (
    id_equipo integer NOT NULL,
    serial_activo character varying(100) NOT NULL,
    id_insumo_base integer,
    fecha_ingreso date DEFAULT CURRENT_DATE,
    estado character varying(50) NOT NULL,
    id_centro_actual integer,
    id_almacen_actual integer,
    id_ultimo_movimiento integer,
    activo boolean DEFAULT true,
    numero_serie character varying(100),
    id_centro_costo_actual integer,
    CONSTRAINT chk_equipos_estado CHECK (((estado)::text = ANY (ARRAY[('SOLICITADO'::character varying)::text, ('DISPONIBLE'::character varying)::text, ('ASIGNADO'::character varying)::text, ('EN_TRANSITO'::character varying)::text, ('EN_REPARACION'::character varying)::text, ('BAJA'::character varying)::text, ('ANULADO'::character varying)::text])))
);


ALTER TABLE public.equipos OWNER TO postgres;

--
-- Name: COLUMN equipos.serial_activo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.equipos.serial_activo IS 'Número de activo/patrimonio (AF-000001) generado al recibir. Mientras está pedido: PEND-...';


--
-- Name: COLUMN equipos.numero_serie; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.equipos.numero_serie IS 'Número de serie del fabricante (opcional)';


--
-- Name: COLUMN equipos.id_centro_costo_actual; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.equipos.id_centro_costo_actual IS 'Centro de costo (sector) donde está el activo';


--
-- Name: equipos_id_equipo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.equipos_id_equipo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.equipos_id_equipo_seq OWNER TO postgres;

--
-- Name: equipos_id_equipo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.equipos_id_equipo_seq OWNED BY public.equipos.id_equipo;


--
-- Name: insumos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.insumos (
    id_insumo integer NOT NULL,
    codigo character varying(50) NOT NULL,
    descripcion text NOT NULL,
    id_unidad_medida integer,
    activo boolean DEFAULT true,
    precio_compra numeric(12,2) DEFAULT 0,
    CONSTRAINT chk_insumos_precio CHECK ((precio_compra >= (0)::numeric))
);


ALTER TABLE public.insumos OWNER TO postgres;

--
-- Name: insumos_id_insumo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.insumos_id_insumo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.insumos_id_insumo_seq OWNER TO postgres;

--
-- Name: insumos_id_insumo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.insumos_id_insumo_seq OWNED BY public.insumos.id_insumo;


--
-- Name: seq_numero_movimiento; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_numero_movimiento
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_numero_movimiento OWNER TO postgres;

--
-- Name: movimientos_activos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.movimientos_activos (
    id_movimiento integer NOT NULL,
    numero_movimiento_unico character varying(50) DEFAULT ((('MOV-'::text || to_char((CURRENT_DATE)::timestamp with time zone, 'YYYYMMDD'::text)) || '-'::text) || lpad((nextval('public.seq_numero_movimiento'::regclass))::text, 6, '0'::text)) NOT NULL,
    fecha_movimiento timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento character varying(20) NOT NULL,
    id_equipo integer,
    id_centro_origen integer,
    id_centro_destino integer,
    id_usuario_emisor integer,
    id_usuario_receptor integer,
    datos_receptor_nombre character varying(100),
    datos_receptor_apellido character varying(100),
    datos_receptor_ci character varying(20),
    datos_receptor_vehiculo_chapa character varying(20),
    datos_receptor_vehiculo_marca character varying(50),
    datos_receptor_vehiculo_modelo character varying(50),
    justificacion_rechazo text,
    id_remision integer,
    id_almacen_origen integer,
    id_almacen_destino integer,
    id_usuario_registro integer,
    estado_anterior character varying(50),
    estado_nuevo character varying(50),
    observaciones text,
    id_centro_costo_origen integer,
    id_centro_costo_destino integer,
    CONSTRAINT movimientos_activos_tipo_movimiento_check CHECK (((tipo_movimiento)::text = ANY (ARRAY[('INGRESO'::character varying)::text, ('ENVIO'::character varying)::text, ('RECEPCION'::character varying)::text, ('RECHAZO'::character varying)::text, ('ASIGNACION'::character varying)::text, ('DEVOLUCION'::character varying)::text, ('REPARACION'::character varying)::text, ('RETORNO_REPARACION'::character varying)::text, ('BAJA'::character varying)::text, ('REUBICACION'::character varying)::text, ('ANULACION_SALIDA'::character varying)::text, ('ANULACION_RECEPCION'::character varying)::text])))
);


ALTER TABLE public.movimientos_activos OWNER TO postgres;

--
-- Name: COLUMN movimientos_activos.id_usuario_registro; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.movimientos_activos.id_usuario_registro IS 'Usuario del sistema que registró el movimiento';


--
-- Name: COLUMN movimientos_activos.observaciones; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.movimientos_activos.observaciones IS 'Motivo o detalle del movimiento';


--
-- Name: movimientos_activos_id_movimiento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.movimientos_activos_id_movimiento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.movimientos_activos_id_movimiento_seq OWNER TO postgres;

--
-- Name: movimientos_activos_id_movimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.movimientos_activos_id_movimiento_seq OWNED BY public.movimientos_activos.id_movimiento;


--
-- Name: movimientos_stock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.movimientos_stock (
    id_mov integer NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_insumo integer NOT NULL,
    id_almacen integer NOT NULL,
    tipo character varying(30) NOT NULL,
    cantidad integer NOT NULL,
    saldo integer NOT NULL,
    costo_unitario numeric(12,2),
    documento character varying(60),
    id_ot integer,
    id_compra integer,
    id_ajuste integer,
    id_transferencia integer,
    id_centro_costo integer,
    id_usuario integer,
    observacion text,
    CONSTRAINT movimientos_stock_cantidad_check CHECK ((cantidad <> 0)),
    CONSTRAINT movimientos_stock_saldo_check CHECK ((saldo >= 0)),
    CONSTRAINT movimientos_stock_tipo_check CHECK (((tipo)::text = ANY (ARRAY[('SALDO_INICIAL'::character varying)::text, ('ENTRADA_COMPRA'::character varying)::text, ('SALIDA_OT'::character varying)::text, ('DEVOLUCION_OT'::character varying)::text, ('AJUSTE'::character varying)::text, ('ANULACION_AJUSTE'::character varying)::text, ('TRANSFERENCIA_SALIDA'::character varying)::text, ('TRANSFERENCIA_ENTRADA'::character varying)::text, ('ANULACION_TRANSFERENCIA'::character varying)::text, ('ANULACION_RECEPCION'::character varying)::text])))
);


ALTER TABLE public.movimientos_stock OWNER TO postgres;

--
-- Name: movimientos_stock_id_mov_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.movimientos_stock_id_mov_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.movimientos_stock_id_mov_seq OWNER TO postgres;

--
-- Name: movimientos_stock_id_mov_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.movimientos_stock_id_mov_seq OWNED BY public.movimientos_stock.id_mov;


--
-- Name: ot; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ot (
    id_ot integer NOT NULL,
    numero_ot character varying(50) NOT NULL,
    fecha_solicitud date DEFAULT CURRENT_DATE NOT NULL,
    id_centro integer NOT NULL,
    id_centro_costo integer,
    descripcion text,
    estado character varying(50) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    id_usuario_solic integer,
    titulo character varying(200) NOT NULL,
    fecha_finalizacion timestamp without time zone,
    id_usuario_finaliza integer,
    observacion_finalizacion text,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_anulacion_fin timestamp without time zone,
    id_usuario_anula_fin integer,
    motivo_anulacion_fin text,
    id_usuario_responsable integer,
    CONSTRAINT chk_ot_estado CHECK (((estado)::text = ANY (ARRAY[('PENDIENTE'::character varying)::text, ('EN_PROCESO'::character varying)::text, ('FINALIZADA'::character varying)::text, ('ANULADA'::character varying)::text])))
);


ALTER TABLE public.ot OWNER TO postgres;

--
-- Name: COLUMN ot.id_centro; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.ot.id_centro IS 'Centro destino: el que resuelve la OT (obligatorio)';


--
-- Name: COLUMN ot.descripcion; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.ot.descripcion IS 'Descripción ampliada (opcional)';


--
-- Name: COLUMN ot.titulo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.ot.titulo IS 'Título breve de la solicitud (ej.: Solicito 20 mouse)';


--
-- Name: COLUMN ot.fecha_creacion; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.ot.fecha_creacion IS 'Fecha y hora en que se creó la OT (automática)';


--
-- Name: COLUMN ot.fecha_anulacion_fin; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.ot.fecha_anulacion_fin IS 'Fecha y hora de la última anulación de la finalización (automática)';


--
-- Name: COLUMN ot.id_usuario_responsable; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.ot.id_usuario_responsable IS 'Usuario del centro destino a cargo de resolver la OT (opcional)';


--
-- Name: ot_eventos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ot_eventos (
    id_evento integer NOT NULL,
    id_ot integer NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo character varying(30) NOT NULL,
    id_usuario integer,
    detalle text,
    CONSTRAINT ot_eventos_tipo_check CHECK (((tipo)::text = ANY (ARRAY[('CREACION'::character varying)::text, ('MODIFICACION'::character varying)::text, ('SUMINISTRO'::character varying)::text, ('QUITAR_SUMINISTRO'::character varying)::text, ('FINALIZACION'::character varying)::text, ('REAPERTURA'::character varying)::text, ('SALIDA'::character varying)::text, ('ANULACION_REMISION'::character varying)::text, ('RESPONSABLE'::character varying)::text, ('CAMBIO_CENTRO'::character varying)::text])))
);


ALTER TABLE public.ot_eventos OWNER TO postgres;

--
-- Name: ot_eventos_id_evento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ot_eventos_id_evento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ot_eventos_id_evento_seq OWNER TO postgres;

--
-- Name: ot_eventos_id_evento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ot_eventos_id_evento_seq OWNED BY public.ot_eventos.id_evento;


--
-- Name: ot_id_ot_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ot_id_ot_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ot_id_ot_seq OWNER TO postgres;

--
-- Name: ot_id_ot_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ot_id_ot_seq OWNED BY public.ot.id_ot;


--
-- Name: proveedores; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.proveedores (
    id_proveedor integer NOT NULL,
    razon_social character varying(200) NOT NULL,
    ruc character varying(20) NOT NULL,
    direccion text,
    telefono character varying(50),
    email character varying(150),
    activo boolean DEFAULT true
);


ALTER TABLE public.proveedores OWNER TO postgres;

--
-- Name: proveedores_id_proveedor_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.proveedores_id_proveedor_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.proveedores_id_proveedor_seq OWNER TO postgres;

--
-- Name: proveedores_id_proveedor_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.proveedores_id_proveedor_seq OWNED BY public.proveedores.id_proveedor;


--
-- Name: recepciones_oc; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recepciones_oc (
    id_recepcion integer NOT NULL,
    id_compra integer NOT NULL,
    numero character varying(60) NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_usuario integer,
    historica boolean DEFAULT false NOT NULL,
    anulada boolean DEFAULT false NOT NULL,
    fecha_anulacion timestamp without time zone,
    id_usuario_anula integer,
    motivo_anulacion text,
    CONSTRAINT chk_recepcion_anulada CHECK (((NOT anulada) OR ((fecha_anulacion IS NOT NULL) AND (motivo_anulacion IS NOT NULL))))
);


ALTER TABLE public.recepciones_oc OWNER TO postgres;

--
-- Name: recepciones_oc_detalle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recepciones_oc_detalle (
    id integer NOT NULL,
    id_recepcion integer NOT NULL,
    id_detalle_compra integer NOT NULL,
    cantidad integer NOT NULL,
    id_almacen integer,
    id_equipo integer,
    id_centro_costo integer,
    numero_activo character varying(100),
    CONSTRAINT recepciones_oc_detalle_cantidad_check CHECK ((cantidad > 0))
);


ALTER TABLE public.recepciones_oc_detalle OWNER TO postgres;

--
-- Name: recepciones_oc_detalle_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.recepciones_oc_detalle_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.recepciones_oc_detalle_id_seq OWNER TO postgres;

--
-- Name: recepciones_oc_detalle_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.recepciones_oc_detalle_id_seq OWNED BY public.recepciones_oc_detalle.id;


--
-- Name: recepciones_oc_id_recepcion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.recepciones_oc_id_recepcion_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.recepciones_oc_id_recepcion_seq OWNER TO postgres;

--
-- Name: recepciones_oc_id_recepcion_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.recepciones_oc_id_recepcion_seq OWNED BY public.recepciones_oc.id_recepcion;


--
-- Name: remisiones; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.remisiones (
    id_remision integer NOT NULL,
    numero_remision character varying(50) NOT NULL,
    fecha_emision date DEFAULT CURRENT_DATE NOT NULL,
    tipo_remision character varying(50) NOT NULL,
    id_ot integer,
    id_movimiento integer,
    motivo_anulacion text,
    anulada boolean DEFAULT false NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_usuario_emite integer,
    transportista_nombre character varying(100),
    transportista_apellido character varying(100),
    transportista_ci character varying(20),
    vehiculo_marca character varying(50),
    vehiculo_modelo character varying(50),
    vehiculo_chapa character varying(20),
    observacion text,
    id_transferencia integer,
    fecha_anulacion timestamp without time zone,
    id_usuario_anula integer,
    id_solicitud_activo integer,
    CONSTRAINT chk_remisiones_tipo CHECK (((tipo_remision)::text = ANY (ARRAY[('SALIDA_INSUMOS'::character varying)::text, ('TRANSFERENCIA'::character varying)::text, ('SALIDA_ACTIVOS'::character varying)::text])))
);


ALTER TABLE public.remisiones OWNER TO postgres;

--
-- Name: remisiones_detalle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.remisiones_detalle (
    id_detalle integer NOT NULL,
    id_remision integer NOT NULL,
    orden integer NOT NULL,
    tipo_item character varying(10) NOT NULL,
    id_insumo integer,
    id_equipo integer,
    codigo character varying(100) NOT NULL,
    descripcion text NOT NULL,
    unidad character varying(20),
    cantidad integer NOT NULL,
    id_almacen integer,
    origen character varying(200),
    CONSTRAINT chk_remisiones_detalle_cantidad CHECK ((cantidad > 0)),
    CONSTRAINT chk_remisiones_detalle_item CHECK (((((tipo_item)::text = 'INSUMO'::text) AND (id_insumo IS NOT NULL) AND (id_equipo IS NULL)) OR (((tipo_item)::text = 'EQUIPO'::text) AND (id_equipo IS NOT NULL) AND (id_insumo IS NULL)))),
    CONSTRAINT chk_remisiones_detalle_tipo CHECK (((tipo_item)::text = ANY (ARRAY[('INSUMO'::character varying)::text, ('EQUIPO'::character varying)::text])))
);


ALTER TABLE public.remisiones_detalle OWNER TO postgres;

--
-- Name: TABLE remisiones_detalle; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.remisiones_detalle IS 'Detalle de cada remisión: copia fija de los insumos o activos que salieron (no cambia aunque cambie el proceso de origen).';


--
-- Name: remisiones_detalle_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.remisiones_detalle_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.remisiones_detalle_id_detalle_seq OWNER TO postgres;

--
-- Name: remisiones_detalle_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.remisiones_detalle_id_detalle_seq OWNED BY public.remisiones_detalle.id_detalle;


--
-- Name: remisiones_id_remision_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.remisiones_id_remision_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.remisiones_id_remision_seq OWNER TO postgres;

--
-- Name: remisiones_id_remision_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.remisiones_id_remision_seq OWNED BY public.remisiones.id_remision;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id_rol integer NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text,
    activo boolean DEFAULT true,
    codigo character varying(30) NOT NULL,
    CONSTRAINT chk_roles_codigo CHECK (((codigo)::text = ANY (ARRAY[('ADMINISTRADOR'::character varying)::text, ('GERENTE'::character varying)::text, ('AUXILIAR'::character varying)::text, ('ENCARGADO_COMPRAS'::character varying)::text, ('CONSULTOR'::character varying)::text])))
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: COLUMN roles.codigo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.codigo IS 'Tipo de permisos del rol (obligatorio). Define qué puede hacer el usuario; el nombre es libre.';


--
-- Name: roles_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_id_rol_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_id_rol_seq OWNER TO postgres;

--
-- Name: roles_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_id_rol_seq OWNED BY public.roles.id_rol;


--
-- Name: seq_ajuste; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_ajuste
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_ajuste OWNER TO postgres;

--
-- Name: seq_baja_activo; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_baja_activo
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_baja_activo OWNER TO postgres;

--
-- Name: seq_numero_activo; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_numero_activo
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_numero_activo OWNER TO postgres;

--
-- Name: seq_remision; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_remision
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_remision OWNER TO postgres;

--
-- Name: seq_solicitud_activo; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_solicitud_activo
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_solicitud_activo OWNER TO postgres;

--
-- Name: seq_transferencia; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seq_transferencia
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_transferencia OWNER TO postgres;

--
-- Name: solicitudes_activos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.solicitudes_activos (
    id_solicitud integer NOT NULL,
    numero character varying(30) NOT NULL,
    tipo character varying(20) NOT NULL,
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_usuario_solicita integer,
    id_proveedor integer,
    id_centro_destino integer,
    id_centro_costo_destino integer,
    id_ot integer,
    motivo_baja character varying(30),
    observacion text,
    transportista_nombre character varying(100),
    transportista_apellido character varying(100),
    transportista_ci character varying(20),
    vehiculo_marca character varying(50),
    vehiculo_modelo character varying(50),
    vehiculo_chapa character varying(20),
    fecha_resolucion timestamp without time zone,
    id_usuario_resuelve integer,
    motivo_rechazo text,
    id_remision integer,
    fecha_anulacion timestamp without time zone,
    id_usuario_anula integer,
    motivo_anulacion text,
    CONSTRAINT solicitudes_activos_estado_check CHECK (((estado)::text = ANY (ARRAY[('PENDIENTE'::character varying)::text, ('APROBADA'::character varying)::text, ('RECHAZADA'::character varying)::text, ('ANULADA'::character varying)::text]))),
    CONSTRAINT solicitudes_activos_motivo_baja_check CHECK (((motivo_baja)::text = ANY (ARRAY[('OBSOLESCENCIA'::character varying)::text, ('ROTURA_IRREPARABLE'::character varying)::text, ('ROBO_PERDIDA'::character varying)::text, ('VENTA'::character varying)::text, ('DONACION'::character varying)::text, ('OTRO'::character varying)::text]))),
    CONSTRAINT solicitudes_activos_tipo_check CHECK (((tipo)::text = ANY (ARRAY[('REPARACION'::character varying)::text, ('TRANSFERENCIA'::character varying)::text, ('ENTREGA_OT'::character varying)::text, ('BAJA'::character varying)::text])))
);


ALTER TABLE public.solicitudes_activos OWNER TO postgres;

--
-- Name: solicitudes_activos_detalle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.solicitudes_activos_detalle (
    id_detalle integer NOT NULL,
    id_solicitud integer NOT NULL,
    id_equipo integer NOT NULL,
    id_centro_origen integer,
    id_centro_costo_origen integer,
    estado_origen character varying(50),
    estado_linea character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    fecha_recepcion timestamp without time zone,
    id_usuario_recibe integer,
    observacion_recepcion text,
    CONSTRAINT solicitudes_activos_detalle_estado_linea_check CHECK (((estado_linea)::text = ANY (ARRAY[('PENDIENTE'::character varying)::text, ('EN_CURSO'::character varying)::text, ('RECIBIDO'::character varying)::text, ('RECHAZADO_DESTINO'::character varying)::text, ('FINALIZADO'::character varying)::text, ('ANULADO'::character varying)::text])))
);


ALTER TABLE public.solicitudes_activos_detalle OWNER TO postgres;

--
-- Name: solicitudes_activos_detalle_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.solicitudes_activos_detalle_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.solicitudes_activos_detalle_id_detalle_seq OWNER TO postgres;

--
-- Name: solicitudes_activos_detalle_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitudes_activos_detalle_id_detalle_seq OWNED BY public.solicitudes_activos_detalle.id_detalle;


--
-- Name: solicitudes_activos_id_solicitud_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.solicitudes_activos_id_solicitud_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.solicitudes_activos_id_solicitud_seq OWNER TO postgres;

--
-- Name: solicitudes_activos_id_solicitud_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitudes_activos_id_solicitud_seq OWNED BY public.solicitudes_activos.id_solicitud;


--
-- Name: stock_almacen; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stock_almacen (
    id_stock integer NOT NULL,
    id_insumo integer NOT NULL,
    id_almacen integer NOT NULL,
    stock_actual integer DEFAULT 0,
    stock_minimo integer DEFAULT 0,
    CONSTRAINT chk_stock_no_negativo CHECK (((stock_actual >= 0) AND (stock_minimo >= 0)))
);


ALTER TABLE public.stock_almacen OWNER TO postgres;

--
-- Name: stock_almacen_id_stock_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.stock_almacen_id_stock_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.stock_almacen_id_stock_seq OWNER TO postgres;

--
-- Name: stock_almacen_id_stock_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.stock_almacen_id_stock_seq OWNED BY public.stock_almacen.id_stock;


--
-- Name: transferencias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transferencias (
    id_transferencia integer NOT NULL,
    numero character varying(30) NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_almacen_origen integer NOT NULL,
    id_almacen_destino integer NOT NULL,
    observacion text,
    id_usuario integer,
    anulada boolean DEFAULT false NOT NULL,
    fecha_anulacion timestamp without time zone,
    id_usuario_anula integer,
    motivo_anulacion text,
    CONSTRAINT transferencias_check CHECK ((id_almacen_origen <> id_almacen_destino))
);


ALTER TABLE public.transferencias OWNER TO postgres;

--
-- Name: transferencias_detalle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transferencias_detalle (
    id_detalle integer NOT NULL,
    id_transferencia integer NOT NULL,
    id_insumo integer NOT NULL,
    cantidad integer NOT NULL,
    CONSTRAINT transferencias_detalle_cantidad_check CHECK ((cantidad > 0))
);


ALTER TABLE public.transferencias_detalle OWNER TO postgres;

--
-- Name: transferencias_detalle_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transferencias_detalle_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transferencias_detalle_id_detalle_seq OWNER TO postgres;

--
-- Name: transferencias_detalle_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transferencias_detalle_id_detalle_seq OWNED BY public.transferencias_detalle.id_detalle;


--
-- Name: transferencias_id_transferencia_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transferencias_id_transferencia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transferencias_id_transferencia_seq OWNER TO postgres;

--
-- Name: transferencias_id_transferencia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transferencias_id_transferencia_seq OWNED BY public.transferencias.id_transferencia;


--
-- Name: unidad_medida; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.unidad_medida (
    id_unidad_medida integer NOT NULL,
    nombre character varying(50) NOT NULL,
    abreviatura character varying(10),
    activo boolean DEFAULT true
);


ALTER TABLE public.unidad_medida OWNER TO postgres;

--
-- Name: unidad_medida_id_unidad_medida_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.unidad_medida_id_unidad_medida_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.unidad_medida_id_unidad_medida_seq OWNER TO postgres;

--
-- Name: unidad_medida_id_unidad_medida_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.unidad_medida_id_unidad_medida_seq OWNED BY public.unidad_medida.id_unidad_medida;


--
-- Name: usuario_centros; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario_centros (
    id_usuario integer NOT NULL,
    id_centro integer NOT NULL,
    fecha_desde date DEFAULT CURRENT_DATE NOT NULL,
    fecha_hasta date,
    observacion character varying(300),
    CONSTRAINT usuario_centros_check CHECK (((fecha_hasta IS NULL) OR (fecha_hasta >= fecha_desde)))
);


ALTER TABLE public.usuario_centros OWNER TO postgres;

--
-- Name: usuario_centros_historial; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario_centros_historial (
    id_historial integer NOT NULL,
    id_usuario integer NOT NULL,
    id_centro integer NOT NULL,
    tipo_acceso character varying(10) NOT NULL,
    accion character varying(15) NOT NULL,
    fecha_desde date,
    fecha_hasta date,
    observacion character varying(300),
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_usuario_registra integer,
    CONSTRAINT chk_uch_accion CHECK (((accion)::text = ANY (ARRAY[('ALTA'::character varying)::text, ('MODIFICACION'::character varying)::text, ('BAJA'::character varying)::text]))),
    CONSTRAINT chk_uch_tipo CHECK (((tipo_acceso)::text = ANY (ARRAY[('PRINCIPAL'::character varying)::text, ('ADICIONAL'::character varying)::text])))
);


ALTER TABLE public.usuario_centros_historial OWNER TO postgres;

--
-- Name: TABLE usuario_centros_historial; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.usuario_centros_historial IS 'Historial de accesos: cuándo y quién dio, modificó o quitó a un usuario el acceso a un centro (principal o adicional).';


--
-- Name: usuario_centros_historial_id_historial_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuario_centros_historial_id_historial_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_centros_historial_id_historial_seq OWNER TO postgres;

--
-- Name: usuario_centros_historial_id_historial_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_centros_historial_id_historial_seq OWNED BY public.usuario_centros_historial.id_historial;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarios (
    id_usuario integer NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    password character varying(255) NOT NULL,
    id_rol integer NOT NULL,
    activo boolean DEFAULT true,
    centro_asignado integer,
    foto_perfil character varying(255),
    notif_vistas_hasta timestamp without time zone
);


ALTER TABLE public.usuarios OWNER TO postgres;

--
-- Name: COLUMN usuarios.notif_vistas_hasta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.usuarios.notif_vistas_hasta IS 'Fecha y hora en que el usuario abrió por última vez la campana de notificaciones (las novedades posteriores se marcan como nuevas).';


--
-- Name: usuarios_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuarios_id_usuario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuarios_id_usuario_seq OWNER TO postgres;

--
-- Name: usuarios_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuarios_id_usuario_seq OWNED BY public.usuarios.id_usuario;


--
-- Name: ajustes id_ajuste; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes ALTER COLUMN id_ajuste SET DEFAULT nextval('public.ajustes_id_ajuste_seq'::regclass);


--
-- Name: ajustes_detalle id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes_detalle ALTER COLUMN id_detalle SET DEFAULT nextval('public.ajustes_detalle_id_detalle_seq'::regclass);


--
-- Name: almacenes id_almacen; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.almacenes ALTER COLUMN id_almacen SET DEFAULT nextval('public.almacenes_id_almacen_seq'::regclass);


--
-- Name: bitacora id_bitacora; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bitacora ALTER COLUMN id_bitacora SET DEFAULT nextval('public.bitacora_id_bitacora_seq'::regclass);


--
-- Name: bitacora_detalle id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bitacora_detalle ALTER COLUMN id_detalle SET DEFAULT nextval('public.bitacora_detalle_id_detalle_seq'::regclass);


--
-- Name: centros id_centro; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros ALTER COLUMN id_centro SET DEFAULT nextval('public.centros_id_centro_seq'::regclass);


--
-- Name: centros_costo id_centro_costo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros_costo ALTER COLUMN id_centro_costo SET DEFAULT nextval('public.centros_costo_id_centro_costo_seq'::regclass);


--
-- Name: compras id_compra; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras ALTER COLUMN id_compra SET DEFAULT nextval('public.compras_id_compra_seq'::regclass);


--
-- Name: detalle_compra id_detalle_compra; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra ALTER COLUMN id_detalle_compra SET DEFAULT nextval('public.detalle_compra_id_detalle_compra_seq'::regclass);


--
-- Name: detalle_ot id_detalle_ot; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot ALTER COLUMN id_detalle_ot SET DEFAULT nextval('public.detalle_ot_id_detalle_ot_seq'::regclass);


--
-- Name: equipos id_equipo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos ALTER COLUMN id_equipo SET DEFAULT nextval('public.equipos_id_equipo_seq'::regclass);


--
-- Name: insumos id_insumo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insumos ALTER COLUMN id_insumo SET DEFAULT nextval('public.insumos_id_insumo_seq'::regclass);


--
-- Name: movimientos_activos id_movimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos ALTER COLUMN id_movimiento SET DEFAULT nextval('public.movimientos_activos_id_movimiento_seq'::regclass);


--
-- Name: movimientos_stock id_mov; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock ALTER COLUMN id_mov SET DEFAULT nextval('public.movimientos_stock_id_mov_seq'::regclass);


--
-- Name: ot id_ot; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot ALTER COLUMN id_ot SET DEFAULT nextval('public.ot_id_ot_seq'::regclass);


--
-- Name: ot_eventos id_evento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot_eventos ALTER COLUMN id_evento SET DEFAULT nextval('public.ot_eventos_id_evento_seq'::regclass);


--
-- Name: proveedores id_proveedor; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.proveedores ALTER COLUMN id_proveedor SET DEFAULT nextval('public.proveedores_id_proveedor_seq'::regclass);


--
-- Name: recepciones_oc id_recepcion; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc ALTER COLUMN id_recepcion SET DEFAULT nextval('public.recepciones_oc_id_recepcion_seq'::regclass);


--
-- Name: recepciones_oc_detalle id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle ALTER COLUMN id SET DEFAULT nextval('public.recepciones_oc_detalle_id_seq'::regclass);


--
-- Name: remisiones id_remision; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones ALTER COLUMN id_remision SET DEFAULT nextval('public.remisiones_id_remision_seq'::regclass);


--
-- Name: remisiones_detalle id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones_detalle ALTER COLUMN id_detalle SET DEFAULT nextval('public.remisiones_detalle_id_detalle_seq'::regclass);


--
-- Name: roles id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN id_rol SET DEFAULT nextval('public.roles_id_rol_seq'::regclass);


--
-- Name: solicitudes_activos id_solicitud; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos ALTER COLUMN id_solicitud SET DEFAULT nextval('public.solicitudes_activos_id_solicitud_seq'::regclass);


--
-- Name: solicitudes_activos_detalle id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle ALTER COLUMN id_detalle SET DEFAULT nextval('public.solicitudes_activos_detalle_id_detalle_seq'::regclass);


--
-- Name: stock_almacen id_stock; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_almacen ALTER COLUMN id_stock SET DEFAULT nextval('public.stock_almacen_id_stock_seq'::regclass);


--
-- Name: transferencias id_transferencia; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias ALTER COLUMN id_transferencia SET DEFAULT nextval('public.transferencias_id_transferencia_seq'::regclass);


--
-- Name: transferencias_detalle id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias_detalle ALTER COLUMN id_detalle SET DEFAULT nextval('public.transferencias_detalle_id_detalle_seq'::regclass);


--
-- Name: unidad_medida id_unidad_medida; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unidad_medida ALTER COLUMN id_unidad_medida SET DEFAULT nextval('public.unidad_medida_id_unidad_medida_seq'::regclass);


--
-- Name: usuario_centros_historial id_historial; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros_historial ALTER COLUMN id_historial SET DEFAULT nextval('public.usuario_centros_historial_id_historial_seq'::regclass);


--
-- Name: usuarios id_usuario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuarios_id_usuario_seq'::regclass);


--
-- Data for Name: ajustes; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: ajustes_detalle; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: almacenes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.almacenes VALUES (1, 'Almacén Central', 'Materiales generales', 'Depósito 1', 1, true);
INSERT INTO public.almacenes VALUES (2, 'Almacén de Insumos', 'Repuestos y consumibles', 'Estante A', 2, true);
INSERT INTO public.almacenes VALUES (6, 'demoalmacen', 'demoalmacen', 'demoalmacen', 6, true);


--
-- Data for Name: bitacora; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.bitacora VALUES (1, '2026-10-25 15:25:25.850086', 6, 'admin@admin.com', '127.0.0.1', 'SEGURIDAD', 'LOGIN', 'usuarios', 6, 'Inicio de sesión');
INSERT INTO public.bitacora VALUES (2, '2026-10-25 15:38:25.25612', 6, 'admin@admin.com', '127.0.0.1', 'SEGURIDAD', 'LOGOUT', 'usuarios', 6, 'Cierre de sesión');
INSERT INTO public.bitacora VALUES (3, '2026-10-25 15:39:02.759443', 6, 'admin@admin.com', '127.0.0.1', 'SEGURIDAD', 'LOGIN', 'usuarios', 6, 'Inicio de sesión');
INSERT INTO public.bitacora VALUES (4, '2026-10-25 17:28:31.806564', 6, 'admin@admin.com', '127.0.0.1', 'SEGURIDAD', 'LOGIN', 'usuarios', 6, 'Inicio de sesión');
INSERT INTO public.bitacora VALUES (5, '2026-10-25 17:28:51.276798', 6, 'admin@admin.com', '127.0.0.1', 'SEGURIDAD', 'LOGOUT', 'usuarios', 6, 'Cierre de sesión');


--
-- Data for Name: bitacora_detalle; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: centros; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.centros VALUES (1, 'CTR001', 'Centro de Asunción', 'Av. República 500', true);
INSERT INTO public.centros VALUES (2, 'CTR002', 'Centro de Encarnación', 'Urb. Sur 234', true);
INSERT INTO public.centros VALUES (3, 'CTR003', 'Centro de Ciudad del Este', 'Av. Curupayty 789', true);
INSERT INTO public.centros VALUES (4, 'CTR004', 'Centro de San Lorenzo', 'Ruta 1 km 18', true);
INSERT INTO public.centros VALUES (5, 'CTR005', 'Centro de Luque', 'Av. General Aquino 333', true);
INSERT INTO public.centros VALUES (6, 'democentro', 'democentro', 'democentro', true);
INSERT INTO public.centros VALUES (12, 'CTR006', 'Centro de Coronel Oviedo', 'Ruta PY02 km 132', true);
INSERT INTO public.centros VALUES (13, 'CTR007', 'Centro de Concepción', 'Av. Pinedo 1450', true);


--
-- Data for Name: centros_costo; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.centros_costo VALUES (1, 'CC001', 'Administración Central', 1, true);
INSERT INTO public.centros_costo VALUES (2, 'CC002', 'Producción Encarnación', 2, true);
INSERT INTO public.centros_costo VALUES (3, 'CC003', 'Logística CDE', 3, true);
INSERT INTO public.centros_costo VALUES (4, 'CC004', 'Ventas San Lorenzo', 4, true);
INSERT INTO public.centros_costo VALUES (5, 'CC005', 'Mantenimiento Luque', 5, true);
INSERT INTO public.centros_costo VALUES (6, 'centrocosto', 'centrocosto', 6, true);
INSERT INTO public.centros_costo VALUES (7, 'CC001-01', 'Administración - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (8, 'CC001-02', 'Contabilidad - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (9, 'CC001-03', 'Taller mecánico - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (10, 'CC001-04', 'Depósito y logística - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (11, 'CC001-05', 'Seguridad y CCTV - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (12, 'CC001-06', 'Tecnología de la información - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (13, 'CC001-07', 'Compras - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (14, 'CC001-08', 'Recursos humanos - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (15, 'CC001-09', 'Gerencia general - Asunción', 1, true);
INSERT INTO public.centros_costo VALUES (16, 'CC002-01', 'Administración - Encarnación', 2, true);
INSERT INTO public.centros_costo VALUES (17, 'CC002-02', 'Contabilidad - Encarnación', 2, true);
INSERT INTO public.centros_costo VALUES (18, 'CC002-03', 'Taller mecánico - Encarnación', 2, true);
INSERT INTO public.centros_costo VALUES (19, 'CC002-04', 'Depósito y logística - Encarnación', 2, true);
INSERT INTO public.centros_costo VALUES (20, 'CC002-05', 'Seguridad y CCTV - Encarnación', 2, true);
INSERT INTO public.centros_costo VALUES (21, 'CC003-01', 'Administración - Ciudad del Este', 3, true);
INSERT INTO public.centros_costo VALUES (22, 'CC003-02', 'Contabilidad - Ciudad del Este', 3, true);
INSERT INTO public.centros_costo VALUES (23, 'CC003-03', 'Taller mecánico - Ciudad del Este', 3, true);
INSERT INTO public.centros_costo VALUES (24, 'CC003-04', 'Depósito y logística - Ciudad del Este', 3, true);
INSERT INTO public.centros_costo VALUES (25, 'CC003-05', 'Seguridad y CCTV - Ciudad del Este', 3, true);
INSERT INTO public.centros_costo VALUES (26, 'CC004-01', 'Administración - San Lorenzo', 4, true);
INSERT INTO public.centros_costo VALUES (27, 'CC004-02', 'Contabilidad - San Lorenzo', 4, true);
INSERT INTO public.centros_costo VALUES (28, 'CC004-03', 'Taller mecánico - San Lorenzo', 4, true);
INSERT INTO public.centros_costo VALUES (29, 'CC004-04', 'Depósito y logística - San Lorenzo', 4, true);
INSERT INTO public.centros_costo VALUES (30, 'CC004-05', 'Seguridad y CCTV - San Lorenzo', 4, true);
INSERT INTO public.centros_costo VALUES (31, 'CC005-01', 'Administración - Luque', 5, true);
INSERT INTO public.centros_costo VALUES (32, 'CC005-02', 'Contabilidad - Luque', 5, true);
INSERT INTO public.centros_costo VALUES (33, 'CC005-03', 'Taller mecánico - Luque', 5, true);
INSERT INTO public.centros_costo VALUES (34, 'CC005-04', 'Depósito y logística - Luque', 5, true);
INSERT INTO public.centros_costo VALUES (35, 'CC005-05', 'Seguridad y CCTV - Luque', 5, true);
INSERT INTO public.centros_costo VALUES (36, 'CC006-01', 'Administración - Coronel Oviedo', 12, true);
INSERT INTO public.centros_costo VALUES (37, 'CC006-02', 'Contabilidad - Coronel Oviedo', 12, true);
INSERT INTO public.centros_costo VALUES (38, 'CC006-03', 'Taller mecánico - Coronel Oviedo', 12, true);
INSERT INTO public.centros_costo VALUES (39, 'CC006-04', 'Depósito y logística - Coronel Oviedo', 12, true);
INSERT INTO public.centros_costo VALUES (40, 'CC006-05', 'Seguridad y CCTV - Coronel Oviedo', 12, true);
INSERT INTO public.centros_costo VALUES (41, 'CC007-01', 'Administración - Concepción', 13, true);
INSERT INTO public.centros_costo VALUES (42, 'CC007-02', 'Contabilidad - Concepción', 13, true);
INSERT INTO public.centros_costo VALUES (43, 'CC007-03', 'Taller mecánico - Concepción', 13, true);
INSERT INTO public.centros_costo VALUES (44, 'CC007-04', 'Depósito y logística - Concepción', 13, true);
INSERT INTO public.centros_costo VALUES (45, 'CC007-05', 'Seguridad y CCTV - Concepción', 13, true);


--
-- Data for Name: compras; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.compras VALUES (3, '2026-07-08', 1, 7, 'SOLICITADO', 'OC-20260708-0001', '', 5, 3);
INSERT INTO public.compras VALUES (1, '2026-07-01', 3, 6, 'RECIBIDO', 'OC-20260701-0001', '', 6, 1);
INSERT INTO public.compras VALUES (6, '2026-07-15', 3, 6, 'RECIBIDO', 'OC-20260715-0002', '', 6, 1);
INSERT INTO public.compras VALUES (2, '2026-07-01', 3, 6, 'RECIBIDO', 'OC-20260701-0002', '', 6, 1);
INSERT INTO public.compras VALUES (9, '2026-07-15', 8, 8, 'PARCIAL', 'OC-20260715-0004', 'a', 8, 1);
INSERT INTO public.compras VALUES (5, '2026-07-15', 3, 6, 'RECIBIDO', 'OC-20260715-0001', '', 6, 1);
INSERT INTO public.compras VALUES (10, '2026-09-16', 4, 6, 'SOLICITADO', 'OC-20260916-0001', '', 6, 1);
INSERT INTO public.compras VALUES (8, '2026-07-15', 8, 8, 'PARCIAL', 'OC-20260715-0003', '', 8, 6);


--
-- Data for Name: correlativo_oc; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.correlativo_oc VALUES ('2026-07-01', 2);
INSERT INTO public.correlativo_oc VALUES ('2026-09-16', 1);
INSERT INTO public.correlativo_oc VALUES ('2026-07-15', 4);
INSERT INTO public.correlativo_oc VALUES ('2026-07-08', 1);


--
-- Data for Name: correlativo_ot; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.correlativo_ot VALUES ('2026-10-21', 1);


--
-- Data for Name: detalle_compra; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.detalle_compra VALUES (4, 3, 'INSUMO', 1, NULL, 5, 20000.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (2, 1, 'INSUMO', 1, NULL, 2, 20000.00, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (12, 6, 'INSUMO', 4, NULL, 1, 120000.00, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (3, 2, 'INSUMO', 1, NULL, 1, 20000.00, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (13, 9, 'INSUMO', 3, NULL, 2, 80000.00, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (6, 5, 'INSUMO', 4, NULL, 1, 120000.00, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (14, 10, 'EQUIPO', NULL, 1, 1, 1000.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (10, 8, 'INSUMO', 2, NULL, 10, 50000.00, 6, 5, NULL, NULL, NULL, NULL);
INSERT INTO public.detalle_compra VALUES (11, 8, 'INSUMO', 5, NULL, 5, 65000.00, 6, 2, NULL, NULL, NULL, NULL);


--
-- Data for Name: detalle_ot; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.detalle_ot VALUES (1, 1, 4, 1, 1, 120000.00, DEFAULT, '2026-10-21 11:59:35.99351', 6, 1);


--
-- Data for Name: equipos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.equipos VALUES (1, 'PEND-1789586675904-6-0', 2, NULL, 'SOLICITADO', NULL, NULL, NULL, true, NULL, NULL);
INSERT INTO public.equipos VALUES (2, 'AF-000001', 40, '2026-03-10', 'ASIGNADO', 1, NULL, 2, true, 'PF-3NX81A27', 8);
INSERT INTO public.equipos VALUES (3, 'AF-000002', 40, '2026-03-10', 'ASIGNADO', 1, NULL, 4, true, 'PF-3NX81A54', 12);
INSERT INTO public.equipos VALUES (4, 'AF-000003', 40, '2026-04-02', 'ASIGNADO', 2, NULL, 6, true, 'PF-3NX82B11', 16);
INSERT INTO public.equipos VALUES (5, 'AF-000004', 41, '2026-03-12', 'ASIGNADO', 1, NULL, 8, true, 'DL-7Q2K9031', 13);
INSERT INTO public.equipos VALUES (6, 'AF-000005', 41, '2026-04-20', 'ASIGNADO', 3, NULL, 10, true, 'DL-7Q2K9147', 22);
INSERT INTO public.equipos VALUES (7, 'AF-000006', 41, '2026-05-05', 'DISPONIBLE', 4, NULL, 11, true, 'DL-7Q2K9188', 26);
INSERT INTO public.equipos VALUES (8, 'AF-000007', 42, '2026-03-12', 'ASIGNADO', 1, NULL, 13, true, 'MN-24F00317', 8);
INSERT INTO public.equipos VALUES (9, 'AF-000008', 42, '2026-04-20', 'ASIGNADO', 3, NULL, 15, true, 'MN-24F00342', 22);
INSERT INTO public.equipos VALUES (10, 'AF-000009', 43, '2026-03-18', 'DISPONIBLE', 1, NULL, 16, true, 'HP-CNB3R0912', 7);
INSERT INTO public.equipos VALUES (11, 'AF-000010', 43, '2026-06-08', 'DISPONIBLE', 5, NULL, 17, true, 'HP-CNB3R1044', 31);
INSERT INTO public.equipos VALUES (12, 'AF-000011', 44, '2026-04-15', 'ASIGNADO', 1, NULL, 19, true, 'CAM-D4M0001', 11);
INSERT INTO public.equipos VALUES (13, 'AF-000012', 44, '2026-04-15', 'ASIGNADO', 1, NULL, 21, true, 'CAM-D4M0002', 11);
INSERT INTO public.equipos VALUES (14, 'AF-000013', 44, '2026-05-20', 'ASIGNADO', 2, NULL, 23, true, 'CAM-D4M0003', 20);
INSERT INTO public.equipos VALUES (15, 'AF-000014', 44, '2026-05-22', 'DISPONIBLE', 3, NULL, 24, true, 'CAM-D4M0004', 25);
INSERT INTO public.equipos VALUES (16, 'AF-000015', 45, '2026-04-15', 'ASIGNADO', 1, NULL, 26, true, 'NVR-16C2210', 11);
INSERT INTO public.equipos VALUES (17, 'AF-000016', 45, '2026-05-20', 'ASIGNADO', 2, NULL, 28, true, 'NVR-16C2245', 20);
INSERT INTO public.equipos VALUES (18, 'AF-000017', 46, '2026-03-25', 'ASIGNADO', 1, NULL, 30, true, 'SW-24P88120', 12);
INSERT INTO public.equipos VALUES (19, 'AF-000018', 47, '2026-03-25', 'ASIGNADO', 1, NULL, 32, true, 'UPS-1500K451', 12);
INSERT INTO public.equipos VALUES (20, 'AF-000019', 48, '2026-02-27', 'ASIGNADO', 1, NULL, 34, true, 'AC-18KB0921', 15);
INSERT INTO public.equipos VALUES (21, 'AF-000020', 48, '2026-06-15', 'DISPONIBLE', 4, NULL, 35, true, 'AC-18KB0977', 26);
INSERT INTO public.equipos VALUES (22, 'AF-000021', 49, '2026-04-08', 'ASIGNADO', 1, NULL, 37, true, 'CMP-100L3307', 9);
INSERT INTO public.equipos VALUES (23, 'AF-000022', 49, '2026-05-12', 'ASIGNADO', 3, NULL, 39, true, 'CMP-100L3318', 23);
INSERT INTO public.equipos VALUES (24, 'AF-000023', 50, '2026-04-08', 'ASIGNADO', 1, NULL, 41, true, 'ELV-2C40T015', 9);
INSERT INTO public.equipos VALUES (25, 'AF-000024', 51, '2026-05-02', 'ASIGNADO', 2, NULL, 43, true, 'GH-3T770412', 18);
INSERT INTO public.equipos VALUES (26, 'AF-000025', 52, '2026-06-01', 'DISPONIBLE', 1, NULL, 44, true, 'SCN-AT55091', 9);
INSERT INTO public.equipos VALUES (27, 'AF-000026', 53, '2026-06-10', 'ASIGNADO', 5, NULL, 46, true, 'SLD-200A6612', 33);
INSERT INTO public.equipos VALUES (28, 'AF-000027', 54, '2026-03-05', 'ASIGNADO', 1, NULL, 48, true, 'GEN-75K10293', 10);
INSERT INTO public.equipos VALUES (29, 'AF-000028', 55, '2026-07-01', 'DISPONIBLE', 1, NULL, 49, true, 'PRY-MX40517', 14);


--
-- Data for Name: insumos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.insumos VALUES (1, 'INS001', 'Tornillo 5/16', 8, true, 20000.00);
INSERT INTO public.insumos VALUES (2, 'INS002', 'Cemento Portland', 2, true, 50000.00);
INSERT INTO public.insumos VALUES (3, 'INS003', 'Pegamento instantáneo', 3, true, 80000.00);
INSERT INTO public.insumos VALUES (4, 'INS004', 'Aceite hidráulico', 4, true, 120000.00);
INSERT INTO public.insumos VALUES (5, 'INS005', 'Pintura epóxica', 4, true, 65000.00);
INSERT INTO public.insumos VALUES (6, 'REP-001', 'Filtro de aceite para camión', 1, true, 45000.00);
INSERT INTO public.insumos VALUES (7, 'REP-002', 'Filtro de aire para camión', 1, true, 95000.00);
INSERT INTO public.insumos VALUES (8, 'REP-003', 'Filtro de combustible', 1, true, 60000.00);
INSERT INTO public.insumos VALUES (9, 'REP-004', 'Pastillas de freno delanteras', 9, true, 280000.00);
INSERT INTO public.insumos VALUES (10, 'REP-005', 'Correa de distribución', 1, true, 210000.00);
INSERT INTO public.insumos VALUES (11, 'REP-006', 'Batería 12V 100Ah', 1, true, 950000.00);
INSERT INTO public.insumos VALUES (12, 'REP-007', 'Lámpara H4 12V', 1, true, 25000.00);
INSERT INTO public.insumos VALUES (13, 'REP-008', 'Neumático 295/80 R22.5', 1, true, 2300000.00);
INSERT INTO public.insumos VALUES (14, 'LUB-001', 'Aceite de motor 15W40', 4, true, 38000.00);
INSERT INTO public.insumos VALUES (15, 'LUB-002', 'Aceite hidráulico ISO 68', 4, true, 32000.00);
INSERT INTO public.insumos VALUES (16, 'LUB-003', 'Líquido de frenos DOT 4', 4, true, 45000.00);
INSERT INTO public.insumos VALUES (17, 'LUB-004', 'Grasa multiuso', 2, true, 42000.00);
INSERT INTO public.insumos VALUES (18, 'LUB-005', 'Refrigerante para radiador', 4, true, 28000.00);
INSERT INTO public.insumos VALUES (19, 'FER-001', 'Tornillo hexagonal 1/2 x 2 (caja de 50)', 8, true, 85000.00);
INSERT INTO public.insumos VALUES (20, 'FER-002', 'Abrazadera metálica 2 pulgadas', 1, true, 6000.00);
INSERT INTO public.insumos VALUES (21, 'FER-003', 'Cinta aisladora', 10, true, 9000.00);
INSERT INTO public.insumos VALUES (22, 'FER-004', 'Disco de corte 4 1/2', 1, true, 12000.00);
INSERT INTO public.insumos VALUES (23, 'FER-005', 'Electrodo de soldadura 2,5 mm', 2, true, 38000.00);
INSERT INTO public.insumos VALUES (24, 'ELE-001', 'Cable eléctrico 2,5 mm', 6, true, 5500.00);
INSERT INTO public.insumos VALUES (25, 'ELE-002', 'Cable UTP categoría 6', 6, true, 4500.00);
INSERT INTO public.insumos VALUES (26, 'ELE-003', 'Conector RJ45 (caja de 100)', 8, true, 65000.00);
INSERT INTO public.insumos VALUES (27, 'ELE-004', 'Fuente de alimentación 12V para cámara', 1, true, 45000.00);
INSERT INTO public.insumos VALUES (28, 'LIM-001', 'Desengrasante industrial', 4, true, 22000.00);
INSERT INTO public.insumos VALUES (29, 'LIM-002', 'Trapo industrial', 2, true, 18000.00);
INSERT INTO public.insumos VALUES (30, 'LIM-003', 'Detergente concentrado', 4, true, 15000.00);
INSERT INTO public.insumos VALUES (31, 'EPP-001', 'Guantes de nitrilo (caja de 100)', 8, true, 55000.00);
INSERT INTO public.insumos VALUES (32, 'EPP-002', 'Lentes de seguridad', 1, true, 25000.00);
INSERT INTO public.insumos VALUES (33, 'EPP-003', 'Botines de seguridad', 9, true, 320000.00);
INSERT INTO public.insumos VALUES (34, 'EPP-004', 'Chaleco reflectivo', 1, true, 35000.00);
INSERT INTO public.insumos VALUES (35, 'OFI-001', 'Resma de papel A4', 1, true, 38000.00);
INSERT INTO public.insumos VALUES (36, 'OFI-002', 'Tóner para impresora láser', 1, true, 420000.00);
INSERT INTO public.insumos VALUES (37, 'INF-001', 'Disco SSD 480 GB', 1, true, 320000.00);
INSERT INTO public.insumos VALUES (38, 'INF-002', 'Memoria RAM DDR4 8 GB', 1, true, 250000.00);
INSERT INTO public.insumos VALUES (39, 'INF-003', 'Disco duro 2 TB para videovigilancia', 1, true, 650000.00);
INSERT INTO public.insumos VALUES (40, 'EQ-001', 'Notebook HP ProBook 450 G10', 1, true, 6500000.00);
INSERT INTO public.insumos VALUES (41, 'EQ-002', 'Computadora de escritorio Dell OptiPlex 3000', 1, true, 5200000.00);
INSERT INTO public.insumos VALUES (42, 'EQ-003', 'Monitor LED 24 pulgadas', 1, true, 950000.00);
INSERT INTO public.insumos VALUES (43, 'EQ-004', 'Impresora multifunción láser HP', 1, true, 2400000.00);
INSERT INTO public.insumos VALUES (44, 'EQ-005', 'Cámara de seguridad IP domo 4 MP', 1, true, 480000.00);
INSERT INTO public.insumos VALUES (45, 'EQ-006', 'Grabador NVR de 16 canales', 1, true, 1850000.00);
INSERT INTO public.insumos VALUES (46, 'EQ-007', 'Switch de red 24 puertos', 1, true, 1600000.00);
INSERT INTO public.insumos VALUES (47, 'EQ-008', 'UPS 1500 VA', 1, true, 1350000.00);
INSERT INTO public.insumos VALUES (48, 'EQ-009', 'Aire acondicionado split 18.000 BTU', 1, true, 4300000.00);
INSERT INTO public.insumos VALUES (49, 'EQ-010', 'Compresor de aire 100 litros', 1, true, 3200000.00);
INSERT INTO public.insumos VALUES (50, 'EQ-011', 'Elevador hidráulico de dos columnas 4 t', 1, true, 28000000.00);
INSERT INTO public.insumos VALUES (51, 'EQ-012', 'Gato hidráulico tipo carro 3 t', 1, true, 1450000.00);
INSERT INTO public.insumos VALUES (52, 'EQ-013', 'Escáner de diagnóstico automotriz', 1, true, 7800000.00);
INSERT INTO public.insumos VALUES (53, 'EQ-014', 'Soldadora inverter 200 A', 1, true, 1900000.00);
INSERT INTO public.insumos VALUES (54, 'EQ-015', 'Generador eléctrico 7,5 kVA', 1, true, 12500000.00);
INSERT INTO public.insumos VALUES (55, 'EQ-016', 'Proyector multimedia', 1, true, 3100000.00);


--
-- Data for Name: movimientos_activos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.movimientos_activos VALUES (1, 'MOV-20260310-000001', '2026-03-10 09:00:00', 'INGRESO', 2, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: PF-3NX81A27', NULL, 8);
INSERT INTO public.movimientos_activos VALUES (2, 'MOV-20260310-000002', '2026-03-10 11:30:00', 'ASIGNACION', 2, 1, 1, 1, NULL, 'María', 'Benítez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a María Benítez', 8, 8);
INSERT INTO public.movimientos_activos VALUES (3, 'MOV-20260310-000003', '2026-03-10 09:00:00', 'INGRESO', 3, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: PF-3NX81A54', NULL, 12);
INSERT INTO public.movimientos_activos VALUES (4, 'MOV-20260310-000004', '2026-03-10 11:30:00', 'ASIGNACION', 3, 1, 1, 1, NULL, 'Carlos', 'Ayala', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Carlos Ayala', 12, 12);
INSERT INTO public.movimientos_activos VALUES (5, 'MOV-20260402-000005', '2026-04-02 09:00:00', 'INGRESO', 4, NULL, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: PF-3NX82B11', NULL, 16);
INSERT INTO public.movimientos_activos VALUES (6, 'MOV-20260402-000006', '2026-04-02 11:30:00', 'ASIGNACION', 4, 2, 2, 1, NULL, 'Lucía', 'Ortiz', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Lucía Ortiz', 16, 16);
INSERT INTO public.movimientos_activos VALUES (7, 'MOV-20260312-000007', '2026-03-12 09:00:00', 'INGRESO', 5, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: DL-7Q2K9031', NULL, 13);
INSERT INTO public.movimientos_activos VALUES (8, 'MOV-20260312-000008', '2026-03-12 11:30:00', 'ASIGNACION', 5, 1, 1, 1, NULL, 'Jorge', 'Gómez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Jorge Gómez', 13, 13);
INSERT INTO public.movimientos_activos VALUES (9, 'MOV-20260420-000009', '2026-04-20 09:00:00', 'INGRESO', 6, NULL, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: DL-7Q2K9147', NULL, 22);
INSERT INTO public.movimientos_activos VALUES (10, 'MOV-20260420-000010', '2026-04-20 11:30:00', 'ASIGNACION', 6, 3, 3, 1, NULL, 'Ana', 'Villalba', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Ana Villalba', 22, 22);
INSERT INTO public.movimientos_activos VALUES (11, 'MOV-20260505-000011', '2026-05-05 09:00:00', 'INGRESO', 7, NULL, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: DL-7Q2K9188', NULL, 26);
INSERT INTO public.movimientos_activos VALUES (12, 'MOV-20260312-000012', '2026-03-12 09:00:00', 'INGRESO', 8, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: MN-24F00317', NULL, 8);
INSERT INTO public.movimientos_activos VALUES (13, 'MOV-20260312-000013', '2026-03-12 11:30:00', 'ASIGNACION', 8, 1, 1, 1, NULL, 'María', 'Benítez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a María Benítez', 8, 8);
INSERT INTO public.movimientos_activos VALUES (14, 'MOV-20260420-000014', '2026-04-20 09:00:00', 'INGRESO', 9, NULL, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: MN-24F00342', NULL, 22);
INSERT INTO public.movimientos_activos VALUES (15, 'MOV-20260420-000015', '2026-04-20 11:30:00', 'ASIGNACION', 9, 3, 3, 1, NULL, 'Ana', 'Villalba', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Ana Villalba', 22, 22);
INSERT INTO public.movimientos_activos VALUES (16, 'MOV-20260318-000016', '2026-03-18 09:00:00', 'INGRESO', 10, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: HP-CNB3R0912', NULL, 7);
INSERT INTO public.movimientos_activos VALUES (17, 'MOV-20260608-000017', '2026-06-08 09:00:00', 'INGRESO', 11, NULL, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: HP-CNB3R1044', NULL, 31);
INSERT INTO public.movimientos_activos VALUES (18, 'MOV-20260415-000018', '2026-04-15 09:00:00', 'INGRESO', 12, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: CAM-D4M0001', NULL, 11);
INSERT INTO public.movimientos_activos VALUES (19, 'MOV-20260415-000019', '2026-04-15 11:30:00', 'ASIGNACION', 12, 1, 1, 1, NULL, 'Ramón', 'Cáceres', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Ramón Cáceres', 11, 11);
INSERT INTO public.movimientos_activos VALUES (20, 'MOV-20260415-000020', '2026-04-15 09:00:00', 'INGRESO', 13, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: CAM-D4M0002', NULL, 11);
INSERT INTO public.movimientos_activos VALUES (21, 'MOV-20260415-000021', '2026-04-15 11:30:00', 'ASIGNACION', 13, 1, 1, 1, NULL, 'Ramón', 'Cáceres', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Ramón Cáceres', 11, 11);
INSERT INTO public.movimientos_activos VALUES (22, 'MOV-20260520-000022', '2026-05-20 09:00:00', 'INGRESO', 14, NULL, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: CAM-D4M0003', NULL, 20);
INSERT INTO public.movimientos_activos VALUES (23, 'MOV-20260520-000023', '2026-05-20 11:30:00', 'ASIGNACION', 14, 2, 2, 1, NULL, 'Pedro', 'Fernández', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Pedro Fernández', 20, 20);
INSERT INTO public.movimientos_activos VALUES (24, 'MOV-20260522-000024', '2026-05-22 09:00:00', 'INGRESO', 15, NULL, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: CAM-D4M0004', NULL, 25);
INSERT INTO public.movimientos_activos VALUES (25, 'MOV-20260415-000025', '2026-04-15 09:00:00', 'INGRESO', 16, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: NVR-16C2210', NULL, 11);
INSERT INTO public.movimientos_activos VALUES (26, 'MOV-20260415-000026', '2026-04-15 11:30:00', 'ASIGNACION', 16, 1, 1, 1, NULL, 'Ramón', 'Cáceres', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Ramón Cáceres', 11, 11);
INSERT INTO public.movimientos_activos VALUES (27, 'MOV-20260520-000027', '2026-05-20 09:00:00', 'INGRESO', 17, NULL, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: NVR-16C2245', NULL, 20);
INSERT INTO public.movimientos_activos VALUES (28, 'MOV-20260520-000028', '2026-05-20 11:30:00', 'ASIGNACION', 17, 2, 2, 1, NULL, 'Pedro', 'Fernández', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Pedro Fernández', 20, 20);
INSERT INTO public.movimientos_activos VALUES (29, 'MOV-20260325-000029', '2026-03-25 09:00:00', 'INGRESO', 18, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: SW-24P88120', NULL, 12);
INSERT INTO public.movimientos_activos VALUES (30, 'MOV-20260325-000030', '2026-03-25 11:30:00', 'ASIGNACION', 18, 1, 1, 1, NULL, 'Carlos', 'Ayala', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Carlos Ayala', 12, 12);
INSERT INTO public.movimientos_activos VALUES (31, 'MOV-20260325-000031', '2026-03-25 09:00:00', 'INGRESO', 19, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: UPS-1500K451', NULL, 12);
INSERT INTO public.movimientos_activos VALUES (32, 'MOV-20260325-000032', '2026-03-25 11:30:00', 'ASIGNACION', 19, 1, 1, 1, NULL, 'Carlos', 'Ayala', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Carlos Ayala', 12, 12);
INSERT INTO public.movimientos_activos VALUES (33, 'MOV-20260227-000033', '2026-02-27 09:00:00', 'INGRESO', 20, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: AC-18KB0921', NULL, 15);
INSERT INTO public.movimientos_activos VALUES (34, 'MOV-20260227-000034', '2026-02-27 11:30:00', 'ASIGNACION', 20, 1, 1, 1, NULL, 'Roberto', 'Duarte', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Roberto Duarte', 15, 15);
INSERT INTO public.movimientos_activos VALUES (35, 'MOV-20260615-000035', '2026-06-15 09:00:00', 'INGRESO', 21, NULL, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: AC-18KB0977', NULL, 26);
INSERT INTO public.movimientos_activos VALUES (36, 'MOV-20260408-000036', '2026-04-08 09:00:00', 'INGRESO', 22, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: CMP-100L3307', NULL, 9);
INSERT INTO public.movimientos_activos VALUES (37, 'MOV-20260408-000037', '2026-04-08 11:30:00', 'ASIGNACION', 22, 1, 1, 1, NULL, 'Hugo', 'Martínez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Hugo Martínez', 9, 9);
INSERT INTO public.movimientos_activos VALUES (38, 'MOV-20260512-000038', '2026-05-12 09:00:00', 'INGRESO', 23, NULL, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: CMP-100L3318', NULL, 23);
INSERT INTO public.movimientos_activos VALUES (39, 'MOV-20260512-000039', '2026-05-12 11:30:00', 'ASIGNACION', 23, 3, 3, 1, NULL, 'Diego', 'Rojas', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Diego Rojas', 23, 23);
INSERT INTO public.movimientos_activos VALUES (40, 'MOV-20260408-000040', '2026-04-08 09:00:00', 'INGRESO', 24, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: ELV-2C40T015', NULL, 9);
INSERT INTO public.movimientos_activos VALUES (41, 'MOV-20260408-000041', '2026-04-08 11:30:00', 'ASIGNACION', 24, 1, 1, 1, NULL, 'Hugo', 'Martínez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Hugo Martínez', 9, 9);
INSERT INTO public.movimientos_activos VALUES (42, 'MOV-20260502-000042', '2026-05-02 09:00:00', 'INGRESO', 25, NULL, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: GH-3T770412', NULL, 18);
INSERT INTO public.movimientos_activos VALUES (43, 'MOV-20260502-000043', '2026-05-02 11:30:00', 'ASIGNACION', 25, 2, 2, 1, NULL, 'Silvio', 'Acosta', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Silvio Acosta', 18, 18);
INSERT INTO public.movimientos_activos VALUES (44, 'MOV-20260601-000044', '2026-06-01 09:00:00', 'INGRESO', 26, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: SCN-AT55091', NULL, 9);
INSERT INTO public.movimientos_activos VALUES (45, 'MOV-20260610-000045', '2026-06-10 09:00:00', 'INGRESO', 27, NULL, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: SLD-200A6612', NULL, 33);
INSERT INTO public.movimientos_activos VALUES (46, 'MOV-20260610-000046', '2026-06-10 11:30:00', 'ASIGNACION', 27, 5, 5, 1, NULL, 'Mario', 'Giménez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Mario Giménez', 33, 33);
INSERT INTO public.movimientos_activos VALUES (47, 'MOV-20260305-000047', '2026-03-05 09:00:00', 'INGRESO', 28, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: GEN-75K10293', NULL, 10);
INSERT INTO public.movimientos_activos VALUES (48, 'MOV-20260305-000048', '2026-03-05 11:30:00', 'ASIGNACION', 28, 1, 1, 1, NULL, 'Néstor', 'Villagra', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DISPONIBLE', 'ASIGNADO', 'Asignado a Néstor Villagra', 10, 10);
INSERT INTO public.movimientos_activos VALUES (49, 'MOV-20260701-000049', '2026-07-01 09:00:00', 'INGRESO', 29, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'SOLICITADO', 'DISPONIBLE', 'Carga inicial de activos - Serie del fabricante: PRY-MX40517', NULL, 14);


--
-- Data for Name: movimientos_stock; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.movimientos_stock VALUES (1, '2026-09-29 08:49:58.624775', 3, 1, 'SALDO_INICIAL', 1, 1, 80000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock existente al habilitar el Kardex');
INSERT INTO public.movimientos_stock VALUES (2, '2026-09-29 08:49:58.624775', 4, 1, 'SALDO_INICIAL', 2, 2, 120000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock existente al habilitar el Kardex');
INSERT INTO public.movimientos_stock VALUES (3, '2026-09-29 08:49:58.624775', 1, 1, 'SALDO_INICIAL', 3, 3, 20000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock existente al habilitar el Kardex');
INSERT INTO public.movimientos_stock VALUES (4, '2026-09-29 08:49:58.624775', 2, 1, 'SALDO_INICIAL', 5, 5, 50000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock existente al habilitar el Kardex');
INSERT INTO public.movimientos_stock VALUES (5, '2026-10-21 11:59:35.99351', 4, 1, 'SALIDA_OT', -1, 1, 120000.00, 'OT-20261021-0001', 1, NULL, NULL, NULL, 1, 6, 'Suministro a la OT');
INSERT INTO public.movimientos_stock VALUES (6, '2026-10-21 12:01:46.008214', 2, 6, 'ENTRADA_COMPRA', 5, 5, 50000.00, 'OC-20260715-0003', NULL, 8, NULL, NULL, NULL, 6, 'Recepción de compra OC-20260715-0003-R01');
INSERT INTO public.movimientos_stock VALUES (7, '2026-10-21 12:01:46.008214', 5, 6, 'ENTRADA_COMPRA', 2, 2, 65000.00, 'OC-20260715-0003', NULL, 8, NULL, NULL, NULL, 6, 'Recepción de compra OC-20260715-0003-R01');
INSERT INTO public.movimientos_stock VALUES (8, '2026-10-01 08:00:00', 24, 1, 'SALDO_INICIAL', 208, 208, 5500.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (9, '2026-10-01 08:00:00', 25, 1, 'SALDO_INICIAL', 315, 315, 4500.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (10, '2026-10-01 08:00:00', 26, 1, 'SALDO_INICIAL', 8, 8, 65000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (11, '2026-10-01 08:00:00', 31, 1, 'SALDO_INICIAL', 2, 2, 55000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (12, '2026-10-01 08:00:00', 32, 1, 'SALDO_INICIAL', 30, 30, 25000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (13, '2026-10-01 08:00:00', 33, 1, 'SALDO_INICIAL', 13, 13, 320000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (14, '2026-10-01 08:00:00', 34, 1, 'SALDO_INICIAL', 22, 22, 35000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (15, '2026-10-01 08:00:00', 20, 1, 'SALDO_INICIAL', 15, 15, 6000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (16, '2026-10-01 08:00:00', 21, 1, 'SALDO_INICIAL', 44, 44, 9000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (17, '2026-10-01 08:00:00', 22, 1, 'SALDO_INICIAL', 57, 57, 12000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (18, '2026-10-01 08:00:00', 23, 1, 'SALDO_INICIAL', 24, 24, 38000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (19, '2026-10-01 08:00:00', 38, 1, 'SALDO_INICIAL', 1, 1, 250000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (20, '2026-10-01 08:00:00', 39, 1, 'SALDO_INICIAL', 6, 6, 650000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (21, '2026-10-01 08:00:00', 28, 1, 'SALDO_INICIAL', 55, 55, 22000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (22, '2026-10-01 08:00:00', 29, 1, 'SALDO_INICIAL', 45, 45, 18000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (23, '2026-10-01 08:00:00', 14, 1, 'SALDO_INICIAL', 30, 30, 38000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (24, '2026-10-01 08:00:00', 15, 1, 'SALDO_INICIAL', 105, 105, 32000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (25, '2026-10-01 08:00:00', 16, 1, 'SALDO_INICIAL', 21, 21, 45000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (26, '2026-10-01 08:00:00', 17, 1, 'SALDO_INICIAL', 32, 32, 42000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (27, '2026-10-01 08:00:00', 35, 1, 'SALDO_INICIAL', 10, 10, 38000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (28, '2026-10-01 08:00:00', 36, 1, 'SALDO_INICIAL', 9, 9, 420000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (29, '2026-10-01 08:00:00', 6, 1, 'SALDO_INICIAL', 32, 32, 45000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (30, '2026-10-01 08:00:00', 7, 1, 'SALDO_INICIAL', 24, 24, 95000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (31, '2026-10-01 08:00:00', 9, 1, 'SALDO_INICIAL', 3, 3, 280000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (32, '2026-10-01 08:00:00', 10, 1, 'SALDO_INICIAL', 11, 11, 210000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (33, '2026-10-01 08:00:00', 11, 1, 'SALDO_INICIAL', 7, 7, 950000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (34, '2026-10-01 08:00:00', 12, 1, 'SALDO_INICIAL', 41, 41, 25000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (35, '2026-10-01 08:00:00', 24, 2, 'SALDO_INICIAL', 209, 209, 5500.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (36, '2026-10-01 08:00:00', 25, 2, 'SALDO_INICIAL', 316, 316, 4500.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (37, '2026-10-01 08:00:00', 27, 2, 'SALDO_INICIAL', 3, 3, 45000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (38, '2026-10-01 08:00:00', 31, 2, 'SALDO_INICIAL', 10, 10, 55000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (39, '2026-10-01 08:00:00', 32, 2, 'SALDO_INICIAL', 20, 20, 25000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (40, '2026-10-01 08:00:00', 33, 2, 'SALDO_INICIAL', 14, 14, 320000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (41, '2026-10-01 08:00:00', 19, 2, 'SALDO_INICIAL', 2, 2, 85000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (42, '2026-10-01 08:00:00', 20, 2, 'SALDO_INICIAL', 70, 70, 6000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (43, '2026-10-01 08:00:00', 21, 2, 'SALDO_INICIAL', 45, 45, 9000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (44, '2026-10-01 08:00:00', 22, 2, 'SALDO_INICIAL', 58, 58, 12000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (45, '2026-10-01 08:00:00', 37, 2, 'SALDO_INICIAL', 1, 1, 320000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (46, '2026-10-01 08:00:00', 38, 2, 'SALDO_INICIAL', 9, 9, 250000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (47, '2026-10-01 08:00:00', 39, 2, 'SALDO_INICIAL', 4, 4, 650000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (48, '2026-10-01 08:00:00', 28, 2, 'SALDO_INICIAL', 56, 56, 22000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (49, '2026-10-01 08:00:00', 30, 2, 'SALDO_INICIAL', 10, 10, 15000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (50, '2026-10-01 08:00:00', 14, 2, 'SALDO_INICIAL', 140, 140, 38000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (51, '2026-10-01 08:00:00', 15, 2, 'SALDO_INICIAL', 106, 106, 32000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (52, '2026-10-01 08:00:00', 16, 2, 'SALDO_INICIAL', 22, 22, 45000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (53, '2026-10-01 08:00:00', 18, 2, 'SALDO_INICIAL', 10, 10, 28000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (54, '2026-10-01 08:00:00', 35, 2, 'SALDO_INICIAL', 49, 49, 38000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (55, '2026-10-01 08:00:00', 36, 2, 'SALDO_INICIAL', 6, 6, 420000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (56, '2026-10-01 08:00:00', 6, 2, 'SALDO_INICIAL', 33, 33, 45000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (57, '2026-10-01 08:00:00', 8, 2, 'SALDO_INICIAL', 5, 5, 60000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (58, '2026-10-01 08:00:00', 9, 2, 'SALDO_INICIAL', 14, 14, 280000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (59, '2026-10-01 08:00:00', 10, 2, 'SALDO_INICIAL', 12, 12, 210000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (60, '2026-10-01 08:00:00', 11, 2, 'SALDO_INICIAL', 8, 8, 950000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');
INSERT INTO public.movimientos_stock VALUES (61, '2026-10-01 08:00:00', 13, 2, 'SALDO_INICIAL', 2, 2, 2300000.00, 'SALDO-INICIAL', NULL, NULL, NULL, NULL, NULL, NULL, 'Stock inicial cargado al habilitar el insumo en el almacén');


--
-- Data for Name: ot; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.ot VALUES (1, 'OT-20261021-0001', '2026-10-21', 1, 1, NULL, 'FINALIZADA', 6, 'Se solicita', '2026-10-21 22:19:20.916221', 6, NULL, '2026-10-21 11:59:10.071834', NULL, NULL, NULL, NULL);


--
-- Data for Name: ot_eventos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.ot_eventos VALUES (1, 1, '2026-10-21 11:59:10.071834', 'CREACION', 6, 'OT creada para el centro Centro de Asunción: Se solicita');
INSERT INTO public.ot_eventos VALUES (2, 1, '2026-10-21 11:59:35.99351', 'SUMINISTRO', 6, 'Insumos entregados: 1 x INS004 - Aceite hidráulico (Almacén Central)');
INSERT INTO public.ot_eventos VALUES (3, 1, '2026-10-21 12:00:31.541767', 'SALIDA', 6, 'Salida de 1 insumo(s) con la remisión REM-2026-000001. Transporta: Pedro Perez (CI 6484864)');
INSERT INTO public.ot_eventos VALUES (4, 1, '2026-10-21 22:19:20.916221', 'FINALIZACION', 6, 'OT finalizada');


--
-- Data for Name: proveedores; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.proveedores VALUES (2, 'Equipos del Paraguay', '80054321-2', 'Ruta Transchaco km 12', '021-654321', 'contacto@equipospy.com', true);
INSERT INTO public.proveedores VALUES (3, 'Distribuidora Norte', '80098765-3', 'Mariscal López 456', '0981-123456', 'norte@distnorte.com', true);
INSERT INTO public.proveedores VALUES (4, 'Tecno Sur S.R.L.', '80011122-4', 'Av. Aviadores 789', '021-987654', 'tecno@sur.com.py', true);
INSERT INTO public.proveedores VALUES (5, 'Logística Integral', '80033344-5', 'San Lorenzo 101', '0985-789012', 'logistica@integral.com', true);
INSERT INTO public.proveedores VALUES (1, 'Insumos S.A.', '80012345-1', 'Av. España 123', '021-123456', 'ventas@insumossa.com', true);
INSERT INTO public.proveedores VALUES (8, 'demoprov', '6546210', 'demoprov', '34646321', 'demoprov@demoprov.com', true);
INSERT INTO public.proveedores VALUES (9, 'Repuestos del Este S.A.', '80101234-5', 'Av. Mcal. López 1520, Asunción', '021-612345', 'ventas@repuestosdeleste.com.py', true);
INSERT INTO public.proveedores VALUES (10, 'Lubricantes Guaraní S.R.L.', '80102345-3', 'Ruta PY02 km 15, San Lorenzo', '021-583210', 'pedidos@lubriguarani.com.py', true);
INSERT INTO public.proveedores VALUES (11, 'Ferretería Industrial Mbareté S.A.', '80103456-1', 'Av. Eusebio Ayala 2450, Asunción', '021-554890', 'compras@mbarete.com.py', true);
INSERT INTO public.proveedores VALUES (12, 'Neumáticos Paraná S.A.', '80104567-0', 'Av. Irrazábal 980, Encarnación', '071-204455', 'contacto@neumaticosparana.com.py', true);
INSERT INTO public.proveedores VALUES (13, 'TecnoRed Informática S.R.L.', '80105678-8', 'Av. Santa Teresa 3010, Asunción', '021-661020', 'ventas@tecnored.com.py', true);
INSERT INTO public.proveedores VALUES (14, 'Seguridad Total CCTV S.A.', '80106789-6', 'Av. Artigas 1855, Asunción', '021-298760', 'info@seguridadtotal.com.py', true);
INSERT INTO public.proveedores VALUES (15, 'Electro Insumos Yguazú S.R.L.', '80107890-4', 'Ruta PY02 km 4, Ciudad del Este', '061-571230', 'ventas@electroyguazu.com.py', true);
INSERT INTO public.proveedores VALUES (16, 'Limpieza y Afines S.A.', '80108901-2', 'Calle Palma 455, Luque', '021-645120', 'pedidos@limpiezayafines.com.py', true);
INSERT INTO public.proveedores VALUES (17, 'EPP Paraguay S.R.L.', '80109012-0', 'Av. Defensores del Chaco 1200, Fdo. de la Mora', '021-503300', 'ventas@epppy.com.py', true);
INSERT INTO public.proveedores VALUES (18, 'Maquinarias y Herramientas S.A.', '80110123-7', 'Av. Fernando de la Mora 3420, Asunción', '021-520870', 'comercial@mayhe.com.py', true);
INSERT INTO public.proveedores VALUES (19, 'Climatización Central S.R.L.', '80111234-5', 'Av. Mcal. Estigarribia 640, Coronel Oviedo', '0521-202530', 'ventas@climacentral.com.py', true);
INSERT INTO public.proveedores VALUES (20, 'Papelería y Oficina Ñandutí S.A.', '80112345-3', 'Av. Brasilia 1120, Asunción', '021-290410', 'pedidos@papeleriananduti.com.py', true);


--
-- Data for Name: recepciones_oc; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.recepciones_oc VALUES (1, 1, 'OC-20260701-0001-R01', '2026-07-01 00:00:00', NULL, true, false, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc VALUES (2, 2, 'OC-20260701-0002-R01', '2026-07-01 00:00:00', NULL, true, false, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc VALUES (3, 5, 'OC-20260715-0001-R01', '2026-07-15 00:00:00', NULL, true, false, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc VALUES (4, 6, 'OC-20260715-0002-R01', '2026-07-15 00:00:00', NULL, true, false, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc VALUES (5, 9, 'OC-20260715-0004-R01', '2026-07-15 00:00:00', NULL, true, false, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc VALUES (6, 8, 'OC-20260715-0003-R01', '2026-10-21 12:01:46.008214', 6, false, false, NULL, NULL, NULL);


--
-- Data for Name: recepciones_oc_detalle; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.recepciones_oc_detalle VALUES (1, 1, 2, 2, NULL, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc_detalle VALUES (2, 2, 3, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc_detalle VALUES (3, 3, 6, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc_detalle VALUES (4, 4, 12, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc_detalle VALUES (5, 5, 13, 1, NULL, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc_detalle VALUES (6, 6, 10, 5, 6, NULL, NULL, NULL);
INSERT INTO public.recepciones_oc_detalle VALUES (7, 6, 11, 2, 6, NULL, NULL, NULL);


--
-- Data for Name: remisiones; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.remisiones VALUES (1, 'REM-2026-000001', '2026-10-21', 'SALIDA_INSUMOS', 1, NULL, NULL, false, '2026-10-21 12:00:31.541767', 6, 'Pedro', 'Perez', '6484864', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: remisiones_detalle; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.remisiones_detalle VALUES (1, 1, 1, 'INSUMO', 4, NULL, 'INS004', 'Aceite hidráulico', 'l', 1, 1, 'Almacén Central');


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.roles VALUES (1, 'ADMINISTRADOR', 'Acceso total, todos los centros', true, 'ADMINISTRADOR');
INSERT INTO public.roles VALUES (2, 'GERENTE', 'Operación, aprobaciones e informes de sus centros', true, 'GERENTE');
INSERT INTO public.roles VALUES (3, 'AUXILIAR', 'Operación de sus centros, sin aprobaciones ni finalización de OT', true, 'AUXILIAR');
INSERT INTO public.roles VALUES (4, 'ENCARGADO DE COMPRAS', 'Proveedores, insumos, compras, OT (sin suministro) e informes', true, 'ENCARGADO_COMPRAS');
INSERT INTO public.roles VALUES (5, 'CONSULTOR', 'Solo informes', true, 'CONSULTOR');


--
-- Data for Name: solicitudes_activos; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: solicitudes_activos_detalle; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: stock_almacen; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.stock_almacen VALUES (3, 2, 1, 5, 5);
INSERT INTO public.stock_almacen VALUES (1, 1, 1, 3, 1);
INSERT INTO public.stock_almacen VALUES (6, 3, 1, 1, 0);
INSERT INTO public.stock_almacen VALUES (4, 4, 1, 1, 0);
INSERT INTO public.stock_almacen VALUES (40, 2, 6, 5, 0);
INSERT INTO public.stock_almacen VALUES (41, 5, 6, 2, 0);
INSERT INTO public.stock_almacen VALUES (42, 24, 1, 208, 100);
INSERT INTO public.stock_almacen VALUES (43, 25, 1, 315, 150);
INSERT INTO public.stock_almacen VALUES (44, 26, 1, 8, 3);
INSERT INTO public.stock_almacen VALUES (45, 27, 1, 0, 6);
INSERT INTO public.stock_almacen VALUES (46, 31, 1, 2, 4);
INSERT INTO public.stock_almacen VALUES (47, 32, 1, 30, 10);
INSERT INTO public.stock_almacen VALUES (48, 33, 1, 13, 6);
INSERT INTO public.stock_almacen VALUES (49, 34, 1, 22, 10);
INSERT INTO public.stock_almacen VALUES (50, 19, 1, 0, 5);
INSERT INTO public.stock_almacen VALUES (51, 20, 1, 15, 30);
INSERT INTO public.stock_almacen VALUES (52, 21, 1, 44, 15);
INSERT INTO public.stock_almacen VALUES (53, 22, 1, 57, 25);
INSERT INTO public.stock_almacen VALUES (54, 23, 1, 24, 10);
INSERT INTO public.stock_almacen VALUES (55, 37, 1, 0, 3);
INSERT INTO public.stock_almacen VALUES (56, 38, 1, 1, 3);
INSERT INTO public.stock_almacen VALUES (57, 39, 1, 6, 2);
INSERT INTO public.stock_almacen VALUES (58, 28, 1, 55, 20);
INSERT INTO public.stock_almacen VALUES (59, 29, 1, 45, 15);
INSERT INTO public.stock_almacen VALUES (60, 30, 1, 0, 20);
INSERT INTO public.stock_almacen VALUES (61, 14, 1, 30, 60);
INSERT INTO public.stock_almacen VALUES (62, 15, 1, 105, 40);
INSERT INTO public.stock_almacen VALUES (63, 16, 1, 21, 10);
INSERT INTO public.stock_almacen VALUES (64, 17, 1, 32, 15);
INSERT INTO public.stock_almacen VALUES (65, 18, 1, 0, 20);
INSERT INTO public.stock_almacen VALUES (66, 35, 1, 10, 20);
INSERT INTO public.stock_almacen VALUES (67, 36, 1, 9, 3);
INSERT INTO public.stock_almacen VALUES (68, 6, 1, 32, 12);
INSERT INTO public.stock_almacen VALUES (69, 7, 1, 24, 8);
INSERT INTO public.stock_almacen VALUES (70, 8, 1, 0, 10);
INSERT INTO public.stock_almacen VALUES (71, 9, 1, 3, 6);
INSERT INTO public.stock_almacen VALUES (72, 10, 1, 11, 4);
INSERT INTO public.stock_almacen VALUES (73, 11, 1, 7, 3);
INSERT INTO public.stock_almacen VALUES (74, 12, 1, 41, 20);
INSERT INTO public.stock_almacen VALUES (75, 13, 1, 0, 4);
INSERT INTO public.stock_almacen VALUES (76, 24, 2, 209, 100);
INSERT INTO public.stock_almacen VALUES (77, 25, 2, 316, 150);
INSERT INTO public.stock_almacen VALUES (78, 26, 2, 0, 3);
INSERT INTO public.stock_almacen VALUES (79, 27, 2, 3, 6);
INSERT INTO public.stock_almacen VALUES (80, 31, 2, 10, 4);
INSERT INTO public.stock_almacen VALUES (81, 32, 2, 20, 10);
INSERT INTO public.stock_almacen VALUES (82, 33, 2, 14, 6);
INSERT INTO public.stock_almacen VALUES (83, 34, 2, 0, 10);
INSERT INTO public.stock_almacen VALUES (84, 19, 2, 2, 5);
INSERT INTO public.stock_almacen VALUES (85, 20, 2, 70, 30);
INSERT INTO public.stock_almacen VALUES (86, 21, 2, 45, 15);
INSERT INTO public.stock_almacen VALUES (87, 22, 2, 58, 25);
INSERT INTO public.stock_almacen VALUES (88, 23, 2, 0, 10);
INSERT INTO public.stock_almacen VALUES (89, 37, 2, 1, 3);
INSERT INTO public.stock_almacen VALUES (90, 38, 2, 9, 3);
INSERT INTO public.stock_almacen VALUES (91, 39, 2, 4, 2);
INSERT INTO public.stock_almacen VALUES (92, 28, 2, 56, 20);
INSERT INTO public.stock_almacen VALUES (93, 29, 2, 0, 15);
INSERT INTO public.stock_almacen VALUES (94, 30, 2, 10, 20);
INSERT INTO public.stock_almacen VALUES (95, 14, 2, 140, 60);
INSERT INTO public.stock_almacen VALUES (96, 15, 2, 106, 40);
INSERT INTO public.stock_almacen VALUES (97, 16, 2, 22, 10);
INSERT INTO public.stock_almacen VALUES (98, 17, 2, 0, 15);
INSERT INTO public.stock_almacen VALUES (99, 18, 2, 10, 20);
INSERT INTO public.stock_almacen VALUES (100, 35, 2, 49, 20);
INSERT INTO public.stock_almacen VALUES (101, 36, 2, 6, 3);
INSERT INTO public.stock_almacen VALUES (102, 6, 2, 33, 12);
INSERT INTO public.stock_almacen VALUES (103, 7, 2, 0, 8);
INSERT INTO public.stock_almacen VALUES (104, 8, 2, 5, 10);
INSERT INTO public.stock_almacen VALUES (105, 9, 2, 14, 6);
INSERT INTO public.stock_almacen VALUES (106, 10, 2, 12, 4);
INSERT INTO public.stock_almacen VALUES (107, 11, 2, 8, 3);
INSERT INTO public.stock_almacen VALUES (108, 12, 2, 0, 20);
INSERT INTO public.stock_almacen VALUES (109, 13, 2, 2, 4);


--
-- Data for Name: transferencias; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: transferencias_detalle; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: unidad_medida; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.unidad_medida VALUES (1, 'UNIDAD', 'und', true);
INSERT INTO public.unidad_medida VALUES (2, 'KILOGRAMO', 'kg', true);
INSERT INTO public.unidad_medida VALUES (3, 'GRAMO', 'g', true);
INSERT INTO public.unidad_medida VALUES (4, 'LITRO', 'l', true);
INSERT INTO public.unidad_medida VALUES (5, 'MILILITRO', 'ml', true);
INSERT INTO public.unidad_medida VALUES (6, 'METRO', 'm', true);
INSERT INTO public.unidad_medida VALUES (7, 'CENTÍMETRO', 'cm', true);
INSERT INTO public.unidad_medida VALUES (8, 'CAJA', 'caja', true);
INSERT INTO public.unidad_medida VALUES (9, 'PAR', 'par', true);
INSERT INTO public.unidad_medida VALUES (10, 'ROLLO', 'rollo', true);
INSERT INTO public.unidad_medida VALUES (11, 'BULTO', 'bulto', true);
INSERT INTO public.unidad_medida VALUES (12, 'TONELADA', 't', true);


--
-- Data for Name: usuario_centros; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: usuario_centros_historial; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.usuario_centros_historial VALUES (1, 1, 1, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (2, 2, 2, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (3, 3, 3, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (4, 4, 4, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (5, 5, 5, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (6, 7, 1, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (7, 8, 6, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);
INSERT INTO public.usuario_centros_historial VALUES (8, 6, 1, 'PRINCIPAL', 'ALTA', NULL, NULL, 'Acceso existente al habilitar el historial', '2026-10-21 22:16:15.458777', NULL);


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.usuarios VALUES (1, 'Juan', 'Pérez', 'juan.perez@ñanday.com', '$2a$10$lClIqd9rAQfFhPBc6bDVR.vHQEnIEHTIRNz7gZVu1jhJFl/viezhm', 1, true, 1, NULL, NULL);
INSERT INTO public.usuarios VALUES (2, 'María', 'Gómez', 'maria.gomez@ñanday.com', '$2a$10$VQ74pKj88XxB.sBAsUntdeum.I0yuljy5zP0Rk0rszuybn7Epgv.O', 2, true, 2, NULL, NULL);
INSERT INTO public.usuarios VALUES (3, 'Carlos', 'López', 'carlos.lopez@ñanday.com', '$2a$10$E3dWgspPb5GNYiY1fRvGK.cQaCxW4OPqLRs32n02XhSwjVc.mwNdW', 3, true, 3, NULL, NULL);
INSERT INTO public.usuarios VALUES (4, 'Ana', 'Martínez', 'ana.martinez@ñanday.com', '$2a$10$FJD7i10v3TYr37cv.0lQm.hDOnZdQmjMPR57Mi96AIrgY17HsAgHK', 4, true, 4, NULL, NULL);
INSERT INTO public.usuarios VALUES (5, 'Luis', 'Fernández', 'luis.fernandez@ñanday.com', '$2a$10$ecrh9N8SQ8JRsdWJqM0dde.xdXr2LaxhrcTE2bAVGt.iRHyrwTOIe', 5, true, 5, NULL, NULL);
INSERT INTO public.usuarios VALUES (7, 'demo', 'demo', 'demo@demo.com', '$2a$10$VuU69Km.qFAAonzAYTsbmOWGxMu3SrSfFsxlM6JTFamiZlHUI9MUu', 1, true, 1, NULL, NULL);
INSERT INTO public.usuarios VALUES (8, 'demouser', 'demouser', 'demouser@demouser.com', '$2a$10$QKUHfMsgwQTe.RhyybhcieVwH5nwFQomP67g3sLClqXtWxTeaYLUG', 1, true, 6, NULL, NULL);
INSERT INTO public.usuarios VALUES (6, 'Admin', 'System', 'admin@admin.com', '$2a$10$JKDmYxf93k8ojyaG61yih.UOD2j9fHG8na/vSz7y1mqhckNclmow6', 1, true, 1, NULL, NULL);


--
-- Name: ajustes_detalle_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ajustes_detalle_id_detalle_seq', 1, false);


--
-- Name: ajustes_id_ajuste_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ajustes_id_ajuste_seq', 1, false);


--
-- Name: almacenes_id_almacen_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.almacenes_id_almacen_seq', 7, true);


--
-- Name: bitacora_detalle_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bitacora_detalle_id_detalle_seq', 1, false);


--
-- Name: bitacora_id_bitacora_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bitacora_id_bitacora_seq', 5, true);


--
-- Name: centros_costo_id_centro_costo_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.centros_costo_id_centro_costo_seq', 45, true);


--
-- Name: centros_id_centro_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.centros_id_centro_seq', 13, true);


--
-- Name: compras_id_compra_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.compras_id_compra_seq', 10, true);


--
-- Name: detalle_compra_id_detalle_compra_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_compra_id_detalle_compra_seq', 14, true);


--
-- Name: detalle_ot_id_detalle_ot_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_ot_id_detalle_ot_seq', 1, true);


--
-- Name: equipos_id_equipo_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.equipos_id_equipo_seq', 29, true);


--
-- Name: insumos_id_insumo_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.insumos_id_insumo_seq', 55, true);


--
-- Name: movimientos_activos_id_movimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.movimientos_activos_id_movimiento_seq', 49, true);


--
-- Name: movimientos_stock_id_mov_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.movimientos_stock_id_mov_seq', 61, true);


--
-- Name: ot_eventos_id_evento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ot_eventos_id_evento_seq', 4, true);


--
-- Name: ot_id_ot_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ot_id_ot_seq', 1, true);


--
-- Name: proveedores_id_proveedor_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.proveedores_id_proveedor_seq', 20, true);


--
-- Name: recepciones_oc_detalle_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.recepciones_oc_detalle_id_seq', 7, true);


--
-- Name: recepciones_oc_id_recepcion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.recepciones_oc_id_recepcion_seq', 6, true);


--
-- Name: remisiones_detalle_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.remisiones_detalle_id_detalle_seq', 1, true);


--
-- Name: remisiones_id_remision_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.remisiones_id_remision_seq', 1, true);


--
-- Name: roles_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_rol_seq', 5, true);


--
-- Name: seq_ajuste; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_ajuste', 1, false);


--
-- Name: seq_baja_activo; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_baja_activo', 1, false);


--
-- Name: seq_numero_activo; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_numero_activo', 28, true);


--
-- Name: seq_numero_movimiento; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_numero_movimiento', 49, true);


--
-- Name: seq_remision; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_remision', 1, true);


--
-- Name: seq_solicitud_activo; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_solicitud_activo', 1, false);


--
-- Name: seq_transferencia; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seq_transferencia', 1, false);


--
-- Name: solicitudes_activos_detalle_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitudes_activos_detalle_id_detalle_seq', 1, false);


--
-- Name: solicitudes_activos_id_solicitud_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitudes_activos_id_solicitud_seq', 1, false);


--
-- Name: stock_almacen_id_stock_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stock_almacen_id_stock_seq', 109, true);


--
-- Name: transferencias_detalle_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.transferencias_detalle_id_detalle_seq', 1, false);


--
-- Name: transferencias_id_transferencia_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.transferencias_id_transferencia_seq', 1, false);


--
-- Name: unidad_medida_id_unidad_medida_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.unidad_medida_id_unidad_medida_seq', 19, true);


--
-- Name: usuario_centros_historial_id_historial_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_centros_historial_id_historial_seq', 8, true);


--
-- Name: usuarios_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuarios_id_usuario_seq', 8, true);


--
-- Name: ajustes_detalle ajustes_detalle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes_detalle
    ADD CONSTRAINT ajustes_detalle_pkey PRIMARY KEY (id_detalle);


--
-- Name: ajustes ajustes_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_numero_key UNIQUE (numero);


--
-- Name: ajustes ajustes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_pkey PRIMARY KEY (id_ajuste);


--
-- Name: almacenes almacenes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.almacenes
    ADD CONSTRAINT almacenes_pkey PRIMARY KEY (id_almacen);


--
-- Name: bitacora_detalle bitacora_detalle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bitacora_detalle
    ADD CONSTRAINT bitacora_detalle_pkey PRIMARY KEY (id_detalle);


--
-- Name: bitacora bitacora_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bitacora
    ADD CONSTRAINT bitacora_pkey PRIMARY KEY (id_bitacora);


--
-- Name: centros centros_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros
    ADD CONSTRAINT centros_codigo_key UNIQUE (codigo);


--
-- Name: centros_costo centros_costo_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros_costo
    ADD CONSTRAINT centros_costo_codigo_key UNIQUE (codigo);


--
-- Name: centros_costo centros_costo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros_costo
    ADD CONSTRAINT centros_costo_pkey PRIMARY KEY (id_centro_costo);


--
-- Name: centros centros_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros
    ADD CONSTRAINT centros_pkey PRIMARY KEY (id_centro);


--
-- Name: compras compras_numero_oc_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_numero_oc_key UNIQUE (numero_oc);


--
-- Name: compras compras_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_pkey PRIMARY KEY (id_compra);


--
-- Name: correlativo_oc correlativo_oc_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.correlativo_oc
    ADD CONSTRAINT correlativo_oc_pkey PRIMARY KEY (fecha);


--
-- Name: correlativo_ot correlativo_ot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.correlativo_ot
    ADD CONSTRAINT correlativo_ot_pkey PRIMARY KEY (fecha);


--
-- Name: detalle_compra detalle_compra_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_pkey PRIMARY KEY (id_detalle_compra);


--
-- Name: detalle_ot detalle_ot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot
    ADD CONSTRAINT detalle_ot_pkey PRIMARY KEY (id_detalle_ot);


--
-- Name: equipos equipos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_pkey PRIMARY KEY (id_equipo);


--
-- Name: equipos equipos_serial_activo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_serial_activo_key UNIQUE (serial_activo);


--
-- Name: insumos insumos_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insumos
    ADD CONSTRAINT insumos_codigo_key UNIQUE (codigo);


--
-- Name: insumos insumos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insumos
    ADD CONSTRAINT insumos_pkey PRIMARY KEY (id_insumo);


--
-- Name: movimientos_activos movimientos_activos_numero_movimiento_unico_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_numero_movimiento_unico_key UNIQUE (numero_movimiento_unico);


--
-- Name: movimientos_activos movimientos_activos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_pkey PRIMARY KEY (id_movimiento);


--
-- Name: movimientos_stock movimientos_stock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_pkey PRIMARY KEY (id_mov);


--
-- Name: ot_eventos ot_eventos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot_eventos
    ADD CONSTRAINT ot_eventos_pkey PRIMARY KEY (id_evento);


--
-- Name: ot ot_numero_ot_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_numero_ot_key UNIQUE (numero_ot);


--
-- Name: ot ot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_pkey PRIMARY KEY (id_ot);


--
-- Name: proveedores proveedores_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.proveedores
    ADD CONSTRAINT proveedores_pkey PRIMARY KEY (id_proveedor);


--
-- Name: proveedores proveedores_ruc_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.proveedores
    ADD CONSTRAINT proveedores_ruc_key UNIQUE (ruc);


--
-- Name: recepciones_oc_detalle recepciones_oc_detalle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle
    ADD CONSTRAINT recepciones_oc_detalle_pkey PRIMARY KEY (id);


--
-- Name: recepciones_oc recepciones_oc_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc
    ADD CONSTRAINT recepciones_oc_numero_key UNIQUE (numero);


--
-- Name: recepciones_oc recepciones_oc_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc
    ADD CONSTRAINT recepciones_oc_pkey PRIMARY KEY (id_recepcion);


--
-- Name: remisiones_detalle remisiones_detalle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones_detalle
    ADD CONSTRAINT remisiones_detalle_pkey PRIMARY KEY (id_detalle);


--
-- Name: remisiones remisiones_numero_remision_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_numero_remision_key UNIQUE (numero_remision);


--
-- Name: remisiones remisiones_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_pkey PRIMARY KEY (id_remision);


--
-- Name: roles roles_nombre_rol_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_nombre_rol_key UNIQUE (nombre);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id_rol);


--
-- Name: solicitudes_activos_detalle solicitudes_activos_detalle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle
    ADD CONSTRAINT solicitudes_activos_detalle_pkey PRIMARY KEY (id_detalle);


--
-- Name: solicitudes_activos solicitudes_activos_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_numero_key UNIQUE (numero);


--
-- Name: solicitudes_activos solicitudes_activos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_pkey PRIMARY KEY (id_solicitud);


--
-- Name: stock_almacen stock_almacen_id_insumo_id_almacen_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_almacen
    ADD CONSTRAINT stock_almacen_id_insumo_id_almacen_key UNIQUE (id_insumo, id_almacen);


--
-- Name: stock_almacen stock_almacen_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_almacen
    ADD CONSTRAINT stock_almacen_pkey PRIMARY KEY (id_stock);


--
-- Name: transferencias_detalle transferencias_detalle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias_detalle
    ADD CONSTRAINT transferencias_detalle_pkey PRIMARY KEY (id_detalle);


--
-- Name: transferencias transferencias_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias
    ADD CONSTRAINT transferencias_numero_key UNIQUE (numero);


--
-- Name: transferencias transferencias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias
    ADD CONSTRAINT transferencias_pkey PRIMARY KEY (id_transferencia);


--
-- Name: unidad_medida unidad_medida_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unidad_medida
    ADD CONSTRAINT unidad_medida_nombre_key UNIQUE (nombre);


--
-- Name: unidad_medida unidad_medida_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unidad_medida
    ADD CONSTRAINT unidad_medida_pkey PRIMARY KEY (id_unidad_medida);


--
-- Name: usuario_centros_historial usuario_centros_historial_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros_historial
    ADD CONSTRAINT usuario_centros_historial_pkey PRIMARY KEY (id_historial);


--
-- Name: usuario_centros usuario_centros_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros
    ADD CONSTRAINT usuario_centros_pkey PRIMARY KEY (id_usuario, id_centro);


--
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id_usuario);


--
-- Name: idx_ajustes_detalle_ajuste; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ajustes_detalle_ajuste ON public.ajustes_detalle USING btree (id_ajuste);


--
-- Name: idx_almacenes_centro; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_almacenes_centro ON public.almacenes USING btree (id_centro);


--
-- Name: idx_bitacora_detalle_cab; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_bitacora_detalle_cab ON public.bitacora_detalle USING btree (id_bitacora);


--
-- Name: idx_bitacora_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_bitacora_fecha ON public.bitacora USING btree (fecha);


--
-- Name: idx_bitacora_registro; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_bitacora_registro ON public.bitacora USING btree (tabla, id_registro);


--
-- Name: idx_bitacora_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_bitacora_usuario ON public.bitacora USING btree (id_usuario, fecha);


--
-- Name: idx_centros_costo_centro; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_centros_costo_centro ON public.centros_costo USING btree (id_centro);


--
-- Name: idx_compras_centro_destino; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_compras_centro_destino ON public.compras USING btree (id_centro_destino);


--
-- Name: idx_compras_comprador; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_compras_comprador ON public.compras USING btree (id_comprador);


--
-- Name: idx_compras_proveedor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_compras_proveedor ON public.compras USING btree (id_proveedor);


--
-- Name: idx_compras_solicitante; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_compras_solicitante ON public.compras USING btree (id_usuario_solicitante);


--
-- Name: idx_detalle_compra_compra; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_compra_compra ON public.detalle_compra USING btree (id_compra);


--
-- Name: idx_detalle_compra_equipo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_compra_equipo ON public.detalle_compra USING btree (id_equipo);


--
-- Name: idx_detalle_compra_insumo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_compra_insumo ON public.detalle_compra USING btree (id_insumo);


--
-- Name: idx_detalle_compra_recibida; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_compra_recibida ON public.detalle_compra USING btree (cantidad_recibida);


--
-- Name: idx_detalle_ot_insumo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_ot_insumo ON public.detalle_ot USING btree (id_insumo);


--
-- Name: idx_detalle_ot_ot; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_ot_ot ON public.detalle_ot USING btree (id_ot);


--
-- Name: idx_detalle_ot_remision; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_ot_remision ON public.detalle_ot USING btree (id_remision);


--
-- Name: idx_equipos_almacen_actual; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_equipos_almacen_actual ON public.equipos USING btree (id_almacen_actual);


--
-- Name: idx_equipos_centro_actual; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_equipos_centro_actual ON public.equipos USING btree (id_centro_actual);


--
-- Name: idx_equipos_insumo_base; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_equipos_insumo_base ON public.equipos USING btree (id_insumo_base);


--
-- Name: idx_insumos_unidad_medida; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_insumos_unidad_medida ON public.insumos USING btree (id_unidad_medida);


--
-- Name: idx_mov_stock_compra; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mov_stock_compra ON public.movimientos_stock USING btree (id_compra);


--
-- Name: idx_mov_stock_insumo_almacen; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mov_stock_insumo_almacen ON public.movimientos_stock USING btree (id_insumo, id_almacen, fecha, id_mov);


--
-- Name: idx_mov_stock_ot; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mov_stock_ot ON public.movimientos_stock USING btree (id_ot);


--
-- Name: idx_movimientos_equipo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_movimientos_equipo ON public.movimientos_activos USING btree (id_equipo);


--
-- Name: idx_movimientos_equipo_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_movimientos_equipo_fecha ON public.movimientos_activos USING btree (id_equipo, fecha_movimiento, id_movimiento);


--
-- Name: idx_movimientos_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_movimientos_fecha ON public.movimientos_activos USING btree (fecha_movimiento);


--
-- Name: idx_ot_centro; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_centro ON public.ot USING btree (id_centro);


--
-- Name: idx_ot_centro_costo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_centro_costo ON public.ot USING btree (id_centro_costo);


--
-- Name: idx_ot_eventos_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_eventos_fecha ON public.ot_eventos USING btree (fecha);


--
-- Name: idx_ot_eventos_ot; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_eventos_ot ON public.ot_eventos USING btree (id_ot, fecha);


--
-- Name: idx_ot_responsable; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_responsable ON public.ot USING btree (id_usuario_responsable);


--
-- Name: idx_ot_solicitante; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_solicitante ON public.ot USING btree (id_usuario_solic);


--
-- Name: idx_ot_usuario_solic; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ot_usuario_solic ON public.ot USING btree (id_usuario_solic);


--
-- Name: idx_recepciones_oc_compra; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recepciones_oc_compra ON public.recepciones_oc USING btree (id_compra);


--
-- Name: idx_recepciones_oc_det_detalle; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recepciones_oc_det_detalle ON public.recepciones_oc_detalle USING btree (id_detalle_compra);


--
-- Name: idx_recepciones_oc_det_equipo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recepciones_oc_det_equipo ON public.recepciones_oc_detalle USING btree (id_equipo);


--
-- Name: idx_recepciones_oc_det_recepcion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recepciones_oc_det_recepcion ON public.recepciones_oc_detalle USING btree (id_recepcion);


--
-- Name: idx_remisiones_detalle_remision; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_remisiones_detalle_remision ON public.remisiones_detalle USING btree (id_remision);


--
-- Name: idx_remisiones_movimiento; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_remisiones_movimiento ON public.remisiones USING btree (id_movimiento);


--
-- Name: idx_remisiones_ot; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_remisiones_ot ON public.remisiones USING btree (id_ot);


--
-- Name: idx_remisiones_solicitud_activo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_remisiones_solicitud_activo ON public.remisiones USING btree (id_solicitud_activo);


--
-- Name: idx_remisiones_transferencia; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_remisiones_transferencia ON public.remisiones USING btree (id_transferencia);


--
-- Name: idx_sol_activos_detalle_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sol_activos_detalle_estado ON public.solicitudes_activos_detalle USING btree (estado_linea);


--
-- Name: idx_sol_activos_solicita; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sol_activos_solicita ON public.solicitudes_activos USING btree (id_usuario_solicita);


--
-- Name: idx_sol_det_centro_origen; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sol_det_centro_origen ON public.solicitudes_activos_detalle USING btree (id_centro_origen);


--
-- Name: idx_sol_det_solicitud; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sol_det_solicitud ON public.solicitudes_activos_detalle USING btree (id_solicitud);


--
-- Name: idx_solicitudes_activos_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_solicitudes_activos_estado ON public.solicitudes_activos USING btree (estado);


--
-- Name: idx_solicitudes_activos_solicita; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_solicitudes_activos_solicita ON public.solicitudes_activos USING btree (id_usuario_solicita, fecha_resolucion);


--
-- Name: idx_stock_almacen_almacen; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_stock_almacen_almacen ON public.stock_almacen USING btree (id_almacen);


--
-- Name: idx_stock_almacen_insumo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_stock_almacen_insumo ON public.stock_almacen USING btree (id_insumo);


--
-- Name: idx_transferencias_detalle_transf; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transferencias_detalle_transf ON public.transferencias_detalle USING btree (id_transferencia);


--
-- Name: idx_uch_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_uch_usuario ON public.usuario_centros_historial USING btree (id_usuario, fecha_registro);


--
-- Name: idx_usuarios_centro_asignado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_centro_asignado ON public.usuarios USING btree (centro_asignado);


--
-- Name: idx_usuarios_rol; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_rol ON public.usuarios USING btree (id_rol);


--
-- Name: ux_equipos_numero_serie; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX ux_equipos_numero_serie ON public.equipos USING btree (upper((numero_serie)::text)) WHERE (numero_serie IS NOT NULL);


--
-- Name: ux_solicitud_activo_abierta; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX ux_solicitud_activo_abierta ON public.solicitudes_activos_detalle USING btree (id_equipo) WHERE ((estado_linea)::text = ANY (ARRAY[('PENDIENTE'::character varying)::text, ('EN_CURSO'::character varying)::text]));


--
-- Name: ux_usuarios_email_lower; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX ux_usuarios_email_lower ON public.usuarios USING btree (lower((email)::text));


--
-- Name: ajustes_detalle ajustes_detalle_id_ajuste_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes_detalle
    ADD CONSTRAINT ajustes_detalle_id_ajuste_fkey FOREIGN KEY (id_ajuste) REFERENCES public.ajustes(id_ajuste) ON DELETE CASCADE;


--
-- Name: ajustes_detalle ajustes_detalle_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes_detalle
    ADD CONSTRAINT ajustes_detalle_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo);


--
-- Name: ajustes ajustes_id_almacen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacenes(id_almacen);


--
-- Name: ajustes ajustes_id_centro_costo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_id_centro_costo_fkey FOREIGN KEY (id_centro_costo) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: ajustes ajustes_id_centro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_id_centro_fkey FOREIGN KEY (id_centro) REFERENCES public.centros(id_centro);


--
-- Name: ajustes ajustes_id_usuario_anula_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_id_usuario_anula_fkey FOREIGN KEY (id_usuario_anula) REFERENCES public.usuarios(id_usuario);


--
-- Name: ajustes ajustes_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ajustes
    ADD CONSTRAINT ajustes_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- Name: almacenes almacenes_id_centro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.almacenes
    ADD CONSTRAINT almacenes_id_centro_fkey FOREIGN KEY (id_centro) REFERENCES public.centros(id_centro);


--
-- Name: bitacora_detalle bitacora_detalle_id_bitacora_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bitacora_detalle
    ADD CONSTRAINT bitacora_detalle_id_bitacora_fkey FOREIGN KEY (id_bitacora) REFERENCES public.bitacora(id_bitacora) ON DELETE CASCADE;


--
-- Name: bitacora bitacora_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bitacora
    ADD CONSTRAINT bitacora_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL;


--
-- Name: centros_costo centros_costo_id_centro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centros_costo
    ADD CONSTRAINT centros_costo_id_centro_fkey FOREIGN KEY (id_centro) REFERENCES public.centros(id_centro);


--
-- Name: compras compras_id_centro_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_id_centro_destino_fkey FOREIGN KEY (id_centro_destino) REFERENCES public.centros(id_centro);


--
-- Name: compras compras_id_comprador_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_id_comprador_fkey FOREIGN KEY (id_comprador) REFERENCES public.usuarios(id_usuario);


--
-- Name: compras compras_id_proveedor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_id_proveedor_fkey FOREIGN KEY (id_proveedor) REFERENCES public.proveedores(id_proveedor);


--
-- Name: compras compras_id_usuario_solicitante_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_id_usuario_solicitante_fkey FOREIGN KEY (id_usuario_solicitante) REFERENCES public.usuarios(id_usuario);


--
-- Name: detalle_compra detalle_compra_id_almacen_asignado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_almacen_asignado_fkey FOREIGN KEY (id_almacen_asignado) REFERENCES public.almacenes(id_almacen);


--
-- Name: detalle_compra detalle_compra_id_almacen_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_almacen_destino_fkey FOREIGN KEY (id_almacen_destino) REFERENCES public.almacenes(id_almacen);


--
-- Name: detalle_compra detalle_compra_id_centro_asignado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_centro_asignado_fkey FOREIGN KEY (id_centro_asignado) REFERENCES public.centros(id_centro);


--
-- Name: detalle_compra detalle_compra_id_centro_costo_asignado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_centro_costo_asignado_fkey FOREIGN KEY (id_centro_costo_asignado) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: detalle_compra detalle_compra_id_compra_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_compra_fkey FOREIGN KEY (id_compra) REFERENCES public.compras(id_compra) ON DELETE CASCADE;


--
-- Name: detalle_compra detalle_compra_id_equipo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_equipo_fkey FOREIGN KEY (id_equipo) REFERENCES public.equipos(id_equipo);


--
-- Name: detalle_compra detalle_compra_id_equipo_generado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_equipo_generado_fkey FOREIGN KEY (id_equipo_generado) REFERENCES public.equipos(id_equipo);


--
-- Name: detalle_compra detalle_compra_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_compra
    ADD CONSTRAINT detalle_compra_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo);


--
-- Name: detalle_ot detalle_ot_id_almacen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot
    ADD CONSTRAINT detalle_ot_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacenes(id_almacen);


--
-- Name: detalle_ot detalle_ot_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot
    ADD CONSTRAINT detalle_ot_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo);


--
-- Name: detalle_ot detalle_ot_id_ot_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot
    ADD CONSTRAINT detalle_ot_id_ot_fkey FOREIGN KEY (id_ot) REFERENCES public.ot(id_ot) ON DELETE CASCADE;


--
-- Name: detalle_ot detalle_ot_id_remision_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot
    ADD CONSTRAINT detalle_ot_id_remision_fkey FOREIGN KEY (id_remision) REFERENCES public.remisiones(id_remision);


--
-- Name: detalle_ot detalle_ot_id_usuario_suministra_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ot
    ADD CONSTRAINT detalle_ot_id_usuario_suministra_fkey FOREIGN KEY (id_usuario_suministra) REFERENCES public.usuarios(id_usuario);


--
-- Name: equipos equipos_id_almacen_actual_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_id_almacen_actual_fkey FOREIGN KEY (id_almacen_actual) REFERENCES public.almacenes(id_almacen);


--
-- Name: equipos equipos_id_centro_actual_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_id_centro_actual_fkey FOREIGN KEY (id_centro_actual) REFERENCES public.centros(id_centro);


--
-- Name: equipos equipos_id_centro_costo_actual_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_id_centro_costo_actual_fkey FOREIGN KEY (id_centro_costo_actual) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: equipos equipos_id_insumo_base_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_id_insumo_base_fkey FOREIGN KEY (id_insumo_base) REFERENCES public.insumos(id_insumo);


--
-- Name: equipos fk_equipos_ultimo_movimiento; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT fk_equipos_ultimo_movimiento FOREIGN KEY (id_ultimo_movimiento) REFERENCES public.movimientos_activos(id_movimiento) ON DELETE SET NULL;


--
-- Name: movimientos_activos fk_movimientos_activos_equipo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT fk_movimientos_activos_equipo FOREIGN KEY (id_equipo) REFERENCES public.equipos(id_equipo);


--
-- Name: movimientos_activos fk_movimientos_activos_remision; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT fk_movimientos_activos_remision FOREIGN KEY (id_remision) REFERENCES public.remisiones(id_remision) ON DELETE SET NULL;


--
-- Name: remisiones fk_remisiones_movimiento; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT fk_remisiones_movimiento FOREIGN KEY (id_movimiento) REFERENCES public.movimientos_activos(id_movimiento) ON DELETE SET NULL;


--
-- Name: usuarios fk_usuarios_centro; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_usuarios_centro FOREIGN KEY (centro_asignado) REFERENCES public.centros(id_centro);


--
-- Name: insumos insumos_id_unidad_medida_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insumos
    ADD CONSTRAINT insumos_id_unidad_medida_fkey FOREIGN KEY (id_unidad_medida) REFERENCES public.unidad_medida(id_unidad_medida);


--
-- Name: movimientos_activos movimientos_activos_id_almacen_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_almacen_destino_fkey FOREIGN KEY (id_almacen_destino) REFERENCES public.almacenes(id_almacen);


--
-- Name: movimientos_activos movimientos_activos_id_almacen_origen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_almacen_origen_fkey FOREIGN KEY (id_almacen_origen) REFERENCES public.almacenes(id_almacen);


--
-- Name: movimientos_activos movimientos_activos_id_centro_costo_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_centro_costo_destino_fkey FOREIGN KEY (id_centro_costo_destino) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: movimientos_activos movimientos_activos_id_centro_costo_origen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_centro_costo_origen_fkey FOREIGN KEY (id_centro_costo_origen) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: movimientos_activos movimientos_activos_id_centro_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_centro_destino_fkey FOREIGN KEY (id_centro_destino) REFERENCES public.centros(id_centro);


--
-- Name: movimientos_activos movimientos_activos_id_centro_origen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_centro_origen_fkey FOREIGN KEY (id_centro_origen) REFERENCES public.centros(id_centro);


--
-- Name: movimientos_activos movimientos_activos_id_usuario_emisor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_usuario_emisor_fkey FOREIGN KEY (id_usuario_emisor) REFERENCES public.usuarios(id_usuario);


--
-- Name: movimientos_activos movimientos_activos_id_usuario_receptor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_usuario_receptor_fkey FOREIGN KEY (id_usuario_receptor) REFERENCES public.usuarios(id_usuario);


--
-- Name: movimientos_activos movimientos_activos_id_usuario_registro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_activos
    ADD CONSTRAINT movimientos_activos_id_usuario_registro_fkey FOREIGN KEY (id_usuario_registro) REFERENCES public.usuarios(id_usuario);


--
-- Name: movimientos_stock movimientos_stock_id_ajuste_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_ajuste_fkey FOREIGN KEY (id_ajuste) REFERENCES public.ajustes(id_ajuste);


--
-- Name: movimientos_stock movimientos_stock_id_almacen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacenes(id_almacen);


--
-- Name: movimientos_stock movimientos_stock_id_centro_costo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_centro_costo_fkey FOREIGN KEY (id_centro_costo) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: movimientos_stock movimientos_stock_id_compra_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_compra_fkey FOREIGN KEY (id_compra) REFERENCES public.compras(id_compra);


--
-- Name: movimientos_stock movimientos_stock_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo);


--
-- Name: movimientos_stock movimientos_stock_id_ot_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_ot_fkey FOREIGN KEY (id_ot) REFERENCES public.ot(id_ot) ON DELETE SET NULL;


--
-- Name: movimientos_stock movimientos_stock_id_transferencia_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_transferencia_fkey FOREIGN KEY (id_transferencia) REFERENCES public.transferencias(id_transferencia);


--
-- Name: movimientos_stock movimientos_stock_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimientos_stock
    ADD CONSTRAINT movimientos_stock_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- Name: ot_eventos ot_eventos_id_ot_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot_eventos
    ADD CONSTRAINT ot_eventos_id_ot_fkey FOREIGN KEY (id_ot) REFERENCES public.ot(id_ot) ON DELETE CASCADE;


--
-- Name: ot_eventos ot_eventos_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot_eventos
    ADD CONSTRAINT ot_eventos_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- Name: ot ot_id_centro_costo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_id_centro_costo_fkey FOREIGN KEY (id_centro_costo) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: ot ot_id_centro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_id_centro_fkey FOREIGN KEY (id_centro) REFERENCES public.centros(id_centro);


--
-- Name: ot ot_id_usuario_anula_fin_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_id_usuario_anula_fin_fkey FOREIGN KEY (id_usuario_anula_fin) REFERENCES public.usuarios(id_usuario);


--
-- Name: ot ot_id_usuario_finaliza_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_id_usuario_finaliza_fkey FOREIGN KEY (id_usuario_finaliza) REFERENCES public.usuarios(id_usuario);


--
-- Name: ot ot_id_usuario_responsable_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_id_usuario_responsable_fkey FOREIGN KEY (id_usuario_responsable) REFERENCES public.usuarios(id_usuario);


--
-- Name: ot ot_id_usuario_solic_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ot
    ADD CONSTRAINT ot_id_usuario_solic_fkey FOREIGN KEY (id_usuario_solic) REFERENCES public.usuarios(id_usuario);


--
-- Name: recepciones_oc_detalle recepciones_oc_detalle_id_almacen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle
    ADD CONSTRAINT recepciones_oc_detalle_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacenes(id_almacen);


--
-- Name: recepciones_oc_detalle recepciones_oc_detalle_id_centro_costo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle
    ADD CONSTRAINT recepciones_oc_detalle_id_centro_costo_fkey FOREIGN KEY (id_centro_costo) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: recepciones_oc_detalle recepciones_oc_detalle_id_detalle_compra_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle
    ADD CONSTRAINT recepciones_oc_detalle_id_detalle_compra_fkey FOREIGN KEY (id_detalle_compra) REFERENCES public.detalle_compra(id_detalle_compra);


--
-- Name: recepciones_oc_detalle recepciones_oc_detalle_id_equipo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle
    ADD CONSTRAINT recepciones_oc_detalle_id_equipo_fkey FOREIGN KEY (id_equipo) REFERENCES public.equipos(id_equipo);


--
-- Name: recepciones_oc_detalle recepciones_oc_detalle_id_recepcion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc_detalle
    ADD CONSTRAINT recepciones_oc_detalle_id_recepcion_fkey FOREIGN KEY (id_recepcion) REFERENCES public.recepciones_oc(id_recepcion) ON DELETE CASCADE;


--
-- Name: recepciones_oc recepciones_oc_id_compra_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc
    ADD CONSTRAINT recepciones_oc_id_compra_fkey FOREIGN KEY (id_compra) REFERENCES public.compras(id_compra);


--
-- Name: recepciones_oc recepciones_oc_id_usuario_anula_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc
    ADD CONSTRAINT recepciones_oc_id_usuario_anula_fkey FOREIGN KEY (id_usuario_anula) REFERENCES public.usuarios(id_usuario);


--
-- Name: recepciones_oc recepciones_oc_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recepciones_oc
    ADD CONSTRAINT recepciones_oc_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- Name: remisiones_detalle remisiones_detalle_id_almacen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones_detalle
    ADD CONSTRAINT remisiones_detalle_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacenes(id_almacen);


--
-- Name: remisiones_detalle remisiones_detalle_id_equipo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones_detalle
    ADD CONSTRAINT remisiones_detalle_id_equipo_fkey FOREIGN KEY (id_equipo) REFERENCES public.equipos(id_equipo);


--
-- Name: remisiones_detalle remisiones_detalle_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones_detalle
    ADD CONSTRAINT remisiones_detalle_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo);


--
-- Name: remisiones_detalle remisiones_detalle_id_remision_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones_detalle
    ADD CONSTRAINT remisiones_detalle_id_remision_fkey FOREIGN KEY (id_remision) REFERENCES public.remisiones(id_remision) ON DELETE CASCADE;


--
-- Name: remisiones remisiones_id_ot_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_id_ot_fkey FOREIGN KEY (id_ot) REFERENCES public.ot(id_ot);


--
-- Name: remisiones remisiones_id_solicitud_activo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_id_solicitud_activo_fkey FOREIGN KEY (id_solicitud_activo) REFERENCES public.solicitudes_activos(id_solicitud);


--
-- Name: remisiones remisiones_id_transferencia_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_id_transferencia_fkey FOREIGN KEY (id_transferencia) REFERENCES public.transferencias(id_transferencia);


--
-- Name: remisiones remisiones_id_usuario_anula_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_id_usuario_anula_fkey FOREIGN KEY (id_usuario_anula) REFERENCES public.usuarios(id_usuario);


--
-- Name: remisiones remisiones_id_usuario_emite_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.remisiones
    ADD CONSTRAINT remisiones_id_usuario_emite_fkey FOREIGN KEY (id_usuario_emite) REFERENCES public.usuarios(id_usuario);


--
-- Name: solicitudes_activos_detalle solicitudes_activos_detalle_id_centro_costo_origen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle
    ADD CONSTRAINT solicitudes_activos_detalle_id_centro_costo_origen_fkey FOREIGN KEY (id_centro_costo_origen) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: solicitudes_activos_detalle solicitudes_activos_detalle_id_centro_origen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle
    ADD CONSTRAINT solicitudes_activos_detalle_id_centro_origen_fkey FOREIGN KEY (id_centro_origen) REFERENCES public.centros(id_centro);


--
-- Name: solicitudes_activos_detalle solicitudes_activos_detalle_id_equipo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle
    ADD CONSTRAINT solicitudes_activos_detalle_id_equipo_fkey FOREIGN KEY (id_equipo) REFERENCES public.equipos(id_equipo);


--
-- Name: solicitudes_activos_detalle solicitudes_activos_detalle_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle
    ADD CONSTRAINT solicitudes_activos_detalle_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitudes_activos(id_solicitud) ON DELETE CASCADE;


--
-- Name: solicitudes_activos_detalle solicitudes_activos_detalle_id_usuario_recibe_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos_detalle
    ADD CONSTRAINT solicitudes_activos_detalle_id_usuario_recibe_fkey FOREIGN KEY (id_usuario_recibe) REFERENCES public.usuarios(id_usuario);


--
-- Name: solicitudes_activos solicitudes_activos_id_centro_costo_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_centro_costo_destino_fkey FOREIGN KEY (id_centro_costo_destino) REFERENCES public.centros_costo(id_centro_costo);


--
-- Name: solicitudes_activos solicitudes_activos_id_centro_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_centro_destino_fkey FOREIGN KEY (id_centro_destino) REFERENCES public.centros(id_centro);


--
-- Name: solicitudes_activos solicitudes_activos_id_ot_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_ot_fkey FOREIGN KEY (id_ot) REFERENCES public.ot(id_ot);


--
-- Name: solicitudes_activos solicitudes_activos_id_proveedor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_proveedor_fkey FOREIGN KEY (id_proveedor) REFERENCES public.proveedores(id_proveedor);


--
-- Name: solicitudes_activos solicitudes_activos_id_remision_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_remision_fkey FOREIGN KEY (id_remision) REFERENCES public.remisiones(id_remision);


--
-- Name: solicitudes_activos solicitudes_activos_id_usuario_anula_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_usuario_anula_fkey FOREIGN KEY (id_usuario_anula) REFERENCES public.usuarios(id_usuario);


--
-- Name: solicitudes_activos solicitudes_activos_id_usuario_resuelve_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_usuario_resuelve_fkey FOREIGN KEY (id_usuario_resuelve) REFERENCES public.usuarios(id_usuario);


--
-- Name: solicitudes_activos solicitudes_activos_id_usuario_solicita_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_activos
    ADD CONSTRAINT solicitudes_activos_id_usuario_solicita_fkey FOREIGN KEY (id_usuario_solicita) REFERENCES public.usuarios(id_usuario);


--
-- Name: stock_almacen stock_almacen_id_almacen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_almacen
    ADD CONSTRAINT stock_almacen_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacenes(id_almacen) ON DELETE CASCADE;


--
-- Name: stock_almacen stock_almacen_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_almacen
    ADD CONSTRAINT stock_almacen_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo) ON DELETE CASCADE;


--
-- Name: transferencias_detalle transferencias_detalle_id_insumo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias_detalle
    ADD CONSTRAINT transferencias_detalle_id_insumo_fkey FOREIGN KEY (id_insumo) REFERENCES public.insumos(id_insumo);


--
-- Name: transferencias_detalle transferencias_detalle_id_transferencia_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias_detalle
    ADD CONSTRAINT transferencias_detalle_id_transferencia_fkey FOREIGN KEY (id_transferencia) REFERENCES public.transferencias(id_transferencia) ON DELETE CASCADE;


--
-- Name: transferencias transferencias_id_almacen_destino_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias
    ADD CONSTRAINT transferencias_id_almacen_destino_fkey FOREIGN KEY (id_almacen_destino) REFERENCES public.almacenes(id_almacen);


--
-- Name: transferencias transferencias_id_almacen_origen_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias
    ADD CONSTRAINT transferencias_id_almacen_origen_fkey FOREIGN KEY (id_almacen_origen) REFERENCES public.almacenes(id_almacen);


--
-- Name: transferencias transferencias_id_usuario_anula_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias
    ADD CONSTRAINT transferencias_id_usuario_anula_fkey FOREIGN KEY (id_usuario_anula) REFERENCES public.usuarios(id_usuario);


--
-- Name: transferencias transferencias_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencias
    ADD CONSTRAINT transferencias_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario);


--
-- Name: usuario_centros_historial usuario_centros_historial_id_centro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros_historial
    ADD CONSTRAINT usuario_centros_historial_id_centro_fkey FOREIGN KEY (id_centro) REFERENCES public.centros(id_centro);


--
-- Name: usuario_centros_historial usuario_centros_historial_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros_historial
    ADD CONSTRAINT usuario_centros_historial_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE;


--
-- Name: usuario_centros_historial usuario_centros_historial_id_usuario_registra_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros_historial
    ADD CONSTRAINT usuario_centros_historial_id_usuario_registra_fkey FOREIGN KEY (id_usuario_registra) REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL;


--
-- Name: usuario_centros usuario_centros_id_centro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros
    ADD CONSTRAINT usuario_centros_id_centro_fkey FOREIGN KEY (id_centro) REFERENCES public.centros(id_centro);


--
-- Name: usuario_centros usuario_centros_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_centros
    ADD CONSTRAINT usuario_centros_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE;


--
-- Name: usuarios usuarios_centro_asignado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_centro_asignado_fkey FOREIGN KEY (centro_asignado) REFERENCES public.centros(id_centro);


--
-- Name: usuarios usuarios_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.roles(id_rol);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT USAGE ON SCHEMA public TO nanday_app;


--
-- Name: TABLE ajustes; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ajustes TO nanday_app;


--
-- Name: TABLE ajustes_detalle; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ajustes_detalle TO nanday_app;


--
-- Name: SEQUENCE ajustes_detalle_id_detalle_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ajustes_detalle_id_detalle_seq TO nanday_app;


--
-- Name: SEQUENCE ajustes_id_ajuste_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ajustes_id_ajuste_seq TO nanday_app;


--
-- Name: TABLE almacenes; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.almacenes TO nanday_app;


--
-- Name: SEQUENCE almacenes_id_almacen_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.almacenes_id_almacen_seq TO nanday_app;


--
-- Name: TABLE bitacora; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT ON TABLE public.bitacora TO nanday_app;


--
-- Name: TABLE bitacora_detalle; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT ON TABLE public.bitacora_detalle TO nanday_app;


--
-- Name: SEQUENCE bitacora_detalle_id_detalle_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.bitacora_detalle_id_detalle_seq TO nanday_app;


--
-- Name: SEQUENCE bitacora_id_bitacora_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.bitacora_id_bitacora_seq TO nanday_app;


--
-- Name: TABLE centros; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.centros TO nanday_app;


--
-- Name: TABLE centros_costo; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.centros_costo TO nanday_app;


--
-- Name: SEQUENCE centros_costo_id_centro_costo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.centros_costo_id_centro_costo_seq TO nanday_app;


--
-- Name: SEQUENCE centros_id_centro_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.centros_id_centro_seq TO nanday_app;


--
-- Name: TABLE compras; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.compras TO nanday_app;


--
-- Name: SEQUENCE compras_id_compra_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.compras_id_compra_seq TO nanday_app;


--
-- Name: TABLE correlativo_oc; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.correlativo_oc TO nanday_app;


--
-- Name: TABLE correlativo_ot; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.correlativo_ot TO nanday_app;


--
-- Name: TABLE detalle_compra; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.detalle_compra TO nanday_app;


--
-- Name: SEQUENCE detalle_compra_id_detalle_compra_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.detalle_compra_id_detalle_compra_seq TO nanday_app;


--
-- Name: TABLE detalle_ot; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.detalle_ot TO nanday_app;


--
-- Name: SEQUENCE detalle_ot_id_detalle_ot_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.detalle_ot_id_detalle_ot_seq TO nanday_app;


--
-- Name: TABLE equipos; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.equipos TO nanday_app;


--
-- Name: SEQUENCE equipos_id_equipo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.equipos_id_equipo_seq TO nanday_app;


--
-- Name: TABLE insumos; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.insumos TO nanday_app;


--
-- Name: SEQUENCE insumos_id_insumo_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.insumos_id_insumo_seq TO nanday_app;


--
-- Name: SEQUENCE seq_numero_movimiento; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_numero_movimiento TO nanday_app;


--
-- Name: TABLE movimientos_activos; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.movimientos_activos TO nanday_app;


--
-- Name: SEQUENCE movimientos_activos_id_movimiento_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.movimientos_activos_id_movimiento_seq TO nanday_app;


--
-- Name: TABLE movimientos_stock; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.movimientos_stock TO nanday_app;


--
-- Name: SEQUENCE movimientos_stock_id_mov_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.movimientos_stock_id_mov_seq TO nanday_app;


--
-- Name: TABLE ot; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ot TO nanday_app;


--
-- Name: TABLE ot_eventos; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ot_eventos TO nanday_app;


--
-- Name: SEQUENCE ot_eventos_id_evento_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ot_eventos_id_evento_seq TO nanday_app;


--
-- Name: SEQUENCE ot_id_ot_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.ot_id_ot_seq TO nanday_app;


--
-- Name: TABLE proveedores; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.proveedores TO nanday_app;


--
-- Name: SEQUENCE proveedores_id_proveedor_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.proveedores_id_proveedor_seq TO nanday_app;


--
-- Name: TABLE recepciones_oc; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.recepciones_oc TO nanday_app;


--
-- Name: TABLE recepciones_oc_detalle; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.recepciones_oc_detalle TO nanday_app;


--
-- Name: SEQUENCE recepciones_oc_detalle_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.recepciones_oc_detalle_id_seq TO nanday_app;


--
-- Name: SEQUENCE recepciones_oc_id_recepcion_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.recepciones_oc_id_recepcion_seq TO nanday_app;


--
-- Name: TABLE remisiones; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.remisiones TO nanday_app;


--
-- Name: TABLE remisiones_detalle; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.remisiones_detalle TO nanday_app;


--
-- Name: SEQUENCE remisiones_detalle_id_detalle_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.remisiones_detalle_id_detalle_seq TO nanday_app;


--
-- Name: SEQUENCE remisiones_id_remision_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.remisiones_id_remision_seq TO nanday_app;


--
-- Name: TABLE roles; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.roles TO nanday_app;


--
-- Name: SEQUENCE roles_id_rol_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.roles_id_rol_seq TO nanday_app;


--
-- Name: SEQUENCE seq_ajuste; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_ajuste TO nanday_app;


--
-- Name: SEQUENCE seq_baja_activo; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_baja_activo TO nanday_app;


--
-- Name: SEQUENCE seq_numero_activo; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_numero_activo TO nanday_app;


--
-- Name: SEQUENCE seq_remision; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_remision TO nanday_app;


--
-- Name: SEQUENCE seq_solicitud_activo; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_solicitud_activo TO nanday_app;


--
-- Name: SEQUENCE seq_transferencia; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.seq_transferencia TO nanday_app;


--
-- Name: TABLE solicitudes_activos; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.solicitudes_activos TO nanday_app;


--
-- Name: TABLE solicitudes_activos_detalle; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.solicitudes_activos_detalle TO nanday_app;


--
-- Name: SEQUENCE solicitudes_activos_detalle_id_detalle_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.solicitudes_activos_detalle_id_detalle_seq TO nanday_app;


--
-- Name: SEQUENCE solicitudes_activos_id_solicitud_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.solicitudes_activos_id_solicitud_seq TO nanday_app;


--
-- Name: TABLE stock_almacen; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.stock_almacen TO nanday_app;


--
-- Name: SEQUENCE stock_almacen_id_stock_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.stock_almacen_id_stock_seq TO nanday_app;


--
-- Name: TABLE transferencias; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.transferencias TO nanday_app;


--
-- Name: TABLE transferencias_detalle; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.transferencias_detalle TO nanday_app;


--
-- Name: SEQUENCE transferencias_detalle_id_detalle_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.transferencias_detalle_id_detalle_seq TO nanday_app;


--
-- Name: SEQUENCE transferencias_id_transferencia_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.transferencias_id_transferencia_seq TO nanday_app;


--
-- Name: TABLE unidad_medida; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.unidad_medida TO nanday_app;


--
-- Name: SEQUENCE unidad_medida_id_unidad_medida_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.unidad_medida_id_unidad_medida_seq TO nanday_app;


--
-- Name: TABLE usuario_centros; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.usuario_centros TO nanday_app;


--
-- Name: TABLE usuario_centros_historial; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.usuario_centros_historial TO nanday_app;


--
-- Name: SEQUENCE usuario_centros_historial_id_historial_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.usuario_centros_historial_id_historial_seq TO nanday_app;


--
-- Name: TABLE usuarios; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.usuarios TO nanday_app;


--
-- Name: SEQUENCE usuarios_id_usuario_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.usuarios_id_usuario_seq TO nanday_app;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO nanday_app;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT SELECT,INSERT,DELETE,UPDATE ON TABLES TO nanday_app;


--
-- PostgreSQL database dump complete
--



-- Tablas y secuencias que se creen más adelante también quedan habilitadas para nanday_app
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO nanday_app;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO nanday_app;

-- Resumen
SELECT (SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE') AS tablas,
       (SELECT count(*) FROM public.usuarios) AS usuarios,
       (SELECT count(*) FROM public.proveedores) AS proveedores,
       (SELECT count(*) FROM public.insumos) AS insumos,
       (SELECT count(*) FROM public.equipos) AS activos,
       (SELECT count(*) FROM public.stock_almacen) AS registros_stock;
