package com.empresa.actas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la consulta de periféricos desde GLPI.
 *
 * Se diferencia de {@link EquipoResponse} en que incluye el
 * inventario: en GLPI los periféricos y monitores sí lo traen
 * (a diferencia de los equipos, donde se digita a mano).
 *
 * {@code encontrado} distingue "no existe en GLPI" de "existe pero
 * sin datos": el frontend usa esa bandera para decidir si muestra el
 * aviso de serial no encontrado, en vez de deducirlo de que los
 * campos vengan vacíos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerifericoResponse {

    private boolean encontrado = false;
    private String marca = "";
    private String tipo = "";
    private String modelo = "";
    private String inventario = "";

    /**
     * Respuesta para un serial que no existe en ninguna entidad.
     */
    public static PerifericoResponse noEncontrado() {
        return new PerifericoResponse(false, "", "", "", "");
    }
}
