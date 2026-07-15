from datetime import datetime
from docxtpl import DocxTemplate


def generar_acta(datos):

    fecha = datetime.now()

    datos["dia"] = fecha.strftime("%d")
    datos["mes"] = fecha.strftime("%m")
    datos["anio"] = fecha.strftime("%Y")

    doc = DocxTemplate(
        "plantillas/Acta de Entrega 2 2 - copia.docx"
    )

    doc.render(datos)

    ruta_salida = (
        "generados/Acta_Generada.docx"
    )

    doc.save(ruta_salida)

    return ruta_salida