package com.empresa.actas.dto.request;

import lombok.Data;

@Data
public class EquipoItem {

    private String serial = "";
    private String marca = "";
    private String tipo = "";
    private String modelo = "";
    private String inventario = "";
    private String estado = "";
}
