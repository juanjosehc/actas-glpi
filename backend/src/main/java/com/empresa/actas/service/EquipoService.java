package com.empresa.actas.service;

import com.empresa.actas.dto.response.EquipoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EquipoService {

    @Value("${glpi.url}")
    private String glpiUrl;

    @Value("${glpi.app-token}")
    private String appToken;

    @Value("${glpi.user-token}")
    private String userToken;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EquipoResponse buscarEquipo(String serial) {
        try {
            String sessionToken = iniciarSesion();

            String url = glpiUrl + "/search/Computer"
                    + "?criteria[0][field]=5"
                    + "&criteria[0][searchtype]=contains"
                    + "&criteria[0][value]=" + serial
                    + "&forcedisplay[0]=23"
                    + "&forcedisplay[1]=4"
                    + "&forcedisplay[2]=40"
                    + "&forcedisplay[3]=17";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("App-Token", appToken)
                    .header("Session-Token", sessionToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = objectMapper.readTree(response.body());
            int count = root.path("count").asInt(0);

            if (count == 0) {
                return new EquipoResponse("", "", "");
            }

            JsonNode data = root.path("data");
            JsonNode first;

            if (data.isArray()) {
                first = data.get(0);
            } else {
                Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
                if (fields.hasNext()) {
                    first = fields.next().getValue();
                } else {
                    return new EquipoResponse("", "", "");
                }
            }

            String marca = getFieldValue(first, "23");
            String tipo = getFieldValue(first, "4");
            String modelo = getFieldValue(first, "40");
            String procesador = getFieldValue(first, "17");

            String sufijoCpu = cpuCorto(procesador);

            String modeloActa = modelo;
            if (sufijoCpu != null && !sufijoCpu.isEmpty()) {
                modeloActa = modelo + " " + sufijoCpu;
            }

            return new EquipoResponse(marca, tipo, modeloActa);

        } catch (Exception e) {
            System.out.println("Error consultando GLPI: " + e.getMessage());
            return new EquipoResponse("", "", "");
        }
    }

    private String iniciarSesion() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(glpiUrl + "/initSession"))
                .header("App-Token", appToken)
                .header("Authorization", "user_token " + userToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("session_token").asText();
    }

    private String cpuCorto(String cpu) {
        if (cpu == null || cpu.isEmpty()) {
            return "";
        }

        String[] patrones = {
                "Ryzen\\s+\\d",
                "Core\\s+Ultra\\s+\\d",
                "Core\\(TM\\)\\s+i\\d",
                "Core\\s+i\\d",
                "i\\d",
                "Pentium",
                "Celeron",
                "Xeon"
        };

        for (String patron : patrones) {
            Matcher matcher = Pattern.compile(
                    patron,
                    Pattern.CASE_INSENSITIVE
            ).matcher(cpu);

            if (matcher.find()) {
                String texto = matcher.group()
                        .replace("Core(TM)", "Core")
                        .replace("Intel(R)", "")
                        .trim();
                return texto;
            }
        }

        return "";
    }

    private String getFieldValue(JsonNode node, String fieldId) {
        JsonNode valueNode = node.path(fieldId);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return "";
        }
        if (valueNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : valueNode) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(item.asText(""));
            }
            return sb.toString().trim();
        }
        return valueNode.asText("");
    }
}
