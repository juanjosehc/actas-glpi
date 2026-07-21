from dotenv import load_dotenv
import os
import tempfile

load_dotenv()

GLPI_URL = os.getenv("GLPI_URL")
GLPI_APP_TOKEN = os.getenv("GLPI_APP_TOKEN")
GLPI_USER_TOKEN = os.getenv("GLPI_USER_TOKEN")

GENERADOS_DIR = os.path.join(
    tempfile.gettempdir(),
    "actas_glpi_generados"
)

os.makedirs(GENERADOS_DIR, exist_ok=True)