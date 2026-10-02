package com.empresa.actas.service;

import com.empresa.actas.dto.response.PerifericoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la búsqueda de periféricos en GLPI.
 *
 * Cubre lo que no se ve en un caso feliz: que un periférico puede vivir
 * en Monitor o en Peripheral, que no se consulte la segunda entidad si la
 * primera acierta, y que el inventario se lea del endpoint de ítem.
 *
 * GlpiClient va mockeado: estas pruebas no tocan la red ni GLPI real.
 *
 * Ejecutar:
 *
 *     cd backend && mvn test -Dtest=PerifericoServiceTest
 */
class PerifericoServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    /** Respuesta de /search con una fila que trae id, marca, tipo y modelo. */
    private JsonNode searchConResultado(int id, String marca, String tipo, String modelo) throws Exception {
        return mapper.readTree("""
                {"totalcount":1,"count":1,"data":[{
                  "2":%d,"23":"%s","4":"%s","40":"%s"
                }]}
                """.formatted(id, marca, tipo, modelo));
    }

    /** Respuesta de /search sin coincidencias. */
    private JsonNode searchVacio() throws Exception {
        return mapper.readTree("{\"totalcount\":0,\"count\":0,\"data\":[]}");
    }

    @Test
    void perifericoEncontradoEnPeripheral() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(eq("Monitor"), anyString())).thenReturn(searchVacio());
        when(glpi.search(eq("Peripheral"), anyString()))
                .thenReturn(searchConResultado(481, "Logitech", "Diadema", "REF.H390"));
        when(glpi.getItem("Peripheral", 481))
                .thenReturn(mapper.readTree("{\"otherserial\":\"INV-9\"}"));

        PerifericoResponse r = new PerifericoService(glpi).buscarPeriferico("2536AYG153G8");

        assertTrue(r.isEncontrado(), "debe encontrarlo en Peripheral");
        assertEquals("Logitech", r.getMarca());
        assertEquals("Diadema", r.getTipo());
        assertEquals("REF.H390", r.getModelo());
        assertEquals("INV-9", r.getInventario());
    }

    @Test
    void perifericoEncontradoEnMonitor() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(eq("Monitor"), anyString()))
                .thenReturn(searchConResultado(77, "AOC", "", "16T20"));

        PerifericoResponse r = new PerifericoService(glpi).buscarPeriferico("A6T2534Z05822");

        assertTrue(r.isEncontrado());
        assertEquals("AOC", r.getMarca());
        assertEquals("16T20", r.getModelo());
    }

    /**
     * Regla de precedencia: si el serial existe en Monitor, no se consulta
     * Peripheral. Esto es lo que hace determinista una colisión de seriales.
     */
    @Test
    void noConsultaPeripheralSiMonitorAcierta() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(eq("Monitor"), anyString()))
                .thenReturn(searchConResultado(5, "BOE", "", "16T20"));

        new PerifericoService(glpi).buscarPeriferico("0000ffa1");

        verify(glpi, never()).search(eq("Peripheral"), anyString());
    }

    @Test
    void serialInexistenteDevuelveNoEncontrado() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(anyString(), anyString())).thenReturn(searchVacio());

        PerifericoResponse r = new PerifericoService(glpi).buscarPeriferico("NOEXISTE");

        assertFalse(r.isEncontrado());
        assertEquals("", r.getMarca());
        assertEquals("", r.getInventario());
    }

    /** Un serial vacío no debe llegar a consultar GLPI. */
    @Test
    void serialVacioNoConsultaGlpi() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        assertFalse(new PerifericoService(glpi).buscarPeriferico("  ").isEncontrado());

        verify(glpi, never()).search(anyString(), anyString());
    }

    /**
     * El inventario es opcional: si el endpoint de ítem falla, el resto de
     * datos debe conservarse en vez de perder toda la respuesta.
     */
    @Test
    void inventarioNoDisponibleNoRompeLaRespuesta() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(eq("Monitor"), anyString())).thenReturn(searchVacio());
        when(glpi.search(eq("Peripheral"), anyString()))
                .thenReturn(searchConResultado(481, "Logitech", "Diadema", "REF.H390"));
        when(glpi.getItem(eq("Peripheral"), eq(481)))
                .thenThrow(new RuntimeException("GLPI caído"));

        PerifericoResponse r = new PerifericoService(glpi).buscarPeriferico("2536AYG153G8");

        assertTrue(r.isEncontrado(), "el hallazgo no depende del inventario");
        assertEquals("Logitech", r.getMarca());
        assertEquals("", r.getInventario());
    }

    /**
     * Si una entidad falla, se intenta la siguiente en vez de abortar:
     * GLPI puede rechazar Monitor por permisos y aún así tener el ítem
     * en Peripheral.
     */
    @Test
    void siUnaEntidadFallaSeIntentaLaSiguiente() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(eq("Monitor"), anyString()))
                .thenThrow(new RuntimeException("HTTP 403"));
        when(glpi.search(eq("Peripheral"), anyString()))
                .thenReturn(searchConResultado(481, "Logitech", "Diadema", "REF.H390"));

        PerifericoResponse r = new PerifericoService(glpi).buscarPeriferico("2536AYG153G8");

        assertTrue(r.isEncontrado(), "debe caer a Peripheral");
        assertEquals("Logitech", r.getMarca());
    }

    /** El serial debe ir URL-encoded en el criterio de búsqueda. */
    @Test
    void serialVaCodificadoEnLaQuery() throws Exception {
        GlpiClient glpi = mock(GlpiClient.class);

        when(glpi.search(anyString(), anyString())).thenReturn(searchVacio());

        new PerifericoService(glpi).buscarPeriferico("AB 12");

        verify(glpi).search(eq("Monitor"), contains("AB+12"));
    }
}
