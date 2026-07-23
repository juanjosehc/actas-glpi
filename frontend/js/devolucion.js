/*
====================================================
ACTA DE DEVOLUCIÓN - FRONTEND
====================================================

Responsabilidades:

- Gestión de datos del acta de devolución.
- Administración dinámica de equipos (agregar/eliminar/buscar).
- Administración dinámica de otros elementos hardware (agregar/eliminar).
- Validaciones de formulario y equipos.
- Construcción del payload para el backend.
- Descarga automática del ZIP generado.

Diferencias con entrega:

- Incluye campo "Estado" por cada equipo (obligatorio).
- No incluye checklist ni sistema operativo.
- No incluye hardware detallado (solo tipo).
- Incluye campos de jefe directo (nombre + cargo).

Endpoints utilizados:

- GET  /equipo/{serial}          → Consulta equipo en GLPI por serial.
- POST /generar-devolucion       → Genera acta de devolución (DOCX).
- GET  /descargar-acta/{archivo} → Descarga el ZIP generado.

Flujo principal:

1. Usuario completa campos obligatorios.
2. Click en "Generar Devolución" ejecuta generarDevolucion().
3. Se validan campos y equipos (serial, inventario, estado).
4. Se construye el payload completo.
5. Se envía POST al backend.
6. Se recibe nombre del ZIP y se descarga automáticamente.

====================================================
*/

/**
 * Genera el acta de devolución.
 *
 * Flujo:
 * 1. Validar campos obligatorios (fecha, entregado_por, cedula, etc.).
 * 2. Validar que cada equipo tenga serial, inventario y estado.
 * 3. Construir objetos de hardware y equipos.
 * 4. Armar el payload completo con campos de jefe directo.
 * 5. Enviar POST a /generar-devolucion.
 * 6. Descargar el ZIP resultante vía /descargar-acta.
 */
async function generarDevolucion() {

    try {

        const camposObligatorios = [

            "fecha",
            "entregado_por",
            "cedula",
            "cargo_entrega",
            "recibido_por",
            "cargo_recibe",
            "area_recibe",
            "motivo",
            "nombre_jefe",
            "cargo_jefe"

        ];

        let primerCampoInvalido = null;

        camposObligatorios.forEach(id => {

            const valido = validarCampo(id);

            if (!valido && !primerCampoInvalido) {

                primerCampoInvalido =
                    document.getElementById(id);

            }

        });

        const errorEquipo = validarEquipos();

        if (primerCampoInvalido) {

            primerCampoInvalido.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });

            setTimeout(() => {
                primerCampoInvalido.focus();
            }, 300);

            mostrarMensaje(
                "Complete los campos obligatorios",
                "error"
            );

            return;
        }

        if (errorEquipo) {

            errorEquipo.elemento.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });

            setTimeout(() => {
                errorEquipo.elemento.focus();
            }, 300);

            mostrarMensaje(
                `Debe completar: ${errorEquipo.nombre}`,
                "error"
            );

            return;
        }

        const hardware = [];

        document
            .querySelectorAll(".hardware-item")
            .forEach(item => {

                hardware.push({

                    tipo:
                        item.querySelector(
                            "[data-tipo]"
                        ).value,

                });

            });

        const equipos = [];

        document
            .querySelectorAll(".equipo-item")
            .forEach(item => {

                equipos.push({

                    serial:
                        item.querySelector(
                            "[data-serial]"
                        ).value,

                    marca:
                        item.querySelector(
                            "[data-marca]"
                        ).value,

                    tipo:
                        item.querySelector(
                            "[data-tipo]"
                        ).value,

                    modelo:
                        item.querySelector(
                            "[data-modelo]"
                        ).value,

                    inventario:
                        item.querySelector(
                            "[data-inventario]"
                        ).value,

                    estado:
                        item.querySelector(
                            "[data-estado]"
                        ).value

                });

            });

        if (
            !document.getElementById(
                "fecha"
            ).value
        ) {

            mostrarMensaje(
                "Debe seleccionar una fecha",
                "error"
            );

            return;

        }

        const payload = {

            fecha:
                document.getElementById("fecha")?.value || "",

            recibido_por:
                document.getElementById("recibido_por")?.value || "",

            entregado_por:
                document.getElementById("entregado_por")?.value || "",

            cargo_recibe:
                document.getElementById("cargo_recibe")?.value || "",

            cargo_entrega:
                document.getElementById("cargo_entrega")?.value || "",

            cedula:
                document.getElementById("cedula")?.value || "",

            area_recibe:
                document.getElementById("area_recibe")?.value || "",

            motivo:
                document.getElementById("motivo")?.value || "",

            nombre_jefe:
                document.getElementById("nombre_jefe")?.value || "",

            cargo_jefe:
                document.getElementById("cargo_jefe")?.value || "",

            hardware:
                hardware,

            equipos:
                equipos,

            observaciones:
                document.getElementById("observaciones")?.value || ""

        };

        const response = await fetch(
            "http://127.0.0.1:8001/generar-devolucion",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {

            const errorData =
                await response.json();

            throw new Error(
                errorData.mensaje ||
                "No fue posible generar la documentación"
            );

        }

        const result =
            await response.json();

        if (!result.success) {

            throw new Error(
                result.mensaje ||
                "Error generando la documentación"
            );

        }

        mostrarMensaje(
            "Documentación generada correctamente",
            "success"
        );

        const descargaResponse = await fetch(
            "http://127.0.0.1:8001/descargar-acta/" +
            result.nombre_zip
        );

        if (!descargaResponse.ok) {
            throw new Error("Error descargando el archivo");
        }

        const blob =
            await descargaResponse.blob();

        const blobUrl =
            URL.createObjectURL(blob);

        const linkDescarga =
            document.createElement("a");

        linkDescarga.href = blobUrl;

        linkDescarga.download =
            result.nombre_zip;

        document.body.appendChild(
            linkDescarga
        );

        linkDescarga.click();

        linkDescarga.remove();

        URL.revokeObjectURL(blobUrl);


    }

    catch (error) {

        mostrarMensaje(
            "Error generando la documentación: " + error.message,
            "error"
        );

    }

}


/*
----------------------------------------------------
INICIALIZACIÓN
----------------------------------------------------
*/

/**
 * Inicializa la página al cargar el DOM.
 *
 * Acciones:
 * - Vincula botones de agregar hardware y equipo.
 * - Crea un equipo y un hardware vacíos por defecto.
 * - Limpia errores de validación al escribir en cualquier campo.
 * - Inicializa el datepicker en el campo de fecha.
 */
document.addEventListener("DOMContentLoaded", () => {

    const btnHardware =
        document.getElementById("btn-add-hardware");

    if (btnHardware) {
        btnHardware.addEventListener("click", agregarHardware);
    }

    const btnEquipo =
        document.getElementById("btn-add-equipo");

    if (btnEquipo) {
        btnEquipo.addEventListener("click", agregarEquipo);
    }

    agregarEquipo();
    agregarHardware();

    document
        .querySelectorAll(".input, .textarea")
        .forEach(campo => {

            campo.addEventListener("input", () => {

                if (campo.value.trim()) {

                    campo.classList.remove("is-invalid");

                    const helper =
                        campo.parentElement.querySelector(
                            ".helper-text"
                        );

                    if (helper) {
                        helper.style.display = "none";
                    }
                }

            });

        });

    flatpickr("#fecha", {
        dateFormat: "Y-m-d",
        monthSelectorType: "static",
        allowInput: true
    });

});

/*
----------------------------------------------------
ADMINISTRACIÓN DINÁMICA DE HARDWARE (OTROS ELEMENTOS)
----------------------------------------------------
*/

/**
 * Agrega un nuevo registro de hardware al formulario.
 *
 * En devolución solo se captura el tipo de hardware
 * (sin descripción ni programa como en entrega).
 * Límite máximo: 11 registros. No se permite eliminar
 * el último registro existente.
 */
function agregarHardware() {

    const container =
        document.getElementById("hardware-container");

    if (
        container.querySelectorAll(".hardware-item").length >= 11
    ) {

        mostrarMensaje(
            "Máximo 11 registros",
            "error"
        );

        return;
    }

    const numeroHardware =
        container.querySelectorAll(".hardware-item").length + 1;

    const fila =
        document.createElement("div");

    fila.className = "hardware-item";

    fila.innerHTML = `

        <div class="card border border-base-300 shadow-md">

            <div class="card-body p-2">

                <div class="item-header">

                    <h4>
                        Hardware     ${numeroHardware}
                    </h4>

                    <button
                        type="button"
                        data-eliminar
                        class="btn btn-outline">

                        Eliminar

                    </button>

                </div>

                <div class="input-floating w-full mb-1">

                <input
                    type="text"
                    class="input"
                    placeholder=" "
                    data-tipo />

                <label class="input-floating-label">

                    Tipo Hardware

                </label>

            </div>

        </div>

    `;

    fila
        .querySelector("[data-eliminar]")
        .addEventListener("click", () => {

            if (
                document.querySelectorAll(".hardware-item").length === 1
            ) {

                mostrarMensaje(
                    "Debe existir al menos un hardware",
                    "error"
                );

                return;

            }

            fila.remove();

            renumerarHardware();

        });

    container.appendChild(fila);

    renumerarHardware();

}

/*
----------------------------------------------------
ADMINISTRACIÓN DINÁMICA DE EQUIPOS
----------------------------------------------------
*/

/**
 * Agrega un nuevo bloque de equipo al formulario.
 *
 * En devolución cada equipo incluye campo adicional "Estado".
 * Cada bloque contiene: serial, botón buscar, marca, tipo,
 * modelo, inventario y estado. Marca/tipo/modelo se
 * autocompletan desde GLPI al hacer click en "Buscar".
 * Se validan serial, inventario y estado antes de enviar.
 * Límite mínimo: 1 equipo (no se puede eliminar el último).
 */
function agregarEquipo() {

    const container =
        document.getElementById("equipos-container");

    const numeroEquipo =
        container.querySelectorAll(".equipo-item").length + 1;

    const equipo =
        document.createElement("div");

    equipo.className = "equipo-item";

    equipo.innerHTML = `

        <div class="card border border-base-300 shadow-sm">

            <div class="card-body p-2">

                <div class="item-header">

                    <h4>
                        Equipo ${numeroEquipo}
                    </h4>

                    <button
                        type="button"
                        data-eliminar
                        class="btn btn-outline">

                        Eliminar

                    </button>

                </div>
                <div class="input-floating w-full mb-1">

                    <input
                        type="text"
                        class="input"
                        placeholder=" "
                        data-serial />

                    <label class="input-floating-label">

                        Serial

                    </label>

                </div>

                <button
                    type="button"
                    data-buscar
                    class="btn btn-outline mb-4">

                    Buscar

                </button>

                <div class="input-floating w-full mb-1">

                <input
                    class="input"
                    placeholder=" "
                    data-marca
                    disabled />

                <label class="input-floating-label">
                    Marca
                </label>

            </div>

            <div class="input-floating w-full mb-1">

                <input
                    class="input"
                    placeholder=" "
                    data-tipo
                    disabled />

                <label class="input-floating-label">
                    Tipo
                </label>

            </div>

            <div class="input-floating w-full mb-1">

                <input
                    class="input"
                    placeholder=" "
                    data-modelo
                    disabled />

                <label class="input-floating-label">
                    Modelo
                </label>

            </div>

            <div class="input-floating w-full mb-1">

                <input
                    class="input"
                    placeholder=" "
                    data-inventario />

                <label class="input-floating-label">
                    Inventario
                </label>

            </div>

            <div class="input-floating w-full">

                <input
                    class="input"
                    placeholder=" "
                    data-estado />

                <label class="input-floating-label">
                    Estado
                </label>

            </div>

            </div>

        </div>

    `;

    container.appendChild(equipo);

    equipo
        .querySelectorAll(".input")
        .forEach(campo => {

            campo.addEventListener("input", () => {

                if (campo.value.trim()) {

                    campo.classList.remove("is-invalid");

                }

            });

        });

    renumerarEquipos();

    equipo
        .querySelector("[data-buscar]")
        .addEventListener("click", () => buscarEquipoBloque(equipo));

    equipo
        .querySelector("[data-eliminar]")
        .addEventListener("click", () => {

            if (
                document.querySelectorAll(".equipo-item").length === 1
            ) {

                mostrarMensaje(
                    "Debe existir al menos un equipo",
                    "error"
                );

                return;

            }

            equipo.remove();

            renumerarEquipos();

        });

}

/**
 * Consulta GLPI por serial y auto completa marca, tipo y modelo.
 *
 * Endpoint: GET /equipo/{serial}
 * Los campos se actualizan dentro del bloque del equipo
 * al que pertenece el botón "Buscar".
 *
 * @param {HTMLElement} bloque - Elemento .equipo-item que contiene los campos.
 */
async function buscarEquipoBloque(bloque) {

    const serial =
        bloque.querySelector("[data-serial]").value;

    const response =
        await fetch(`http://127.0.0.1:8001/equipo/${serial}`);

    const data =
        await response.json();

    bloque.querySelector("[data-marca]").value =
        data.marca ?? "";

    bloque.querySelector("[data-tipo]").value =
        data.tipo ?? "";

    bloque.querySelector("[data-modelo]").value =
        data.modelo ?? "";

}

/*
----------------------------------------------------
UTILIDADES DE RENUMERACIÓN
----------------------------------------------------
*/

/**
 * Actualiza los títulos "Equipo N" después de agregar o eliminar.
 *
 * Recorre todos los .equipo-item y asigna el número
 * secuencial basado en su posición actual en el DOM.
 */
function renumerarEquipos() {

    document
        .querySelectorAll(".equipo-item")
        .forEach((equipo, index) => {

            equipo.querySelector("h4").textContent =
                `Equipo ${index + 1}`;

        });

}

/**
 * Actualiza los títulos "Hardware N" después de agregar o eliminar.
 *
 * Mismo comportamiento que renumerarEquipos pero para
 * los bloques de hardware.
 */
function renumerarHardware() {

    document
        .querySelectorAll(".hardware-item")
        .forEach((hardware, index) => {

            hardware.querySelector("h4").textContent =
                `Hardware ${index + 1}`;

        });

}

/*
----------------------------------------------------
VALIDACIONES
----------------------------------------------------
*/

/**
 * Valida un campo obligatorio por su ID.
 *
 * Aplica la clase "is-invalid" y muestra el helper-text
 * si el campo está vacío. Remueve ambos si tiene valor.
 *
 * @param {string} id - ID del elemento input a validar.
 * @returns {boolean} true si el campo tiene valor, false si está vacío.
 */
function validarCampo(id) {

    const campo =
        document.getElementById(id);

    const helper =
        campo.parentElement.querySelector(".helper-text");

    const vacio =
        !campo.value.trim();

    if (vacio) {

        campo.classList.add("is-invalid");

        if (helper) {
            helper.style.display = "block";
        }

        return false;
    }

    campo.classList.remove("is-invalid");

    if (helper) {
        helper.style.display = "none";
    }

    return true;
}

/**
 * Valida los equipos agregados dinámicamente.
 *
 * En devolución valida tres campos por equipo: serial, inventario y estado.
 * Retorna el primer error encontrado para permitir scroll
 * automático y foco en el campo inválido.
 *
 * @returns {Object|null} Primer error: { elemento, nombre } o null si todo es válido.
 */
function validarEquipos() {

    let primerError = null;

    document
        .querySelectorAll(".equipo-item")
        .forEach((equipo, index) => {

            const campos = [
                {
                    elemento: equipo.querySelector("[data-serial]"),
                    nombre: `Serial del Equipo ${index + 1}`
                },
                {
                    elemento: equipo.querySelector("[data-inventario]"),
                    nombre: `Inventario del Equipo ${index + 1}`
                },
                {
                    elemento: equipo.querySelector("[data-estado]"),
                    nombre: `Estado del Equipo ${index + 1}`
                }
            ];

            campos.forEach(campo => {

                const vacio =
                    !campo.elemento?.value?.trim();

                if (vacio) {

                    campo.elemento.classList.add("is-invalid");

                    if (!primerError) {

                        primerError = {
                            elemento: campo.elemento,
                            nombre: campo.nombre
                        };

                    }

                } else {

                    campo.elemento.classList.remove("is-invalid");

                }

            });

        });

    return primerError;
}
