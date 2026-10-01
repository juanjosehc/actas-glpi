package com.empresa.actas.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO de entrada para la generación del acta de entrega.
 *
 * Contiene toda la información necesaria para generar:
 * - Acta de entrega (DOCX)
 * - Lista de chequeo (DOCX)
 *
 * Campos obligatorios validados con @NotBlank:
 * - fecha, entregado_a, cargo_recibe, entregado_por,
 *   cargo_entrega, asunto, numero_sac, sistema_operativo.
 *
 * Campos opcionales con valores por defecto:
 * - hardware, equipos, checklist, observaciones.
 */
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

    private String numero_sac;

    private String observaciones = "";

    private String sistema_operativo;

    /**
     * Modalidad del acta: "EQUIPO" (por defecto) o "PERIFERICO".
     *
     * En una entrega exclusiva de periféricos no hay lista de chequeo
     * ni hardware/software, así que numero_sac y sistema_operativo
     * dejan de ser obligatorios. Si no se envía, se asume EQUIPO y se
     * mantienen las validaciones de siempre.
     */
    private String modo;

    /** true si el acta es una entrega exclusiva de periféricos. */
    public boolean esModoPeriferico() {
        return "PERIFERICO".equalsIgnoreCase(modo);
    }

    /**
     * numero_sac solo se imprime en la lista de chequeo, que no se
     * genera en modalidad Periférico.
     */
    @AssertTrue(message = "El numero_sac es obligatorio")
    private boolean isNumeroSacValido() {
        return esModoPeriferico() || (numero_sac != null && !numero_sac.isBlank());
    }

    /** El sistema operativo también pertenece solo al checklist. */
    @AssertTrue(message = "El sistema operativo es obligatorio")
    private boolean isSistemaOperativoValido() {
        return esModoPeriferico()
                || (sistema_operativo != null && !sistema_operativo.isBlank());
    }
}
