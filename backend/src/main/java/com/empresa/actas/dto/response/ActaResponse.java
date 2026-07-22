package com.empresa.actas.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActaResponse {

    private boolean success;

    @JsonProperty("nombre_zip")
    private String nombreZip;

    private String mensaje;

    public static ActaResponse ok(String nombreZip) {
        return new ActaResponse(true, nombreZip, "Documentacion generada correctamente");
    }

    public static ActaResponse error(String mensaje) {
        return new ActaResponse(false, null, mensaje);
    }
}
