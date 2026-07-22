package com.empresa.actas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ActaRequest {

    @NotBlank(message = "La fecha es obligatoria")
    private String fecha;

    @NotBlank(message = "El campo entregado_a es obligatorio")
    private String entregado_a;

    @NotBlank(message = "El campo cargo_recibe es obligatorio")
    private String cargo_recibe;

    @NotBlank(message = "El campo entregado_por es obligatorio")
    private String entregado_por;

    @NotBlank(message = "El campo cargo_entrega es obligatorio")
    private String cargo_entrega;

    @NotBlank(message = "El campo asunto es obligatorio")
    private String asunto;

    private List<HardwareItem> hardware = new ArrayList<>();

    private List<EquipoItem> equipos = new ArrayList<>();

    private Map<String, Boolean> checklist = new HashMap<>();

    @NotBlank(message = "El numero_sac es obligatorio")
    private String numero_sac;

    private String observaciones = "";

    @NotBlank(message = "El sistema operativo es obligatorio")
    private String sistema_operativo;
}
