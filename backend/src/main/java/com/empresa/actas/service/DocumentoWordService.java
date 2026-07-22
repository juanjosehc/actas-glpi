package com.empresa.actas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentoWordService {

    @Value("${app.generated-dir}")
    private String generatedDir;

    @Value("${app.templates-dir}")
    private String templatesDir;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Path generarActa(Map<String, Object> datos) throws IOException {

        Path outputDir = Paths.get(generatedDir);
        Files.createDirectories(outputDir);

        prepararFecha(datos);

        for (int i = 1; i <= 16; i++) {
            datos.put("hw_" + i + "_tipo", "");
            datos.put("hw_" + i + "_descripcion", "");
            datos.put("hw_" + i + "_programa", "");
        }

        List<Map<String, Object>> hwList = asMapList(datos.get("hardware"));
        int idx = 0;
        for (Map<String, Object> hw : hwList) {
            idx++;
            if (idx > 11) break;
            datos.put("hw_" + idx + "_tipo", hw.getOrDefault("tipo", ""));
            datos.put("hw_" + idx + "_descripcion", hw.getOrDefault("descripcion", ""));
            datos.put("hw_" + idx + "_programa", hw.getOrDefault("programa", ""));
        }

        for (int i = 1; i <= 10; i++) {
            datos.put("eq_" + i + "_marca", "");
            datos.put("eq_" + i + "_tipo", "");
            datos.put("eq_" + i + "_modelo", "");
            datos.put("eq_" + i + "_serial", "");
            datos.put("eq_" + i + "_inventario", "");
        }

        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        idx = 0;
        for (Map<String, Object> eq : eqList) {
            idx++;
            if (idx > 10) break;
            datos.put("eq_" + idx + "_marca", eq.getOrDefault("marca", ""));
            datos.put("eq_" + idx + "_tipo", eq.getOrDefault("tipo", ""));
            datos.put("eq_" + idx + "_modelo", eq.getOrDefault("modelo", ""));
            datos.put("eq_" + idx + "_serial", eq.getOrDefault("serial", ""));
            datos.put("eq_" + idx + "_inventario", eq.getOrDefault("inventario", ""));
        }

        Map<String, String> vars = new HashMap<>();
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            vars.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        Path templatePath = resolveTemplate("Acta de Entrega 2 2 - copia.docx");

        String asunto = datos.getOrDefault("asunto", "").toString()
                .replaceAll("[^a-zA-Z0-9]", "");

        String serial = "SinSerial";
        if (!eqList.isEmpty()) {
            serial = eqList.get(0).getOrDefault("serial", "SinSerial").toString();
        }

        String fileName = "ActaEntrega_" + serial + "_" + asunto + ".docx";
        Path outputPath = outputDir.resolve(fileName);

        return DocxTemplateEngine.processTemplate(templatePath, vars, outputPath);
    }

    public Path generarChecklist(Map<String, Object> datos) throws IOException {

        Path outputDir = Paths.get(generatedDir);
        Files.createDirectories(outputDir);

        prepararFecha(datos);

        datos.put("responsable_verificacion",
                datos.getOrDefault("entregado_por", ""));

        String so = datos.getOrDefault("sistema_operativo", "").toString();

        datos.put("win10", "Windows 10".equals(so) ? "\u25A0" : "\u25A1");
        datos.put("win11", "Windows 11".equals(so) ? "\u25A0" : "\u25A1");
        datos.put("macos", "Mac OS".equals(so) ? "\u25A0" : "\u25A1");

        Map<String, Object> chk = asMap(datos.get("checklist"));
        for (int i = 1; i <= 36; i++) {
            boolean valor = false;
            Object val = chk.get("chk_" + i);
            if (val instanceof Boolean b) {
                valor = b;
            } else if (val != null) {
                valor = Boolean.parseBoolean(val.toString());
            }
            datos.put("chk_" + i + "_si", valor ? "\u25A0" : "\u25A1");
            datos.put("chk_" + i + "_no", valor ? "\u25A1" : "\u25A0");
        }

        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        if (!eqList.isEmpty()) {
            Map<String, Object> eq = eqList.get(0);
            datos.put("eq_1_marca", eq.getOrDefault("marca", ""));
            datos.put("eq_1_tipo", eq.getOrDefault("tipo", ""));
            datos.put("eq_1_modelo", eq.getOrDefault("modelo", ""));
            datos.put("eq_1_serial", eq.getOrDefault("serial", ""));
            datos.put("eq_1_inventario", eq.getOrDefault("inventario", ""));
        } else {
            datos.put("eq_1_marca", "");
            datos.put("eq_1_tipo", "");
            datos.put("eq_1_modelo", "");
            datos.put("eq_1_serial", "");
            datos.put("eq_1_inventario", "");
        }

        Map<String, String> vars = new HashMap<>();
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            vars.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        Path templatePath = resolveTemplate("ListaChequeo.docx");

        String asunto = datos.getOrDefault("asunto", "").toString()
                .replaceAll("[^a-zA-Z0-9]", "");

        String serial = "SinSerial";
        if (!eqList.isEmpty()) {
            serial = eqList.get(0).getOrDefault("serial", "SinSerial").toString();
        }

        String fileName = "Checklist_" + serial + "_" + asunto + ".docx";
        Path outputPath = outputDir.resolve(fileName);

        return DocxTemplateEngine.processTemplate(templatePath, vars, outputPath);
    }

    public Path generarDevolucion(Map<String, Object> datos) throws IOException {

        Path outputDir = Paths.get(generatedDir);
        Files.createDirectories(outputDir);

        prepararFecha(datos);

        for (int i = 1; i <= 10; i++) {
            datos.put("eq_" + i + "_marca", "");
            datos.put("eq_" + i + "_tipo", "");
            datos.put("eq_" + i + "_modelo", "");
            datos.put("eq_" + i + "_serial", "");
            datos.put("eq_" + i + "_inventario", "");
            datos.put("eq_" + i + "_estado", "");
        }

        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        int idx = 0;
        for (Map<String, Object> eq : eqList) {
            idx++;
            if (idx > 10) break;
            datos.put("eq_" + idx + "_marca", eq.getOrDefault("marca", ""));
            datos.put("eq_" + idx + "_tipo", eq.getOrDefault("tipo", ""));
            datos.put("eq_" + idx + "_modelo", eq.getOrDefault("modelo", ""));
            datos.put("eq_" + idx + "_serial", eq.getOrDefault("serial", ""));
            datos.put("eq_" + idx + "_inventario", eq.getOrDefault("inventario", ""));
            datos.put("eq_" + idx + "_estado", eq.getOrDefault("estado", ""));
        }

        for (int i = 1; i <= 10; i++) {
            datos.put("ot_" + i + "_tipo", "");
        }

        List<Map<String, Object>> hwList = asMapList(datos.get("hardware"));
        idx = 0;
        for (Map<String, Object> hw : hwList) {
            idx++;
            if (idx > 10) break;
            datos.put("ot_" + idx + "_tipo", hw.getOrDefault("tipo", ""));
        }

        Map<String, String> vars = new HashMap<>();
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            vars.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        Path templatePath = resolveTemplate("ActaDevolucion.docx");

        String serial = "SinSerial";
        if (!eqList.isEmpty()) {
            serial = eqList.get(0).getOrDefault("serial", "SinSerial").toString();
        }

        String motivo = datos.getOrDefault("motivo", "").toString()
                .replaceAll("[^a-zA-Z0-9]", "");

        String fileName = "Devolucion_" + serial + "_" + motivo + ".docx";
        Path outputPath = outputDir.resolve(fileName);

        return DocxTemplateEngine.processTemplate(templatePath, vars, outputPath);
    }

    private Path resolveTemplate(String templateName) throws IOException {
        if (templatesDir.startsWith("classpath:")) {
            String classpath = templatesDir.substring("classpath:".length());
            String resourcePath = classpath + "/" + templateName;
            ClassPathResource resource = new ClassPathResource(resourcePath);

            Path tempDir = Files.createTempDirectory("actas-tpl-");
            Path tempFile = tempDir.resolve(templateName);
            Files.copy(resource.getInputStream(), tempFile);
            return tempFile;
        }
        return Paths.get(templatesDir, templateName);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object obj) {
        if (obj instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private void prepararFecha(Map<String, Object> datos) {
        String fechaStr = datos.getOrDefault("fecha", "").toString();
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, DATE_FMT);
            datos.put("dia", String.format("%02d", fecha.getDayOfMonth()));
            datos.put("mes", String.format("%02d", fecha.getMonthValue()));
            datos.put("anio", String.valueOf(fecha.getYear()));
        } catch (Exception e) {
            datos.put("dia", "");
            datos.put("mes", "");
            datos.put("anio", "");
        }
    }
}
