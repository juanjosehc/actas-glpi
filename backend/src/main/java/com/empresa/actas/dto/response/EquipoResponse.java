package com.empresa.actas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoResponse {

    private String marca = "";
    private String tipo = "";
    private String modelo = "";
}
