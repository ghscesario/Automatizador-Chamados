package com.project.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.Service.ConversationService;
import com.project.Service.EvolutionApiService;

@RestController
public class EvolutionWebhookController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private EvolutionApiService evolutionApiService;

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> payload) {
        System.out.println("Mensagem recebida do EvolutionAPI: " + payload);

        String event = (String) payload.get("event");
        if (!"messages.upsert".equals(event)) {
            System.out.println("Evento ignorado: " + event);
            return ResponseEntity.ok("Evento ignorado");
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            return ResponseEntity.badRequest().body("Payload sem dados");
        }

        Map<String, Object> key = (Map<String, Object>) data.get("key");
        Map<String, Object> message = (Map<String, Object>) data.get("message");

        if (key == null || message == null) {
            return ResponseEntity.ok("Mensagem sem key ou message");
        }

        String text = (String) message.get("conversation");
        if (text == null || text.trim().isEmpty()) {
            System.out.println("Mensagem vazia ou tipo diferente de 'conversation'");
            return ResponseEntity.ok("Mensagem vazia ou tipo não suportado");
        }

        String remoteJid = (String) key.get("remoteJid");
        if (remoteJid == null) {
            return ResponseEntity.ok("Número remoto não informado");
        }
        // Extrai número sem sufixo
        String userNumber = remoteJid.replace("@s.whatsapp.net", "");

        System.out.println("Recebido do usuário " + userNumber + ": " + text);

        // Passa o número e texto para processar a conversa
        String resposta = conversationService.processUserMessage(userNumber, text);

        // Envia a resposta para o mesmo número do usuário
        evolutionApiService.sendTextMessage(userNumber, resposta);

        return ResponseEntity.ok("Mensagem processada");
    }
}
