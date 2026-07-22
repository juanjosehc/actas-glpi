package com.empresa.actas.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class AppConfig {

    @Value("${app.generated-dir}")
    private String generatedDir;

    @PostConstruct
    public void init() {
        loadDotenv();

        File dir = new File(generatedDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void loadDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("../")
                    .filename(".env")
                    .load();

            setEnvIfMissing("GLPI_URL", dotenv.get("GLPI_URL"));
            setEnvIfMissing("GLPI_APP_TOKEN", dotenv.get("GLPI_APP_TOKEN"));
            setEnvIfMissing("GLPI_USER_TOKEN", dotenv.get("GLPI_USER_TOKEN"));
        } catch (Exception e) {
            System.out.println("No se pudo cargar .env: " + e.getMessage());
        }
    }

    private void setEnvIfMissing(String key, String value) {
        if (value != null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
