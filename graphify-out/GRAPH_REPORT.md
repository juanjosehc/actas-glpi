# Graph Report - C:/Users/juanhern/OneDrive - COMPANIA DE FINANCIAMIENTO COMERCIAL COLTEFINANCIERA S.A/Documentos/actas-glpi  (2026-08-26)

## Corpus Check
- Corpus is ~23,095 words - fits in a single context window. You may not need a graph.

## Summary
- 204 nodes · 400 edges · 16 communities (14 shown, 2 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 30 edges (avg confidence: 0.86)
- Token cost: 0 input · 195,632 output

## Community Hubs (Navigation)
- Backend REST Spring Boot
- Controllers Generación Actas
- Equipos y Usuarios API
- Generación Documentos DOCX
- DTOs de Petición y Respuesta
- Configuración y CORS Backend
- Lógica Frontend Actas
- Manejo Global Errores
- Tooling CSS Tailwind FlyonUI
- Lógica Frontend Devolución
- Arranque Aplicación Spring
- Utilidades UI Toasts
- Marca Coltefinanciera
- Proyecto Maven Actas

## God Nodes (most connected - your core abstractions)
1. `ActaResponse` - 14 edges
2. `DocumentoWordService` - 13 edges
3. `Backend API REST Spring Boot (puerto 8001)` - 13 edges
4. `UsuarioService` - 11 edges
5. `ErrorResponse` - 10 edges
6. `ActaService` - 10 edges
7. `DevolucionService` - 10 edges
8. `EquipoService` - 10 edges
9. `GlpiClient` - 10 edges
10. `UsuarioResponse` - 9 edges

## Surprising Connections (you probably didn't know these)
- `Checklist de Verificación (36 ítems, 6 secciones)` --semantically_similar_to--> `Checklist HTML (chk_1..chk_36)`  [INFERRED] [semantically similar]
  FLUJO_FUNCIONAL.md → frontend/pages/acta-entrega.html
- `Dockerfile Frontend (nginx:alpine)` --implements--> `Frontend Web Estático (HTML/CSS/JS)`  [INFERRED]
  docker.md → ARQUITECTURA.md
- `Dockerfile Backend (eclipse-temurin:21-jre)` --implements--> `Backend API REST Spring Boot (puerto 8001)`  [INFERRED]
  docker.md → ARQUITECTURA.md
- `docker-compose.yml (backend + frontend)` --conceptually_related_to--> `Arquitectura Cliente-Servidor de Dos Capas`  [INFERRED]
  docker.md → ARQUITECTURA.md
- `Configuración server.port: 8001` --shares_data_with--> `Frontend Web Estático (HTML/CSS/JS)`  [INFERRED]
  backend/src/main/resources/application.yml → ARQUITECTURA.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Flujo de Generación Documental (Word → ZIP)** — arquitectura_documentowordservice, arquitectura_doctemplateengine, arquitectura_zipservice [EXTRACTED 1.00]
- **Integración con GLPI (equipos y usuarios)** — arquitectura_glpiclient, arquitectura_token_auth, flujo_funcional_busqueda_equipo, flujo_funcional_autocompletado_usuarios [EXTRACTED 1.00]
- **Arquitectura Cliente-Servidor de Dos Capas** — arquitectura_cliente_servidor, arquitectura_frontend, arquitectura_backend, arquitectura_glpi_integracion [EXTRACTED 1.00]

## Communities (16 total, 2 thin omitted)

### Community 0 - "Backend REST Spring Boot"
Cohesion: 0.08
Nodes (40): Backend API REST Spring Boot (puerto 8001), Arquitectura Cliente-Servidor de Dos Capas, Configuración CORS para Orígenes Locales, DocxTemplateEngine, DocumentoWordService, Generación de Documentos Word (DOCX), Frontend Web Estático (HTML/CSS/JS), Integración con API REST de GLPI (+32 more)

### Community 1 - "Controllers Generación Actas"
Cohesion: 0.17
Nodes (12): ActaController, DevolucionController, ActaRequest, DevolucionRequest, ActaResponse, ActaService, DevolucionService, ZipService (+4 more)

### Community 2 - "Equipos y Usuarios API"
Cohesion: 0.14
Nodes (9): EquipoController, UsuarioController, EquipoService, GlpiClient, UsuarioService, com.fasterxml.jackson.databind.JsonNode, java.net.http.HttpClient, org.springframework.stereotype.Component (+1 more)

### Community 3 - "Generación Documentos DOCX"
Cohesion: 0.22
Nodes (5): DocumentoWordService, DocxTemplateEngine, java.util.regex.Pattern, org.apache.poi.xwpf.usermodel.XWPFParagraph, SuppressWarnings

### Community 4 - "DTOs de Petición y Respuesta"
Cohesion: 0.28
Nodes (9): EquipoItem, HardwareItem, OtroElementoItem, EquipoResponse, ErrorResponse, UsuarioResponse, lombok.AllArgsConstructor, lombok.Data (+1 more)

### Community 5 - "Configuración y CORS Backend"
Cohesion: 0.24
Nodes (7): AppConfig, CorsConfig, jakarta.annotation.PostConstruct, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.WebMvcConfigurer, WebMvcConfigurer

### Community 6 - "Lógica Frontend Actas"
Cohesion: 0.24
Nodes (12): abrirTodosLosAccordions(), agregarEquipo(), agregarHardware(), buscarEquipoBloque(), cerrarTodosLosAccordions(), desmarcarTodosLosChecks(), generarActa(), marcarTodosLosChecks() (+4 more)

### Community 7 - "Manejo Global Errores"
Cohesion: 0.38
Nodes (5): GlobalExceptionHandler, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.ExceptionHandler, org.springframework.web.bind.annotation.RestControllerAdvice, org.springframework.web.bind.MethodArgumentNotValidException

### Community 8 - "Tooling CSS Tailwind FlyonUI"
Cohesion: 0.18
Nodes (10): flyonui, dependencies, tailwindcss, @tailwindcss/cli, devDependencies, flyonui, scripts, build:css (+2 more)

### Community 9 - "Lógica Frontend Devolución"
Cohesion: 0.36
Nodes (8): agregarEquipo(), agregarHardware(), buscarEquipoBloque(), generarDevolucion(), renumerarEquipos(), renumerarHardware(), validarCampo(), validarEquipos()

### Community 11 - "Utilidades UI Toasts"
Cohesion: 0.67
Nodes (3): ICONOS_TOAST, mostrarMensaje(), obtenerContenedorToast()

### Community 12 - "Marca Coltefinanciera"
Cohesion: 1.00
Nodes (3): Gestión de Actas (frontend Coltefinanciera), Coltefinanciera S.A., Logo Coltefinanciera

## Knowledge Gaps
- **17 isolated node(s):** `com.empresa:actas-glpi`, `ICONOS_TOAST`, `build:css`, `@tailwindcss/cli`, `tailwindcss` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ErrorResponse` connect `DTOs de Petición y Respuesta` to `Controllers Generación Actas`, `Manejo Global Errores`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Why does `DocumentoWordService` connect `Generación Documentos DOCX` to `Controllers Generación Actas`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Why does `ActaResponse` connect `Controllers Generación Actas` to `DTOs de Petición y Respuesta`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `Backend API REST Spring Boot (puerto 8001)` (e.g. with `Stack Tecnológico (Java 21, Spring Boot 3.4.1, Tailwind 4.3.3, FlyonUI 2.4.1)` and `Dockerfile Backend (eclipse-temurin:21-jre)`) actually correct?**
  _`Backend API REST Spring Boot (puerto 8001)` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.empresa:actas-glpi`, `ICONOS_TOAST`, `build:css` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Backend REST Spring Boot` be split into smaller, more focused modules?**
  _Cohesion score 0.0782051282051282 - nodes in this community are weakly interconnected._
- **Should `Equipos y Usuarios API` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._