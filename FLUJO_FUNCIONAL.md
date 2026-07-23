# Flujo Funcional

Este documento describe paso a paso el funcionamiento de cada tipo de acta, desde la captura de datos hasta la descarga del documento.

---

## Tabla de contenidos

1. [Flujo general del sistema](#1-flujo-general-del-sistema)
2. [Acta de Entrega](#2-acta-de-entrega)
3. [Acta de Devolución](#3-acta-de-devolución)
4. [Búsqueda de equipo en GLPI](#4-búsqueda-de-equipo-en-glpi)
5. [Generación de documentos Word](#5-generación-de-documentos-word)
6. [Empaquetado y descarga ZIP](#6-empaquetado-y-descarga-zip)
7. [Validaciones](#7-validaciones)

---

## 1. Flujo general del sistema

```mermaid
flowchart TD
    A[Usuario abre la página] --> B{¿Qué tipo de acta?}
    B -->|Entrega| C[acta-entrega.html]
    B -->|Devolución| D[acta-devolucion.html]
    C --> E[Completar formulario]
    D --> E
    E --> F[Buscar equipos en GLPI]
    F --> G[Agregar hardware y otros]
    G --> H[Completar checklist]
    H --> I[Click en Generar Acta]
    I --> J[Validar campos]
    J -->|Error| K[Mostrar error y scroll al campo]
    K --> E
    J -->|OK| L[Enviar POST al backend]
    L --> M[Backend genera DOCX]
    M --> N[Backend empaqueta ZIP]
    N --> O[Frontend descarga ZIP]
    O --> P[Usuario abre documentos]
```

---

## 2. Acta de Entrega

La acta de entrega genera **dos documentos**: el acta de entrega y la lista de chequeo.

### 2.1 Captura de datos

```mermaid
flowchart LR
    subgraph DatosActa["Datos del Acta"]
        A1[Fecha]
        A2[Entregado a]
        A3[Cargo quien recibe]
        A4[Entregado por]
        A5[Cargo quien entrega]
        A6[Asunto]
    end

    subgraph Equi["Equipos"]
        B1[Serial]
        B2[Buscar - GLPI auto-completa]
        B3[Inventario]
    end

    subgraph Hard["Hardware"]
        C1[Tipo]
        C2[Descripción]
        C3[Programa]
    end

    subgraph Check["Checklist"]
        D1[Número SAC]
        D2[Sistema operativo: Win10, Win11 o Mac]
        D3[36 checkboxes en 6 secciones]
    end
```

**Campos obligatorios del acta:** Fecha, Entregado a, Cargo quien recibe, Entregado por, Cargo quien entrega, Asunto, Número SAC.

**Campos obligatorios por equipo:** Serial, Inventario.

**Campos del checklist:** Sistema operativo (radio), 36 checkboxes agrupados.

### 2.2 Checklist - Secciones

| Sección | Checkboxes | Ejemplos |
|---------|-----------|----------|
| Seguridad y Configuración | 1–10 | Antivirus, DLP, Cifrado, Firewall |
| Software Base | 11–18 | Office, Adobe Reader, Java, 7-Zip |
| Sistema Operativo | 19–24 | NetBIOS, Wake On LAN, OneDrive |
| Conectividad | 25–27 | VPN, RDP, Impresoras |
| Aplicaciones Corporativas | 28–32 | Directorio Activo, Cobis, Cisco |
| Áreas Específicas | 33–36 | Comercio Exterior, Tesorería |

### 2.3 Envío y respuesta

```mermaid
sequenceDiagram
    participant U as Frontend
    participant B as Backend
    participant W as WordService
    participant Z as ZipService

    U->>B: POST /generar-acta
    Note over U,B: Payload: fecha, entregado_a, equipos, hardware, checklist

    B->>B: Validar @NotBlank en ActaRequest
    alt Validacion falla
        B-->>U: 400 con error
    end

    B->>W: generarActa datos
    W->>W: Preparar fecha en dia, mes, anio
    W->>W: Indexar 11 hardware items
    W->>W: Indexar 10 equipos
    W->>W: Procesar template Acta de Entrega
    W-->>B: Archivo ActaEntrega DOCX

    B->>W: generarChecklist datos
    W->>W: Preparar SO con cuadrados
    W->>W: Preparar 36 checkboxes
    W->>W: Solo primer equipo para identificacion
    W->>W: Procesar template ListaChequeo
    W-->>B: Archivo Checklist DOCX

    B->>Z: crearZip con acta y checklist
    Z-->>B: ZIP generado

    B-->>U: Respuesta con nombre_zip

    U->>U: GET /descargar-acta con nombre_zip
    U->>U: Crear enlace de descarga y hacer click
```

### 2.4 Documentos generados

| Documento | Contenido |
|-----------|-----------|
| ActaEntrega con serial y asunto | Acta de entrega con datos de entrega, equipos y hardware |
| Checklist con serial y asunto | Lista de 36 verificaciones con SO y datos del primer equipo |

---

## 3. Acta de Devolución

La acta de devolución genera **un solo documento**: el acta de devolución.

### 3.1 Captura de datos

```mermaid
flowchart LR
    subgraph DatosActa["Datos del Acta"]
        A1[Fecha]
        A2[Nombre quien entrega]
        A3[Cedula quien entrega]
        A4[Cargo quien entrega]
        A5[Recibido por]
        A6[Cargo quien recibe]
        A7[Area quien recibe]
        A8[Motivo devolucion]
        A9[Nombre jefe inmediato]
        A10[Cargo jefe inmediato]
    end

    subgraph Equi["Equipos"]
        B1[Serial]
        B2[Buscar GLPI]
        B3[Inventario]
        B4[Estado]
    end

    subgraph Otros["Otros Elementos"]
        C1[Tipo]
    end
```

> **Nota:** El campo **Estado** existe unicamente en el flujo de devolucion. El bloque **Otros Elementos** solo solicita el tipo de elemento y no incluye descripcion.

**Campos obligatorios:** Fecha, Nombre quien entrega, Cedula, Cargo quien entrega, Recibido por, Cargo quien recibe, Area quien recibe, Motivo, Nombre jefe, Cargo jefe.

**Campos obligatorios por equipo:** Serial, Inventario, **Estado**.

> **Diferencia clave con entrega:** El acta de devolucion NO incluye checklist ni sistema operativo. SI incluye campo "Estado" por cada equipo.

### 3.2 Envío y respuesta

```mermaid
sequenceDiagram
    participant U as Frontend
    participant B as Backend
    participant W as WordService
    participant Z as ZipService

    U->>B: POST /generar-devolucion
    Note over U,B: Payload: fecha, entregado_por, cedula, equipos, hardware

    B->>B: Validar @NotBlank en DevolucionRequest
    alt Validacion falla
        B-->>U: 400 con error
    end

    B->>W: generarDevolucion datos
    W->>W: Preparar fecha en dia, mes, anio
    W->>W: Indexar 10 equipos con estado
    W->>W: Indexar 10 otros elementos
    W->>W: Procesar template ActaDevolucion
    W-->>B: Archivo Devolucion DOCX

    B->>Z: crearZip con devolucion
    Z-->>B: ZIP generado

    B-->>U: Respuesta con nombre_zip

    U->>U: GET /descargar-acta con nombre_zip
    U->>U: Crear enlace de descarga y hacer click
```

### 3.3 Documento generado

| Documento | Contenido |
|-----------|-----------|
| Devolucion con serial y motivo | Acta de devolucion con datos de entrega y devolucion, equipos con estado y otros elementos |

---

## 4. Búsqueda de equipo en GLPI

Cuando el usuario hace click en "Buscar" dentro de un bloque de equipo:

```mermaid
flowchart TD
    A[Click en Buscar] --> B[Leer serial del input]
    B --> C[GET /equipo con serial]
    C --> D[Backend: POST /initSession]
    D --> E[Backend: GET /search/Computer]
    E --> F{¿Equipo encontrado?}
    F -->|No| G[Retornar marca tipo modelo vacios]
    F -->|Si| H[Extraer campos 23 4 40 17]
    H --> I[Abreviar CPU]
    I --> J[Concatenar modelo y sufijo CPU]
    J --> K[Retornar EquipoResponse]
    G --> L[Actualizar inputs deshabilitados]
    K --> L
    L --> M[Marca Tipo Modelo auto-completados]
```

**Procesamiento del CPU:**

El nombre completo del procesador se abrevia para el acta:

| GLPI campo 17 | Acta |
|-----------------|------|
| Intel Core i5-12400 | Core i5 |
| AMD Ryzen 5 5600X | Ryzen 5 |
| 12th Gen Intel Core i7-12700K | Core i7 |
| Intel Xeon E5-2620 | Xeon |

---

## 5. Generación de documentos Word

### 5.1 Motor de templates DocxTemplateEngine

El motor reemplaza placeholders en formato doble llave en documentos Word preservando el formato original.

```mermaid
flowchart TD
    A[Template DOCX] --> B[Copiar a archivo de salida]
    B --> C[Abrir con Apache POI]
    C --> D{¿Mas parrafos?}
    D -->|Si| E[Leer runs del parrafo]
    E --> F[Concatenar texto de todos los runs]
    F --> G{¿Contiene marcador de variable?}
    G -->|No| D
    G -->|Si| H[Buscar placeholders con regex]
    H --> I[Para cada run reconstruir texto]
    I --> J[Reemplazar placeholder con valor]
    J --> K[Guardar texto en el run]
    K --> D
    D -->|No| L[Procesar tablas]
    L --> M[Guardar documento]
```

### 5.2 Por qué a nivel de run

Cuando Word aplica formato diferente (negrita, color, tamaño) a partes de un mismo texto, lo fragmenta en multiples "runs". Ejemplo:

```
Run 1: "Serial: "           formato normal
Run 2: "placeholder_serial" formato negrita
Run 3: " "                  formato normal
```

El placeholder esta completamente en el Run 2. Este motor detecta en que run inicia el placeholder y escribe el valor ahi, preservando la negrita del Run 2.

### 5.3 Preparación de datos

Antes de pasar los datos al motor, DocumentoWordService transforma la informacion:

**Fecha:**

```
fecha: 2026-07-23  -->  dia: 23, mes: 07, anio: 2026
```

**Equipos indexados:**

```
equipos[0].marca = Dell      -->  eq_1_marca = Dell
equipos[0].serial = ABC123   -->  eq_1_serial = ABC123
equipos[1].marca = HP        -->  eq_2_marca = HP
```

**Hardware indexado:**

```
hardware[0].tipo = Monitor            -->  hw_1_tipo = Monitor
hardware[0].descripcion = 24 pulgadas -->  hw_1_descripcion = 24 pulgadas
```

**Checkboxes marcados y desmarcados:**

```
chk_1 = true   -->  chk_1_si = cuadrado lleno, chk_1_no = cuadrado vacio
chk_2 = false  -->  chk_2_si = cuadrado vacio, chk_2_no = cuadrado lleno
```

**Sistema operativo:**

```
sistema_operativo = Windows 11
  --> win10 = vacio, win11 = lleno, macos = vacio
```

---

## 6. Empaquetado y descarga ZIP

### 6.1 Creación del ZIP

```mermaid
flowchart LR
    A[DOCX 1 Acta] --> C[ZipOutputStream]
    B[DOCX 2 Checklist] --> C
    C --> D[ZIP con nombre basado en serial y asunto]
```

El nombre del ZIP se construye con:

- **Entrega:** ActaLista + serial del primer equipo + guion bajo + asunto sin caracteres especiales + .zip
- **Devolución:** Devolucion + serial del primer equipo + guion bajo + motivo sin caracteres especiales + .zip

Los caracteres especiales se eliminan del asunto o motivo con replaceAll.

### 6.2 Descarga

```mermaid
sequenceDiagram
    participant U as Frontend
    participant B as Backend
    participant N as Navegador

    U->>B: GET /descargar-acta con nombreZip
    B->>B: Verificar que el archivo existe
    alt Archivo no existe
        B-->>U: Error archivo no encontrado
    end
    B-->>U: 200 OK con Content-Type octet-stream
    Note over U: Content-Disposition attachment

    U->>U: Crear Blob desde response
    U->>U: Crear URL temporal
    U->>U: Crear elemento enlace con href y download
    U->>U: Agregar enlace al DOM
    U->>N: click en el enlace
    N->>N: Descargar archivo
    U->>U: Eliminar enlace del DOM
    U->>U: Revocar URL temporal
```

El navegador muestra la descarga en la barra de descargas. El usuario puede abrir el ZIP directamente.

---

## 7. Validaciones

### 7.1 Acta de Entrega

```mermaid
flowchart TD
    A[Click en Generar Acta] --> B{¿Campos obligatorios validos?}
    B -->|No| C[Marcar is-invalid]
    C --> D[Scroll al primer campo invalido]
    D --> E[Mostrar Complete los campos obligatorios]
    B -->|Si| F{¿Sistema operativo seleccionado?}
    F -->|No| G[Marcar radio-so-error en todos los radios]
    G --> H[Scroll al SO]
    H --> I[Mostrar Debe seleccionar un sistema operativo]
    F -->|Si| J{¿Equipos validos?}
    J -->|No| K[Marcar campos invalidos en equipo]
    K --> L[Scroll al primer error]
    L --> M[Mostrar Debe completar Serial o Inventario]
    J -->|Si| N[Construir payload]
    N --> O[Enviar POST]
    O --> P{¿Respuesta OK?}
    P -->|No| Q[Mostrar error del backend]
    P -->|Si| R[Mostrar Documentacion generada correctamente]
    R --> S[Descargar ZIP]
```

### 7.2 Acta de Devolución

Mismo flujo que entrega, con estas diferencias:

- **Campos obligatorios diferentes:** Incluye cedula, area, motivo, nombre y cargo jefe.
- **Sin validacion de SO:** No hay sistema operativo.
- **Validacion de equipo incluye Estado:** Serial, Inventario y Estado son obligatorios.
- **Sin checklist:** Se omite toda la seccion de verificacion.

### 7.3 Resumen de validaciones por campo

| Campo | Entrega | Devolución | Obligatorio |
|-------|---------|------------|-------------|
| Fecha | Si | Si | Si |
| Entregado a | Si | No | Si |
| Cargo quien recibe | Si | Si | Si |
| Entregado por | Si | Si | Si |
| Cargo quien entrega | Si | Si | Si |
| Asunto | Si | No | Si |
| Número SAC | Si | No | Si |
| Sistema operativo | Si | No | Si |
| Cédula | No | Si | Si |
| Área quien recibe | No | Si | Si |
| Motivo | No | Si | Si |
| Nombre jefe | No | Si | Si |
| Cargo jefe | No | Si | Si |
| Serial equipo | Si | Si | Si |
| Inventario equipo | Si | Si | Si |
| Estado equipo | No | Si | Si |
