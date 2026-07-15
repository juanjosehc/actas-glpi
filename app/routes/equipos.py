from fastapi import APIRouter
from app.services.glpi import buscar_equipo

router = APIRouter()

@router.get("/equipo/{serial}")
def obtener_equipo(serial: str):

    return buscar_equipo(serial)