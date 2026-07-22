package com.empresa.actas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DevolucionRequest {

    @NotBlank(message = "La fecha es obligatoria")
    private String fecha;

    private String recibido_por = "";

    private String entregado_por = "";

    private String cargo_recibe = "";  

    private String cedula = "";

    private String area_recibe = "";

    private String motivo = "";

    private String nombre_entrega = "";

    private String cargo_entrega = "";

    private String nombre_jefe = "";

    private String cargo_jefe = "";

    private String nombre_recepcion = "";

    private List<EquipoItem> equipos = new ArrayList<>();

    private List<OtroElementoItem> hardware = new ArrayList<>();

    private String observaciones = "";
}
