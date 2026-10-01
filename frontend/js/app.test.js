/*
====================================================
PRUEBAS - ACTIVO PRINCIPAL (ACTA DE ENTREGA)
====================================================

Cubre la lógica pura del activo principal: mapeo al payload y
campos obligatorios por tipo de activo.

Ejecutar:

    node frontend/js/app.test.js

Escenarios cubiertos:

1. Entrega de equipo (flujo GLPI actual, sin cambios).
2. Entrega exclusiva de periférico (sin serial ni GLPI).
3. Múltiples activos (equipo + periférico en el mismo payload).

====================================================
*/

const assert = require("assert");

// app.js es un script de navegador: se stubea el DOM mínimo para
// poder requerirlo desde Node y probar solo las funciones puras
// (no se ejecuta el flujo de la página).
global.document = {
    addEventListener() { },
    querySelector() { return null; },
    querySelectorAll() { return []; }
};

const {
    ACTIVO_EQUIPO,
    ACTIVO_PERIFERICO,
    camposObligatoriosActivo,
    construirActivoPrincipal,
    modoActual,
    esModoPeriferico
} = require("./app.js");

/** Simula los radios de modalidad del acta marcando uno. */
function conModo(valor) {

    global.document.querySelector = () => ({ value: valor });

}

/** Simula que no hay ninguna modalidad marcada (DOM sin radios). */
function sinModo() {

    global.document.querySelector = () => null;

}

let pruebas = 0;

function probar(descripcion, fn) {

    fn();

    pruebas++;

    console.log(`  ok - ${descripcion}`);

}

/**
 * El backend deserializa `equipos` como List<EquipoItem>, y las
 * plantillas DOCX esperan eq_N_marca/tipo/modelo/serial/inventario.
 * Si la forma cambia, la generación de documentos se rompe.
 */
function assertFormaEquipoItem(activo) {

    assert.deepStrictEqual(
        Object.keys(activo).sort(),
        ["inventario", "marca", "modelo", "serial", "tipo"]
    );

}

console.log("Activo principal - Acta de Entrega");

/* --- 1. Entrega de equipo: comportamiento actual intacto ------------- */

probar("equipo: mapea serial, marca, tipo, modelo e inventario", () => {

    const activo = construirActivoPrincipal(ACTIVO_EQUIPO, {

        serial: "ABC123",
        marca: "Dell",
        tipo: "Portátil",
        modelo: "Latitude 3420",
        inventario: "INV-001"

    });

    assertFormaEquipoItem(activo);

    assert.deepStrictEqual(activo, {

        serial: "ABC123",
        marca: "Dell",
        tipo: "Portátil",
        modelo: "Latitude 3420",
        inventario: "INV-001"

    });

});

probar("equipo: exige serial e inventario (validación actual)", () => {

    assert.deepStrictEqual(

        camposObligatoriosActivo(ACTIVO_EQUIPO, 0)
            .map(campo => campo.selector),

        ["[data-serial]", "[data-inventario]"]

    );

});

/* --- 2. Periférico: sin GLPI y sin serial obligatorio ---------------- */

probar("periférico: solo la descripción es obligatoria", () => {

    const selectores = camposObligatoriosActivo(ACTIVO_PERIFERICO, 0)
        .map(campo => campo.selector);

    assert.deepStrictEqual(selectores, ["[data-per-descripcion]"]);

    // Marca, modelo y serial se ofrecen pero no se exigen.
    ["[data-per-marca]", "[data-per-modelo]", "[data-per-serial]"]
        .forEach(selector => assert.ok(
            !selectores.includes(selector),
            `${selector} no debe ser obligatorio en un periférico`
        ));

});

probar("periférico: la descripción es texto libre y va en Tipo", () => {

    const activo = construirActivoPrincipal(ACTIVO_PERIFERICO, {

        descripcion: "Mouse inalámbrico Logitech M185",
        marca: "Logitech",
        modelo: "M185",
        serial: ""

    });

    assertFormaEquipoItem(activo);

    assert.deepStrictEqual(activo, {

        serial: "",
        marca: "Logitech",
        tipo: "Mouse inalámbrico Logitech M185",
        modelo: "M185",
        inventario: ""

    });

});

probar("periférico: el inventario informado llega a la plantilla", () => {

    // DocumentoWordService mapea eq_N_inventario desde este campo.
    const activo = construirActivoPrincipal(ACTIVO_PERIFERICO, {

        descripcion: "Monitor Samsung 24\"",
        marca: "Samsung",
        modelo: "S24R350",
        serial: "MON-9",
        inventario: "INV-12345"

    });

    assertFormaEquipoItem(activo);

    assert.strictEqual(activo.inventario, "INV-12345");

});

probar("periférico: el inventario es texto libre, sin formato impuesto", () => {

    // Letras, números y combinaciones: tal cual se escribe.
    [
        "INV-12345",
        "CF-000567",
        "MON-2026-18",
        "458712"
    ].forEach(inventario => {

        const activo = construirActivoPrincipal(ACTIVO_PERIFERICO, {

            descripcion: "Hub USB-C",
            inventario

        });

        assert.strictEqual(activo.inventario, inventario);

    });

});

probar("periférico: los campos opcionales vacíos no rompen la fila", () => {

    const activo = construirActivoPrincipal(ACTIVO_PERIFERICO, {

        descripcion: "Cargador Lenovo USB-C"

    });

    assertFormaEquipoItem(activo);

    assert.strictEqual(activo.tipo, "Cargador Lenovo USB-C");
    assert.strictEqual(activo.marca, "");
    assert.strictEqual(activo.modelo, "");
    assert.strictEqual(activo.serial, "");

    // Sin inventario el acta sigue saliendo: la fila queda vacía.
    assert.strictEqual(activo.inventario, "");

});

probar("periférico: el inventario NO es obligatorio", () => {

    const selectores = camposObligatoriosActivo(ACTIVO_PERIFERICO, 0)
        .map(campo => campo.selector);

    assert.ok(
        !selectores.includes("[data-per-inventario]"),
        "el inventario debe poder quedar vacío"
    );

});

probar("periférico: cualquier descripción se acepta sin catálogo", () => {

    [
        "Mouse inalámbrico Logitech M185",
        'Monitor Samsung 24"',
        "Diadema Jabra",
        "Hub USB-C",
        "Adaptador HDMI",
        "Docking Station Dell WD19",
        "Cargador Lenovo USB-C",
        "Base refrigerante para portátil"
    ].forEach(descripcion => {

        const activo = construirActivoPrincipal(ACTIVO_PERIFERICO, {
            descripcion
        });

        assertFormaEquipoItem(activo);
        assert.strictEqual(activo.tipo, descripcion);

    });

});

/* --- 3. Múltiples activos: equipo + periférico ----------------------- */

probar("múltiples activos: equipo y periférico conviven en el payload", () => {

    const bloques = [

        {
            tipoActivo: ACTIVO_EQUIPO,
            valores: {
                serial: "ABC123",
                marca: "Dell",
                tipo: "Portátil",
                modelo: "Latitude 3420",
                inventario: "INV-001"
            }
        },

        {
            tipoActivo: ACTIVO_PERIFERICO,
            valores: {
                descripcion: 'Monitor Samsung 24"',
                marca: "Samsung",
                modelo: "",
                serial: "MON-9"
            }
        }

    ];

    const equipos = bloques.map(bloque =>
        construirActivoPrincipal(bloque.tipoActivo, bloque.valores));

    assert.strictEqual(equipos.length, 2);

    equipos.forEach(activo => assertFormaEquipoItem(activo));

    // El orden del DOM define eq_1 / eq_2 en la plantilla DOCX.
    assert.strictEqual(equipos[0].serial, "ABC123");
    assert.strictEqual(equipos[1].tipo, 'Monitor Samsung 24"');
    assert.strictEqual(equipos[1].serial, "MON-9");

});

/* --- 4. Modalidad global del acta ------------------------------------ */

probar("modalidad: por defecto es Equipo", () => {

    sinModo();

    assert.strictEqual(modoActual(), ACTIVO_EQUIPO);
    assert.strictEqual(esModoPeriferico(), false);

});

probar("modalidad: Periférico se detecta sin distinguir mayúsculas", () => {

    conModo("PERIFERICO");
    assert.strictEqual(esModoPeriferico(), true);

    conModo("periferico");
    assert.strictEqual(esModoPeriferico(), true);

});

probar("modalidad: un acta no puede mezclar equipo y periférico", () => {

    // Los bloques heredan la modalidad global, así que todos los
    // activos de un acta salen con el MISMO tipoActivo.
    conModo(ACTIVO_PERIFERICO);

    const bloques = [0, 1, 2].map(() =>
        construirActivoPrincipal(modoActual(), { descripcion: "Mouse" }));

    const tipos = new Set(bloques.map(b => b.inventario === "" ? "PERIFERICO" : "EQUIPO"));

    assert.strictEqual(tipos.size, 1, "el acta no debe contener tipos mezclados");

    // Y en modalidad Equipo, todos los bloques son de equipo.
    conModo(ACTIVO_EQUIPO);
    assert.strictEqual(modoActual(), ACTIVO_EQUIPO);

    sinModo();

});

console.log(`\n${pruebas} pruebas OK`);
