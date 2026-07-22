package com.empresa.actas.service;

import com.empresa.actas.dto.request.ActaRequest;
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
public class ActaService {

    @Value("${app.generated-dir}")
    private String generatedDir;

    private final DocumentoWordService wordService;
    private final DocumentoPdfService pdfService;
    private final ZipService zipService;
    private final ObjectMapper objectMapper;

    public ActaService(
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

    public ActaResponse generarActa(ActaRequest request) {
        try {
            Path outputDir = Paths.get(generatedDir);
            Files.createDirectories(outputDir);

            Map<String, Object> datos = objectMapper.convertValue(
                    request,
                    new TypeReference<Map<String, Object>>() {}
            );

            Path rutaActa = wordService.generarActa(datos);
            System.out.println("ACTA DOCX: " + rutaActa);

            Path rutaChecklist = wordService.generarChecklist(datos);
            System.out.println("CHECKLIST DOCX: " + rutaChecklist);

            Path rutaPdf = pdfService.generarActaPdf(datos);
            System.out.println("ACTA PDF: " + rutaPdf);

            String asunto = request.getAsunto()
                    .replaceAll("[^a-zA-Z0-9]", "");

            String serial = "SinSerial";
            if (request.getEquipos() != null && !request.getEquipos().isEmpty()) {
                serial = request.getEquipos().get(0).getSerial();
            }

            String nombreZip = "ActaLista_" + serial + "_" + asunto + ".zip";
            Path rutaZip = outputDir.resolve(nombreZip);

            zipService.crearZip(rutaZip, rutaActa, rutaChecklist, rutaPdf);
            System.out.println("ZIP CREADO: " + rutaZip);

            return ActaResponse.ok(nombreZip);

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ActaResponse.error("Error generando documentacion: " + e.getMessage());
        }
    }
}
