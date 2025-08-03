package com.project.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    @Autowired
    private final EvolutionApiService evolutionApiService;

    private final ChamadoService chamadoService;

    private String retornoChamado="";

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
        "HOSPITAL.1º ANDAR", List.of("CENTRO CIRÚRGICO: COPA", "CENTRO CIRÚRGICO: RECEPIÇÃO", "RPA 1", "CENTRO CIRÚRGICO: SALA 01", "CENTRO CIRÚRGICO: SALA 02", "CENTRO CIRÚRGICO: SALA 03"
        , "CENTRO CIRÚRGICO: SALA 04", "CENTRO CIRÚRGICO: SALA 05", "CENTRO CIRÚRGICO: SALA 06", "CENTRO CIRÚRGICO: SALA 07", "CENTRO CIRÚRGICO: SALA 08", "CENTRO CIRÚRGICO: SALA 09", "CENTRO CIRÚRGICO: SALA 10", "FARMÁCIA SATÉLITE 2", "SALA DOS MÉDICOS", "UTI 1: COPA", "UTI 2: COPA"),
        "HOSPITAL.2º ANDAR", List.of("POSTO 1", "POSTO 2", "PRESCRIÇÃO MÉDICA", "SALA DA CHEFIA"),
        "HOSPITAL.3º ANDAR", List.of("POSTO 1", "POSTO 2", "PRESCRIÇÃO MÉDICA"),
        "HOSPITAL.4º ANDAR", List.of("POSTO 1", "POSTO 2", "PRESCRIÇÃO MÉDICA", "CARCERAGEM"),
        "HOSPITAL.TÉRREO", List.of("(GESSO) SALA DOS MÉDICOS", "AGENDAMENTO DE RETORNO", "AMBULATÓRIO DE ENFERMAGEM", "CONSULTÓRIO 01", "CONSULTÓRIO 02",
         "CONSULTÓRIO 03", "CONSULTÓRIO 04", "CONSULTÓRIO 05", "CONSULTÓRIO 06", "CONSULTÓRIO 07", "CONSULTÓRIO 08", "CONSULTÓRIO 09", "APOIO OPERACIONAL",
          "EMERGÊNCIA/ REPOUSO DE ENFERMAGEM", "(UI)-01", "(UI)-02", "UDC", "FARMÁCIA SATELITE", "FARMÁCIA AMBULATORIAL (EMERGÊNCIA)", "FONOAUDIOLOGIA", "HOSPITAL DIA",
           "MDA/ RECEPÇÃO", "MDA/ SALA DE CONTROLE DA TOMOGRAFIA", "MDA/ SALA DE EXAMES 03", "ENDOSCOPIA", "ULTRASSONOGRAFIA", "MDA/ SALA DE LAUDO", "RAIO-X 01",
            "RAIO-X 02", "RAIO-X 03", "SALA DE VACINAS", "NAVEGAÇÃO", "NIR", "NQSP", "SERVIÇO SOCIAL", "CURATIVO", "SALA VERMELHA", "SAME", "UTI 3: COPA", "UTI 4: COPA"),
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
    private static final Map<String, List<String>> SISTEMAS_POR_SUBCATEGORIA = Map.of(
    "Sistemas Corporativos", List.of("MV SOUL - HIS Gestão Hospitalar", "Rison Web / Deep Unity", "MATRIX"),
    "Sistemas de Imagem", List.of("PACS")
    );
    private static final List<String> URGENCIAS = List.of("Poucos equipamentos", "O meu departamento e não afeta diretamente o atendimento ao cliente"
    ,"Um ou mais computador(es) ligado(s) a equipamento(s) médico(s)", "O meu departamento e diretamente o atendimento ao cliente", "Toda unidade");
    private static final List<String> SINTOMAS = List.of("Indisponibilidade", "Falha/Erro", "Lentidão", "Intermitência");

    ConversationService(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
        this.evolutionApiService = null;
    }

    @SuppressWarnings("UseSpecificCatch")
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
            return "📌 Informações da T.I:\n- Atendimento 24h \n- Suporte emergencial: ramal 4423 ou 4425 \n- Email: tihugo@einstein.br \n\nDigite 'menu' para voltar.";
        }

        if (MODO_ATENDENTE.equals(modo)) {
            return " ";
        }

        // Se chegou aqui, está no modo chamado
        int step = userStep.getOrDefault(user, 0);
        Map<String, String> responses = userResponses.computeIfAbsent(user, k -> new LinkedHashMap<>());

        if (step == 0 && !responses.containsKey("telefone")) {
            responses.put("telefone", user);
        }

        switch (step) {
            case 0 -> {
                if (!responses.containsKey("telefone")) {
                    String telefoneLimpo = limparTelefone(user);
                    if (!telefoneValido(telefoneLimpo)) {
                        return "Número inválido. Por favor, envie seu número com DDD, ex: 62911111111";
                    }
                    responses.put("telefone", telefoneLimpo);
                }
                userStep.put(user, 1);
                return "Informe seu horário de trabalho (ex: 07h às 19h):";
            }


            case 1 -> {
                responses.put("horario", message.trim());
                userStep.put(user, 2);
                return buildOptionsMessage("Selecione o bloco:", BLOCO_KEYS);
            }

            case 2 -> {
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
            }

            case 3 -> {
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
            }

            case 4 -> {
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
            }

            case 5 -> {
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
            }

            case 6 -> {
                try {
                    String categoria = responses.get("categoria");
                    List<String> subcats = SUBCATEGORIAS.getOrDefault(categoria, List.of("Nenhuma"));
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String subcat = subcats.get(idx);
                    responses.put("subcategoria", subcat);

                    // Se categoria for "Sistemas" e subcat tiver sistemas, pede seleção do sistema
                    if ("Sistemas".equals(categoria) && SISTEMAS_POR_SUBCATEGORIA.containsKey(subcat)) {
                        userStep.put(user, 61); // próximo passo: escolher sistema
                        return buildOptionsMessage("Selecione o sistema:", SISTEMAS_POR_SUBCATEGORIA.get(subcat));
                    }

                    // Caso contrário, segue para urgência normalmente
                    userStep.put(user, 7);
                    return buildOptionsMessage("Selecione o nível de urgência:", URGENCIAS);

                } catch (Exception e) {
                    return "❌ Subcategoria inválida. Tente novamente.";
                }
            }

            case 7 -> {
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String urgencia = URGENCIAS.get(idx);
                    responses.put("urgencia", urgencia);
                    userStep.put(user, 8);
                    return buildOptionsMessage("Selecione o sintoma:", SINTOMAS);
                } catch (Exception e) {
                    return "❌ Urgência inválida. Tente novamente.";
                }
            }

            case 8 -> {
                try {
                    int idx = Integer.parseInt(message.trim()) - 1;
                    String sintoma = SINTOMAS.get(idx);
                    responses.put("sintoma", sintoma);
                    userStep.put(user, 9);
                    return "Digite o título do chamado:";
                } catch (Exception e) {
                    return "❌ Sintoma inválido. Tente novamente.";
                }
            }

            case 9 -> {
                responses.put("resumo", message.trim());
                userStep.put(user, 10);
                return "Descreva o problema com mais detalhes:";
            }

            case 10 -> {
                responses.put("descricao", message.trim());

                // Envia mensagem de "Aguardando"
                userStep.put(user, 11); // Definimos o próximo passo para controle, mesmo que não vá ser usado

                // Executa abertura de chamado em background
                new Thread(() -> {
                    try {
                        String telefoneOriginal = responses.get("telefone");
                        String telefoneLimpo = limparTelefone(telefoneOriginal);  // Limpa novamente, por segurança

                        String horario = responses.get("horario");
                        String bloco = responses.get("bloco");
                        String andar = responses.get("andar");
                        String area = responses.get("area");
                        String categoria = responses.get("categoria");
                        String subcategoria = responses.get("subcategoria");
                        String sistema = responses.get("sistema");
                        String urgencia = responses.get("urgencia");
                        String sintoma = responses.get("sintoma");
                        String resumo = responses.get("resumo");
                        String descricao = responses.get("descricao");

                        retornoChamado = chamadoService.criarChamadoInterno(telefoneLimpo, horario, bloco, andar, area, categoria, subcategoria, sistema, urgencia, sintoma, resumo, descricao);
                    } catch (Exception e) {
                        System.err.println("Erro ao executar a criação do chamado com Playwright: " + e.getMessage());
                    }

                    // Atualiza passo para menu final
                    userStep.put(user, 999);
                    userModes.put(user, MODO_CHAMADO); // Garante permanência no modo até terminar

                    // Envia a mensagem final para o usuário
                    String numero = responses.get("telefone");
                    String mensagemFinal = "✅ Chamado de número: "+retornoChamado+", aberto com sucesso!\n\n📋 Deseja fazer mais alguma coisa?\n1 - Abrir novo chamado\n2 - Falar com atendente\n3 - Informações da T.I\n\nOu digite 'menu' para começar novamente.";

                    // Aqui é necessário um meio de envio manual — você pode injetar o `EvolutionApiService` para isso:
                    try {
                        evolutionApiService.sendTextMessage("teste", numero, mensagemFinal);
                    } catch (Exception e) {
                        System.err.println("Erro ao enviar mensagem final: " + e.getMessage());
                    }

                }).start();

                return "🛠️ Abrindo chamado, aguarde...";
            }

            case 61 -> {
                try {
                    String subcat = responses.get("subcategoria");
                    List<String> sistemas = SISTEMAS_POR_SUBCATEGORIA.getOrDefault(subcat, List.of());
                    int idx = Integer.parseInt(message.trim()) - 1;
                    if (idx < 0 || idx >= sistemas.size()) throw new NumberFormatException();
                    String sistema = sistemas.get(idx);
                    responses.put("sistema", sistema);
                    userStep.put(user, 7);  // Após sistema, continua para urgência
                    return buildOptionsMessage("Selecione o nível de urgência:", URGENCIAS);
                } catch (Exception e) {
                    return "❌ Sistema inválido. Tente novamente.";
                }
            }
            
            case 999 -> {
                return processarMenuInicial(user, message.trim());
            }

            default -> {
                userStep.remove(user);
                userResponses.remove(user);
                userModes.remove(user);
                return "❗ Conversa reiniciada. Digite 'menu' para começar.";
            }
        }
    } 

    private String processarMenuInicial(String user, String input) {
        switch (input) {
            case "1" -> {
                userModes.put(user, MODO_CHAMADO); 
                userStep.put(user, 0);
                userResponses.put(user, new LinkedHashMap<>());
                return processUserMessage(user, "");
            }
            case "2" -> {
                userModes.put(user, MODO_ATENDENTE);
                return "Para falar diretamente com um de nossos analistas de suporte acesse o link à seguir e seja redirecionado para a conversa: https://api.whatsapp.com/send/?phone=556292928949&text&type=phone_number&app_absent=0 \n *Para voltar ao menu inicial digite 'menu'*";
            }
            case "3" -> {
                userModes.put(user, MODO_INFO);
                return "📌 Informações da T.I:\n- Atendimento 24h\n- Suporte emergencial: ramal 4423 ou 4425\n- Email: tihugo@einstein.br \n*Para voltar ao menu inicial digite 'menu'*";
            }
            default -> {
                return getMenuInicial();
            }
        }
    }

    private String getMenuInicial() {
        return "👋 Bem vindo(a) ao Suporte HUGO! Como podemos te ajudar?\n\n1 - Abrir chamado\n2 - Falar com analista de suporte\n3 - Informações sobre a T.I\n \n *Em caso de algum erro no preenchimento digitar 'menu' para voltar ao menu inicial*";
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
        raw = raw.replaceAll("@.*", "");

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
