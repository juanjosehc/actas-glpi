# ActasV1 — Contexto permanente del proyecto

> **Qué es este archivo.** Memoria documental del proyecto para asistentes IA
> (Copilot, Claude, ChatGPT) y para onboarding técnico. **No forma parte de la
> lógica funcional de la aplicación** y no se carga en tiempo de ejecución.
>
> **Cómo usarlo.** Al iniciar una conversación nueva basta con decir:
> *"Estamos trabajando sobre ActasV1. Usa `docs/COPILOT_CONTEXT.md` como fuente de verdad."*
>
> **Estado.** Última actualización relevante: **2026-10-01**, tras la iteración
> de modalidad Equipo/Periférico y la reubicación de la modalidad como
> configuración global del acta.
>
> ⚠️ **Antes de confiar en cualquier otro documento:** hay cambios **sin
> commitear** en el árbol de trabajo (modalidad Equipo/Periférico, puerto 8087,
> naming de ZIP, tests) que los `.md` todavía no reflejan. **El código es la
> fuente de verdad**; ver *Deuda de documentación* más abajo.

---

## Estado del proyecto

- **En producción.** La aplicación es operativa: genera actas reales contra la
  instancia GLPI de producción (`sac-i.connser.com.co`).
- **Versión en curso:** `3.6.3` (según historial de commits).
- **Rama principal:** `main`.
- **Aplica a las tres actas** (entrega, devolución, formateo seguro), aunque la
  modalidad Equipo/Periférico existe **solo en Acta de Entrega**.

---

## Objetivo del sistema

ActasV1 permite al personal de TI generar **documentos Word (DOCX)** de actas
oficiales de **entrega**, **devolución** y **formateo seguro** de activos
tecnológicos, a partir de datos capturados en un formulario web. Los documentos
se generan desde plantillas Word, se empaquetan en un ZIP y se descargan
automáticamente en el navegador.

El sistema se integra con **GLPI** para dos cosas:

1. Consultar los datos de un equipo a partir de su serial (marca, tipo, modelo).
2. Autocompletar nombres de personas al escribir (entregado a / entregado por, etc.).

---

## Arquitectura

> ⚠️ **Corrección importante respecto a supuestos habituales:** el frontend
> **NO es React**. Es HTML/CSS/JS vanilla sin framework y sin bundler de
> aplicación. Tailwind se usa solo como compilador de CSS.

### Frontend

| Aspecto | Valor real |
|---|---|
| Tecnología | HTML5, CSS3, **JavaScript vanilla** (sin framework, sin SPA) |
| Bundler | **No hay** bundler de aplicación. Tailwind se compila aparte |
| Estilos | Tailwind CSS 4.3.3 (compilado a `frontend/css/output.css`) |
| Componentes UI | FlyonUI 2.4.1 (vía `node_modules`, requiere `npm install`) |
| Fechas | Flatpickr (CDN) |
| Módulos JS | `config.js`, `ui.js`, `autocomplete.js`, `app.js`, `devolucion.js`, `formateo.js` |
| Páginas | `frontend/pages/acta-entrega.html`, `acta-devolucion.html`, `acta-formateo.html` |

**Punto crítico de mantenimiento:** `frontend/js/ui.js` es **compartido por las
tres páginas**. Cualquier cambio ahí afecta entrega, devolución y formateo a la
vez. Las funciones específicas de entrega se implementan en `app.js` para no
tocar `ui.js` (por eso existe `renumerarActivos()` en `app.js`, que sustituye a
`renumerarEquipos()` de `ui.js` solo en esa página).

### Backend

| Aspecto | Valor real |
|---|---|
| Lenguaje | **Java 21** |
| Framework | **Spring Boot 3.4.1** |
| Build | Maven (artefacto `actas-glpi-1.0.0.jar`) |
| DOCX | Apache POI 5.2.5 |
| Otros | Jackson, Lombok, Jakarta Validation, dotenv-java 3.2.0 |

**Paquetes** (`backend/src/main/java/com/empresa/actas/`):

- `config/` → `AppConfig` (carga `.env`), `CorsConfig`
- `controller/` → `Acta`, `Devolucion`, `FormateoSeguro`, `Equipo`, `Usuario`
- `dto/request/` → `ActaRequest`, `DevolucionRequest`, `FormateoSeguroRequest`,
  `EquipoItem`, `HardwareItem`, `OtroElementoItem`
- `dto/response/` → `ActaResponse`, `EquipoResponse`, `ErrorResponse`, `UsuarioResponse`
- `exception/` → `GlobalExceptionHandler`
- `service/` → `Acta`, `Devolucion`, `FormateoSeguro`, `DocumentoWord`,
  `DocxTemplateEngine`, `Equipo`, `Usuario`, `GlpiClient`, `Zip`, `NombreArchivoUtil`

### Infraestructura

- **Docker: propuesto, NO implementado.** No existen `Dockerfile` ni
  `docker-compose.yml` en el repositorio. `docker.md` contiene la propuesta
  (backend `eclipse-temurin:21-jre`, frontend `nginx:alpine`, compose con
  `env_file: .env`). Ver la sección **Docker** más abajo para el detalle real.
- **Operación actual:** backend con `mvn spring-boot:run` o `java -jar`;
  frontend servido como estático. El puerto de desarrollo configurado en
  `.vscode/settings.json` es **Live Server en 5501**.
- **Sin autenticación.** No hay Spring Security ni login: los endpoints están
  abiertos a quien alcance el puerto.
- **Sin CI/CD.**

---

## Actas soportadas

| Acta | ZIP generado | Contenido |
|---|---|---|
| **Acta de Entrega** | `ActaEntrega_{serial}_{nombre}.zip` — con `SINSERIAL` en lugar del serial cuando no lo hay (caso periférico) | Acta + Lista de Chequeo (2 DOCX) en modo Equipo. Solo acta en modo Periférico |
| **Acta de Devolución** | `ActaDevolucion_{serial}_{nombre}.zip` | Acta de devolución (1 DOCX) |
| **Acta de Formateo Seguro** | `ActaFormateoSeguro_{serial}_{nombre}.zip` | Acta de formateo (1 DOCX, **máx. 4 equipos**) |

> El naming está centralizado en `NombreArchivoUtil.nombreBase(tipoActa, serial, nombre)`
> con el patrón `[TIPO_ACTA]_[SERIAL]_[NOMBRE]`. `{nombre}` es **la persona**
> (`entregado_a` en Entrega, `entregado_por` en Devolución y Formateo), **no el
> asunto ni el motivo**. El serial se normaliza a mayúsculas sin espacios; el
> nombre pierde tildes y pasa a camelCase (`Juan José Hernández` →
> `JuanJoseHernandezCorrea`). Fallbacks: `SINSERIAL`, `SINNOMBRE`. Ejemplos reales
> observados: `ActaEntrega_ABC123_JuanPerez.zip`, `ActaEntrega_SINSERIAL_JuanPerez.zip`.
>
> ⚠️ **Esto contradice a los .md.** `README.md`, `ARQUITECTURA.md`,
> `DOCUMENTACION_TECNICA.md` y `FLUJO_FUNCIONAL.md` describen el naming antiguo
> (`ActaLista_{serial}_{asunto}.zip`, con el asunto limpiado). **Manda el código.**

### Acta de Entrega

Única acta con **modalidad Equipo/Periférico**. En modo Equipo exige serial e
inventario por activo, incluye lista de chequeo (36 ítems en 6 secciones + SO) y
la card de Hardware y Software. En modo Periférico omite todo eso.

### Acta de Devolución

Solo `fecha` es obligatoria en el backend; el resto de campos son opcionales por
diseño (`recibido_por`, `entregado_por`, `cedula`, `area_recibe`, `motivo`,
`cargo_recibe`, `cargo_entrega`). Los equipos llevan un campo extra `estado`, y
los elementos sueltos van como `ot_N_` (tipo). **No tiene modalidad.**

### Acta de Formateo Seguro

Exige fecha, entregado a/por, cargos y asunto. **Límite duro de 4 equipos**
validado con `@Size(max = 4)` **y** reforzado en el frontend. No admite hardware.

---

## Reglas de negocio

### Modalidad Equipo (por defecto)

- **Sí** consulta GLPI (botón "Buscar" por serial).
- **Serial obligatorio** (y `inventario` obligatorio).
- Marca, tipo y modelo se **autocompletan desde GLPI** y llegan **deshabilitados**
  (`readonly`/`disabled`) — el usuario no los edita.
- **Sí** exige `numero_sac` y `sistema_operativo`.
- Genera **Acta + Lista de Chequeo**.

### Modalidad Periférico

- **No** consulta GLPI.
- **Descripción obligatoria** (texto libre, campo `Descripción del periférico`).
- **Marca, Modelo, Serial e Inventario opcionales** (todos).
- **No** exige `numero_sac` ni `sistema_operativo`.
- Genera **únicamente el Acta de Entrega** (sin lista de chequeo).
- El orden de campos en el formulario es: Descripción * / Inventario / Marca /
  Modelo / Serial.

> **Matiz crítico sobre dónde vive cada validación.** La obligatoriedad de la
> Descripción (y de la Marca/Modelo/Serial/Inventario como opcionales) es
> **100 % frontend**: `camposObligatoriosActivo()` en `frontend/js/app.js`
> devuelve `["[data-per-descripcion]"]` en periférico y
> `["[data-serial]", "[data-inventario]"]` en equipo. El **backend no valida
> `EquipoItem`**: `ActaRequest` declara `List<EquipoItem>` sin `@Valid` y
> `EquipoItem` no tiene anotaciones. Un cliente que llame a la API directamente
> puede enviar un periférico sin descripción y el backend lo aceptará.
>
> **Segundo matiz:** en periférico la Descripción se envía en el campo `tipo` del
> `EquipoItem` (`construirActivoPrincipal` → `tipo: valores.descripcion`), porque
> la tabla de la plantilla DOCX no tiene una columna "Descripción". Hay un
> comentario `ponytail:` en `app.js` anotando que se debe añadir columna propia
> cuando se edite la plantilla.

### Cómo se valida la modalidad en el backend

En `ActaRequest.java`:

- Campo `private String modo;` — `"EQUIPO"` (por defecto) o `"PERIFERICO"`.
  **Es un `String` libre, no un enum.**
- `public boolean esModoPeriferico()` → `"PERIFERICO".equalsIgnoreCase(modo)`.
- `numero_sac` y `sistema_operativo` **perdieron `@NotBlank`**; ahora se validan
  con dos `@AssertTrue` que los exigen **solo cuando NO es periférico**.
- **Cualquier valor distinto de `"PERIFERICO"` —incluido `null` o basura— se
  trata como EQUIPO.** No hay validación de que `modo` sea uno de los dos valores.

En `ActaService.generarActa()` el ZIP se arma distinto según la modalidad:

```java
if (request.esModoPeriferico()) {
    zipService.crearZip(rutaZip, rutaActa);          // solo el acta
} else {
    Path rutaChecklist = wordService.generarChecklist(datos);
    zipService.crearZip(rutaZip, rutaActa, rutaChecklist);  // acta + checklist
}
```

En el frontend (`app.js`):

- `modoActual()` lee `document.querySelector("[data-modo-radio]:checked")?.value`
  y cae a `ACTIVO_EQUIPO` si no hay radio marcado.
- `esModoPeriferico()` compara contra `ACTIVO_PERIFERICO`.
- `aplicarModoGlobal(modo)` alterna la clase `.modo-periferico` en
  `#acta-entrega-page` y aplica `aplicarModoAlBloque()` a cada `.equipo-item`.
- El CSS reacciona a esa clase: oculta `#checklist-section` y `#hardware-group`,
  y cambia el grid de 3 a 2 columnas.

> **Consecuencia de diseño:** como los radios son la única fuente de verdad y el
> listener va sobre `[data-modo-radio]`, **mover los radios en el DOM no rompe
> nada**. Esto se verificó explícitamente al reubicarlos en la iteración de
> octubre de 2026.

---

## Restricciones

- **No se pueden mezclar Equipos y Periféricos en una misma acta.**
- **La modalidad es global para toda el acta**: todos los activos comparten tipo.
- Los bloques **heredan** la modalidad global; no tienen selector propio.
- **El checklist solo aplica a Equipos.**
- **Hardware y Software no aplica** para entregas exclusivas de Periféricos.
- **No se generan PDF.** Solo DOCX empaquetados en ZIP (ver `README.md`).
- **Límites de plantilla:** Entrega 10 equipos / 11 hardware; Devolución 10
  equipos / 10 otros; Formateo 4 equipos. El frontend impone topes menores.

---

## Decisiones UX

### Modalidad global del acta

La modalidad **no es una propiedad del Activo Principal**: decide qué activos se
admiten, si hay lista de chequeo, qué validaciones corren y qué documento se
genera. Visual y conceptualmente es una **propiedad global del acta**.

### Ubicación visual de la modalidad

La selección se movió **fuera de la columna "Activo Principal"**, a una fila
propia entre el hero (`Acta de Entrega` + subtítulo) y las tres columnas:

```
Acta de Entrega
Generación de actas y listas de chequeo para activos tecnológicos.

Modalidad de la entrega    ● Equipo    ○ Periférico
────────────────────────────────────────────────────
[Información del Acta]  [Activo Principal]  [Hardware y Software]
```

**Por qué:** cuando vivía dentro del encabezado de la columna central, competía
por el ancho con el título y con el botón "Añadir Activo", sobrecargaba el
encabezado y hacía parecer la modalidad un detalle secundario. Además, al salir
de la columna, el encabezado de "Activo Principal" quedó **idéntico** al de las
otras dos (título + botón en una línea) y las tres cards arrancan **a la misma
altura sin ningún ajuste extra** (desfase medido: **0 px** de 1025 a 1920 px).

Historial de este detalle: la versión anterior había metido la modalidad
*dentro* del encabezado de la columna central, compensando su altura para
alinear (desfase residual 8 px, y 58 px a exactamente 1025 px). Sacarla de la
columna eliminó la necesidad de cualquier compensación.

### Por qué no se usan catálogos para periféricos

La descripción del periférico es **texto libre**. Un catálogo cerrado obligaría a
mantener una lista maestra de periféricos (mouse, teclados, docks, adaptadores,
diademas…), que cambia constantemente y no aporta valor al acta. El acta solo
necesita describir lo entregado; la búsqueda GLPI no aplica porque un periférico
no es un activo inventariado con serial.

### Por qué Inventario es opcional en periféricos

En el flujo de Equipo el inventario es obligatorio porque GLPI lo garantiza. Un
periférico **puede no tener inventario** (o no estar registrado). Exigirlo
bloquearía entregas legítimas, así que es texto libre y opcional, sin validaciones
especiales. Se acepta tal cual: `INV-12345`, `CF-000567`, `MON-2026-18`, `458712`
o vacío.

### Por qué se eliminó el checklist para periféricos

La lista de chequeo verifica configuración de seguridad de un **equipo de
cómputo** (antivirus, DLP, cifrado de disco, grupos de administradores, software
base, SO…). Nada de eso aplica a un mouse o a un monitor. Generarla vacía sería
ruido y daría apariencia de control inexistente.

---

## Generación documental

### Equipo

Genera **Acta de Entrega + Lista de Chequeo** (2 DOCX en el ZIP).

### Periférico

Genera **Acta de Entrega** únicamente.

**No genera:** Lista de Chequeo.

### Motor de plantillas

`DocxTemplateEngine` reemplaza `{{ placeholder }}` directamente en
`word/document.xml` del DOCX (a nivel de *run*), no con la API de alto nivel de
POI. Los ZIP/DOCX se guardan en `app.generated-dir`
(por defecto `java.io.tmpdir/actas_glpi_generados`). La descarga va por
`GET /descargar-acta/{nombreZip}`; `/generar-acta` devuelve **JSON** con el
nombre del ZIP, no el binario.

### Variables de plantilla (indexadas)

| Acta | Prefijo | Máx. | Campos |
|---|---|---|---|
| Entrega | `eq_N_` | 10 | marca, tipo, modelo, serial, inventario |
| Entrega | `hw_N_` | 11 | tipo, descripcion, programa |
| Checklist | `chk_N_si` / `chk_N_no` | 36 | `"X"` o `""` |
| Checklist | `win10` / `win11` / `macos` | 1 | `"X"` si coincide el SO |
| Devolución | `eq_N_` | 10 | + **estado** |
| Devolución | `ot_N_` | 10 | tipo |
| Formateo | `eq_N_` | 4 | + **gb** |

Fecha: `dia`, `mes`, `anio`.

---

## Integración GLPI

### Autenticación

`GlpiClient` hace `POST /initSession` con `App-Token` y `User-Token`, y usa la
`Session-Token` resultante para las consultas. Variables **obligatorias** en
`.env` (en la raíz del proyecto, no dentro de `backend/`):

```
GLPI_URL=http://10.86.1.33/glpi/apirest.php
GLPI_APP_TOKEN=...
GLPI_USER_TOKEN=...
```

`GLPI_APP_TOKEN` y `GLPI_USER_TOKEN` **no tienen valor por defecto: si faltan,
la aplicación NO ARRANCA**. `GLPI_URL` sí tiene default
(`http://10.86.1.33/glpi/apirest.php`).

### Búsqueda de equipos

`GET /equipo/{serial}` → `POST /initSession` → `GET /search/Computer`. Obtiene
los campos `23` (marca), `4` (tipo), `40` (modelo) y `17` (CPU). El nombre del
procesador se **abrevia** para el acta (`EquipoService.cpuCorto`):

| GLPI (campo 17) | Acta |
|---|---|
| `Intel(R) Core(TM) i5-12400` | Core i5 |
| `AMD Ryzen 5 5600X` | Ryzen 5 |
| `12th Gen Intel(R) Core(TM) i7-12700K` | Core i7 |
| `Intel(R) Xeon E5-2620` | Xeon |

El modelo final se concatena con el sufijo de CPU. El serial del usuario se
inserta en la query **sin URL-encode** (a diferencia de `UsuarioService`, que sí
usa `URLEncoder`).

### Comportamiento ante fallo — **importante**

Si GLPI no responde o el equipo no existe, el backend devuelve un
`EquipoResponse` con **marca, tipo y modelo vacíos**. **Nunca rompe el flujo por
un error de GLPI**: el manejo es silencioso y sin log. Esto es deliberado, pero
tiene una consecuencia operativa: **un fallo de GLPI se ve como "datos vacíos",
no como error**. Mismo patrón en `UsuarioService`.

### ⚠️ Riesgo de seguridad activo: validación TLS desactivada

`GlpiClient` **desactiva por completo la validación TLS** para su conexión: usa un
`SSLContext` con un `X509TrustManager` que acepta cualquier certificado y un
`HostnameVerifier` que **siempre devuelve `true`**. El comentario `ponytail/security:`
en el código explica el motivo (el host de producción sirve un wildcard que no lo
cubre) y pide reemplazarlo por un fix de SAN en el certificado.

**Esto deja la conexión con GLPI expuesta a un ataque man-in-the-middle.** Además
es **redundante e incoherente** con el fix del certificado que propone `docker.md`:
el código ya ignora la validación, así que importar el intermedio GeoTrust no
tendría efecto mientras este bloque siga presente. Está anotado como pendiente en
la sección correspondiente, pero **conviene resolverlo antes de cualquier
despliegue a producción fuera de la red interna**.

### Autocompletado de usuarios

`GET /usuarios?texto=...` se activa con **3+ caracteres**. El texto se separa por
espacios; cada término busca `contains` en firstname (`9`), realname (`34`) y
login (`1`) encadenados con **OR**; los grupos se encadenan con **AND**. Limita a
10 resultados. Así *"Julian Celis"* encuentra a un usuario con
`firstname = "Julian Alejandro"` y `realname = "Celis Valderrama"` aunque las
palabras no sean consecutivas. Campos: Entrega (entregado a / por), Devolución
(entregado por / recibido por), Formateo (entregado a / por).

---

## Docker

**Estado real: propuesto, no implementado.** No hay `Dockerfile` ni
`docker-compose.yml` en el repositorio. `docker.md` (sin seguimiento en git)
contiene la propuesta.

### Configuración especial relevante: certificado TLS de GLPI de producción

Este es el punto no obvio que **debe** sobrevivir en el Dockerfile cuando se
implemente:

- **Síntoma:** tras apuntar a producción, la app muestra **datos vacíos, sin
  errores visibles**.
- **Causa raíz:** el servidor HTTPS de GLPI en producción
  (`sac-i.connser.com.co`) envía una **cadena incompleta** (solo el certificado
  hoja `*.connser.com.co`, falta el intermedio `GeoTrust TLS RSA CA G1`). El
  truststore de Java no puede construir la ruta de confianza y lanza
  `SSLHandshakeException: PKIX path building failed`. Los servicios capturan la
  excepción en un `catch` genérico y devuelven resultados vacíos.
- **Fix cliente (aplicado en la propuesta):** importar el intermedio al
  truststore de la JVM:

  ```dockerfile
  COPY certs/geotrust-tls-rsa-ca-g1.der /certs/geotrust-tls-rsa-ca-g1.der
  RUN keytool -cacerts -storepass changeit -importcert -noprompt \
      -alias geotrust-tls-rsa-ca-g1 -file /certs/geotrust-tls-rsa-ca-g1.der
  ```
- **Fix servidor (correcto, pendiente del admin de GLPI):** completar la cadena
  HTTPS en `sac-i.connser.com.co` añadiendo el intermedio
  (`SSLCertificateChainFile` / nginx concatenado al `ssl_certificate`). Eso
  corrige la conexión para **cualquier** cliente, no solo Java.
- Intermedio descargado de `https://cacerts.digicert.com/GeoTrustTLSRSACAG1.crt`,
  guardado en `backend/certs/geotrust-tls-rsa-ca-g1.der`.

> ⚠️ **El fix cliente del Dockerfile es HOY inoperante.** `GlpiClient` ya
> desactiva la validación TLS por completo (ver *Riesgo de seguridad activo*
> arriba), así que importar el intermedio no cambia nada mientras ese bloque
> siga en el código. Las dos soluciones coexisten de forma redundante. El orden
> correcto es: primero corregir la cadena del lado del servidor GLPI, después
> quitar el trust-all de `GlpiClient`, y solo entonces el `keytool` del
> Dockerfile pasa a ser la red de seguridad que se pretendía.

### Puertos y configuración

- **Backend:** `server.port: 8087` en `application.yml`.
- **`frontend/js/config.js`** resuelve `API_URL` así: `window.API_URL` si existe;
  si no, `http://{hostname}:8087`. **Debe cargarse antes que el resto de scripts.**
  Ningún HTML del repo define `window.API_URL`, así que en la práctica siempre
  cae al puerto 8087.
- **CORS:** `cors.allowed-origins` con default
  `http://127.0.0.1, http://localhost, :5500, :5501, :8080, :8001`.
  Sobrescribible con `CORS_ALLOWED_ORIGINS`. **Si sirves el frontend en un puerto
  que no esté en esa lista, el navegador bloquea por CORS** (pasó con 5502).
  Dos detalles: la lista por defecto **sigue teniendo `:8001`, no `:8087`**; y el
  split por comas no hace `trim()`, así que `CORS_ALLOWED_ORIGINS=a, b` con
  espacio deja el segundo origen sin coincidir.

---

## Deuda de documentación (los .md mienten en estos puntos)

El árbol tiene cambios **sin commitear** que los documentos todavía no reflejan.
Si una IA lee solo los `.md`, va a construir mal. Divergencias confirmadas
código vs documentación:

| # | Dicen los .md | Dice el código |
|---|---|---|
| 1 | Puerto backend **8001** (en *todos* los .md) | **8087** — `application.yml` y `config.js` |
| 2 | ZIP `ActaLista_{serial}_{asunto}.zip` | `ActaEntrega_{serial}_{entregado_a}.zip` |
| 3 | ZIP `Devolucion_` / `FormateoSeguro_` con asunto/motivo | `ActaDevolucion_` / `ActaFormateoSeguro_` con `entregado_por` |
| 4 | "Sin tests automatizados, `backend/src/test` vacío" | Existen `ActaRequestTest.java` (5 tests) y `frontend/js/app.test.js` (13 pruebas) |
| 5 | "`OtroElementoItem` no se usa — DTO muerto" | Se usa: `DevolucionRequest` declara `List<OtroElementoItem>` |
| 6 | Entrega no tiene concepto de modalidad | Tiene `modo` EQUIPO/PERIFERICO |
| 7 | `hw_N_` indexa 1..11 | Inicializa 16 slots y rellena 11 |
| 8 | Nada | TLS trust-all en `GlpiClient`; `.env` duplicado con tokens de producción |

**Consecuencia práctica:** cualquier `curl http://127.0.0.1:8001/...` que copies
de los docs está roto hoy — usa **8087**.

---

## Riesgo operativo: `.env` duplicado

Existen **dos copias** del archivo de secretos con los tokens reales de GLPI de
producción en claro:

- `.env` (raíz del proyecto) — es la que carga `AppConfig`.
- `.vscode/.env` — copia idéntica.

Ambas están fuera de git (`.gitignore` excluye `.env` y `.env.*`, y `.vscode/*`
salvo `settings.json`), pero **siguen siendo secretos de producción duplicados en
disco**. `AppConfig` lee la ruta relativa `../.env`, lo que la hace dependiente
del directorio de trabajo — motivo por el cual probablemente se creó la copia en
`.vscode/`. Vale la pena consolidar en una sola ubicación y rotar los tokens si
alguno estuvo alguna vez expuesto.

---

## Pendientes

**Seguridad (prioridad alta):**

- **Quitar el trust-all TLS de `GlpiClient`.** Es un riesgo MITM activo en la
  conexión con GLPI. Requiere primero corregir el certificado del lado servidor.
- **Consolidar el `.env` duplicado** (`.env` + `.vscode/.env`) con tokens de
  producción, y rotar los tokens si alguno pudo exponerse.
- **Añadir autenticación.** Hoy los 6 endpoints están abiertos, sin login ni
  Spring Security.
- **URL-encode del serial** en `EquipoService` (hoy se concatena crudo a la query).

**Infraestructura y operación:**

- **Implementar Docker de verdad** (`Dockerfile` backend + frontend, compose) —
  `docs/GUIA_DESPLIEGUE_DOCKER.md` es un runbook completo pero aspiracional: su
  sección de archivos a crear nunca se materializó.
- **Corregir la cadena TLS en el servidor GLPI de producción** (lado admin).
- **Limpieza de archivos generados.** El volumen de ZIP/DOCX crece sin límite: el
  sistema no elimina los antiguos.
- **Sin CI/CD.**
- **Plantillas dentro del JAR:** editarlas exige recompilar.

**Calidad:**

- **Logging en los fallos de GLPI.** Hoy el manejo es silencioso por diseño, lo
  que convierte un problema de infraestructura en "datos vacíos".
- **Falta validación de servidor de `EquipoItem`.** La obligatoriedad de la
  Descripción en periférico vive solo en el frontend; la API acepta lo que le
  manden.
- **`modo` es un `String` libre, sin enum.** Un valor inválido se interpreta como
  EQUIPO en silencio.
- **Sin paginación robusta en la búsqueda de equipos:** se toma el primer
  resultado e ignora el resto.
- **Placeholders sin rellenar quedan literales** en el DOCX (`{{ var }}`).

**Evolución funcional:**

- **Extender la modalidad Equipo/Periférico a Devolución y Formateo** (hoy solo
  existe en Entrega). No está pedido; se anota por si surge.
- **Columna propia para "Descripción" en la plantilla de periférico**, en vez de
  reutilizar el campo `tipo` del `EquipoItem`.

**Documentación:**

- **Alinear los `.md` con el código** en los 8 puntos de la tabla *Deuda de
  documentación* (puerto 8087, naming de ZIP, tests existentes, etc.).

---

## Historial de decisiones importantes

| Fecha | Decisión | Motivo |
|---|---|---|
| — | **Modalidad global Equipo/Periférico** | No se pueden mezclar tipos en una misma acta; el checklist, las validaciones y la generación dependen de ella |
| — | **Checklist eliminado en Periférico** | Verifica configuración de seguridad de un equipo de cómputo; no aplica a un periférico |
| — | **Inventario opcional en Periférico** | Un periférico puede no estar inventariado; exigirlo bloquearía entregas legítimas |
| — | **Descripción de periférico como texto libre** | Evita mantener un catálogo maestro que cambia constantemente |
| — | **`numero_sac` y `sistema_operativo` dejan de ser obligatorios en Periférico** | Solo se imprimen en la lista de chequeo, que no se genera en ese modo |
| — | **Sin generación de PDF** | Alcance del proyecto: solo DOCX empaquetados en ZIP |
| 2026-10-01 | **Modalidad movida al encabezado de "Activo Principal"** (luego revertida) | Primer intento de alinear las tres columnas compensando alturas |
| 2026-10-01 | **Modalidad sacada de la columna → fila global bajo el hero** | Es una propiedad global del acta; además dejó el encabezado central limpio y las tres columnas alineadas a 0 px sin compensaciones |

---

## Documentos relacionados

| Documento | Contenido |
|---|---|
| [`README.md`](../README.md) | Visión general, instalación, endpoints *(desactualizado en puerto y nombres de ZIP)* |
| [`ARQUITECTURA.md`](../ARQUITECTURA.md) | Capas, stack, GLPI, generación, CORS |
| [`DOCUMENTACION_TECNICA.md`](../DOCUMENTACION_TECNICA.md) | Especificación detallada de endpoints, DTOs, motor DOCX, ZIP |
| [`FLUJO_FUNCIONAL.md`](../FLUJO_FUNCIONAL.md) | Paso a paso de cada acta, GLPI y generación |
| [`docker.md`](../docker.md) | Propuesta de despliegue + incidente del certificado TLS |
| [`docs/GUIA_PRUEBAS_MANUALES.md`](GUIA_PRUEBAS_MANUALES.md) | QA: casos positivos/negativos, curls reales, verificación DOCX |
| [`docs/GUIA_EDITAR_PLANTILLAS.md`](GUIA_EDITAR_PLANTILLAS.md) | Edición de plantillas Word, placeholders y límites |
| [`docs/GUIA_DESPLIEGUE_DOCKER.md`](GUIA_DESPLIEGUE_DOCKER.md) | Runbook de despliegue Docker |
| [`docs/GUIA_DESARROLLADOR.md`](GUIA_DESARROLLADOR.md) | Onboarding técnico |
| [`docs/MANTENIMIENTO.md`](MANTENIMIENTO.md) | Mantenimiento y evolución |
| [`docs/MANUAL_USUARIO.md`](MANUAL_USUARIO.md) | Manual de usuario |
