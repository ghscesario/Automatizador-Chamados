package com.project.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final ChamadoService chamadoService;

    private final Map<String, Integer> userStep = new HashMap<>();
    private final Map<String, Map<String, String>> userResponses = new HashMap<>();
    private final Map<String, String> userModes = new HashMap<>();

    // Constantes de modo
    private static final String MODO_CHAMADO = "chamado";
    private static final String MODO_ATENDENTE = "atendente";
    private static final String MODO_INFO = "info";

    // Dados estáticos
    private static final List<String> BLOCO_KEYS = List.of("HOSPITAL", "BLOCO ADMINISTRATIVO");
    private static final Map<String, List<String>> BLOCO_ANDARES = Map.of(
        "HOSPITAL", List.of("SUBSOLO", "TÉRREO", "1º ANDAR", "2º ANDAR", "3º ANDAR", "4º ANDAR"),
        "BLOCO ADMINISTRATIVO", List.of("TÉRREO", "1º ANDAR")
    );
    private static final Map<String, List<String>> BLOCO_ANDAR_AREAS = Map.of(
        "HOSPITAL.SUBSOLO", List.of("COPA CME", "FARMÁCIA CENTRAL", "NUTRIÇÃO", "AGENCIA TRANSFUSIONAL", "CAF", "NECROTÉRIO"),
        "HOSPITAL.TÉRREO", List.of("AGENDAMENTO DE RETORNO", "AMBULATÓRIO DE ENFERMAGEM", "CONSULTÓRIO 01", "CONSULTÓRIO 02", "UTI 4: COPA"),
        "BLOCO ADMINISTRATIVO.TÉRREO", List.of("AUDITÓRIO", "BIBLIOTECA", "RH", "T.I"),
        "BLOCO ADMINISTRATIVO.1º ANDAR", List.of("COMPRAS", "CONTROLADORIA", "DIRETORIA ADMINISTRATIVA")
    );
    private static final List<String> CATEGORIAS = List.of("Áudio & Vídeo", "E-mail", "Equipamentos de TI", "Impressoras", "Rede", "Sistemas", "Telefonia");
    private static final Map<String, List<String>> SUBCATEGORIAS = Map.of(
        "Áudio & Vídeo", List.of("Microfone", "Monitor", "Projetor", "Som", "Tela de Projeção", "Videoconferência"),
        "E-mail", List.of("Nenhuma"),
        "Equipamentos de TI", List.of("Desktop", "Notebook", "Tablet"),
        "Impressoras", List.of("Impressora de Papel", "Impressora Térmica"),
        "Rede", List.of("Internet", "WiFi", "VPN"),
        "Sistemas", List.of("Sistemas Corporativos", "Sistemas de Imagem"),
        "Telefonia", List.of("Ramal/Telefone fixo", "Celular corporativo")
    );
    private static final List<String> URGENCIAS = List.of("Poucos equipamentos", "O meu departamento e não", "Toda a unidade");
    private static final List<String> SINTOMAS = List.of("Indisponibilidade", "Falha/Erro", "Lentidão", "Intermitência");

    ConversationService(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    public String processUserMessage(String user, String message) {
        // Permitir reset a qualquer momento
        if ("menu".equalsIgnoreCase(message.trim())) {
            userStep.remove(user);
            userResponses.remove(user);
            userModes.remove(user);
            return getMenuInicial();
        }

        // Verifica se o usuário tem um modo ativo
        if (!userModes.containsKey(user)) {
            return processarMenuInicial(user, message.trim());
        }

        String modo = userModes.get(user);

        if (MODO_INFO.equals(modo)) {
            return "📌 Informações da T.I:\n- Atendimento de segunda a sexta, das 08h às 18h\n- Suporte emergencial: ramal 1234\n- Email: suporte@hospital.com\n\nDigite 'menu' para voltar.";
        }

        if (MODO_ATENDENTE.equals(modo)) {
            return null; // modo silencioso
        }

        // Se chegou aqui, está no modo chamado
        int step = userStep.getOrDefault(user, 0);
        Map<String, String> responses = userResponses.computeIfAbsent(user, k -> new LinkedHashMap<>());

        if (step == 0 && !responses.containsKey("telefone")) {
            responses.put("telefone", user);
        }

        switch (step) {
            case 0:
                if (!responses.containsKey("telefone")) {
                    String telefoneLimpo = limparTelefone(user);
                    if (!telefoneValido(telefoneLimpo)) {
                        return "Número inválido. Por favor, envie seu número com DDD, ex: 62982595863";
                    }
                    responses.put("telefone", telefoneLimpo);
                }
                userStep.put(user, 1);
                return "Informe seu horário de trabalho (ex: 07h às 19h):";


            case 1:
                responses.put("horario", message.trim());
                userStep.put(user, 2);
                return buildOptionsMessage("Selecione o bloco:", BLOCO_KEYS);

            case 2:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if (idx < 0 || idx >= BLOCO_KEYS.size()) throw new NumberFormatException();
                    String bloco = BLOCO_KEYS.get(idx);
                    responses.put("bloco", bloco);
                    userStep.put(user, 3);
                    return buildOptionsMessage("Selecione o andar do bloco " + bloco + ":", BLOCO_ANDARES.get(bloco));
                } catch (Exception e) {
                    return "❌ Bloco inválido. Tente novamente.";
                }

            case 3:
                try {
                    String bloco = responses.get("bloco");
                    List<String> andares = BLOCO_ANDARES.get(bloco);
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String andar = andares.get(idx);
                    responses.put("andar", andar);
                    userStep.put(user, 4);
                    List<String> areas = BLOCO_ANDAR_AREAS.getOrDefault(bloco + "." + andar, List.of("Área não disponível"));
                    return buildOptionsMessage("Selecione a área:", areas);
                } catch (Exception e) {
                    return "❌ Andar inválido. Tente novamente.";
                }

            case 4:
                try {
                    String bloco = responses.get("bloco");
                    String andar = responses.get("andar");
                    List<String> areas = BLOCO_ANDAR_AREAS.getOrDefault(bloco + "." + andar, List.of("Área não disponível"));
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String area = areas.get(idx);
                    responses.put("area", area);
                    userStep.put(user, 5);
                    return buildOptionsMessage("Selecione a categoria:", CATEGORIAS);
                } catch (Exception e) {
                    return "❌ Área inválida. Tente novamente.";
                }

            case 5:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String categoria = CATEGORIAS.get(idx);
                    responses.put("categoria", categoria);
                    userStep.put(user, 6);
                    List<String> subcats = SUBCATEGORIAS.getOrDefault(categoria, List.of("Nenhuma"));
                    return buildOptionsMessage("Selecione a subcategoria:", subcats);
                } catch (Exception e) {
                    return "❌ Categoria inválida. Tente novamente.";
                }

            case 6:
                try {
                    String categoria = responses.get("categoria");
                    List<String> subcats = SUBCATEGORIAS.getOrDefault(categoria, List.of("Nenhuma"));
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String subcat = subcats.get(idx);
                    responses.put("subcategoria", subcat);
                    userStep.put(user, 7);
                    return buildOptionsMessage("Selecione o nível de urgência:", URGENCIAS);
                } catch (Exception e) {
                    return "❌ Subcategoria inválida. Tente novamente.";
                }

            case 7:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String urgencia = URGENCIAS.get(idx);
                    responses.put("urgencia", urgencia);
                    userStep.put(user, 8);
                    return buildOptionsMessage("Selecione o sintoma:", SINTOMAS);
                } catch (Exception e) {
                    return "❌ Urgência inválida. Tente novamente.";
                }

            case 8:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String sintoma = SINTOMAS.get(idx);
                    responses.put("sintoma", sintoma);
                    userStep.put(user, 9);
                    return "Digite um resumo do problema:";
                } catch (Exception e) {
                    return "❌ Sintoma inválido. Tente novamente.";
                }

            case 9:
                responses.put("resumo", message.trim());
                userStep.put(user, 10);
                return "Descreva o problema com mais detalhes:";

            case 10:
                responses.put("descricao", message.trim());

                // Impressão dos dados no console
                System.out.println("\n--- DADOS COLETADOS DO USUÁRIO " + user + " ---");
                System.out.println("Telefone: " + responses.get("telefone"));
                System.out.println("Horário: " + responses.get("horario"));
                System.out.println("Bloco: " + responses.get("bloco"));
                System.out.println("Andar: " + responses.get("andar"));
                System.out.println("Área: " + responses.get("area"));
                System.out.println("Categoria: " + responses.get("categoria"));
                System.out.println("Subcategoria: " + responses.get("subcategoria"));
                System.out.println("Urgência: " + responses.get("urgencia"));
                System.out.println("Sintoma: " + responses.get("sintoma"));
                System.out.println("Resumo: " + responses.get("resumo"));
                System.out.println("Descrição detalhada: " + responses.get("descricao"));
                System.out.println("-----------------------------");

                // Chamada ao serviço Playwright para abrir o chamado
            try {
                String telefoneOriginal = responses.get("telefone");
                String telefoneLimpo = limparTelefone(telefoneOriginal);  // Limpa aqui de novo para garantir

                String horario = responses.get("horario");
                String bloco = responses.get("bloco");
                String andar = responses.get("andar");
                String area = responses.get("area");
                String categoria = responses.get("categoria");
                String subcategoria = responses.get("subcategoria");
                String urgencia = responses.get("urgencia");
                String sintoma = responses.get("sintoma");
                String resumo = responses.get("resumo");
                String descricao = responses.get("descricao");

                chamadoService.criarChamado(
                    telefoneLimpo, horario, bloco, andar, area, categoria,
                    subcategoria, urgencia, sintoma, resumo, descricao
                );
            } catch (Exception e) {
                System.err.println("Erro ao executar a criação do chamado com Playwright: " + e.getMessage());
            }



                // Limpa estado e oferece nova ação
                userStep.put(user, 999); // menu extra
                return "✅ Chamado finalizado com sucesso!\n\n📋 Deseja fazer mais alguma coisa?\n1 - Abrir novo chamado\n2 - Falar com atendente\n3 - Informações da T.I\n\nOu digite 'menu' para começar novamente.";
            
            case 999:
                return processarMenuInicial(user, message.trim());

            default:
                userStep.remove(user);
                userResponses.remove(user);
                userModes.remove(user);
                return "❗ Conversa reiniciada. Digite 'menu' para começar.";
        }
    }

    private String processarMenuInicial(String user, String input) {
        switch (input) {
            case "1":
                userModes.put(user, MODO_CHAMADO);
                userStep.put(user, 0);
                userResponses.put(user, new LinkedHashMap<>());
                return processUserMessage(user, "");
            case "2":
                userModes.put(user, MODO_ATENDENTE);
                return "📞 Um atendente será acionado. Aguarde o contato.";
            case "3":
                userModes.put(user, MODO_INFO);
                return "📌 Informações da T.I:\n- Atendimento de segunda a sexta, das 08h às 18h\n- Suporte emergencial: ramal 1234\n- Email: suporte@hospital.com";
            default:
                return getMenuInicial();
        }
    }

    private String getMenuInicial() {
        return "👋 Olá! Como posso te ajudar?\n\n1 - Abrir chamado\n2 - Falar com atendente\n3 - Informações sobre a T.I";
    }

    private String buildOptionsMessage(String prompt, List<String> options) {
        StringBuilder sb = new StringBuilder(prompt).append("\n");
        for (int i = 0; i < options.size(); i++) {
            sb.append(i + 1).append(" - ").append(options.get(i)).append("\n");
        }
        return sb.toString();
    }

    private String limparTelefone(String raw) {
        if (raw == null) return "";

        // Remove sufixo WhatsApp
        raw = raw.replace("@s.whatsapp.net", "");

        // Remove todos caracteres não numéricos (ex: espaços, sinais)
        raw = raw.replaceAll("\\D", "");

        // Remove prefixo 55 se presente e se o número for maior que 11 dígitos
        if (raw.startsWith("55") && raw.length() > 11) {
            raw = raw.substring(2);
        }

        return raw;
    }

    private boolean telefoneValido(String telefone) {
    return telefone != null && telefone.matches("\\d{10,11}");
    }

}
