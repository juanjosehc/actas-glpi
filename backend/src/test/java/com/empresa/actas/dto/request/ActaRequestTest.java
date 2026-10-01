package com.empresa.actas.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de validación de ActaRequest.
 *
 * El punto crítico: numero_sac y sistema_operativo solo se imprimen en
 * la lista de chequeo, que no se genera en modalidad PERIFERICO. Por eso
 * dejan de ser obligatorios ahí, pero siguen siéndolo en modalidad EQUIPO
 * (y cuando no se envía el campo modo, para no romper clientes antiguos).
 *
 * Ejecutar:
 *
 *     cd backend && mvn test -Dtest=ActaRequestTest
 */
class ActaRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    /** Nombres de las propiedades que fallaron la validación. */
    private static Set<String> camposInvalidos(ActaRequest request) {
        return validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    /** Request con todo lo obligatorio de la cabecera del acta. */
    private static ActaRequest base() {
        ActaRequest r = new ActaRequest();
        r.setFecha("2026-10-01");
        r.setEntregado_a("Juan Perez");
        r.setCargo_recibe("Analista");
        r.setEntregado_por("Maria Lopez");
        r.setCargo_entrega("Coordinador TI");
        r.setAsunto("entrega");
        return r;
    }

    @Test
    void equipoExigeNumeroSacYSistemaOperativo() {

        Set<String> invalidos = camposInvalidos(base());

        assertTrue(invalidos.contains("numeroSacValido"),
                "en modalidad Equipo el numero_sac debe ser obligatorio");
        assertTrue(invalidos.contains("sistemaOperativoValido"),
                "en modalidad Equipo el sistema operativo debe ser obligatorio");
    }

    @Test
    void sinModoSeAsumeEquipo() {

        ActaRequest r = base();
        r.setNumero_sac("123456789");
        r.setSistema_operativo("Windows 11");

        assertFalse(r.esModoPeriferico(),
                "sin campo modo el acta debe comportarse como Equipo");
        assertTrue(camposInvalidos(r).isEmpty(),
                "un acta de equipo completa no debe tener errores");
    }

    @Test
    void perifericoNoExigeNumeroSacNiSistemaOperativo() {

        ActaRequest r = base();
        r.setModo("PERIFERICO");

        assertTrue(r.esModoPeriferico());
        assertTrue(camposInvalidos(r).isEmpty(),
                "una entrega de periférico no requiere checklist: "
                        + "numero_sac ni sistema_operativo no deben exigirse");
    }

    @Test
    void laCabeceraSigueSiendoObligatoriaEnPeriferico() {

        ActaRequest r = new ActaRequest();
        r.setModo("PERIFERICO");

        Set<String> invalidos = camposInvalidos(r);

        // La simplificación del checklist no debe relajar la cabecera.
        assertTrue(invalidos.contains("fecha"));
        assertTrue(invalidos.contains("entregado_a"));
        assertTrue(invalidos.contains("cargo_recibe"));
        assertTrue(invalidos.contains("entregado_por"));
        assertTrue(invalidos.contains("cargo_entrega"));
        assertTrue(invalidos.contains("asunto"));
    }

    @Test
    void elModoNoDistingueMayusculas() {

        ActaRequest r = base();
        r.setModo("periferico");

        assertTrue(r.esModoPeriferico());
        assertTrue(camposInvalidos(r).isEmpty());
    }
}
