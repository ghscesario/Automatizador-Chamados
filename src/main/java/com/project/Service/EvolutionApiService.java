package com.project.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EvolutionApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${evolutionapi.base-url}")
    private String baseUrl;

    @Value("${evolutionapi.api-key}")
    private String apiKey;

    @SuppressWarnings("UseSpecificCatch")
    public void sendTextMessage(String instanceId, String number, String text) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("Mensagem vazia. Ignorando envio.");
            return;
        }

        String url = baseUrl + "/message/sendText/" + instanceId;

        Map<String, String> body = new HashMap<>();
        body.put("number", number);
        body.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Resposta da EvolutionAPI: " + response.getStatusCode() + " - " + response.getBody());
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}
