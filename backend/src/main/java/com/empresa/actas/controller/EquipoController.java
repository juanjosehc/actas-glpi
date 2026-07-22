package com.empresa.actas.controller;

import com.empresa.actas.dto.response.EquipoResponse;
import com.empresa.actas.service.EquipoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping("/equipo/{serial}")
    public EquipoResponse obtenerEquipo(@PathVariable String serial) {
        return equipoService.buscarEquipo(serial);
    }
}
