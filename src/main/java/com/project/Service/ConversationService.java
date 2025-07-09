package com.project.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final Map<String, Integer> userStep = new HashMap<>();
    private final Map<String, Map<String, String>> userResponses = new HashMap<>();

    // Dados estáticos completos
    private static final List<String> BLOCO_KEYS = List.of("HOSPITAL", "BLOCO ADMINISTRATIVO");
    private static final Map<String, List<String>> BLOCO_ANDARES = Map.of(
        "HOSPITAL", List.of("SUBSOLO", "TÉRREO", "1º ANDAR", "2º ANDAR", "3º ANDAR", "4º ANDAR"),
        "BLOCO ADMINISTRATIVO", List.of("TÉRREO", "1º ANDAR")
    );
    private static final Map<String, List<String>> BLOCO_ANDAR_AREAS = Map.of(
        "HOSPITAL.SUBSOLO", List.of("COPA CME", "FARMÁCIA CENTRAL", "NUTRIÇÃO", "AGENCIA TRANSFUSIONAL", "CAF", "NECROTÉRIO"),
        "HOSPITAL.TÉRREO", List.of(
            "(GESSO) SALA DOS MÉDICOS", "AGENDAMENTO DE RETORNO", "AMBULATÓRIO DE ENFERMAGEM", "CONSULTÓRIO 01", "CONSULTÓRIO 02",
            "CONSULTÓRIO 03", "CONSULTÓRIO 04", "CONSULTÓRIO 05", "CONSULTÓRIO 06", "CONSULTÓRIO 07", "CONSULTÓRIO 08", "CONSULTÓRIO 09",
            "APOIO OPERACIONAL", "EMERGÊNCIA/ REPOUSO DE ENFERMAGEM", "(UI)-01", "(UI)-02", "UDC", "FARMÁCIA SATELITE",
            "FARMÁCIA AMBULATORIAL (EMERGÊNCIA)", "FONOAUDIOLOGIA", "HOSPITAL DIA", "MDA/ RECEPÇÃO", "MDA/ SALA DE CONTROLE DA TOMOGRAFIA",
            "MDA/ SALA DE EXAMES 03", "ENDOSCOPIA", "ULTRASSONOGRAFIA", "MDA/ SALA DE LAUDO", "RAIO-X 01", "RAIO-X 02", "RAIO-X 03",
            "SALA DE VACINAS", "NAVEGAÇÃO", "NIR", "NQSP", "SERVIÇO SOCIAL", "CURATIVO", "SALA VERMELHA", "SAME", "UTI 3: COPA", "UTI 4: COPA"
        ),
        "BLOCO ADMINISTRATIVO.TÉRREO", List.of("AUDITÓRIO", "BIBLIOTECA", "RH", "ENSINO E PESQUISA", "SALA DE AULA 01", "SALA DE AULA 02", "SALA DE AULA 03", "SALA DE AULA 04", "SAÚDE DO TRABALHO", "T.I"),
        "BLOCO ADMINISTRATIVO.1º ANDAR", List.of("COMPRAS", "CONTROLADORIA", "DIRETORIA ADMINISTRATIVA", "DIRETORIA TÉCNICA", "SALA DIRETORIA GERAL", "SUPRIMENTOS")
    );
    private static final List<String> CATEGORIAS = List.of("Áudio & Vídeo", "E-mail", "Equipamentos de TI", "Impressoras", "Rede", "Sistemas", "Telefonia");
    private static final Map<String, List<String>> SUBCATEGORIAS = Map.of(
        "Áudio & Vídeo", List.of("Microfone", "Monitor", "Projetor", "Som", "Tela de Projeção", "Videoconferência"),
        "E-mail", List.of("Nenhuma"),
        "Equipamentos de TI", List.of("Carrinho beira-leito", "Desktop", "Equipamentos Millenium", "Leitor de código de barras", "Monitor", "Notebook", "Periféricos", "Relógio de ponto", "Scanners", "Tablet", "Totem Autoatendimento", "Workstation"),
        "Impressoras", List.of("Impressora de Crachá", "Impressora de Cupom Fiscal", "Impressora de Papel", "Impressora Térmica"),
        "Rede", List.of("Diretório de rede", "Akamai", "Firewall", "Internet", "Ponto de rede", "Rede cabeada", "VPN", "WiFi"),
        "Sistemas", List.of("Sistemas Corporativos e Assistenciais", "Sistemas de Ensino", "Sistemas de Imagem", "Sistemas de TI", "Softwares de Prateleira"),
        "Telefonia", List.of("Celular corporativo", "Ramal/Telefone fixo")
    );
    private static final List<String> URGENCIAS = List.of("Poucos equipamentos", "O meu departamento e não", "Um ou mais", "O meu departamento e diretamente", "Toda a unidade");
    private static final List<String> SINTOMAS = List.of("Indisponibilidade", "Falha/Erro", "Lentidão", "Intermitência");

    public String processUserMessage(String user, String message) {
        int step = userStep.getOrDefault(user, 0);
        Map<String, String> responses = userResponses.computeIfAbsent(user, k -> new LinkedHashMap<>());

        // Preenche telefone no primeiro contato se ainda não tiver
        if (step == 0 && !responses.containsKey("telefone")) {
            responses.put("telefone", user); // telefone recebe o número do usuário
        }

        // Novo passo para perguntar se quer continuar ou não após o chamado finalizado
        if (step == 11) {
            String lower = message.trim().toLowerCase();
            if (lower.equals("sim") || lower.equals("s") || lower.equals("quero") || lower.equals("yes")) {
                // reinicia o fluxo do atendimento
                userStep.put(user, 1);
                userResponses.put(user, new LinkedHashMap<>());
                responses = userResponses.get(user);
                responses.put("telefone", user); // mantém o telefone
                return "Ok! Informe seu horário de trabalho (ex: 07h às 19h):";
            } else {
                // encerra a conversa
                userStep.remove(user);
                userResponses.remove(user);
                return "Chat finalizado. Se precisar, estamos aqui!";
            }
        }

        switch(step) {
            case 0:
                // Como o telefone já foi preenchido, aqui só pede o horário
                userStep.put(user, 1);
                return "Informe seu horário de trabalho (ex: 07h às 19h):";

            case 1:
                responses.put("horario", message.trim());
                userStep.put(user, 2);
                return buildOptionsMessage("Selecione o bloco:", BLOCO_KEYS);

            case 2:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= BLOCO_KEYS.size()) throw new NumberFormatException();
                    String bloco = BLOCO_KEYS.get(idx);
                    responses.put("bloco", bloco);
                    userStep.put(user, 3);
                    return buildOptionsMessage("Selecione o andar do bloco " + bloco + ":", BLOCO_ANDARES.get(bloco));
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para o bloco.";
                }

            case 3:
                try {
                    String bloco = responses.get("bloco");
                    List<String> andares = BLOCO_ANDARES.get(bloco);
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= andares.size()) throw new NumberFormatException();
                    String andar = andares.get(idx);
                    responses.put("andar", andar);
                    userStep.put(user, 4);

                    List<String> areas = BLOCO_ANDAR_AREAS.getOrDefault(bloco + "." + andar, List.of("Área não disponível"));
                    return buildOptionsMessage("Selecione a área:", areas);
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para o andar.";
                }

            case 4:
                try {
                    String bloco = responses.get("bloco");
                    String andar = responses.get("andar");
                    List<String> areas = BLOCO_ANDAR_AREAS.getOrDefault(bloco + "." + andar, List.of("Área não disponível"));
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= areas.size()) throw new NumberFormatException();
                    String area = areas.get(idx);
                    responses.put("area", area);
                    userStep.put(user, 5);

                    return buildOptionsMessage("Selecione a categoria:", CATEGORIAS);
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para a área.";
                }

            case 5:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= CATEGORIAS.size()) throw new NumberFormatException();
                    String categoria = CATEGORIAS.get(idx);
                    responses.put("categoria", categoria);
                    userStep.put(user, 6);

                    List<String> subcats = SUBCATEGORIAS.getOrDefault(categoria, List.of("Nenhuma"));
                    return buildOptionsMessage("Selecione a subcategoria:", subcats);
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para a categoria.";
                }

            case 6:
                try {
                    String categoria = responses.get("categoria");
                    List<String> subcats = SUBCATEGORIAS.getOrDefault(categoria, List.of("Nenhuma"));
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= subcats.size()) throw new NumberFormatException();
                    String subcat = subcats.get(idx);
                    responses.put("subcategoria", subcat);
                    userStep.put(user, 7);

                    return buildOptionsMessage("Selecione o nível de urgência:", URGENCIAS);
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para a subcategoria.";
                }

            case 7:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= URGENCIAS.size()) throw new NumberFormatException();
                    String urgencia = URGENCIAS.get(idx);
                    responses.put("urgencia", urgencia);
                    userStep.put(user, 8);

                    return buildOptionsMessage("Selecione o sintoma:", SINTOMAS);
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para a urgência.";
                }

            case 8:
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if(idx < 0 || idx >= SINTOMAS.size()) throw new NumberFormatException();
                    String sintoma = SINTOMAS.get(idx);
                    responses.put("sintoma", sintoma);
                    userStep.put(user, 9);

                    return "Digite um resumo do problema:";
                } catch(Exception e) {
                    return "Resposta inválida. Por favor, selecione um número válido para o sintoma.";
                }

            case 9:
                responses.put("resumo", message.trim());
                userStep.put(user, 10);
                return "Descreva o problema com mais detalhes:";

            case 10:
                responses.put("descricao", message.trim());

                // Imprime os dados coletados no console
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

                userStep.put(user, 11); // passo para perguntar se quer mais ajuda
                return "Obrigado! Seu chamado foi registrado com sucesso. 📝\nPosso ajudar com mais alguma coisa? (Sim/Não)";

            default:
                userStep.remove(user);
                userResponses.remove(user);
                return "Erro: estado inválido da conversa.";
        }
    }

    private String buildOptionsMessage(String prompt, List<String> options) {
        StringBuilder sb = new StringBuilder(prompt).append("\n");
        for (int i = 0; i < options.size(); i++) {
            sb.append(i + 1).append(" - ").append(options.get(i)).append("\n");
        }
        return sb.toString();
    }
}
