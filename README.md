# Actas GLPI

Sistema para la generación automatizada de documentos Word (DOCX) de actas de entrega y devolución de activos tecnológicos, integrado con GLPI para la consulta de equipos.

## Descripción

El sistema permite a personal de TI generar actas oficiales y listas de chequeo a partir de datos capturados en un formulario web. Los documentos se generan en formato DOCX, se empaquetan en ZIP y se descargan automáticamente al navegador.

### Tipos de documento generados

| Tipo | Archivos ZIP | Descripción |
|------|-------------|-------------|
| **Acta de Entrega** | `ActaLista_{serial}_{asunto}.zip` | Acta de entrega + Lista de chequeo (2 DOCX) |
| **Acta de Devolución** | `Devolucion_{serial}_{motivo}.zip` | Acta de devolución (1 DOCX) |

## Requisitos previos

- Java 21
- Maven 3.8+
- Node.js (opcional, para Tailwind CSS)
- Cuenta activa en instancia GLPI con permisos de API REST

## Estructura del proyecto

```
actas-glpi/
├── backend/                    # API REST (Spring Boot 3.4.1)
│   ├── pom.xml                 # Dependencias Maven
│   └── src/main/
│       ├── java/com/empresa/actas/
│       │   ├── ActasApplication.java
│       │   ├── config/         # CORS, variables de entorno
│       │   ├── controller/     # Endpoints REST
│       │   ├── dto/            # Request/Response DTOs
│       │   ├── exception/      # Manejo global de errores
│       │   └── service/        # Lógica de negocio
│       └── resources/
│           ├── application.yml
│           └── plantillas/     # Templates DOCX
├── frontend/                   # Interfaz web (HTML/CSS/JS)
│   ├── css/                    # Estilos (Tailwind + CSS custom)
│   ├── js/                     # Lógica JavaScript
│   └── pages/                  # Páginas HTML
├── plantillas/                 # Templates DOCX (copia de referencia)
├── generados/                  # Directorio de ZIPs generados
└── .env                        # Variables de entorno (GLPI tokens)
```

## Instalación

### 1. Backend

```bash
cd backend
mvn clean package -DskipTests
```

### 2. Variables de entorno

Crear archivo `.env` en la raíz del proyecto:

```
GLPI_URL=http://tu-servidor-glpi/glpi/apirest.php
GLPI_APP_TOKEN=tu-app-token
GLPI_USER_TOKEN=tu-user-token
```

### 3. Ejecutar

```bash
cd backend
mvn spring-boot:run
```

El servidor arranca en `http://127.0.0.1:8001`.

### 4. Frontend

Abrir directamente en el navegador:

```
frontend/pages/acta-entrega.html
frontend/pages/acta-devolucion.html
```

O usar Live Server de VS Code en el puerto 5500.

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/generar-acta` | Genera acta de entrega + checklist |
| `POST` | `/generar-devolucion` | Genera acta de devolución |
| `GET` | `/equipo/{serial}` | Consulta equipo en GLPI por serial |
| `GET` | `/descargar-acta/{nombreZip}` | Descarga el ZIP generado |

## Tecnologías

**Backend:**
- Java 21
- Spring Boot 3.4.1
- Apache POI 5.2.5 (manipulación DOCX)
- Jackson (serialización JSON)
- Lombok
- Jakarta Validation
- dotenv-java 3.2.0

**Frontend:**
- HTML5, CSS3, JavaScript vanilla
- Tailwind CSS 4.3.3
- FlyonUI 2.4.1 (componentes UI)
- Flatpickr (selector de fechas)

## Notas importantes

- El frontend está hardcodeado a `http://127.0.0.1:8001`. El backend **debe** ejecutarse en el puerto 8001.
- Los tokens de GLPI en `.env` están excluidos del repositorio (`.gitignore`).
- Los archivos generados se guardan en el directorio temporal del sistema (`/tmp/actas_glpi_generados`).
- No se generan documentos PDF — solo DOCX empaquetados en ZIP.
