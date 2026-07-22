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
import java.util.ArrayList;
import java.util.List;
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
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) {
            return;
        }

        int n = runs.size();
        int[] boundary = new int[n + 1];
        StringBuilder concat = new StringBuilder();
        for (int r = 0; r < n; r++) {
            boundary[r] = concat.length();
            String t = runs.get(r).text();
            concat.append(t != null ? t : "");
        }
        boundary[n] = concat.length();

        String full = concat.toString();
        if (!full.contains("{{")) {
            return;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(full);
        if (!matcher.find()) {
            return;
        }

        matcher.reset();
        List<int[]> matchRanges = new ArrayList<>();
        List<String> matchValues = new ArrayList<>();
        while (matcher.find()) {
            matchRanges.add(new int[]{matcher.start(), matcher.end()});
            matchValues.add(variables.getOrDefault(matcher.group(1), ""));
        }

        for (int r = 0; r < n; r++) {
            int runStart = boundary[r];
            int runEnd = boundary[r + 1];

            boolean touches = false;
            for (int[] range : matchRanges) {
                if (range[0] < runEnd && range[1] > runStart) {
                    touches = true;
                    break;
                }
            }
            if (!touches) {
                continue;
            }

            StringBuilder newRunText = new StringBuilder();
            int pos = runStart;

            while (pos < runEnd) {
                int[] hitRange = null;
                int hitIdx = -1;
                for (int m = 0; m < matchRanges.size(); m++) {
                    int[] range = matchRanges.get(m);
                    if (pos >= range[0] && pos < range[1]) {
                        hitRange = range;
                        hitIdx = m;
                        break;
                    }
                }

                if (hitRange == null) {
                    newRunText.append(full.charAt(pos));
                    pos++;
                } else {
                    if (r == findRun(boundary, n, hitRange[0])) {
                        newRunText.append(matchValues.get(hitIdx));
                    }
                    pos = hitRange[1];
                }
            }

            runs.get(r).setText(newRunText.toString(), 0);
        }
    }

    private static int findRun(int[] boundary, int n, int position) {
        for (int r = 0; r < n; r++) {
            if (position >= boundary[r] && position < boundary[r + 1]) {
                return r;
            }
        }
        return n - 1;
    }
}
