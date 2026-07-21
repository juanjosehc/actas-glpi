from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routes.equipos import router as equipos_router
from app.routes.actas import router as actas_router

app = FastAPI()

# Rutas
app.include_router(equipos_router)
app.include_router(actas_router)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=[
        "Content-Disposition"
    ]
)