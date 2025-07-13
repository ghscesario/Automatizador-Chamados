package com.project.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.Service.ConversationService;
import com.project.Service.EvolutionApiService;
import com.project.Service.ListasService;

@RestController
public class EvolutionWebhookController {

    private final ConversationService conversationService;
    private final EvolutionApiService evolutionApiService;
    private final ListasService       listasService;

    /** instance‑id configurado em application.properties **/
    @Value("${evolutionapi.instance-id}")
    private String instanceId;

    public EvolutionWebhookController(ConversationService conversationService,
                                      EvolutionApiService evolutionApiService,
                                      ListasService listasService) {
        this.conversationService = conversationService;
        this.evolutionApiService = evolutionApiService;
        this.listasService       = listasService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> payload) {
        System.out.println("Mensagem recebida do EvolutionAPI: " + payload);

        /* ‑‑ 1. Valida evento ------------------------------------------------ */
        if (!"messages.upsert".equals(payload.get("event"))) {
            return ResponseEntity.ok("Evento ignorado");
        }

        Map<String, Object> data    = (Map<String, Object>) payload.get("data");
        Map<String, Object> key     = data == null ? null : (Map<String, Object>) data.get("key");
        Map<String, Object> message = data == null ? null : (Map<String, Object>) data.get("message");
        if (key == null || message == null) return ResponseEntity.ok("payload incompleto");

        String remoteJid = (String) key.get("remoteJid");
        if (remoteJid == null) return ResponseEntity.ok("sem remoteJid");

        /* ‑‑ 2. Ignora grupos ------------------------------------------------ */
        if (remoteJid.contains("@g.us")) {
            System.out.println("Grupo detectado – ignorado");
            return ResponseEntity.ok("grupo");
        }

        /* ‑‑ 3. Normaliza número --------------------------------------------- */
        String numero = remoteJid.replace("@s.whatsapp.net", "");

        /* ‑‑ 4. Filtra pela whitelist obrigatória --------------------------- */
        if (!listasService.emWhitelist(numero)) {
            System.out.println("Número fora da whitelist – ignorado: " + numero);
            return ResponseEntity.ok("fora da whitelist");
        }

        /* ‑‑ 5. Filtra pela blacklist ---------------------------------------- */
        if (listasService.emBlacklist(numero)) {
            System.out.println("Blacklist silencioso: " + numero);
            return ResponseEntity.ok("blacklist");
        }

        /* ‑‑ 6. Obtém texto -------------------------------------------------- */
        String texto = (String) message.get("conversation");
        if (texto == null || texto.isBlank()) {
            return ResponseEntity.ok("mensagem vazia");
        }
        System.out.println("<<" + numero + ": " + texto);

        /* ‑‑ 7. Processa pelo bot ------------------------------------------- */
        String resposta = conversationService.processUserMessage(numero, texto);

        /* ‑‑ 8. Envia (se houver) ------------------------------------------- */
        if (resposta != null && !resposta.isBlank()) {
            evolutionApiService.sendTextMessage(instanceId, numero, resposta);
        } else {
            System.out.println("(modo atendente / sem resposta)");
        }

        return ResponseEntity.ok("ok");
    }
}
