package com.empresa.actas.controller;

import com.empresa.actas.dto.request.DevolucionRequest;
import com.empresa.actas.dto.response.ActaResponse;
import com.empresa.actas.service.DevolucionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @PostMapping("/generar-devolucion")
    public ActaResponse generarDevolucion(@Valid @RequestBody DevolucionRequest request) {
        System.out.println("========== DEVOLUCION RECIBIDA ==========");
        System.out.println(request);

        ActaResponse response = devolucionService.generarDevolucion(request);

        System.out.println("=== RESULTADO DEVOLUCION ===");
        System.out.println(response);

        return response;
    }
}
