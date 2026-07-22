package com.empresa.actas.controller;

import com.empresa.actas.dto.request.ActaRequest;
import com.empresa.actas.dto.response.ActaResponse;
import com.empresa.actas.dto.response.ErrorResponse;
import com.empresa.actas.service.ActaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ActaController {

    @Value("${app.generated-dir}")
    private String generatedDir;

    private final ActaService actaService;

    public ActaController(ActaService actaService) {
        this.actaService = actaService;
    }

    @PostMapping("/generar-acta")
    public ActaResponse generarActa(@Valid @RequestBody ActaRequest request) {
        System.out.println("========== DATOS RECIBIDOS ==========");
        System.out.println(request);

        ActaResponse response = actaService.generarActa(request);

        System.out.println("=== RESULTADO ===");
        System.out.println(response);

        return response;
    }

    @GetMapping("/descargar-acta/{nombreZip}")
    public ResponseEntity<?> descargarActa(@PathVariable String nombreZip) {
        Path rutaZip = Paths.get(generatedDir, nombreZip);

        System.out.println("DESCARGAR: " + rutaZip);
        System.out.println("EXISTE: " + rutaZip.toFile().exists());

        if (!rutaZip.toFile().exists()) {
            return ResponseEntity.ok(ErrorResponse.of("Archivo no encontrado"));
        }

        Resource resource = new FileSystemResource(rutaZip.toFile());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreZip + "\"")
                .body(resource);
    }
}
