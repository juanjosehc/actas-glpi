from fastapi import APIRouter
from fastapi.responses import FileResponse
from app.services.acta import generar_acta
from app.services.checklist_entrega import generar_checklist

from zipfile import ZipFile

import os
import copy

router = APIRouter()


@router.post("/generar-acta")
def generar(data: dict):

    print("========== DATOS RECIBIDOS ==========")
    print(data)

    # =========================
    # COPIAS INDEPENDIENTES
    # =========================

    datos_acta = copy.deepcopy(data)

    datos_checklist = copy.deepcopy(data)

    # =========================
    # GENERAR DOCUMENTOS
    # =========================

    resultado = generar_acta(
        datos_acta
    )

    resultado_checklist = generar_checklist(
        datos_checklist
    )

    print("=== RESULTADO ACTA ===")
    print(resultado)

    print("=== RESULTADO CHECKLIST ===")
    print(resultado_checklist)

    # =========================
    # VALIDACIONES
    # =========================

    if not resultado["success"]:
        return resultado

    
    if not resultado_checklist["success"]:

        print("ERROR CHECKLIST:")
        print(resultado_checklist)

        raise Exception(
            resultado_checklist["mensaje"]
        )


    # =========================
    # NOMBRE DEL ZIP
    # =========================

    asunto = "".join(
        c for c in data["asunto"]
        if c.isalnum()
    )

    serial = "SinSerial"

    if data.get("equipos"):

        if len(data["equipos"]) > 0:

            serial = (
                data["equipos"][0]["serial"]
            )

    nombre_zip = (
        f"ActaLista_{serial}_{asunto}.zip"
    )

    ruta_zip = (
        f"generados/{nombre_zip}"
    )

    # =========================
    # CREAR ZIP
    # =========================

    with ZipFile(
        ruta_zip,
        "w"
    ) as zip_file:

        zip_file.write(
            resultado["ruta"],
            arcname=os.path.basename(
                resultado["ruta"]
            )
        )

        zip_file.write(
            resultado_checklist["ruta"],
            arcname=os.path.basename(
                resultado_checklist["ruta"]
            )
        )

    print("ZIP:", ruta_zip)

    print(
        "EXISTE:",
        os.path.exists(ruta_zip)
    )

    print("ZIP CREADO")

    # =========================
    # RETORNAR ZIP
    # =========================
    print("ENTRANDO A FILERESPONSE")
    
    return FileResponse(
        path=ruta_zip,
        filename=nombre_zip,
        media_type="application/zip",
        headers={
            "Content-Disposition":
                f'attachment; filename="{nombre_zip}"'
        }
    )