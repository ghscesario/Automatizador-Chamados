package com.project.Controller;



import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class EvolutionWebhookController {

    @PostMapping("/webhook")
public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> payload) {
    System.out.println("Mensagem recebida do EvolutionAPI: " + payload);

    String event = (String) payload.get("event");
    if ("messages.upsert".equals(event)) {
        // Extrair dados da mensagem do payload
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        // Aqui depende da estrutura exata do data — normalmente mensagens ficam dentro de "messages" ou similar
        System.out.println("Recebeu mensagem do usuário: " + data);
        // Sua lógica para armazenar, responder, etc.
    } else {
        System.out.println("Evento ignorado: " + event);
    }

    return ResponseEntity.ok("Recebido");
}

    
}

    
        

