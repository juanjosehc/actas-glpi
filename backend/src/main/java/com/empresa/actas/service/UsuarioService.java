package com.empresa.actas.service;

import com.empresa.actas.dto.response.UsuarioResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Servicio de búsqueda de usuarios en GLPI.
 *
 * Flujo de búsqueda:
 * 1. Autenticación con App-Token y User-Token (vía GlpiClient).
 * 2. Buscar en /search/User por firstname (field 9) u realname (field 34).
 * 3. Pedir explícitamente la columna de ID (field 2) con forcedisplay.
 * 4. Construir el nombre completo: firstname + " " + realname.
 * 5. Limitar resultados a 10 (range=0-9).
 *
 * La autenticación y el HttpClient son compartidos a través de GlpiClient.
 *
 * Campos GLPI:
 * - Field 2:  ID del usuario.
 * - Field 9:  firstname (nombres).
 * - Field 34: realname (apellidos).
 */
@Service
public class UsuarioService {

    private final GlpiClient glpiClient;

    public UsuarioService(GlpiClient glpiClient) {
        this.glpiClient = glpiClient;
    }

    /**
     * Busca usuarios en GLPI cuyo firstname o realname
     * contenga el texto indicado.
     *
     * @param texto Texto de búsqueda (mínimo 3 caracteres).
     * @return Lista de usuarios con id y nombreCompleto. Vacía si no hay coincidencias.
     */
    public List<UsuarioResponse> buscarUsuarios(String texto) {
        List<UsuarioResponse> resultados = new ArrayList<>();

        if (texto == null || texto.trim().length() < 3) {
            return resultados;
        }

        try {
            String valor = URLEncoder.encode(
                    texto.trim(),
                    StandardCharsets.UTF_8
            );

            String query = "?criteria[0][field]=9"
                    + "&criteria[0][searchtype]=contains"
                    + "&criteria[0][value]=" + valor
                    + "&criteria[1][link]=OR"
                    + "&criteria[1][field]=34"
                    + "&criteria[1][searchtype]=contains"
                    + "&criteria[1][value]=" + valor
                    + "&forcedisplay[0]=2"
                    + "&forcedisplay[1]=9"
                    + "&forcedisplay[2]=34"
                    + "&range=0-9";

            JsonNode root = glpiClient.search("User", query);
            JsonNode data = root.path("data");

            if (data.isArray()) {
                for (JsonNode item : data) {
                    agregarUsuario(resultados, extraerId(item), item);
                }
            } else {
                Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    int id;
                    try {
                        id = Integer.parseInt(entry.getKey());
                    } catch (NumberFormatException e) {
                        id = extraerId(entry.getValue());
                    }
                    agregarUsuario(resultados, id, entry.getValue());
                }
            }

        } catch (Exception e) {
            return resultados;
        }

        return resultados;
    }

    /**
     * Extrae el id del usuario desde una fila del resultado de búsqueda.
     *
     * En el endpoint /search/User las columnas se identifican por el
     * número del searchoption. La columna "2" corresponde al ID del
     * usuario y se solicita explícitamente con forcedisplay[0]=2
     * (de lo contrario GLPI no la incluye en la respuesta).
     *
     * @param item Nodo JSON de la fila del resultado.
     * @return Id del usuario, o -1 si no se puede determinar.
     */
    private int extraerId(JsonNode item) {
        JsonNode idNode = item.path("id");
        if (idNode.canConvertToInt()) {
            return idNode.asInt(-1);
        }
        return item.path("2").asInt(-1);
    }

    /**
     * Agrega un usuario a la lista si tiene id válido y nombre completo.
     *
     * @param resultados Lista donde se acumulan los resultados.
     * @param id         Id del usuario en GLPI.
     * @param item       Nodo JSON del usuario (firstname/realname).
     */
    private void agregarUsuario(List<UsuarioResponse> resultados, int id, JsonNode item) {
        if (id <= 0) {
            return;
        }

        String firstname = getFieldValue(item, "9");
        String realname = getFieldValue(item, "34");

        String nombreCompleto = (firstname + " " + realname).trim();

        if (nombreCompleto.isEmpty()) {
            return;
        }

        resultados.add(new UsuarioResponse(id, nombreCompleto));
    }

    /**
     * Extrae el valor de un campo específico de un nodo JSON de GLPI.
     *
     * Si el campo es array se concatena con espacio. Si es string
     * se retorna directamente.
     *
     * @param node    Nodo JSON del usuario.
     * @param fieldId ID del campo GLPI (como string).
     * @return Valor del campo, o cadena vacía si no existe.
     */
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
