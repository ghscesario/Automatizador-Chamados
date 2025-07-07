package com.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Prompt {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Map<String, Map<String, List<String>>> estrutura = getEstrutura();

        // TELEFONE E HORÁRIO
        System.out.print("Digite seu número de telefone com DDD: ");
        String telefone = sc.nextLine();

        System.out.print("Informe seu horário de trabalho (ex: 07h às 19h): ");
        String horario = sc.nextLine();

        // BLOCO
        List<String> blocos = new ArrayList<>(estrutura.keySet());
        System.out.println("\nSelecione o bloco:");
        for (int i = 0; i < blocos.size(); i++) {
            System.out.println((i + 1) + " - " + blocos.get(i));
        }
        int opBloco = sc.nextInt();
        sc.nextLine();
        String bloco = blocos.get(opBloco - 1);

        // ANDAR
        List<String> andares = new ArrayList<>(estrutura.get(bloco).keySet());
        System.out.println("\nSelecione o andar:");
        for (int i = 0; i < andares.size(); i++) {
            System.out.println((i + 1) + " - " + andares.get(i));
        }
        int opAndar = sc.nextInt();
        sc.nextLine();
        String andar = andares.get(opAndar - 1);

        // ÁREA
        List<String> areas = estrutura.get(bloco).get(andar);
        System.out.println("\nSelecione a área:");
        for (int i = 0; i < areas.size(); i++) {
            System.out.println((i + 1) + " - " + areas.get(i));
        }
        int opArea = sc.nextInt();
        sc.nextLine();
        String area = areas.get(opArea - 1);

        // CATEGORIA
        String[] categorias = {
            "Áudio & Vídeo", "E-mail", "Equipamentos de TI",
            "Impressoras", "Rede", "Sistemas", "Telefonia"
        };
        System.out.println("\nSelecione a categoria:");
        for (int i = 0; i < categorias.length; i++) {
            System.out.println((i + 1) + " - " + categorias[i]);
        }
        int opCategoria = sc.nextInt();
        sc.nextLine();
        String categoria = categorias[opCategoria - 1];

        // SUBCATEGORIA
        Map<String, List<String>> subcategorias = getSubcategorias();
        List<String> listaSub = subcategorias.getOrDefault(categoria, Collections.singletonList("Nenhuma"));
        System.out.println("\nSelecione a subcategoria:");
        for (int i = 0; i < listaSub.size(); i++) {
            System.out.println((i + 1) + " - " + listaSub.get(i));
        }
        int opSub = sc.nextInt();
        sc.nextLine();
        String subCategoria = listaSub.get(opSub - 1);

        // URGÊNCIA
        String[] urgencias = {
            "Poucos equipamentos", "O meu departamento e não",
            "Um ou mais", "O meu departamento e diretamente",
            "Toda a unidade"
        };
        System.out.println("\nSelecione o nível de urgência:");
        for (int i = 0; i < urgencias.length; i++) {
            System.out.println((i + 1) + " - " + urgencias[i]);
        }
        int opUrgencia = sc.nextInt();
        sc.nextLine();
        String urgencia = urgencias[opUrgencia - 1];

        // SINTOMA
        String[] sintomas = {"Indisponibilidade", "Falha/Erro", "Lentidão", "Intermitência"};
        System.out.println("\nSelecione o sintoma:");
        for (int i = 0; i < sintomas.length; i++) {
            System.out.println((i + 1) + " - " + sintomas[i]);
        }
        int opSintoma = sc.nextInt();
        sc.nextLine();
        String sintoma = sintomas[opSintoma - 1];

        // DESCRIÇÕES
        System.out.print("\nDigite um resumo do problema: ");
        String descResumo = sc.nextLine();

        System.out.print("Descreva o problema com mais detalhes: ");
        String descDetalhada = sc.nextLine();

        // OUTPUT FINAL (ou chamada do método)
        System.out.println("\n--- DADOS DO CHAMADO ---");
        System.out.printf("""
            Telefone: %s
            Horário: %s
            Bloco: %s
            Andar: %s
            Área: %s
            Categoria: %s
            Subcategoria: %s
            Urgência: %s
            Sintoma: %s
            Resumo: %s
            Descrição detalhada: %s
        """, telefone, horario, bloco, andar, area, categoria, subCategoria, urgencia, sintoma, descResumo, descDetalhada);
    }

    // Estrutura hierárquica de blocos > andares > áreas
    private static Map<String, Map<String, List<String>>> getEstrutura() {
        Map<String, Map<String, List<String>>> estrutura = new LinkedHashMap<>();

        // HOSPITAL
        Map<String, List<String>> hospital = new LinkedHashMap<>();
        hospital.put("SUBSOLO", List.of("COPA CME", "FARMÁCIA CENTRAL", "NUTRIÇÃO", "AGENCIA TRANSFUSIONAL", "CAF", "NECROTÉRIO"));
        hospital.put("TÉRREO", List.of(
            "(GESSO) SALA DOS MÉDICOS", "AGENDAMENTO DE RETORNO", "AMBULATÓRIO DE ENFERMAGEM", "CONSULTÓRIO 01", "CONSULTÓRIO 02", "CONSULTÓRIO 03",
            "CONSULTÓRIO 04", "CONSULTÓRIO 05", "CONSULTÓRIO 06", "CONSULTÓRIO 07", "CONSULTÓRIO 08", "CONSULTÓRIO 09", "APOIO OPERACIONAL",
            "EMERGÊNCIA/ REPOUSO DE ENFERMAGEM", "(UI)-01", "(UI)-02", "UDC", "FARMÁCIA SATELITE", "FARMÁCIA AMBULATORIAL (EMERGÊNCIA)",
            "FONOAUDIOLOGIA", "HOSPITAL DIA", "MDA/ RECEPÇÃO", "MDA/ SALA DE CONTROLE DA TOMOGRAFIA", "MDA/ SALA DE EXAMES 03",
            "ENDOSCOPIA", "ULTRASSONOGRAFIA", "MDA/ SALA DE LAUDO", "RAIO-X 01", "RAIO-X 02", "RAIO-X 03", "SALA DE VACINAS",
            "NAVEGAÇÃO", "NIR", "NQSP", "SERVIÇO SOCIAL", "CURATIVO", "SALA VERMELHA", "SAME", "UTI 3: COPA", "UTI 4: COPA"
        ));
        hospital.put("1º ANDAR", List.of(
            "CENTRO CIRÚRGICO: COPA",
            "CENTRO CIRÚRGICO: RECEPIÇÃO",
            "RPA 1",
            "CENTRO CIRÚRGICO: SALA 01",
            "CENTRO CIRÚRGICO: SALA 02",
            "CENTRO CIRÚRGICO: SALA 03",
            "CENTRO CIRÚRGICO: SALA 04",
            "CENTRO CIRÚRGICO: SALA 05",
            "CENTRO CIRÚRGICO: SALA 06",
            "CENTRO CIRÚRGICO: SALA 07",
            "CENTRO CIRÚRGICO: SALA 08",
            "CENTRO CIRÚRGICO: SALA 09",
            "CENTRO CIRÚRGICO: SALA 10",
            "FARMÁCIA SATÉLITE 2",
            "SALA DOS MÉDICOS",
            "UTI 1: COPA",
            "UTI 2: COPA"
        ));
        hospital.put("2º ANDAR", List.of("POSTO 1", "POSTO 2", "PRESCRIÇÃO MÉDICA"));
        hospital.put("3º ANDAR", List.of("POSTO 1", "POSTO 2", "PRESCRIÇÃO MÉDICA"));
        hospital.put("4º ANDAR", List.of("POSTO 1", "POSTO 2", "PRESCRIÇÃO MÉDICA"));
        estrutura.put("HOSPITAL", hospital);

        // BLOCO ADMINISTRATIVO
        Map<String, List<String>> adm = new LinkedHashMap<>();
        adm.put("TÉRREO", List.of("AUDITÓRIO", "BIBLIOTECA", "RH", "ENSINO E PESQUISA", "SALA DE AULA 01", "SALA DE AULA 02", "SALA DE AULA 03", "SALA DE AULA 04", "SAÚDE DO TRABALHO", "T.I"));
        adm.put("1º ANDAR", List.of("COMPRAS", "CONTROLADORIA", "DIRETORIA ADMINISTRATIVA", "DIRETORIA TÉCNICA", "SALA DIRETORIA GERAL", "SUPRIMENTOS"));
        estrutura.put("BLOCO ADMINISTRATIVO", adm);

        return estrutura;
    }

    // Subcategorias por categoria
    private static Map<String, List<String>> getSubcategorias() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("Áudio & Vídeo", List.of("Microfone", "Monitor", "Projetor", "Som", "Tela de Projeção", "Videoconferência"));
        map.put("E-mail", List.of("Nenhuma"));
        map.put("Equipamentos de TI", List.of("Carrinho beira-leito", "Desktop", "Equipamentos Millenium", "Leitor de código de barras", "Monitor", "Notebook", "Periféricos", "Relógio de ponto", "Scanners", "Tablet", "Totem Autoatendimento", "Workstation"));
        map.put("Impressoras", List.of("Impressora de Crachá", "Impressora de Cupom Fiscal", "Impressora de Papel", "Impressora Térmica"));
        map.put("Rede", List.of("Diretório de rede", "Akamai", "Firewall", "Internet", "Ponto de rede", "Rede cabeada", "VPN", "WiFi"));
        map.put("Sistemas", List.of("Sistemas Corporativos e Assistenciais", "Sistemas de Ensino", "Sistemas de Imagem", "Sistemas de TI", "Softwares de Prateleira"));
        map.put("Telefonia", List.of("Celular corporativo", "Ramal/Telefone fixo"));
        return map;
    }

}
