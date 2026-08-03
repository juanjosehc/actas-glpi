/*
====================================================
UI UTILITIES - FRONTEND
====================================================

Propósito:

Funciones compartidas de presentación utilizadas
por ambas páginas (acta de entrega y acta de
devolución).

Incluye el sistema centralizado de notificaciones
Toast:

- Posición superior derecha.
- Diseño moderno con icono según tipo.
- Animación suave de entrada y salida.
- Desaparición automática después de unos segundos.
- Tipos: success, warning, error, info.

Dependencias:

- Ninguna. Este archivo se carga primero.

====================================================
*/

/**
 * Tiempo (ms) que permanece visible cada notificación.
 */
const DURACION_TOAST = 3500;

/**
 * Iconos por tipo de notificación.
 */
const ICONOS_TOAST = {
    success: "✅",
    warning: "⚠️",
    error: "❌",
    info: "ℹ️"
};

/**
 * Obtiene o crea el contenedor de toasts.
 *
 * El contenedor se coloca fijo en la parte superior
 * derecha de la pantalla y agrupa todas las
 * notificaciones activas.
 *
 * @returns {HTMLElement} Contenedor #toast-container.
 */
function obtenerContenedorToast() {

    let contenedor =
        document.getElementById(
            "toast-container"
        );

    if (!contenedor) {

        contenedor =
            document.createElement("div");

        contenedor.id = "toast-container";

        document.body.appendChild(contenedor);

    }

    return contenedor;

}

/**
 * Muestra una notificación Toast.
 *
 * Crea un elemento dentro del contenedor #toast-container
 * con el texto y tipo indicados. El toast se anima al
 * entrar, permanece visible unos segundos y se elimina
 * automáticamente con animación de salida.
 *
 * Tipos soportados:
 * - "success": Operación exitosa (verde).
 * - "warning": Límite funcional alcanzado (ámbar).
 * - "error": Error de comunicación/backend (rojo).
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
        obtenerContenedorToast();

    const toast =
        document.createElement("div");

    toast.className = `toast toast--${tipo}`;

    const icono =
        document.createElement("span");

    icono.className = "toast-icon";

    icono.textContent =
        ICONOS_TOAST[tipo] || ICONOS_TOAST.info;

    const texto =
        document.createElement("span");

    texto.className = "toast-message";

    texto.textContent = mensaje;

    toast.appendChild(icono);

    toast.appendChild(texto);

    contenedor.appendChild(toast);

    const eliminar = () => {

        toast.classList.add("is-out");

        setTimeout(() => {
            toast.remove();
        }, 300);

    };

    setTimeout(eliminar, DURACION_TOAST);

}
