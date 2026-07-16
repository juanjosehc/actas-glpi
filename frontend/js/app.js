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

        document
            .querySelectorAll(
                ".hardware-item"
            )
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
            .querySelectorAll(
                ".equipo-item"
            )
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
                equipos

        };

        const response = await fetch(
            "http://127.0.0.1:8001/generar-acta",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {

            throw new Error(
                "No fue posible generar el acta"
            );

        }

        const blob =
            await response.blob();

        const url =
            window.URL.createObjectURL(blob);

        const a =
            document.createElement("a");

        a.href = url;

        const disposition =
            response.headers.get(
                "Content-Disposition"
            );

        let nombreArchivo =
            "ActaEntrega.docx";

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

        console.log(
            response.headers.get(
                "Content-Disposition"
            )
        );

        console.log(nombreArchivo);

        a.download =
            nombreArchivo;

        a.click();

        a.remove();

        window.URL.revokeObjectURL(url);

        
        mostrarMensaje(
            "Acta generada correctamente",
            "success"
        );
        console.log(payload.hardware);


    }

    catch (error) {

        console.error(error);

        
        mostrarMensaje(
            "Error generando el acta",
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

        <div class="item-header">

            <h4>
                Hardware ${numeroHardware}
            </h4>

            <button
                type="button"
                data-eliminar>

                Eliminar

            </button>

        </div>

        <input
            type="text"
            placeholder="Tipo Hardware"
            data-tipo>

        <input
            type="text"
            placeholder="Descripción"
            data-descripcion>

        <input
            type="text"
            placeholder="Programa"
            data-programa>

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

        <div class="item-header">

            <h4>
                Equipo ${numeroEquipo}
            </h4>

            <button
                type="button"
                data-eliminar>

                Eliminar

            </button>

        </div>

        <input
            type="text"
            placeholder="Serial"
            data-serial>

        <button
            type="button"
            data-buscar>

            Buscar

        </button>

        <input
            placeholder="Marca"
            data-marca
            readonly>

        <input
            placeholder="Tipo"
            data-tipo
            readonly>

        <input
            placeholder="Modelo"
            data-modelo
            readonly>

        <input
            placeholder="Inventario"
            data-inventario>

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