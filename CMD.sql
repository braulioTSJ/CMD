-- =========================================================
-- PROYECTO: CMD (Cartilla Médica Digital)
-- BASE DE DATOS: cmd_db  
-- MOTOR: MySQL 8.0.16+
-- =========================================================

-- 1. Base de datos
-- OJO: DROP DATABASE solo para desarrollo. Nunca ejecutar en producción.
DROP DATABASE IF EXISTS cmd_db;
CREATE DATABASE cmd_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cmd_db;

SET time_zone = '+00:00';  -- guardar todo en UTC; convertir en la app

-- =========================================================
-- 2. CATÁLOGOS
-- =========================================================

CREATE TABLE cat_roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE cat_parentescos (
    id_parentesco INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE cat_tipos_sangre (
    id_tipo_sangre INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE cat_vacunas (
    id_cat_vacuna INT AUTO_INCREMENT PRIMARY KEY,
    nombre_vacuna VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE cat_especialidades (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,
    nombre_especialidad VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE cat_tipos_documento (
    id_tipo_documento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- =========================================================
-- 3. USUARIOS, SEGURIDAD Y PERFILES
-- =========================================================

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    correo_electronico VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,          -- bcrypt/argon2, NUNCA texto plano
    telefono VARCHAR(20),
    id_rol INT NOT NULL,
    twofa_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    twofa_secreto VARBINARY(255) NULL,              -- si usas TOTP: guardar CIFRADO desde la app
    estado ENUM('Activo','Inactivo','Bloqueado') NOT NULL DEFAULT 'Activo',
    intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,   -- anti fuerza bruta
    bloqueado_hasta DATETIME NULL,
    ultimo_login DATETIME NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,                       -- borrado lógico
    CONSTRAINT chk_usuario_correo CHECK (correo_electronico LIKE '%_@_%._%'),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES cat_roles(id_rol) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Tokens OTP: se guarda el HASH del código, no el código
CREATE TABLE tokens_2fa (
    id_token INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    codigo_otp_hash CHAR(64) NOT NULL,              -- SHA-256 (hex) del OTP (+ salt/pepper en la app)
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    intentos TINYINT UNSIGNED NOT NULL DEFAULT 0,   -- invalidar tras 3-5 intentos
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_otp_exp CHECK (fecha_expiracion > fecha_creacion),
    CONSTRAINT fk_otp_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_otp_busqueda (id_usuario, usado, fecha_expiracion)
) ENGINE=InnoDB;

-- Sesiones / refresh tokens (para la app Android), solo se guarda el hash
CREATE TABLE sesiones (
    id_sesion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    refresh_token_hash CHAR(64) NOT NULL UNIQUE,
    dispositivo VARCHAR(150),
    ip_origen VARCHAR(45),                          -- cubre IPv6
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    revocada BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sesion_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_sesion_usuario (id_usuario, revocada, fecha_expiracion)
) ENGINE=InnoDB;

-- Recuperación de contraseña
CREATE TABLE tokens_recuperacion (
    id_token INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    fecha_expiracion DATETIME NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_rec_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE perfiles_familiares (
    id_perfil INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE NOT NULL,                 -- validada por trigger (no futura)
    genero ENUM('Masculino','Femenino','Otro') NOT NULL,
    id_tipo_sangre INT NULL,
    id_parentesco INT NOT NULL,
    es_titular BOOLEAN NOT NULL DEFAULT FALSE,
    -- Columna generada: garantiza UN solo titular por usuario (NULL no choca en UNIQUE)
    titular_unico INT GENERATED ALWAYS AS (IF(es_titular, id_usuario, NULL)) STORED,
    foto_url VARCHAR(255),                          -- idealmente ruta relativa, no URL pública
    alergias TEXT,
    antecedentes TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    UNIQUE KEY uq_un_titular_por_usuario (titular_unico),
    UNIQUE KEY uq_perfil_usuario (id_perfil, id_usuario),   -- apoyo para autorización
    INDEX idx_perfil_usuario (id_usuario, deleted_at),
    -- RESTRICT: evita perder historiales médicos por borrar un usuario por error
    CONSTRAINT fk_perfil_usuario    FOREIGN KEY (id_usuario)     REFERENCES usuarios(id_usuario)         ON DELETE RESTRICT,
    CONSTRAINT fk_perfil_parentesco FOREIGN KEY (id_parentesco)  REFERENCES cat_parentescos(id_parentesco) ON DELETE RESTRICT,
    CONSTRAINT fk_perfil_sangre     FOREIGN KEY (id_tipo_sangre) REFERENCES cat_tipos_sangre(id_tipo_sangre) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =========================================================
-- 4. HISTORIAL MÉDICO Y RECETAS
-- =========================================================

CREATE TABLE eventos_medicos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil INT NOT NULL,
    fecha_evento DATE NOT NULL,
    id_especialidad INT NULL,
    medico_tratante VARCHAR(150),
    institucion VARCHAR(150),
    motivo_consulta TEXT NOT NULL,
    diagnostico TEXT,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    UNIQUE KEY uq_evento_perfil (id_evento, id_perfil),     -- para FK compuesta desde recetas
    INDEX idx_evento_perfil_fecha (id_perfil, fecha_evento),
    CONSTRAINT fk_evento_perfil FOREIGN KEY (id_perfil) REFERENCES perfiles_familiares(id_perfil) ON DELETE RESTRICT,
    CONSTRAINT fk_evento_espec  FOREIGN KEY (id_especialidad) REFERENCES cat_especialidades(id_especialidad) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE recetas (
    id_receta INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil INT NOT NULL,
    id_evento INT NULL,
    medico_emisor VARCHAR(150) NOT NULL,
    fecha_emision DATE NOT NULL,
    url_imagen_original VARCHAR(255),               -- ML Kit / OCR
    origen ENUM('Manual','OCR') NOT NULL DEFAULT 'Manual',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    INDEX idx_receta_perfil_fecha (id_perfil, fecha_emision),
    CONSTRAINT fk_receta_perfil FOREIGN KEY (id_perfil) REFERENCES perfiles_familiares(id_perfil) ON DELETE RESTRICT,
    -- FK compuesta: la receta solo puede ligarse a un evento DEL MISMO perfil
    CONSTRAINT fk_receta_evento FOREIGN KEY (id_evento, id_perfil)
        REFERENCES eventos_medicos(id_evento, id_perfil) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE detalle_medicamentos (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_receta INT NOT NULL,
    nombre_medicamento VARCHAR(150) NOT NULL,
    dosis VARCHAR(100) NOT NULL,
    frecuencia VARCHAR(100) NOT NULL,
    duracion VARCHAR(100),
    notas VARCHAR(255),
    CONSTRAINT fk_detalle_receta FOREIGN KEY (id_receta) REFERENCES recetas(id_receta) ON DELETE CASCADE,
    INDEX idx_detalle_receta (id_receta)
) ENGINE=InnoDB;

-- =========================================================
-- 5. DOCUMENTOS Y VACUNACIÓN
-- =========================================================

CREATE TABLE documentos_clinicos (
    id_documento INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil INT NOT NULL,
    id_tipo_documento INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    fecha_documento DATE NOT NULL,
    url_archivo VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),                         -- p. ej. application/pdf, image/jpeg
    tamano_bytes BIGINT UNSIGNED,
    sha256 CHAR(64),                                -- integridad / detectar duplicados
    fecha_subida DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    INDEX idx_doc_perfil_fecha (id_perfil, fecha_documento),
    CONSTRAINT fk_doc_perfil FOREIGN KEY (id_perfil) REFERENCES perfiles_familiares(id_perfil) ON DELETE RESTRICT,
    CONSTRAINT fk_doc_tipo   FOREIGN KEY (id_tipo_documento) REFERENCES cat_tipos_documento(id_tipo_documento) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE vacunacion (
    id_vacunacion INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil INT NOT NULL,
    id_cat_vacuna INT NOT NULL,
    numero_dosis TINYINT UNSIGNED NOT NULL DEFAULT 1,  -- 1ª, 2ª, refuerzo...
    fecha_aplicacion DATE NULL,
    dosis_refuerzo VARCHAR(50),
    lote VARCHAR(50),
    centro_salud VARCHAR(150),
    estatus ENUM('Aplicada','Pendiente','Programada') NOT NULL DEFAULT 'Aplicada',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    -- Una vacuna "Aplicada" debe tener fecha
    CONSTRAINT chk_vac_fecha CHECK (estatus <> 'Aplicada' OR fecha_aplicacion IS NOT NULL),
    -- Evita registrar dos veces la misma dosis de la misma vacuna
    UNIQUE KEY uq_vac_dosis (id_perfil, id_cat_vacuna, numero_dosis),
    CONSTRAINT fk_vac_perfil FOREIGN KEY (id_perfil) REFERENCES perfiles_familiares(id_perfil) ON DELETE RESTRICT,
    CONSTRAINT fk_vac_cat    FOREIGN KEY (id_cat_vacuna) REFERENCES cat_vacunas(id_cat_vacuna) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =========================================================
-- 6. AUDITORÍA (clave en datos de salud: quién vio/modificó qué)
-- =========================================================

CREATE TABLE bitacora (
    id_bitacora BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NULL,
    accion VARCHAR(50) NOT NULL,                    -- LOGIN, LOGIN_FALLIDO, VER_PERFIL, EDITAR, EXPORTAR...
    entidad VARCHAR(50),
    id_entidad BIGINT,
    ip_origen VARCHAR(45),
    detalle JSON NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bit_usuario_fecha (id_usuario, fecha),
    INDEX idx_bit_entidad (entidad, id_entidad)
) ENGINE=InnoDB;

-- =========================================================
-- 7. TRIGGERS DE VALIDACIÓN (los CHECK no permiten CURDATE())
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_perfil_bi BEFORE INSERT ON perfiles_familiares
FOR EACH ROW
BEGIN
    IF NEW.fecha_nacimiento > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La fecha de nacimiento no puede ser futura';
    END IF;
END$$

CREATE TRIGGER trg_perfil_bu BEFORE UPDATE ON perfiles_familiares
FOR EACH ROW
BEGIN
    IF NEW.fecha_nacimiento > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La fecha de nacimiento no puede ser futura';
    END IF;
END$$

CREATE TRIGGER trg_evento_bi BEFORE INSERT ON eventos_medicos
FOR EACH ROW
BEGIN
    DECLARE v_nac DATE;
    SELECT fecha_nacimiento INTO v_nac FROM perfiles_familiares WHERE id_perfil = NEW.id_perfil;
    IF NEW.fecha_evento < v_nac THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El evento no puede ser anterior al nacimiento del perfil';
    END IF;
END$$

DELIMITER ;

-- =========================================================
-- 8. VISTA DE RESUMEN (sin subconsultas correlacionadas, respeta borrado lógico)
-- =========================================================

CREATE SQL SECURITY INVOKER VIEW vw_resumen_medico AS
SELECT
    p.id_perfil,
    p.id_usuario,
    p.nombre_completo,
    c.descripcion AS parentesco,
    ts.tipo       AS tipo_sangre,
    p.alergias,
    p.antecedentes,
    COALESCE(e.total, 0) AS total_eventos,
    COALESCE(r.total, 0) AS total_recetas,
    COALESCE(v.total, 0) AS total_vacunas
FROM perfiles_familiares p
JOIN cat_parentescos c ON c.id_parentesco = p.id_parentesco
LEFT JOIN cat_tipos_sangre ts ON ts.id_tipo_sangre = p.id_tipo_sangre
LEFT JOIN (SELECT id_perfil, COUNT(*) total FROM eventos_medicos
           WHERE deleted_at IS NULL GROUP BY id_perfil) e ON e.id_perfil = p.id_perfil
LEFT JOIN (SELECT id_perfil, COUNT(*) total FROM recetas
           WHERE deleted_at IS NULL GROUP BY id_perfil) r ON r.id_perfil = p.id_perfil
LEFT JOIN (SELECT id_perfil, COUNT(*) total FROM vacunacion
           WHERE estatus = 'Aplicada' AND deleted_at IS NULL GROUP BY id_perfil) v ON v.id_perfil = p.id_perfil
WHERE p.deleted_at IS NULL;

-- =========================================================
-- 9. LIMPIEZA AUTOMÁTICA (requiere: SET GLOBAL event_scheduler = ON)
-- =========================================================

CREATE EVENT ev_limpiar_tokens
ON SCHEDULE EVERY 1 HOUR
DO
    DELETE FROM tokens_2fa
    WHERE fecha_expiracion < NOW() - INTERVAL 1 DAY OR usado = TRUE;

-- =========================================================
-- 10. USUARIO DE LA APP CON MÍNIMOS PRIVILEGIOS
--     (el backend NUNCA debe conectarse como root)
-- =========================================================

-- CREATE USER 'cmd_app'@'%' IDENTIFIED BY '<<contraseña_larga_y_unica>>' REQUIRE SSL;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON cmd_db.* TO 'cmd_app'@'%';
-- (sin DROP, ALTER, CREATE ni GRANT)

-- =========================================================
-- 11. DATOS INICIALES (catálogos)
-- =========================================================

INSERT INTO cat_roles (nombre_rol) VALUES ('Superusuario'), ('Usuario');
INSERT INTO cat_parentescos (descripcion) VALUES ('Titular'), ('Padre'), ('Madre'), ('Hijo/a'), ('Cónyuge'), ('Otro');
INSERT INTO cat_tipos_sangre (tipo) VALUES ('O+'), ('O-'), ('A+'), ('A-'), ('B+'), ('B-'), ('AB+'), ('AB-');
INSERT INTO cat_vacunas (nombre_vacuna) VALUES ('Influenza'), ('COVID-19'), ('Hepatitis B'), ('Tétanos');
INSERT INTO cat_especialidades (nombre_especialidad) VALUES ('Medicina General'), ('Pediatría'), ('Ortopedia'), ('Cardiología');
INSERT INTO cat_tipos_documento (nombre_tipo) VALUES ('Laboratorio'), ('Radiografía'), ('Receta'), ('Comprobante');