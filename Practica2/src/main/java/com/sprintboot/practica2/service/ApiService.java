package com.sprintboot.practica2.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:5000/api";

    public String simularErrorArchivo() {
        return invocarApi("/archivo", "archivo");
    }

    public String simularErrorBaseDatos() {
        return invocarApi("/basedatos", "base de datos");
    }

    public String simularErrorPokemon() {
        return invocarApi("/pokemon", "API externa");
    }

    private String invocarApi(String path, String tipoError) {
        try {
            return restTemplate.getForObject(BASE_URL + path, String.class);
        } catch (HttpStatusCodeException ex) {
            // Error específico de respuesta 4xx o 5xx
            return generarMensajeUsuario(tipoError);
        } catch (Exception ex) {
            // Error general (servidor caído, conexión fallida)
            return "❌ No se pudo conectar al servidor. Por favor, inténtalo más tarde.";
        }
    }


    private String generarMensajeUsuario(String tipoError) {
        switch (tipoError) {
            case "archivo":
                return "⚠️ No fue posible acceder al archivo solicitado. Por favor, revisa que el archivo exista o contacta con soporte.";
            case "base de datos":
                return "⚠️ Se ha producido un error al acceder a la base de datos. Intenta de nuevo más tarde.";
            case "API externa":
                return "⚠️ No fue posible obtener información de la API externa. Puede que el servicio esté caído o inalcanzable.";
            default:
                return "⚠️ Se ha producido un error inesperado. Intenta de nuevo.";
        }
    }
}
