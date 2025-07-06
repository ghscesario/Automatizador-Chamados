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

            page.navigate("https://hiaeprod.service-now.com.mcas.ms/now/sow/record/incident/-1_uid_1/params/query/opened_atONToday%40javascript%3Ags.beginningOfToday()%40javascript%3Ags.endOfToday()%5Elocation%3Db6452e92474ae210c6e5dd6df26d4380");

            System.out.println("Faça o login manualmente (com MFA), depois pressione ENTER...");
            System.in.read();

            context.storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("session.json")));

            System.out.println("Sessão salva com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // EXECUTAR SEM LOGIN
    public void criarChamado() {
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

            //Selecionar Centro de Custo
            selecionarCusto(page, "ORTI");

            //Selecionar Categoria
            selecionarCategoria(page, "Equipamentos de TI");

            // RESUMO
            page.waitForSelector("textarea[name='short_description']");
            Locator inputResumo = page.locator("textarea[name='short_description']");
            inputResumo.fill("teste");

            System.out.println("Chamado criado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void selecionarCusto(Page page, String valorDesejado) {
    try {
        // Abre o dropdown
        Locator setaDropdown = page.locator("#s2id_sp_formfield_centro_custo .select2-arrow");
        setaDropdown.click();

        // Aguarda o dropdown abrir completamente
        page.waitForSelector("div.select2-drop-active", new Page.WaitForSelectorOptions().setTimeout(3000));

        // Aguarda a lista carregar
        page.waitForSelector("div[id^='select2-result-label-']", new Page.WaitForSelectorOptions().setTimeout(5000));

        // Encontra qualquer div com id começando com 'select2-result-label-' que contenha o texto
        Locator opcao = page.locator("div[id^='select2-result-label-']").filter(
            new Locator.FilterOptions().setHasText(valorDesejado)
        );

        opcao.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        opcao.click();

        System.out.println("Centro de custo selecionado: " + valorDesejado);
    } catch (Exception e) {
        System.err.println("Erro ao selecionar centro de custo: " + valorDesejado);
        e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
