package com.empresa.actas.controller;

import com.empresa.actas.dto.response.PerifericoResponse;
import com.empresa.actas.service.PerifericoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para la consulta de periféricos desde GLPI.
 *
 * Endpoint:
 * - GET /periferico/{serial} → Busca el serial en Monitor y Peripheral,
 *   y retorna marca, tipo, modelo e inventario.
 *
 * Utilizado por el frontend en la modalidad Periférico del acta de
 * entrega, para auto completar los datos al hacer click en "Buscar".
 *
 * Es un endpoint aparte de /equipo/{serial} porque el DTO de respuesta
 * difiere (incluye inventario y la bandera "encontrado"). Mantenerlos
 * separados evita alterar el contrato que ya consumen las páginas de
 * entrega, devolución y formateo.
 */
@RestController
public class PerifericoController {

    private final PerifericoService perifericoService;

    public PerifericoController(PerifericoService perifericoService) {
        this.perifericoService = perifericoService;
    }

    /**
     * Consulta un periférico en GLPI por su número de serial.
     *
     * @param serial Número de serial del periférico a buscar.
     * @return PerifericoResponse con marca, tipo, modelo e inventario.
     *         Si no existe en Monitor ni en Peripheral, encontrado=false.
     */
    @GetMapping("/periferico/{serial}")
    public PerifericoResponse obtenerPeriferico(@PathVariable String serial) {
        return perifericoService.buscarPeriferico(serial);
    }
}
