import requests
import re
from app.config import *
from app.models import *

def iniciar_sesion():

    headers = {
        "App-Token": GLPI_APP_TOKEN,
        "Authorization": f"user_token {GLPI_USER_TOKEN}"
    }

    r = requests.get(
        f"{GLPI_URL}/initSession",
        headers=headers
    )

    print("STATUS:", r.status_code)
    print("RESPUESTA:", r.text)

    return r.json()["session_token"]


def cpu_corto(cpu):

    if not cpu:
        return ""

    patrones = [
        r"Ryzen\s+\d",
        r"Core\s+Ultra\s+\d",
        r"Core\(TM\)\s+i\d",
        r"Core\s+i\d",
        r"i\d",
        r"Pentium",
        r"Celeron",
        r"Xeon"
    ]

    for patron in patrones:

        match = re.search(
            patron,
            cpu,
            re.IGNORECASE
        )

        if match:

            texto = (
                match.group()
                .replace("Core(TM)", "Core")
                .replace("Intel(R)", "")
                .strip()
            )

            return texto

    return ""

def buscar_equipo(serial):

    session_token = iniciar_sesion()

    headers = {
        "App-Token": GLPI_APP_TOKEN,
        "Session-Token": session_token
    }

    url = (
        f"{GLPI_URL}/search/Computer"
        f"?criteria[0][field]=5"
        f"&criteria[0][searchtype]=contains"
        f"&criteria[0][value]={serial}"
        f"&forcedisplay[0]=23"
        f"&forcedisplay[1]=4"
        f"&forcedisplay[2]=40"
        f"&forcedisplay[3]=17"
    )

    r = requests.get(url, headers=headers)

    resultado = r.json()

    if resultado["count"] == 0:
        return {}

    equipo = resultado["data"][0]

    marca = equipo.get("23")
    tipo = equipo.get("4")
    modelo = equipo.get("40")
    procesador = equipo.get("17")

    sufijo_cpu = cpu_corto(procesador)

    modelo_acta = modelo

    if sufijo_cpu:
        modelo_acta = f"{modelo} {sufijo_cpu}"

    return {
        "marca": marca,
        "tipo": tipo,
        "modelo": modelo_acta
    }

