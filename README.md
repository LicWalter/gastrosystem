# GastroSystem 🍽️📊
> **Grupo/Package:** `com.appetit` | **Artefacto:** `gastrosystem`  
> **Base de Datos:** `dbappetit`

Plataforma web integral para la gestión de restaurantes que automatiza la administración de mesas, menús, reservas, pedidos (tanto en físico como a domicilio), clientes y reportes de ventas en tiempo real. 

Este proyecto está diseñado bajo una arquitectura limpia y segura, controlando minuciosamente el ciclo de vida de cada pedido desde su creación en cocina hasta su entrega y facturación.

---

## 🚀 Tecnologías Utilizadas

El sistema está desarrollado con un stack moderno, robusto y altamente demandado en el entorno empresarial:

* **Backend:** Java 17+ / Spring Boot 3.x
    * *Spring Data JPA* (Persistencia y mapeo ORM)
    * *Spring Security* (Autenticación y autorización basada en Roles)
* **Frontend:** Thymeleaf (Motor de plantillas), HTML5, CSS3, JavaScript
* **Diseño UI:** Bootstrap 5 (Interfaz totalmente responsiva y adaptada a dispositivos móviles)
* **Base de Datos:** MariaDB (Motor relacional óptimo y de alto rendimiento)
* **Herramientas:** Maven (Gestión de dependencias), Git/GitHub (Control de versiones)

---

## 👥 Roles del Sistema

El sistema implementa seguridad basada en roles (**RBAC** con *Spring Security*) para segmentar las funciones de los usuarios:

1.  **Administrador:** Control total. Gestiona platos, categorías, inventario, usuarios, asignación de roles y visualiza reportes financieros.
2.  **Mesero:** Administra el estado de las mesas, crea pedidos presenciales y los envía directamente al módulo de cocina.
3.  **Cocina:** Visualiza en tiempo real los platos pendientes por preparar, actualizando su estado a "Listo" cuando finaliza.
4.  **Domiciliario:** Consulta las entregas pendientes, toma el control del reparto y marca los domicilios como "Entregados" o "Pagados".
5.  **Cliente:** Registra sus datos, genera reservas en fechas/horas específicas y realiza pedidos a domicilio desde la comodidad de su hogar.

---

## 📦 Módulos Principales

* **🛒 Menú:** Registro de platos con categorías (Entradas, Platos Fuertes, Bebidas, Postres), precios y control básico de stock.
* **🪑 Mesas:** Gestión visual de la disponibilidad y capacidad de las mesas del establecimiento.
* **📅 Reservas:** Control de agendas por fecha, hora, cliente y número de comensales.
* **📝 Pedidos:** Motor de flujo que calcula automáticamente los totales y maneja el ciclo de estados: `RECIBIDO` ➡️ `EN_PREPARACION` ➡️ `LISTO` ➡️ `ENTREGADO` ➡️ `PAGADO` ➡️ `CANCELADO`.
* **🛵 Domicilios:** Gestión logística de repartos asignados a domiciliarios.
* **💳 Pagos & Cierre:** Registro de transacciones financieras y cierre de cuentas.
* **📈 Reportes:** Panel analítico de ventas diarias y métricas de los platos más vendidos.

---

## 🗄️ Estructura de la Base de Datos (`dbappetit`)

Para cumplir rigurosamente con los requisitos de seguridad y control de usuarios internos y externos, el sistema implementa un modelo relacional de **Muchos a Muchos (M:N)** entre Usuarios y Roles.

```sql
-- Estructura Inicial para Gestión de Accesos Bases de Datos HEIDISQL (Usuarios y Roles)

Script:

-- ============================================================
-- GastroSystem - com.appetit
-- Base de datos: dbappetit
-- Módulo: Usuarios y Roles
-- ============================================================

CREATE DATABASE IF NOT EXISTS dbappetit
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE dbappetit;

-- ------------------------------------------------------------
-- Tabla: roles
-- Roles del sistema: ADMINISTRADOR, MESERO, COCINA,
-- DOMICILIARIO, CLIENTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id_rol      INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(30)  NOT NULL UNIQUE,
    descripcion VARCHAR(150)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ------------------------------------------------------------
-- Tabla: usuarios
-- Incluye tanto al personal interno (admin, mesero, cocina,
-- domiciliario) como a los clientes que se registran en la
-- plataforma para reservar o pedir a domicilio.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(60)  NOT NULL,
    apellido          VARCHAR(60)  NOT NULL,
    tipo_documento    VARCHAR(20),
    numero_documento  VARCHAR(30)  UNIQUE,
    email             VARCHAR(100) NOT NULL UNIQUE,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL,
    telefono          VARCHAR(20),
    direccion         VARCHAR(150),
    fecha_registro    DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo            BOOLEAN DEFAULT TRUE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ------------------------------------------------------------
-- Tabla intermedia: usuario_rol (relación muchos a muchos)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_rol (
    id_usuario BIGINT NOT NULL,
    id_rol     INT    NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol)
        REFERENCES roles (id_rol) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ------------------------------------------------------------
-- Datos iniciales: roles del sistema
-- ------------------------------------------------------------
INSERT INTO roles (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Gestiona menú, mesas, usuarios, inventario y reportes'),
('MESERO',        'Crea y gestiona pedidos por mesa, envía órdenes a cocina'),
('COCINA',        'Visualiza y actualiza el estado de preparación de los pedidos'),
('DOMICILIARIO',  'Gestiona la entrega de pedidos a domicilio'),
('CLIENTE',       'Realiza reservas y pedidos a domicilio desde la plataforma');

-- ------------------------------------------------------------
-- Usuario administrador inicial (para poder entrar al sistema
-- la primera vez). Usuario: admin / Contraseña: admin123
-- La contraseña ya está cifrada con BCrypt (fuerza 10).
-- CAMBIA esta contraseña apenas inicies sesión por primera vez.
-- ------------------------------------------------------------
INSERT INTO usuarios (nombre, apellido, email, username, password, activo)
VALUES ('Admin', 'GastroSystem', 'admin@appetit.com', 'admin',
        '$2b$10$nHaxULZHDDy7TQOaMT/YM.3zTqcz.F/5HBITMeEKL5wCikana4fSK', TRUE);

INSERT INTO usuario_rol (id_usuario, id_rol)
SELECT u.id_usuario, r.id_rol
FROM usuarios u, roles r
WHERE u.username = 'admin' AND r.nombre = 'ADMINISTRADOR';


dbappetit




-- ¿Existen las 3 tablas?
SHOW TABLES;

-- ¿Quedaron los 5 roles?
SELECT * FROM roles;

USE dbappetit;
SELECT * FROM usuarios;

-- ¿Existe el usuario admin?
SELECT id_usuario, nombre, username, email, activo FROM usuarios;

-- ¿Quedó bien asociado admin -> ADMINISTRADOR?
SELECT u.username, r.nombre AS rol
FROM usuarios u
JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario
JOIN roles r ON ur.id_rol = r.id_rol;
