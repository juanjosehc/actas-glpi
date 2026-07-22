package com.empresa.actas.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    public void crearZip(Path destino, Path... archivos) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(
                new java.io.FileOutputStream(destino.toFile()))) {

            for (Path archivo : archivos) {
                ZipEntry entry = new ZipEntry(archivo.getFileName().toString());
                zos.putNextEntry(entry);
                java.nio.file.Files.copy(archivo, zos);
                zos.closeEntry();
            }
        }
    }
}
