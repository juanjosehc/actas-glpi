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
}

async function generarActa() {

    const payload = {

        entregado_a:
            document.getElementById("entregado_a").value,

        cargo_entregado:
            document.getElementById("cargo_entregado").value,

        entregado_por:
            document.getElementById("entregado_por").value,

        cargo_entrega:
            document.getElementById("cargo_entrega").value,

        asunto:
            document.getElementById("asunto").value,

        marca:
            document.getElementById("marca").value,

        tipo:
            document.getElementById("tipo").value,

        modelo:
            document.getElementById("modelo").value,

        serial:
            document.getElementById("serial").value,

        inventario:
            document.getElementById("inventario").value

        
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

    const blob = await response.blob();

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement("a");

    a.href = url;
    a.download = "Acta_Entrega.docx";

    document.body.appendChild(a);

    a.click();

    a.remove();

    window.URL.revokeObjectURL(url);
}