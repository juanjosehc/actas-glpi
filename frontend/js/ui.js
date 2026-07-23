/*
====================================================
UI UTILITIES - FRONTEND
====================================================

Propósito:

Funciones compartidas de presentación utilizadas
por ambas páginas (acta de entrega y acta de
devolución).

Dependencias:

- Ninguna. Este archivo se carga primero.

====================================================
*/

/**
 * Muestra un mensaje de notificación al usuario.
 *
 * Crea un elemento <div> dentro del contenedor #mensaje-app
 * con el texto y tipo indicados. Los mensajes anteriores
 * se eliminan automáticamente (solo uno visible a la vez).
 *
 * Tipos soportados:
 * - "success": Notificación de éxito (verde).
 * - "error": Notificación de error (rojo).
 * - "info": Informativo por defecto (azul).
 *
 * @param {string} mensaje - Texto a mostrar.
 * @param {string} [tipo="info"] - Tipo de notificación.
 */
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
