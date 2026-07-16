from datetime import datetime
from annotated_types import *
from docxtpl import DocxTemplate
import re
import os

def generar_acta(datos):

    try:

        os.makedirs(
            "generados",
            exist_ok=True
        )

        fecha = datetime.strptime(
            datos["fecha"],
            "%Y-%m-%d"
        )

        datos["dia"] = fecha.strftime("%d")
        datos["mes"] = fecha.strftime("%m")
        datos["anio"] = fecha.strftime("%Y")

        doc = DocxTemplate(
            "plantillas/Acta de Entrega 2 2 - copia.docx"
        )

        for i in range(1, 17):

            datos[f"hw_{i}_tipo"] = ""
            datos[f"hw_{i}_descripcion"] = ""
            datos[f"hw_{i}_programa"] = ""
        
        for indice, item in enumerate(
            datos["hardware"],
            start=1
        ):

            if indice > 11:
                break

            datos[f"hw_{indice}_tipo"] = (
                item["tipo"]
            )

            datos[f"hw_{indice}_descripcion"] = (
                item["descripcion"]
            )

            datos[f"hw_{indice}_programa"] = (
                item["programa"]
            )
        
        for i in range(1, 11):

            datos[f"eq_{i}_marca"] = ""
            datos[f"eq_{i}_tipo"] = ""
            datos[f"eq_{i}_modelo"] = ""
            datos[f"eq_{i}_serial"] = ""
            datos[f"eq_{i}_inventario"] = ""
        
        for indice, equipo in enumerate(
            datos["equipos"],
            start=1
        ):

            if indice > 10:
                break

            datos[f"eq_{indice}_marca"] = (
                equipo["marca"]
            )

            datos[f"eq_{indice}_tipo"] = (
                equipo["tipo"]
            )

            datos[f"eq_{indice}_modelo"] = (
                equipo["modelo"]
            )

            datos[f"eq_{indice}_serial"] = (
                equipo["serial"]
            )

            datos[f"eq_{indice}_inventario"] = (
                equipo["inventario"]
            )

        print("=== EQUIPOS ===")
        print(datos["equipos"])

        print("=== HARDWARE ===")
        print(datos["hardware"])

        doc.render(datos)

        asunto = re.sub(
            r"[^a-zA-Z0-9]",
            "",
            datos["asunto"]
        )

        serial_principal = "SinSerial"

        if len(datos["equipos"]) > 0:

            serial_principal = (
                datos["equipos"][0]["serial"]
            )

        ruta_salida = (
            f"generados/ActaEntrega_{serial_principal}_{asunto}.docx"
        )

        print("Guardando:", ruta_salida)
        doc.save(ruta_salida)
        print("Guardado correctamente")

        print(ruta_salida)

        return {
            "success": True,
            "ruta": ruta_salida
        }

    except Exception as e:

        return {
            "success": False,
            "mensaje": str(e)
        }
    