/*
====================================================
CONFIGURACIÓN DEL FRONTEND
====================================================

URL base del backend (API).
Centralizada en este archivo para facilitar el
despliegue (Docker, redes, dominios, etc.).

En desarrollo local apunta al backend Spring Boot
(puerto 8001). Debe cargarse ANTES que el resto de
scripts en los HTML.

====================================================
*/
const API_URL = "http://127.0.0.1:8001";
