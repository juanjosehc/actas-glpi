from fastapi import APIRouter
from fastapi.responses import FileResponse

from app.services.acta import generar_acta

router = APIRouter()

@router.post("/generar-acta")
def generar(data: dict):

    archivo = generar_acta(data)

    return FileResponse(
        path=archivo,
        filename="Acta_Entrega.docx",
        media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )