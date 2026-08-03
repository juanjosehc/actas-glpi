# Actas GLPI

Sistema para la generación automatizada de documentos Word (DOCX) de actas de entrega y devolución de activos tecnológicos, integrado con GLPI para la consulta de equipos y usuarios.

## Descripción

El sistema permite al personal de TI generar actas oficiales y listas de chequeo a partir de datos capturados en un formulario web. Los documentos se generan en formato DOCX, se empaquetan en ZIP y se descargan automáticamente al navegador.

El frontend es una aplicación web estática (HTML/CSS/JS vanilla con Tailwind CSS y FlyonUI). El backend es una API REST en Java 21 + Spring Boot 3.4.1 que consulta GLPI, genera los DOCX desde plantillas Word y los empaqueta en ZIP.

### Tipos de documento generados

| Tipo | ZIP | Contenido |
|------|-----|-----------|
| **Acta de Entrega** | `ActaLista_{serial}_{asunto}.zip` | Acta de entrega + Lista de chequeo (2 DOCX) |
| **Acta de Devolución** | `Devolucion_{serial}_{motivo}.zip` | Acta de devolución (1 DOCX) |

## Requisitos previos

- **Java 21** (JDK).
- **Maven 3.8+** (para el backend).
- **Node.js 18+** (opcional, para regenerar estilos Tailwind y necesaria para que el frontend funcione, ver [Frontend](#4-frontend)).
- Cuenta en una instancia **GLPI** con permisos de API REST (App-Token + User-Token).

## Estructura del proyecto

```
actas-glpi/
├── backend/                     # API REST (Spring Boot 3.4.1, Java 21)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/empresa/actas/
│       │   ├── ActasApplication.java
│       │   ├── config/          # AppConfig (.env), CorsConfig
│       │   ├── controller/      # Acta, Devolucion, Equipo, Usuario
│       │   ├── dto/             # Request / Response DTOs
│       │   ├── exception/       # GlobalExceptionHandler
│       │   └── service/         # Acta, Devolucion, DocumentoWord, DocxTemplateEngine,
│       │                        # Equipo, Usuario, GlpiClient, Zip
│       └── resources/
│           ├── application.yml
│           └── plantillas/      # Templates DOCX (acta, checklist, devolución)
├── frontend/                    # Interfaz web (HTML/CSS/JS)
│   ├── css/                     # styles.css, output.css (Tailwind), app.css (fuente)
│   ├── js/                      # config.js, ui.js, autocomplete.js, app.js, devolucion.js
│   ├── pages/                   # acta-entrega.html, acta-devolucion.html
│   ├── img/                     # logo.png
│   └── package.json             # Tailwind CSS + FlyonUI
├── docs/
│   ├── MANUAL_USUARIO.md        # Manual de usuario
│   └── MANTENIMIENTO.md         # Guía de mantenimiento
├── .env.example                 # Plantilla de variables de entorno
├── ARQUITECTURA.md              # Documentación técnica
└── FLUJO_FUNCIONAL.md           # Flujo funcional por tipo de acta
```

## Instalación

### 1. Backend

```bash
cd backend
mvn clean package -DskipTests
```

### 2. Variables de entorno

Copiar `.env.example` a `.env` en la **raíz del proyecto** (no dentro de `backend/`) y completar los valores:

```
GLPI_URL=http://10.86.1.33/glpi/apirest.php
GLPI_APP_TOKEN=tu-app-token
GLPI_USER_TOKEN=tu-user-token
```

- `GLPI_APP_TOKEN` y `GLPI_USER_TOKEN` son **obligatorios**: si no están definidos, la aplicación **no arranca** (no hay valor por defecto).
- `GLPI_URL` tiene un valor por defecto; puede sobrescribirse con la variable de entorno del sistema o el `.env`.
- El archivo `.env` está excluido de git (`.gitignore`).

### 3. Ejecutar el backend

```bash
cd backend
mvn spring-boot:run
```

El servidor arranca en `http://127.0.0.1:8001`. El puerto debe coincidir con `API_URL` de `frontend/js/config.js`.

### 4. Frontend

Instalar dependencias (FlyonUI y Tailwind):

```bash
cd frontend
npm install
```

Abrir las páginas en el navegador:

```
frontend/pages/acta-entrega.html
frontend/pages/acta-devolucion.html
```

O usar Live Server de VS Code (puerto configurado: **5501**). El CORS del backend ya permite `127.0.0.1`/`localhost` en puertos `5500`, `5501` y `8080`.

> **Nota:** `flyonui.js` se carga directamente desde `node_modules`, por lo que `npm install` es obligatorio para que los componentes FlyonUI funcionen. El CSS compilado (`output.css`) sí está versionado y funciona sin build.

### 5. Regenerar los estilos Tailwind (opcional)

Los cambios de estilos se hacen en `frontend/css/app.css` y se compilan con:

```bash
cd frontend
npm run build:css
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/generar-acta` | Genera acta de entrega + lista de chequeo (DOCX → ZIP) |
| `POST` | `/generar-devolucion` | Genera acta de devolución (DOCX → ZIP) |
| `GET` | `/descargar-acta/{nombreZip}` | Descarga el ZIP generado |
| `GET` | `/equipo/{serial}` | Consulta un equipo en GLPI por serial (marca, tipo, modelo) |
| `GET` | `/usuarios?texto=...` | Busca usuarios en GLPI por nombre (autocompletado) |

## Tecnologías

**Backend:**
- Java 21, Spring Boot 3.4.1
- Apache POI 5.2.5 (manipulación DOCX)
- Jackson (JSON), Lombok, Jakarta Validation
- dotenv-java 3.2.0 (carga de `.env`)

**Frontend:**
- HTML5, CSS3, JavaScript vanilla
- Tailwind CSS 4.3.3, FlyonUI 2.4.1
- Flatpickr (selector de fechas)

## Notas importantes

- La URL del backend se centraliza en `frontend/js/config.js` (`API_URL`). El backend **debe** ejecutarse en el puerto 8001.
- Los DOCX generados se guardan en `app.generated-dir` (por defecto `java.io.tmpdir/actas_glpi_generados`); puede sobrescribirse con la propiedad de Spring `app.generated-dir`.
- No se generan documentos PDF: solo DOCX empaquetados en ZIP.
- La documentación de arquitectura, flujos, manual de usuario y mantenimiento está en `ARQUITECTURA.md`, `FLUJO_FUNCIONAL.md` y `docs/`.
