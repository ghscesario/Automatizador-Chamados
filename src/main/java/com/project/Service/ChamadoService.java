package com.project.Service;

import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class ChamadoService {

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
    public void criarChamadoPadrao() {
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
            Locator inputTelefone = page.locator("input[name='telefone_celular']");
            inputTelefone.fill("1199999999");

            // HORÁRIO TRABALHO
            page.waitForSelector("input[name='horario_escala_trabalho']");
            Locator inputHorario = page.locator("input[name='horario_escala_trabalho']");
            inputHorario.fill("24h");

            // Selecionar Unidade (digitação e seleção)
            selecionarUnidade(page, "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");

            // Selecionar Bloco (abre dropdown e confirma primeira opção com Enter)
            selecionarBloco(page, "HOSPITAL");

            //Selecionar Andar
            selecionarAndar(page, "1º ANDAR");

            //Selecionar Área
            selecionarArea(page, "CENTRO CIRUGICO CORREDORES");         

            //Selecionar Categoria
            selecionarCategoria(page, "Equipamentos de TI");

            //Selecionar Sub Categoria
            selecionarSubCategoria(page, "Desktop");

            //Selecionar Centro de Custo
            selecionarCentroCusto(page, "ORTI");

            //Selecionar Urgencia
            selecionarUrgencia(page, "Poucos equipamentos");

            //Selecionar Sintoma
            selecionarSintoma(page, "Falha");

            // Descrição Curta
            page.waitForSelector("textarea[name='short_description']");
            Locator inputResumo = page.locator("textarea[name='short_description']");
            inputResumo.fill("teste 3");
            
            // Descrição detalhada
            page.waitForSelector("textarea[name='description']");
            Locator inputDescricao = page.locator("textarea[name='description']");
            inputDescricao.fill("teste");

            Locator botaoEnviar = page.locator("#submit-btn");

            // Aguarda o botão estar visível e habilitado
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Clica no botão
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            System.out.println("Chamado criado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
        }
    }


    // EXECUTAR SEM LOGIN
    public void criarChamado(String telefone, String horario, String bloco, String andar, String area, String categoria, String subCategoria, String urgencia, String sintoma, String descResumo, String descDetalhada) {
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
            Locator inputTelefone = page.locator("input[name='telefone_celular']");
            inputTelefone.fill(telefone);

            // HORÁRIO TRABALHO
            page.waitForSelector("input[name='horario_escala_trabalho']");
            Locator inputHorario = page.locator("input[name='horario_escala_trabalho']");
            inputHorario.fill(horario);

            // Selecionar Unidade (digitação e seleção)
            selecionarUnidade(page, "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");

            // Selecionar Bloco (abre dropdown e confirma primeira opção com Enter)
            selecionarBloco(page, bloco);

            //Selecionar Andar
            selecionarAndar(page, andar);

            //Selecionar Área
            selecionarArea(page, area);         

            //Selecionar Categoria
            selecionarCategoria(page, categoria);

            //Selecionar Sub Categoria
            selecionarSubCategoria(page, subCategoria);

            //Selecionar Centro de Custo
            selecionarCentroCusto(page, "ORTI");

            //Selecionar Urgencia
            selecionarUrgencia(page, urgencia);

            //Selecionar Sintoma
            selecionarSintoma(page, sintoma);

            // Descrição Curta
            page.waitForSelector("textarea[name='short_description']");
            Locator inputResumo = page.locator("textarea[name='short_description']");
            inputResumo.fill("teste 3");
            
            // Descrição detalhada
            page.waitForSelector("textarea[name='description']");
            Locator inputDescricao = page.locator("textarea[name='description']");
            inputDescricao.fill("teste");

            Locator botaoEnviar = page.locator("#submit-btn");

            // Aguarda o botão estar visível e habilitado
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Clica no botão
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            System.out.println("Chamado criado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
        }
    }

    // EXECUTAR SEM LOGIN
    public void criarChamadoImpressora(String ip) {
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
            Locator inputTelefone = page.locator("input[name='telefone_celular']");
            inputTelefone.fill("1199999999");

            // HORÁRIO TRABALHO
            page.waitForSelector("input[name='horario_escala_trabalho']");
            Locator inputHorario = page.locator("input[name='horario_escala_trabalho']");
            inputHorario.fill("(BOT)");

            // Selecionar Unidade (digitação e seleção)
            selecionarUnidade(page, "HOSP EST DE URGÊNCIAS DE GOIÁS (IIRS)");

            // Selecionar Bloco (abre dropdown e confirma primeira opção com Enter)
            selecionarBloco(page, "BLOCO ADMINISTRATIVO");

            //Selecionar Andar
            selecionarAndar(page, "TÉRREO");

            //Selecionar Área
            selecionarArea(page, "T.I");         

            //Selecionar Categoria
            selecionarCategoria(page, "Equipamentos de TI");

            //Selecionar Sub Categoria
            selecionarSubCategoria(page, "Equipamentos Millennium");

            //Selecionar Centro de Custo
            selecionarCentroCusto(page, "ORTI");

            //Selecionar Urgencia
            selecionarUrgencia(page, "O meu departamento e não");

            //Selecionar Sintoma
            selecionarSintoma(page, "Indisponibilidade");

            // Descrição Curta
            page.waitForSelector("textarea[name='short_description']");
            Locator inputResumo = page.locator("textarea[name='short_description']");
            inputResumo.fill("Troca de toner, impressora:"+ip+" (BOT)");
            
            // Descrição detalhada
            page.waitForSelector("textarea[name='description']");
            Locator inputDescricao = page.locator("textarea[name='description']");
            inputDescricao.fill("Sistema automatizado identificou na varredura que a impressora: "+ip+", necessita da substituição do toner!");

            Locator botaoEnviar = page.locator("#submit-btn");

            // Aguarda o botão estar visível e habilitado
            botaoEnviar.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Clica no botão
            botaoEnviar.click();

            System.out.println("Clique em 'Enviar Solicitação' realizado.");

            System.out.println("Chamado criado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
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

    private void selecionarArea(Page page, String valorDesejado) {
        try {
            // Clica na seta para abrir o dropdown do Bloco
            Locator setaDropdown = page.locator("#s2id_sp_formfield_unidade_departamento .select2-arrow");
            setaDropdown.click();

            // Espera o container da lista aparecer
            page.waitForSelector("ul#select2-results-12", new Page.WaitForSelectorOptions().setTimeout(5000));

            // Localiza o item <li> da lista que contém o texto da opção desejada e clica
            Locator opcao = page.locator("ul#select2-results-12 > li", new Page.LocatorOptions().setHasText(valorDesejado));
            opcao.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            opcao.click();

            System.out.println("Bloco selecionado: " + valorDesejado);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar bloco: " + valorDesejado);
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
