package com.empresa.actas.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class DocumentoPdfService {

    @Value("${app.generated-dir}")
    private String generatedDir;

    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_RIGHT = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    public Path generarActaPdf(Map<String, Object> datos) throws IOException {

        Path outputDir = Paths.get(generatedDir);
        Files.createDirectories(outputDir);

        String asunto = datos.getOrDefault("asunto", "").toString()
                .replaceAll("[^a-zA-Z0-9]", "");
        String serial = "SinSerial";
        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        if (!eqList.isEmpty()) {
            serial = eqList.get(0).getOrDefault("serial", "SinSerial").toString();
        }

        String fileName = "ActaEntrega_" + serial + "_" + asunto + ".pdf";
        Path outputPath = outputDir.resolve(fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                drawActa(cs, datos, font, fontBold, PDRectangle.A4.getHeight());
            }

            document.save(outputPath.toFile());
        }

        return outputPath;
    }

    public Path generarPdfDevolucion(Map<String, Object> datos) throws IOException {

        Path outputDir = Paths.get(generatedDir);
        Files.createDirectories(outputDir);

        String motivo = datos.getOrDefault("motivo", "").toString()
                .replaceAll("[^a-zA-Z0-9]", "");
        String serial = "SinSerial";
        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        if (!eqList.isEmpty()) {
            serial = eqList.get(0).getOrDefault("serial", "SinSerial").toString();
        }

        String fileName = "Devolucion_" + serial + "_" + motivo + ".pdf";
        Path outputPath = outputDir.resolve(fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                drawDevolucion(cs, datos, font, fontBold, PDRectangle.A4.getHeight());
            }

            document.save(outputPath.toFile());
        }

        return outputPath;
    }

    private void drawDevolucion(
            PDPageContentStream cs,
            Map<String, Object> datos,
            PDFont font,
            PDFont fontBold,
            float pageHeight
    ) throws IOException {

        float y = pageHeight - 50;

        y = drawTextCentered(cs, "ACTA DE DEVOLUCION DE DISPOSITIVOS", fontBold, y, 14);
        y -= 15;

        String fecha = datos.getOrDefault("fecha", "").toString();
        y = drawTextLeft(cs, "Fecha: " + fecha, font, y, 11);
        y -= 5;

        String entregadoPor = datos.getOrDefault("entregado_por", "").toString();
        y = drawTextLeft(cs, "Entregado por: " + entregadoPor, font, y, 11);
        y -= 5;

        String cargoEntrega = datos.getOrDefault("cargo_entrega", "").toString();
        y = drawTextLeft(cs, "Cargo: " + cargoEntrega, font, y, 11);
        y -= 5;

        String cedula = datos.getOrDefault("cedula", "").toString();
        y = drawTextLeft(cs, "Cedula: " + cedula, font, y, 11);
        y -= 5;

        String recibidoPor = datos.getOrDefault("recibido_por", "").toString();
        y = drawTextLeft(cs, "Recibido por: " + recibidoPor, font, y, 11);
        y -= 5;

        String cargoRecibe = datos.getOrDefault("cargo_recibe", "").toString();
        y = drawTextLeft(cs, "Cargo: " + cargoRecibe, font, y, 11);
        y -= 5;

        String area = datos.getOrDefault("area_recibe", "").toString();
        y = drawTextLeft(cs, "Area: " + area, font, y, 11);
        y -= 5;

        String motivo = datos.getOrDefault("motivo", "").toString();
        y = drawTextLeft(cs, "Motivo: " + motivo, font, y, 11);
        y -= 10;

        y = drawTextCentered(cs, "EQUIPOS DEVUELTOS", fontBold, y, 12);
        y -= 10;

        y = drawDevolucionEquipoTable(cs, datos, font, fontBold, y);
        y -= 15;

        List<Map<String, Object>> hwList = asMapList(datos.get("hardware"));
        if (!hwList.isEmpty()) {
            y = drawTextCentered(cs, "OTROS ELEMENTOS", fontBold, y, 12);
            y -= 10;
            y = drawOtroElementoTable(cs, datos, font, fontBold, y);
            y -= 15;
        }

        String observaciones = datos.getOrDefault("observaciones", "").toString();
        if (!observaciones.isEmpty()) {
            y = drawTextLeft(cs, "Observaciones: " + observaciones, font, y, 10);
            y -= 15;
        }

        y = drawTextLeft(cs, "Atentamente.", font, y, 10);
        y -= 25;

        y = drawTextLeft(cs, "__________________________", font, y, 10);
        y -= 8;
        y = drawTextLeft(cs, "Firma", font, y, 8);
        y -= 3;
        y = drawTextLeft(cs, "Entregado por: " + entregadoPor, font, y, 9);
        y -= 4;
        y = drawTextLeft(cs, "Cargo: " + cargoEntrega, font, y, 9);
        y -= 15;

        y = drawTextLeft(cs, "__________________________", font, y, 10);
        y -= 8;
        y = drawTextLeft(cs, "Firma", font, y, 8);
        y -= 3;
        y = drawTextLeft(cs, "Recibido por: " + recibidoPor, font, y, 9);
        y -= 4;
        y = drawTextLeft(cs, "Cargo: " + cargoRecibe, font, y, 9);
        y -= 15;

        String nombreJefe = datos.getOrDefault("nombre_jefe", "").toString();
        String cargoJefe = datos.getOrDefault("cargo_jefe", "").toString();
        if (!nombreJefe.isEmpty()) {
            y = drawTextLeft(cs, "__________________________", font, y, 10);
            y -= 8;
            y = drawTextLeft(cs, "Firma", font, y, 8);
            y -= 3;
            y = drawTextLeft(cs, "Jefe: " + nombreJefe, font, y, 9);
            y -= 4;
            y = drawTextLeft(cs, "Cargo: " + cargoJefe, font, y, 9);
        }
    }

    private float drawDevolucionEquipoTable(
            PDPageContentStream cs,
            Map<String, Object> datos,
            PDFont font,
            PDFont fontBold,
            float y
    ) throws IOException {

        String[] headers = {"Marca", "Tipo", "Modelo", "Serial", "Nro. Inventario", "Estado"};
        float[] widths = {0.15f, 0.15f, 0.20f, 0.18f, 0.17f, 0.15f};

        y = drawTableRow(cs, headers, widths, fontBold, y, true);

        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        for (Map<String, Object> eq : eqList) {
            String[] row = {
                    String.valueOf(eq.getOrDefault("marca", "")),
                    String.valueOf(eq.getOrDefault("tipo", "")),
                    String.valueOf(eq.getOrDefault("modelo", "")),
                    String.valueOf(eq.getOrDefault("serial", "")),
                    String.valueOf(eq.getOrDefault("inventario", "")),
                    String.valueOf(eq.getOrDefault("estado", ""))
            };
            y = drawTableRow(cs, row, widths, font, y, false);
        }

        return y;
    }

    private float drawOtroElementoTable(
            PDPageContentStream cs,
            Map<String, Object> datos,
            PDFont font,
            PDFont fontBold,
            float y
    ) throws IOException {

        String[] headers = {"Tipo"};
        float[] widths = {1.0f};

        y = drawTableRow(cs, headers, widths, fontBold, y, true);

        List<Map<String, Object>> hwList = asMapList(datos.get("hardware"));
        for (Map<String, Object> hw : hwList) {
            String[] row = {
                    String.valueOf(hw.getOrDefault("tipo", ""))
            };
            y = drawTableRow(cs, row, widths, font, y, false);
        }

        return y;
    }

    private void drawActa(
            PDPageContentStream cs,
            Map<String, Object> datos,
            PDFont font,
            PDFont fontBold,
            float pageHeight
    ) throws IOException {

        float y = pageHeight - 50;

        y = drawTextCentered(cs, "MEMORANDO DE ENTREGA DE DISPOSITIVOS", fontBold, y, 14);
        y -= 15;

        String fecha = datos.getOrDefault("fecha", "").toString();
        y = drawTextLeft(cs, "Fecha: " + fecha, font, y, 11);
        y -= 5;

        String entregadoA = datos.getOrDefault("entregado_a", "").toString();
        y = drawTextLeft(cs, "Entregado a: " + entregadoA, font, y, 11);
        y -= 5;

        String cargoRecibe = datos.getOrDefault("cargo_recibe", "").toString();
        y = drawTextLeft(cs, "Cargo: " + cargoRecibe, font, y, 11);
        y -= 5;

        String entregadoPor = datos.getOrDefault("entregado_por", "").toString();
        y = drawTextLeft(cs, "Entregado por: " + entregadoPor, font, y, 11);
        y -= 5;

        String cargoEntrega = datos.getOrDefault("cargo_entrega", "").toString();
        y = drawTextLeft(cs, "Cargo: " + cargoEntrega, font, y, 11);
        y -= 5;

        String asunto = datos.getOrDefault("asunto", "").toString();
        y = drawTextLeft(cs, "Asunto: " + asunto, font, y, 11);
        y -= 10;

        y = drawTextLeft(cs, "Cordialmente se relaciona el dispositivo que le fue asignado.", font, y, 10);
        y -= 15;

        y = drawTextCentered(cs, "DESCRIPCION DEL EQUIPO DE COMPUTO", fontBold, y, 12);
        y -= 10;

        y = drawEquipoTable(cs, datos, font, fontBold, y);
        y -= 15;

        y = drawTextCentered(cs, "CONTENIDO DEL DISPOSITIVO", fontBold, y, 12);
        y -= 10;

        y = drawHardwareTable(cs, datos, font, fontBold, y);
        y -= 20;

        y = drawTextLeft(cs, "Atentamente.", font, y, 10);
        y -= 30;
        y = drawTextLeft(cs, "__________________________", font, y, 10);
        y -= 10;
        y = drawTextLeft(cs, "Firma", font, y, 9);
        y -= 20;
        y = drawTextLeft(cs, "Recibido por: " + entregadoA, font, y, 10);
        y -= 5;
        y = drawTextLeft(cs, "Cargo: " + cargoRecibe, font, y, 10);
    }

    private float drawEquipoTable(
            PDPageContentStream cs,
            Map<String, Object> datos,
            PDFont font,
            PDFont fontBold,
            float y
    ) throws IOException {

        String[] headers = {"Marca", "Tipo", "Modelo", "Serial", "Nro. Inventario"};
        float[] widths = {0.18f, 0.18f, 0.24f, 0.22f, 0.18f};

        y = drawTableRow(cs, headers, widths, fontBold, y, true);

        List<Map<String, Object>> eqList = asMapList(datos.get("equipos"));
        for (Map<String, Object> eq : eqList) {
            String[] row = {
                    String.valueOf(eq.getOrDefault("marca", "")),
                    String.valueOf(eq.getOrDefault("tipo", "")),
                    String.valueOf(eq.getOrDefault("modelo", "")),
                    String.valueOf(eq.getOrDefault("serial", "")),
                    String.valueOf(eq.getOrDefault("inventario", ""))
            };
            y = drawTableRow(cs, row, widths, font, y, false);
        }

        return y;
    }

    private float drawHardwareTable(
            PDPageContentStream cs,
            Map<String, Object> datos,
            PDFont font,
            PDFont fontBold,
            float y
    ) throws IOException {

        String[] headers = {"Tipo", "Descripcion", "Programa"};
        float[] widths = {0.25f, 0.40f, 0.35f};

        y = drawTableRow(cs, headers, widths, fontBold, y, true);

        List<Map<String, Object>> hwList = asMapList(datos.get("hardware"));
        for (Map<String, Object> hw : hwList) {
            String[] row = {
                    String.valueOf(hw.getOrDefault("tipo", "")),
                    String.valueOf(hw.getOrDefault("descripcion", "")),
                    String.valueOf(hw.getOrDefault("programa", ""))
            };
            y = drawTableRow(cs, row, widths, font, y, false);
        }

        return y;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object obj) {
        if (obj instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private float drawTableRow(
            PDPageContentStream cs,
            String[] cells,
            float[] widths,
            PDFont font,
            float y,
            boolean isHeader
    ) throws IOException {

        float rowHeight = isHeader ? 20 : 18;
        float cellY = y - rowHeight;

        cs.setFont(font, isHeader ? 10 : 9);

        for (int i = 0; i < cells.length; i++) {
            float cellX = MARGIN_LEFT + (USABLE_WIDTH * sumWidths(widths, i));
            float cellW = USABLE_WIDTH * widths[i];

            cs.addRect(cellX, cellY, cellW, rowHeight);
            cs.stroke();

            cs.beginText();
            cs.newLineAtOffset(cellX + 3, cellY + 5);
            String text = truncateText(cells[i], font, 9, cellW - 6);
            cs.showText(text);
            cs.endText();
        }

        return cellY;
    }

    private float sumWidths(float[] widths, int index) {
        float sum = 0;
        for (int i = 0; i < index; i++) {
            sum += widths[i];
        }
        return sum;
    }

    private String truncateText(String text, PDFont font, float fontSize, float maxWidth) {
        if (text == null) return "";
        try {
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            if (textWidth <= maxWidth) return text;
            while (text.length() > 0 && font.getStringWidth(text + "...") / 1000 * fontSize > maxWidth) {
                text = text.substring(0, text.length() - 1);
            }
            return text + "...";
        } catch (Exception e) {
            return text;
        }
    }

    private float drawTextCentered(
            PDPageContentStream cs,
            String text,
            PDFont font,
            float y,
            float fontSize
    ) throws IOException {
        cs.setFont(font, fontSize);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = MARGIN_LEFT + (USABLE_WIDTH - textWidth) / 2;
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - fontSize;
    }

    private float drawTextLeft(
            PDPageContentStream cs,
            String text,
            PDFont font,
            float y,
            float fontSize
    ) throws IOException {
        cs.setFont(font, fontSize);
        cs.beginText();
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText(text);
        cs.endText();
        return y - fontSize;
    }
}
