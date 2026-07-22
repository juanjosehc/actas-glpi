package com.empresa.actas.service;

import com.empresa.actas.dto.request.DevolucionRequest;
import com.empresa.actas.dto.response.ActaResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class DevolucionService {

    @Value("${app.generated-dir}")
    private String generatedDir;

    private final DocumentoWordService wordService;
    private final DocumentoPdfService pdfService;
    private final ZipService zipService;
    private final ObjectMapper objectMapper;

    public DevolucionService(
            DocumentoWordService wordService,
            DocumentoPdfService pdfService,
            ZipService zipService,
            ObjectMapper objectMapper
    ) {
        this.wordService = wordService;
        this.pdfService = pdfService;
        this.zipService = zipService;
        this.objectMapper = objectMapper;
    }

    public ActaResponse generarDevolucion(DevolucionRequest request) {
        try {
            Path outputDir = Paths.get(generatedDir);
            Files.createDirectories(outputDir);

            Map<String, Object> datos = objectMapper.convertValue(
                    request,
                    new TypeReference<Map<String, Object>>() {}
            );

            Path rutaDevolucion = wordService.generarDevolucion(datos);
            System.out.println("DEVOLUCION DOCX: " + rutaDevolucion);

            Path rutaPdf = pdfService.generarPdfDevolucion(datos);
            System.out.println("DEVOLUCION PDF: " + rutaPdf);

            String serial = "SinSerial";
            if (request.getEquipos() != null && !request.getEquipos().isEmpty()) {
                serial = request.getEquipos().get(0).getSerial();
            }

            String motivo = request.getMotivo()
                    .replaceAll("[^a-zA-Z0-9]", "");

            String nombreZip = "Devolucion_" + serial + "_" + motivo + ".zip";
            Path rutaZip = outputDir.resolve(nombreZip);

            zipService.crearZip(rutaZip, rutaDevolucion, rutaPdf);
            System.out.println("ZIP DEVOLUCION CREADO: " + rutaZip);

            return ActaResponse.ok(nombreZip);

        } catch (Exception e) {
            System.out.println("ERROR DEVOLUCION: " + e.getMessage());
            e.printStackTrace();
            return ActaResponse.error("Error generando devolucion: " + e.getMessage());
        }
    }
}
