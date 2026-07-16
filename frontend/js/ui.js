function mostrarMensaje(
    mensaje,
    tipo = "info"
) {

    const contenedor =
        document.getElementById(
            "mensaje-app"
        );

    contenedor.innerHTML = "";

    const div =
        document.createElement("div");

    div.textContent = mensaje;

    div.dataset.tipo = tipo;

    contenedor.appendChild(div);

}