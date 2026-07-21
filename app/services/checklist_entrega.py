from datetime import datetime
from docxtpl import DocxTemplate
import os
import re

from app.config import GENERADOS_DIR


def generar_checklist(datos):

    try:

        os.makedirs(
            GENERADOS_DIR,
            exist_ok=True
        )

        fecha = datetime.strptime(
            datos["fecha"],
            "%Y-%m-%d"
        )

        datos["dia"] = fecha.strftime("%d")
        datos["mes"] = fecha.strftime("%m")
        datos["anio"] = fecha.strftime("%Y")

        # =========================
        # SISTEMA OPERATIVO
        # =========================

        datos["responsable_verificacion"] = (
            datos["entregado_por"]
        )

        so = datos.get(
            "sistema_operativo",
            ""
        )

        datos["win10"] = (
            "■"
            if so == "Windows 10"
            else "□"
        )

        datos["win11"] = (
            "■"
            if so == "Windows 11"
            else "□"
        )

        datos["macos"] = (
            "■"
            if so == "Mac OS"
            else "□"
        )

        # =========================
        # CHECKLIST 1-36
        # =========================

        for i in range(1, 37):

            valor = (
                datos
                .get("checklist", {})
                .get(f"chk_{i}", False)
            )

            datos[f"chk_{i}_si"] = (
                "■" if valor else "□"
            )

            datos[f"chk_{i}_no"] = (
                "□" if valor else "■"
            )

        # =========================
        # EQUIPO PRINCIPAL
        # =========================

        if datos["equipos"]:

            equipo = datos["equipos"][0]

            datos["marca"] = (
                equipo["marca"]
            )

            datos["tipo"] = (
                equipo["tipo"]
            )

            datos["modelo"] = (
                equipo["modelo"]
            )

            datos["serial"] = (
                equipo["serial"]
            )

            datos["inventario"] = (
                equipo["inventario"]
            )

        else:

            datos["marca"] = ""
            datos["tipo"] = ""
            datos["modelo"] = ""
            datos["serial"] = ""
            datos["inventario"] = ""
        
        print(os.path.abspath("plantillas"))
        print(os.listdir("plantillas"))


        BASE_DIR = os.path.dirname(
            os.path.dirname(
                os.path.dirname(__file__)
            )
        )

        ruta_plantilla = os.path.join(
            BASE_DIR,
            "plantillas",
            "ListaChequeo.docx"
        )

        print(ruta_plantilla)

        doc = DocxTemplate(
            ruta_plantilla
        )
        doc.render(datos)

        asunto = re.sub(
            r"[^a-zA-Z0-9]",
            "",
            datos["asunto"]
        )

        serial_principal = "SinSerial"

        if datos["equipos"]:

            serial_principal = (
                datos["equipos"][0]["serial"]
            )

        ruta_salida = (
            os.path.join(GENERADOS_DIR, f"Checklist_{serial_principal}_{asunto}.docx")
        )

        doc.save(
            ruta_salida
        )

        return {

            "success": True,
            "ruta": ruta_salida

        }

    except Exception as e:

        return {

            "success": False,
            "mensaje": str(e)

        }