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

        String remoteJid = (String) key.get("remoteJid");
        if (remoteJid == null) {
            return ResponseEntity.ok("Número remoto não informado");
        }

        // IGNORA MENSAGENS DE GRUPOS
        if (remoteJid.contains("@g.us")) {
            System.out.println("Mensagem recebida de grupo. Ignorando.");
            return ResponseEntity.ok("Mensagem de grupo ignorada");
        }

        String text = (String) message.get("conversation");
        if (text == null || text.trim().isEmpty()) {
            System.out.println("Mensagem vazia ou tipo diferente de 'conversation'");
            return ResponseEntity.ok("Mensagem vazia ou tipo não suportado");
        }

        String userNumber = remoteJid.replace("@s.whatsapp.net", "");
        System.out.println("Recebido do usuário " + userNumber + ": " + text);

        String resposta = conversationService.processUserMessage(userNumber, text);

        // Se o modo for "atendente", o serviço retorna null e nenhuma mensagem é enviada
        if (resposta != null && !resposta.trim().isEmpty()) {
            evolutionApiService.sendTextMessage("test2", userNumber, resposta);
        } else {
            System.out.println("Modo silencioso ou sem resposta necessária.");
        }

        return ResponseEntity.ok("Mensagem processada");
    }
}
