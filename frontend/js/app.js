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

async function generarActa() {

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

                    descripcion:
                        item.querySelector(
                            "[data-descripcion]"
                        ).value,

                    programa:
                        item.querySelector(
                            "[data-programa]"
                        ).value

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
                document.getElementById("fecha").value,

            entregado_a:
                document.getElementById("entregado_a").value,

            cargo_recibe:
                document.getElementById("cargo_recibe").value,

            entregado_por:
                document.getElementById("entregado_por").value,

            cargo_entrega:
                document.getElementById("cargo_entrega").value,

            asunto:
                document.getElementById("asunto").value,

            hardware:
                hardware,

            equipos:
                equipos,

            checklist:
                checklist,

            numero_sac:
                document.getElementById(
                    "numero_sac"
                ).value,

            sistema_operativo:
                document.querySelector(
                    'input[name="so"]:checked'
                )?.value || ""

        };

        const response = await fetch(
            "http://127.0.0.1:8001/generar-acta",
            {
                method: "POST",
                headers: {
                    "Content-Type":
                        "application/json"
                },
                body:
                    JSON.stringify(payload)
            }
        );

        if (!response.ok) {

            throw new Error(
                "No fue posible generar la documentación"
            );

        }

        const blob =
            await response.blob();

        const url =
            window.URL.createObjectURL(blob);

        const a =
            document.createElement("a");

        const disposition =
            response.headers.get(
                "Content-Disposition"
            );

        let nombreArchivo =
            "Documentacion.zip";

        if (disposition) {

            const match =
                disposition.match(
                    /filename="?([^"]+)"?/
                );

            if (match) {

                nombreArchivo =
                    match[1];

            }

        }

        a.href = url;

        a.download =
            nombreArchivo;

        document.body.appendChild(a);

        a.click();

        document.body.removeChild(a);

        window.URL.revokeObjectURL(url);

        mostrarMensaje(
            "Documentación generada correctamente",
            "success"
        );

        
        console.log(
            response.headers.get(
                "content-type"
            )
        );


    }

    catch (error) {

        console.error(error);

        mostrarMensaje(
            "Error generando la documentación",
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
                        Hardware ${numeroHardware}
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

            <div class="input-floating w-full mb-1">

                <input
                    type="text"
                    class="input"
                    placeholder=" "
                    data-descripcion />

                <label class="input-floating-label">

                    Descripción

                </label>

            </div>

            <div class="input-floating w-full">

                <input
                    type="text"
                    class="input"
                    placeholder=" "
                    data-programa />

                <label class="input-floating-label">

                    Programa

                </label>

            </div>

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
