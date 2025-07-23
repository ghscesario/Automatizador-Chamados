package com.project.Service;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.project.Model.Chamado;
import com.project.Repository.ChamadoRepository;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    // RODAR UMA VEZ PARA SALVAR A SESSÃO
    public void salvarSessao() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            System.out.println("Faça o login manualmente (com MFA), depois pressione ENTER...");
            System.in.read();

            context.storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("session.json")));

            System.out.println("Sessão salva com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao criar sessão!");
        }
    }

    // EXECUTAR SEM LOGIN
    // CHAMADO SUPORTE
    public void criarChamado(String telefone, String horario, String bloco, String andar, String area, String categoria, String subCategoria, String urgencia, String sintoma, String descResumo, String descDetalhada) {
        boolean sucesso = false;

        String chamadoGerado = null;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            // TELEFONE
            page.waitForSelector("input[name='telefone_celular']");
            page.locator("input[name='telefone_celular']").fill(telefone);

            // HORÁRIO TRABALHO
            page.waitForSelector("input[name='horario_escala_trabalho']");
            page.locator("input[name='horario_escala_trabalho']").fill(horario);

            selecionarUnidade(page, "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");
            selecionarBloco(page, bloco);
            selecionarAndar(page, andar);
            selecionarArea(page, area);
            selecionarCategoria(page, categoria);
            selecionarSubCategoria(page, subCategoria);
            selecionarCentroCusto(page, "CMHG");
            selecionarUrgencia(page, urgencia);
            selecionarSintoma(page, sintoma);

            page.locator("textarea[name='short_description']").fill(descResumo);
            page.locator("textarea[name='description']").fill(descDetalhada);

            Locator botaoEnviar = page.locator("#submit-btn");
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            // 2) Aguarda o ServiceNow gerar e exibir o número do chamado
            Locator numChamado = page.locator("div#data\\.number\\.name");   // CSS: o ponto precisa ser escapado
            numChamado.waitFor(new Locator.WaitForOptions().setTimeout(10_000)); // ajuste se precisar

            // 3) Lê o texto e grava
            chamadoGerado = numChamado.innerText().trim();
            System.out.println("Número do chamado gerado: " + chamadoGerado);

            sucesso = true;
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
        }

        if (sucesso) {
            Chamado chamado = new Chamado();
            chamado.setTelefone(telefone);
            chamado.setHorario(horario);
            chamado.setBloco(bloco);
            chamado.setAndar(andar);
            chamado.setArea(area);
            chamado.setCategoria(categoria);
            chamado.setSubcategoria(subCategoria);
            chamado.setUrgencia(urgencia);
            chamado.setSintoma(sintoma);
            chamado.setResumo(descResumo);
            chamado.setDescricao(descDetalhada);
            chamado.setNumeroChamado(chamadoGerado);
            chamadoRepository.save(chamado);
            System.out.println("Chamado registrado no banco de dados com sucesso.");
        }
    }
    
        //CHAMADO SISTEMAS
        public void criarChamadoSistemas(String telefone, String horario, String bloco, String andar, String area, String sistema, String urgencia, String sintoma, String descResumo, String descDetalhada) {
        boolean sucesso = false;

        String chamadoGerado = null;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            // TELEFONE
            page.waitForSelector("input[name='telefone_celular']");
            page.locator("input[name='telefone_celular']").fill(telefone);

            // HORÁRIO TRABALHO
            page.waitForSelector("input[name='horario_escala_trabalho']");
            page.locator("input[name='horario_escala_trabalho']").fill(horario);

            selecionarUnidade(page, "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");
            selecionarBloco(page, bloco);
            selecionarAndar(page, andar);
            selecionarArea(page, area);
            selecionarCategoria(page, "Sistemas, Softwares e Apps");
            selecionarSubCategoria(page, "Sistemas Corporativos e Assistenciais");
            selecionarSistema(page, sistema);
            selecionarCentroCusto(page, "CMHG");
            selecionarUrgencia(page, urgencia);
            selecionarSintoma(page, sintoma);

            page.locator("textarea[name='short_description']").fill(descResumo);
            page.locator("textarea[name='description']").fill(descDetalhada);

            Locator botaoEnviar = page.locator("#submit-btn");
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            // 2) Aguarda o ServiceNow gerar e exibir o número do chamado
            Locator numChamado = page.locator("div#data\\.number\\.name");   // CSS: o ponto precisa ser escapado
            numChamado.waitFor(new Locator.WaitForOptions().setTimeout(10_000)); // ajuste se precisar

            // 3) Lê o texto e grava
            chamadoGerado = numChamado.innerText().trim();
            System.out.println("Número do chamado gerado: " + chamadoGerado);

            sucesso = true;
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
        }

        if (sucesso) {
            Chamado chamado = new Chamado();
            chamado.setTelefone(telefone);
            chamado.setHorario(horario);
            chamado.setBloco(bloco);
            chamado.setAndar(andar);
            chamado.setArea(area);
            chamado.setCategoria("Sistemas, Softwares e Apps");
            chamado.setSubcategoria("Sistemas Corporativos e Assistenciais");
            chamado.setUrgencia(urgencia);
            chamado.setSintoma(sintoma);
            chamado.setResumo(descResumo);
            chamado.setDescricao(descDetalhada);
            chamado.setNumeroChamado(chamadoGerado);
            chamadoRepository.save(chamado);
            System.out.println("Chamado registrado no banco de dados com sucesso.");
        }
    }

    //CHAMADOS GERAL
    public String criarChamadoInterno(String telefone, String horario, String bloco, String andar, String area,
                                String categoria, String subCategoria, String sistema,
                                String urgencia, String sintoma,
                                String descResumo, String descDetalhada) {
        boolean sucesso = false;
        String chamadoGerado = null;
        String numChamadoFinal = "";

        try (Playwright playwright = Playwright.create()) { 
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            page.waitForSelector("input[name='telefone_celular']");
            page.locator("input[name='telefone_celular']").fill(telefone);

            page.waitForSelector("input[name='horario_escala_trabalho']");
            page.locator("input[name='horario_escala_trabalho']").fill(horario);

            selecionarUnidade(page, "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");
            selecionarBloco(page, bloco);
            selecionarAndar(page, andar);
            selecionarArea(page, area);
            selecionarCategoria(page, categoria);
            selecionarSubCategoria(page, subCategoria);

            if (sistema != null && !sistema.isBlank()) {
                selecionarSistema(page, sistema);
            }

            selecionarCentroCusto(page, "CMHG");
            selecionarUrgencia(page, urgencia);
            selecionarSintoma(page, sintoma);

            page.locator("textarea[name='short_description']").fill(descResumo);
            page.locator("textarea[name='description']").fill(descDetalhada);

            Locator botaoEnviar = page.locator("#submit-btn");
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoEnviar.click();

            Locator numChamado = page.locator("div#data\\.number\\.name");
            numChamado.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
            chamadoGerado = numChamado.innerText().trim();

            System.out.println("Número do chamado gerado: " + chamadoGerado);
            sucesso = true;
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado: " + e.getMessage());
        }

        if (sucesso) {
            Chamado chamado = new Chamado();
            chamado.setTelefone(telefone);
            chamado.setHorario(horario);
            chamado.setBloco(bloco);
            chamado.setAndar(andar);
            chamado.setArea(area);
            chamado.setCategoria(categoria);
            chamado.setSubcategoria(subCategoria);
            chamado.setUrgencia(urgencia);
            chamado.setSintoma(sintoma);
            chamado.setResumo(descResumo);
            chamado.setDescricao(descDetalhada);
            chamado.setNumeroChamado(chamadoGerado);
            chamadoRepository.save(chamado);
            numChamadoFinal = chamadoGerado;
            System.out.println("Chamado registrado no banco de dados com sucesso.");
        }
        return numChamadoFinal;
    }


    // EXECUTAR SEM LOGIN
    public String criarChamadoImpressoraColorida(String ip, String cores, String name) {
        boolean sucesso = false;
        String chamadoGerado = null;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            page.locator("input[name='telefone_celular']").fill("1199999999");
            page.locator("input[name='horario_escala_trabalho']").fill("(BOT)");

            selecionarUnidade(page,       "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");
            selecionarBloco(page,         "BLOCO ADMINISTRATIVO");
            selecionarAndar(page,         "TÉRREO");
            selecionarArea(page,          "T.I");
            selecionarCategoria(page,     "Impressoras");
            selecionarSubCategoria(page,  "Impressora de papel");
            selecionarCentroCusto(page,   "TIHG");
            selecionarUrgencia(page,      "O meu departamento e não");
            selecionarSintoma(page,       "Indisponibilidade");

            String resumo   = "Troca de toner, impressora: " + ip + " (BOT)"; 
            String detalhado = "Sistema automatizado identificou na varredura que a impressora: " + ip +
                            ", necessita da substituição do(s) toner(s): "+cores;

            page.locator("textarea[name='short_description']").fill(resumo);
            page.locator("textarea[name='description']").fill(detalhado);

            Locator botaoEnviar = page.locator("#submit-btn");
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            Locator numChamado = page.locator("div#data\\.number\\.name");
            numChamado.waitFor(new Locator.WaitForOptions().setTimeout(10_000));

            chamadoGerado = numChamado.innerText().trim();
            System.out.println("Número do chamado gerado: " + chamadoGerado);

            sucesso = true;
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado (Playwright): " + e.getMessage());
        }

        if (sucesso) {
            Chamado c = new Chamado();
            c.setTelefone("6282595863");
            c.setHorario("(BOT)");
            c.setBloco("BLOCO ADMINISTRATIVO");
            c.setAndar("TÉRREO");
            c.setArea("T.I");
            c.setCategoria("Impressoras");
            c.setSubcategoria("Impressora de papel");
            c.setUrgencia("O meu departamento e não");
            c.setSintoma("Indisponibilidade");
            c.setResumo("Troca de toner, impressora: " + name + " de ip:" + ip + " (BOT)");
            c.setDescricao("Sistema automatizado identificou na varredura que a impressora: " + ip +
                                ", necessita da substituição do toner!");
            c.setNumeroChamado(chamadoGerado);
            c.setMensagemEnviada(true);

            try {
                chamadoRepository.save(c);
                System.out.println("Chamado salvo no banco!");
            } catch (Exception e) {
                System.err.println("Falha ao salvar no banco: " + e.getMessage());
            }
        }

        // retorna o número do chamado gerado
        return chamadoGerado;
    }


    // EXECUTAR SEM LOGIN
    public String criarChamadoImpressora(String ip, String name) {
        @SuppressWarnings("unused")
        boolean sucesso = false;

        String chamadoGerado = null;

        String chamadoFinal="";

        // Playwright: cria o chamado no Service‑Now
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            page.locator("input[name='telefone_celular']").fill("6282595863");
            page.locator("input[name='horario_escala_trabalho']").fill("(BOT)");

            selecionarUnidade(page,       "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");
            selecionarBloco(page,         "BLOCO ADMINISTRATIVO");
            selecionarAndar(page,         "TÉRREO");
            selecionarArea(page,          "T.I");
            selecionarCategoria(page,     "Impressoras");
            selecionarSubCategoria(page,  "Impressora de papel");
            selecionarCentroCusto(page,   "TIHG");
            selecionarUrgencia(page,      "O meu departamento e não");
            selecionarSintoma(page,       "Indisponibilidade");

            String resumo   = "Troca de toner, impressora: "+ name +" de ip: "+ ip + " (BOT)"; 
            String detalhado = "Sistema automatizado identificou na varredura que a impressora: " + ip +
                            ", necessita da substituição do toner!";

            page.locator("textarea[name='short_description']").fill(resumo);
            page.locator("textarea[name='description']").fill(detalhado);

            Locator botaoEnviar = page.locator("#submit-btn");
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoEnviar.click();
            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            // 2) Aguarda o ServiceNow gerar e exibir o número do chamado
            Locator numChamado = page.locator("div#data\\.number\\.name");   // CSS: o ponto precisa ser escapado
            numChamado.waitFor(new Locator.WaitForOptions().setTimeout(10_000)); // ajuste se precisar

            // 3) Lê o texto e grava
            chamadoGerado = numChamado.innerText().trim();
            System.out.println("Número do chamado gerado: " + chamadoGerado);

            sucesso = true;
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado (Playwright): " + e.getMessage());
        }

        // Persistir no banco só se o passo 1 deu certo
         if (sucesso) {
             Chamado c = new Chamado();
             c.setTelefone("1199999999");
             c.setHorario("(BOT)");
             c.setBloco("BLOCO ADMINISTRATIVO");
             c.setAndar("TÉRREO");
             c.setArea("T.I");
             c.setCategoria("Impressoras");
             c.setSubcategoria("Impressora de papel");
             c.setUrgencia("O meu departamento e não");
             c.setSintoma("Indisponibilidade");
             c.setResumo("Troca de toner, impressora: " + ip + " (BOT)");
             c.setDescricao("Sistema automatizado identificou na varredura que a impressora: " + ip +
                                     ", necessita da substituição do toner!");
            c.setNumeroChamado(chamadoGerado);
            c.setMensagemEnviada(true);
            chamadoFinal=chamadoGerado;
             try {
                 chamadoRepository.save(c);
                 System.out.println("Chamado salvo no banco!");
             } catch (Exception e) {
                 System.err.println("Falha ao salvar no banco: " + e.getMessage());
             }
        }
        return chamadoFinal;
    }

    public void criarChamadoTeste() {

        // Executa o Playwright
        boolean sucesso = false;

        String chamadoGerado = null;

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate("https://hiaeprod.service-now.com/esc?id=sc_cat_item&sys_id=d4a89f4c878b16104be0ea480cbb3543");

            /* ‑‑‑‑‑ preenchimento do formulário ‑‑‑‑‑ */
            page.waitForSelector("input[name='telefone_celular']");
            page.locator("input[name='telefone_celular']").fill("1199999999");

            page.waitForSelector("input[name='horario_escala_trabalho']");
            page.locator("input[name='horario_escala_trabalho']").fill("24h");

            selecionarUnidade(page,        "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");
            selecionarBloco(page,          "HOSPITAL");
            selecionarAndar(page,          "1º ANDAR");
            selecionarArea(page,           "CENTRO CIRUGICO CORREDORES");
            selecionarCategoria(page,      "Equipamentos de TI");
            selecionarSubCategoria(page,   "Desktop");
            selecionarCentroCusto(page,    "CMHG");
            selecionarUrgencia(page,       "Poucos equipamentos");
            selecionarSintoma(page,        "Falha");

            page.locator("textarea[name='short_description']").fill("teste 5");
            page.locator("textarea[name='description']").fill("teste");

            Locator botaoEnviar = page.locator("#submit-btn");
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            // 2) Aguarda o ServiceNow gerar e exibir o número do chamado
            Locator numChamado = page.locator("div#data\\.number\\.name");   // CSS: o ponto precisa ser escapado
            numChamado.waitFor(new Locator.WaitForOptions().setTimeout(10_000)); // ajuste se precisar

            // 3) Lê o texto e grava
            chamadoGerado = numChamado.innerText().trim();
            System.out.println("Número do chamado gerado: " + chamadoGerado);

            sucesso = true;                           // ← chegou até aqui sem exceção
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado (Playwright): " + e.getMessage());
        }

        // Persistir no banco apenas se deu certo
         if (sucesso) {
             Chamado c = new Chamado();
             c.setTelefone("1199999999");
             c.setHorario("24h");
             c.setBloco("HOSPITAL");
             c.setAndar("1º ANDAR");
             c.setArea("CENTRO CIRUGICO CORREDORES");
             c.setCategoria("Equipamentos de TI");
             c.setSubcategoria("Desktop");
             c.setUrgencia("Poucos equipamentos");
             c.setSintoma("Falha");
             c.setResumo("teste 4 08/07/2025");
             c.setDescricao("teste");
             c.setNumeroChamado(chamadoGerado);

             try {
                 chamadoRepository.save(c);
                 System.out.println("Chamado salvo no banco!");
             } catch (Exception e) {
                 System.err.println("Falha ao salvar no banco: " + e.getMessage());
             }
         }
    }

    private void selecionarUnidade(Page page, String valorDesejado) {
        selecionarOpcaoSelect2(page, "s2id_sp_formfield_unidade", valorDesejado);
        System.out.println("Unidade selecionada: " + valorDesejado);
    }

    private void selecionarBloco(Page page, String valorDesejado) {
        try {
            // Clica na seta para abrir o dropdown do Bloco
            Locator setaDropdown = page.locator("#s2id_sp_formfield_bloco .select2-arrow");
            setaDropdown.click();

            // Espera o container da lista aparecer
            page.waitForSelector("ul#select2-results-11", new Page.WaitForSelectorOptions().setTimeout(5000));

            // Localiza o item <li> da lista que contém o texto da opção desejada e clica
            Locator opcao = page.locator("ul#select2-results-11 > li", new Page.LocatorOptions().setHasText(valorDesejado));
            opcao.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            opcao.click();

            System.out.println("Bloco selecionado: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar bloco: " + valorDesejado);
        }
    }

    private void selecionarAndar(Page page, String valorDesejado) {
        try {
            // Clica na seta para abrir o dropdown do Bloco
            Locator setaDropdown = page.locator("#s2id_sp_formfield_andar .select2-arrow");
            setaDropdown.click();

            // Espera o container da lista aparecer
            page.waitForSelector("ul#select2-results-9", new Page.WaitForSelectorOptions().setTimeout(5000));

            // Localiza o item <li> da lista que contém o texto da opção desejada e clica
            Locator opcao = page.locator("ul#select2-results-9 > li", new Page.LocatorOptions().setHasText(valorDesejado));
            opcao.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            opcao.click();

            System.out.println("Bloco selecionado: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar bloco: " + valorDesejado);
        }
    }

    /////////////////////////////////////////////////////////////////
    ////////////////////// MÉTODO ANTIGO ///////////////////////////
    ///////////////////////////////////////////////////////////////
    
    // private void selecionarArea(Page page, String valorDesejado) {
    //     try {
    //         // Clica na seta para abrir o dropdown do Bloco
    //         Locator setaDropdown = page.locator("#s2id_sp_formfield_unidade_departamento .select2-arrow");
    //         setaDropdown.click();

    //         // Espera o container da lista aparecer
    //         page.waitForSelector("ul#select2-results-12", new Page.WaitForSelectorOptions().setTimeout(5000));

    //         // Localiza o item <li> da lista que contém o texto da opção desejada e clica
    //         Locator opcao = page.locator("ul#select2-results-12 > li", new Page.LocatorOptions().setHasText(valorDesejado));
    //         opcao.waitFor(new Locator.WaitForOptions().setTimeout(5000));
    //         opcao.click();

    //         System.out.println("Bloco selecionado: " + valorDesejado);
    //     } catch (Exception e) {
    //         System.err.println("Erro ao selecionar bloco: " + valorDesejado);
    //     }
    // }

    private void selecionarArea(Page page, String valorDesejado) {
        try {
            // Aguarda brevemente antes de iniciar
            page.waitForTimeout(1000);

            // Clica no campo select2 para abrir o dropdown
            Locator dropdown = page.locator("#s2id_sp_formfield_unidade_departamento");
            dropdown.click();

            // Aguarda o campo de input aparecer
            Locator campoBusca = page.locator("#s2id_autogen12_search");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Digita o valor desejado com um pequeno delay entre teclas
            campoBusca.fill(""); // limpa o campo
            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            // Aguarda a lista atualizar
            page.waitForTimeout(1000); // pode ajustar esse valor caso a lista demore mais

            // Pressiona Enter para selecionar a primeira opção correspondente
            page.keyboard().press("Enter");

            System.out.println("Area selecionada: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar Area: " + valorDesejado);
        }
    }


    private void selecionarCategoria(Page page, String valorDesejado) {
        try {
            // Clica na seta para abrir o dropdown do Bloco
            Locator setaDropdown = page.locator("#s2id_sp_formfield_category .select2-arrow");
            setaDropdown.click();

            // Espera o container da lista aparecer
            page.waitForSelector("ul#select2-results-4", new Page.WaitForSelectorOptions().setTimeout(5000));

            // Localiza o item <li> da lista que contém o texto da opção desejada e clica
            Locator opcao = page.locator("ul#select2-results-4 > li", new Page.LocatorOptions().setHasText(valorDesejado));
            opcao.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            opcao.click();

            System.out.println("Bloco selecionado: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar bloco: " + valorDesejado);
        }
    }

    private void selecionarSubCategoria(Page page, String valorDesejado) {
        try {
            // Aguarda brevemente antes de iniciar
            page.waitForTimeout(1000);

            // Clica no campo select2 para abrir o dropdown
            Locator dropdown = page.locator("#s2id_sp_formfield_subcategory");
            dropdown.click();

            // Aguarda o campo de input aparecer
            Locator campoBusca = page.locator("#s2id_autogen7_search");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Digita o valor desejado com um pequeno delay entre teclas
            campoBusca.fill(""); // limpa o campo
            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            // Aguarda a lista atualizar
            page.waitForTimeout(1000); // pode ajustar esse valor caso a lista demore mais

            // Pressiona Enter para selecionar a primeira opção correspondente
            page.keyboard().press("Enter");

            System.out.println("Subcategoria selecionada: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar subcategoria: " + valorDesejado);
        }
    }


    private void selecionarCentroCusto(Page page, String valorDesejado) {
        try {
            // Aguarda brevemente antes de iniciar
            page.waitForTimeout(1000);

            // Clica no campo select2 para abrir o dropdown
            Locator dropdown = page.locator("#s2id_sp_formfield_centro_custo");
            dropdown.click();

            // Aguarda o campo de input aparecer
            Locator campoBusca = page.locator("#s2id_autogen10_search");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Digita o valor desejado com um pequeno delay entre teclas
            campoBusca.fill(""); // limpa o campo
            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            // Aguarda a lista atualizar
            page.waitForTimeout(1000); // pode ajustar esse valor caso a lista demore mais

            // Pressiona Enter para selecionar a primeira opção correspondente
            page.keyboard().press("Enter");

            System.out.println("Subcategoria selecionada: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar subcategoria: " + valorDesejado);
        }
    }

    private void selecionarUrgencia(Page page, String valorDesejado) {
        try {
            // Aguarda brevemente antes de iniciar
            page.waitForTimeout(1000);

            // Clica no campo select2 para abrir o dropdown
            Locator dropdown = page.locator("#s2id_sp_formfield_urgency");
            dropdown.click();

            // Aguarda o campo de input aparecer
            Locator campoBusca = page.locator("#s2id_autogen5_search");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Digita o valor desejado com um pequeno delay entre teclas
            campoBusca.fill(""); // limpa o campo
            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            // Aguarda a lista atualizar
            page.waitForTimeout(1000); // pode ajustar esse valor caso a lista demore mais

            // Pressiona Enter para selecionar a primeira opção correspondente
            page.keyboard().press("Enter");

            System.out.println("Subcategoria selecionada: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar subcategoria: " + valorDesejado);
        }
    }

    private void selecionarSintoma(Page page, String valorDesejado) {
        try {
            // Aguarda brevemente antes de iniciar
            page.waitForTimeout(1000);

            // Clica no campo select2 para abrir o dropdown
            Locator dropdown = page.locator("#s2id_sp_formfield_u_symptom");
            dropdown.click();

            // Aguarda o campo de input aparecer
            Locator campoBusca = page.locator("#s2id_autogen6_search");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Digita o valor desejado com um pequeno delay entre teclas
            campoBusca.fill(""); // limpa o campo
            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            // Aguarda a lista atualizar
            page.waitForTimeout(1000); // pode ajustar esse valor caso a lista demore mais

            // Pressiona Enter para selecionar a primeira opção correspondente
            page.keyboard().press("Enter");

            System.out.println("Subcategoria selecionada: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar subcategoria: " + valorDesejado);
        }
    }

    private void selecionarSistema(Page page, String valorDesejado) {
        try {
            // Aguarda brevemente antes de iniciar
            page.waitForTimeout(1000);

            // Clica no campo select2 para abrir o dropdown
            Locator dropdown = page.locator("#s2id_sp_formfield_u_system");
            dropdown.click();

            // Aguarda o campo de input aparecer
            Locator campoBusca = page.locator("#s2id_autogen15_search");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Digita o valor desejado com um pequeno delay entre teclas
            campoBusca.fill(""); // limpa o campo
            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            // Aguarda a lista atualizar
            page.waitForTimeout(1000); // pode ajustar esse valor caso a lista demore mais

            // Pressiona Enter para selecionar a primeira opção correspondente
            page.keyboard().press("Enter");

            System.out.println("Sistema selecionado: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar sistema: " + valorDesejado);
        }
    }


    // Reutilizável para campos Select2 com digitação
    private void selecionarOpcaoSelect2(Page page, String select2Id, String valorDesejado) {
        try {
            Locator setaDropdown = page.locator("#" + select2Id + " .select2-arrow");
            setaDropdown.click();

            page.waitForTimeout(500);
            Locator campoBusca = page.locator("div.select2-drop-active input.select2-input");
            campoBusca.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            campoBusca.type(valorDesejado, new Locator.TypeOptions().setDelay(100));

            Locator resultado = page.locator("li.select2-result:has-text('" + valorDesejado + "')");
            resultado.waitFor(new Locator.WaitForOptions().setTimeout(7000));
            resultado.click();
        } catch (Exception e) {
            System.err.println("Erro ao selecionar opção do select2: " + valorDesejado);
        }
    }
}