from fastapi import APIRouter
from fastapi.responses import FileResponse
from app.services.acta import generar_acta
import os

router = APIRouter()

@router.post("/generar-acta")
def generar(data: dict):

    print("========== DATOS RECIBIDOS ==========")
    print(data)
    print("====================================")
    print("========== HARDWARE ==========")

    for item in data.get("hardware", []):
        print(item)

    print("==============================")

    
    print(
        f"Cantidad hardware: {len(data.get('hardware', []))}"
    )


    resultado = generar_acta(data)

    if not resultado["success"]:
        return resultado

    print("ARCHIVO:", resultado["ruta"])

    return FileResponse(
        path=resultado["ruta"],
        filename=os.path.basename(resultado["ruta"]),
        media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )