package com.empresa.actas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private boolean success;
    private String mensaje;

    public static ErrorResponse of(String mensaje) {
        return new ErrorResponse(false, mensaje);
    }
}
