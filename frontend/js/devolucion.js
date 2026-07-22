async function buscarEquipo() {

    const serial =
        document.getElementById("serial").value;

    const response =
        await fetch(
            `http://127.0.0.1:8001/equipo/${serial}`
        );

    const data =
        await response.json();

        document.getElementById("marca").value =
            data.marca ?? "";

        document.getElementById("tipo").value =
            data.tipo ?? "";

        document.getElementById("modelo").value =
            data.modelo ?? "";

        document.getElementById("serial_acta").value =
            document.getElementById("serial").value;
}

async function generarDevolucion() {
    console.log("ENTRO A GENERAR ACTA");
    
    try {

        const hardware = [];
        const checklist = {};

        for (let i = 1; i <= 36; i++) {

            checklist[`chk_${i}`] =
                document.getElementById(
                    `chk_${i}`
                )?.checked ?? false;

        }

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

            cedula_entrega:
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

        console.log("ANTES DEL FETCH");

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

        console.log("DESPUES DEL FETCH");

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

        console.log("RESULTADO:", result);

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

        console.log("INICIANDO DESCARGA:", result.nombre_zip);

        const descargaResponse = await fetch(
            "http://127.0.0.1:8001/descargar-acta/" +
            result.nombre_zip
        );

        console.log("RESPUESTA DESCARGA:", descargaResponse.status);

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

        console.error("ERROR COMPLETO:", error);
        console.error("MENSAJE:", error.message);

        mostrarMensaje(
            "Error generando la documentación: " + error.message,
            "error"
        );

    }

}


document.addEventListener(
    "DOMContentLoaded",
    () => {

        const btnHardware =
            document.getElementById(
                "btn-add-hardware"
            );

        if (btnHardware) {

            btnHardware.addEventListener(
                "click",
                agregarHardware
            );

        }

        const btnEquipo =
            document.getElementById(
                "btn-add-equipo"
            );

        if (btnEquipo) {

            btnEquipo.addEventListener(
                "click",
                agregarEquipo
            );

        }

        agregarEquipo();

        agregarHardware();

    }
);

function agregarHardware() {

    const container =
        document.getElementById(
            "hardware-container"
        );

    if (
        container.querySelectorAll(
            ".hardware-item"
        ).length >= 11
    ) {

        mostrarMensaje(
            "Máximo 11 registros",
            "error"
        );

        return;
    }

    const numeroHardware =
        container.querySelectorAll(
            ".hardware-item"
        ).length + 1;

    const fila =
        document.createElement(
            "div"
        );

    fila.className =
        "hardware-item";

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
        .querySelector(
            "[data-eliminar]"
        )
        .addEventListener(
            "click",
            () => {

                if (
                    document.querySelectorAll(
                        ".hardware-item"
                    ).length === 1
                ) {

                    mostrarMensaje(
                        "Debe existir al menos un hardware",
                        "error"
                    );

                    return;

                }

                fila.remove();

                renumerarHardware();

            }
        );

    container.appendChild(
        fila
    );

    renumerarHardware();

}

function agregarEquipo() {

    const container =
        document.getElementById(
            "equipos-container"
        );

    const numeroEquipo =
        container.querySelectorAll(
            ".equipo-item"
        ).length + 1;

    const equipo =
        document.createElement(
            "div"
        );

    equipo.className =
        "equipo-item";

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
                    readonly />

                <label class="input-floating-label">
                    Marca
                </label>

            </div>

            <div class="input-floating w-full mb-1">

                <input
                    class="input"
                    placeholder=" "
                    data-tipo
                    readonly />

                <label class="input-floating-label">
                    Tipo
                </label>

            </div>

            <div class="input-floating w-full mb-1">

                <input
                    class="input"
                    placeholder=" "
                    data-modelo
                    readonly />

                <label class="input-floating-label">
                    Modelo
                </label>

            </div>

            <div class="input-floating w-full">

                <input
                    class="input"
                    placeholder=" "
                    data-inventario />

                <label class="input-floating-label">
                    Inventario
                </label>

            </div>

            </div>

        </div>

    `;

    container.appendChild(
        equipo
    );

    renumerarEquipos();

    equipo
        .querySelector(
            "[data-buscar]"
        )
        .addEventListener(
            "click",
            () => buscarEquipoBloque(
                equipo
            )
        );

    equipo
        .querySelector(
            "[data-eliminar]"
        )
        .addEventListener(
            "click",
            () => {

                if (
                    document.querySelectorAll(
                        ".equipo-item"
                    ).length === 1
                ) {

                    mostrarMensaje(
                        "Debe existir al menos un equipo",
                        "error"
                    );

                    return;

                }

                equipo.remove();

                renumerarEquipos();

            }
        );

}

async function buscarEquipoBloque(
    bloque
) {

    const serial =
        bloque.querySelector(
            "[data-serial]"
        ).value;

    const response =
        await fetch(
            `http://127.0.0.1:8001/equipo/${serial}`
        );

    const data =
        await response.json();

    bloque.querySelector(
        "[data-marca]"
    ).value =
        data.marca ?? "";

    bloque.querySelector(
        "[data-tipo]"
    ).value =
        data.tipo ?? "";

    bloque.querySelector(
        "[data-modelo]"
    ).value =
        data.modelo ?? "";

}

function renumerarEquipos() {

    document
        .querySelectorAll(".equipo-item")
        .forEach((equipo, index) => {

            equipo.querySelector("h4").textContent =
                `Equipo ${index + 1}`;

        });

}

function renumerarHardware() {

    document
        .querySelectorAll(".hardware-item")
        .forEach((hardware, index) => {

            hardware.querySelector("h4").textContent =
                `Hardware ${index + 1}`;

        });

}

document.addEventListener(
    "DOMContentLoaded",
    () => {

        const entregadoPor =
            document.getElementById(
                "entregado_por"
            );

        const responsable =
            document.getElementById(
                "responsable_verificacion"
            );

        if (
            entregadoPor &&
            responsable
        ) {

            entregadoPor.addEventListener(
                "input",
                () => {

                    responsable.value =
                        entregadoPor.value;

                }
            );

        }

    }
);

window.addEventListener("load", () => {

    flatpickr("#fecha", {
        dateFormat: "d-m-Y",
        monthSelectorType: "static",
        allowInput: true
    });

});



