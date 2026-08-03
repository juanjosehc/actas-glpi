package com.empresa.actas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la búsqueda de usuarios en GLPI.
 *
 * Contiene únicamente los datos necesarios para el
 * autocompletado en el frontend: id y nombre completo
 * construido como CONCAT(firstname, ' ', realname).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private int id;
    private String nombreCompleto;
}
