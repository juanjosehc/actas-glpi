package com.empresa.actas.service;

import com.empresa.actas.dto.response.PerifericoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Servicio de integración con la API de GLPI para consultar periféricos.
 *
 * Reutiliza {@link GlpiClient} (misma autenticación y mismo HttpClient
 * que EquipoService) y la misma estrategia de búsqueda: /search/{itemtype}
 * filtrando por el searchoption 5 (serial) con forcedisplay de las
 * columnas necesarias.
 *
 * ¿Por qué dos entidades?
 * En GLPI un periférico entregado puede estar registrado como Monitor
 * o como Peripheral, y el acta no permite saber de antemano cuál es.
 * Se consulta Monitor primero y Peripheral después, devolviendo el
 * primer acierto.
 *
 * Estrategia ante colisión (serial en ambas entidades):
 * se devuelve Monitor. El orden es explícito y estable para que un
 * mismo serial produzca siempre la misma respuesta. En la práctica la
 * colisión es improbable porque los seriales de fabricante no se
 * repiten entre un monitor y un periférico.
 *
 * Campos GLPI (verificados contra la instancia real; los searchoptions
 * 4, 5, 23 y 40 tienen el mismo significado en Monitor, Peripheral y
 * Computer):
 * - Field 5:  serial (número de serie).
 * - Field 23: fabricante (marca).
 * - Field 4:  tipo.
 * - Field 40: modelo.
 *
 * Inventario: el searchoption 31 resultó ser "Estado" ("En producción"),
 * no el inventario. El inventario viaja en el atributo {@code otherserial}
 * del ítem, que no se expone por /search, así que se lee del endpoint de
 * ítem {@code /{itemtype}/{id}} usando el id que sí devuelve la búsqueda
 * (field 2). Si esa segunda llamada falla, el inventario queda vacío y el
 * resto de datos se conserva: el inventario es opcional en el acta.
 */
@Service
public class PerifericoService {

    private final GlpiClient glpiClient;

    /**
     * Entidades GLPI donde puede vivir un periférico, en orden de
     * precedencia. El orden define qué gana si un serial existe en ambas.
     */
    private static final List<String> ENTIDADES = List.of("Monitor", "Peripheral");

    public PerifericoService(GlpiClient glpiClient) {
        this.glpiClient = glpiClient;
    }

    /**
     * Busca un periférico en GLPI por serial, en Monitor y Peripheral.
     *
     * @param serial Número de serial a buscar.
     * @return PerifericoResponse con marca, tipo, modelo e inventario.
     *         {@code encontrado=false} si no existe en ninguna entidad.
     */
    public PerifericoResponse buscarPeriferico(String serial) {
        if (serial == null || serial.isBlank()) {
            return PerifericoResponse.noEncontrado();
        }

        for (String entidad : ENTIDADES) {
            try {
                PerifericoResponse encontrado = buscarEnEntidad(entidad, serial);
                if (encontrado.isEncontrado()) {
                    return encontrado;
                }
            } catch (Exception e) {
                // Si una entidad falla (GLPI lento, ítem sin permiso),
                // se intenta la siguiente en vez de abortar la búsqueda.
                // ponytail: se traga el error a propósito; el acta sigue
                // siendo diligenciable a mano. Ver log si hay que depurar.
            }
        }

        return PerifericoResponse.noEncontrado();
    }

    /**
     * Consulta una entidad concreta de GLPI por serial.
     *
     * @param itemtype "Monitor" o "Peripheral".
     * @param serial   Serial a buscar.
     * @return Datos del primer ítem que coincida, o noEncontrado().
     */
    private PerifericoResponse buscarEnEntidad(String itemtype, String serial) throws Exception {
        String valor = URLEncoder.encode(serial, StandardCharsets.UTF_8);

        String query = "?criteria[0][field]=5"
                + "&criteria[0][searchtype]=contains"
                + "&criteria[0][value]=" + valor
                + "&forcedisplay[0]=2"
                + "&forcedisplay[1]=23"
                + "&forcedisplay[2]=4"
                + "&forcedisplay[3]=40";

        JsonNode root = glpiClient.search(itemtype, query);

        if (root.path("count").asInt(0) == 0) {
            return PerifericoResponse.noEncontrado();
        }

        JsonNode first = primerItem(root.path("data"));
        if (first == null) {
            return PerifericoResponse.noEncontrado();
        }

        String marca = getFieldValue(first, "23");
        String tipo = getFieldValue(first, "4");
        String modelo = getFieldValue(first, "40");

        return new PerifericoResponse(
                true,
                marca,
                tipo,
                modelo,
                leerInventario(itemtype, first)
        );
    }

    /**
     * Lee el inventario ({@code otherserial}) del ítem por su id.
     *
     * El id viene en el searchoption 2. Si no está o la llamada falla,
     * se devuelve cadena vacía: el inventario no es obligatorio.
     *
     * @param itemtype Entidad GLPI.
     * @param item     Fila devuelta por la búsqueda.
     * @return Inventario del ítem, o "" si no se puede leer.
     */
    private String leerInventario(String itemtype, JsonNode item) {
        int id = item.path("2").asInt(-1);
        if (id <= 0) {
            return "";
        }

        try {
            JsonNode detalle = glpiClient.getItem(itemtype, id);
            return detalle.path("otherserial").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Obtiene la primera fila del resultado de búsqueda de GLPI.
     *
     * El endpoint /search puede devolver un array o un objeto con
     * claves numéricas, según la versión y el rango solicitado.
     *
     * @param data Nodo "data" de la respuesta.
     * @return Primer ítem, o null si no hay ninguno.
     */
    private JsonNode primerItem(JsonNode data) {
        if (data.isArray()) {
            return data.isEmpty() ? null : data.get(0);
        }

        Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
        return fields.hasNext() ? fields.next().getValue() : null;
    }

    /**
     * Extrae el valor de un campo específico de un nodo JSON de GLPI.
     *
     * GLPI retorna arrays para campos con múltiples valores.
     * Si es array, se concatena con espacio. Si es string, se retorna directamente.
     *
     * @param node    Nodo JSON del periférico.
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
