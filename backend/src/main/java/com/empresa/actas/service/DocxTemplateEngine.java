package com.empresa.actas.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocxTemplateEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    public static Path processTemplate(
            Path templatePath,
            Map<String, String> variables,
            Path outputPath
    ) throws IOException {

        Files.copy(templatePath, outputPath, StandardCopyOption.REPLACE_EXISTING);

        try (FileInputStream fis = new FileInputStream(outputPath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceInParagraph(paragraph, variables);
            }

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceInParagraph(paragraph, variables);
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                document.write(fos);
            }
        }

        return outputPath;
    }

    private static void replaceInParagraph(
            XWPFParagraph paragraph,
            Map<String, String> variables
    ) {
        String fullText = getFullParagraphText(paragraph);

        Matcher matcher = VARIABLE_PATTERN.matcher(fullText);
        if (!matcher.find()) {
            return;
        }

        matcher.reset();
        String replaced = fullText;
        while (matcher.find()) {
            String varName = matcher.group(1);
            String value = variables.getOrDefault(varName, "");
            replaced = replaced.replace(matcher.group(), value);
        }

        while (paragraph.getRuns().size() > 0) {
            paragraph.removeRun(0);
        }

        XWPFRun newRun = paragraph.createRun();
        newRun.setText(replaced);
    }

    private static String getFullParagraphText(XWPFParagraph paragraph) {
        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            sb.append(run.text());
        }
        return sb.toString();
    }
}
