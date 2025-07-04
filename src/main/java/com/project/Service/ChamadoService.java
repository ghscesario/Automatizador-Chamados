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
            page.navigate("https://hiaeprod.service-now.com.mcas.ms/now/sow/record/incident/-1_uid_1/params/query/opened_atONToday%40javascript%3Ags.beginningOfToday()%40javascript%3Ags.endOfToday()%5Elocation%3Db6452e92474ae210c6e5dd6df26d4380");

            // Aguarda o carregamento da página
            page.waitForTimeout(5000);

            // Caminho completo até o campo de telefone no Shadow DOM
            Locator inputTelefone = page.locator("macroponent-f51912f4c700201072b211d4d8c26010")
                .locator("#item-snCanvasAppshellMain")
                .locator("macroponent-c276387cc331101080d6d3658940ddd2")
                .locator("#item-wsContent")
                .locator("sn-canvas-screen")
                .locator("screen-action-transformer-ddd9404843fa2110f20fff53e9b8f2bf")
                .locator("macroponent-c5d9c00443fa2110f20fff53e9b8f2d0")
                .locator("#item-details_resizable_panes > now-record-form-section-column-layout")
                .locator("div.sn-form-column-layout-container.-vertical > div > div > div.sn-form-column-layout-sections > section:nth-child(1) > div > div > div:nth-child(1) > div:nth-child(1) > sn-record-input-connected:nth-child(3)")
                .locator("now-popover > now-input")
                .locator("#form-field-gd8vh8chlup7-1574");

            // Preenche o telefone
            inputTelefone.fill("1199999999");

            // Clica no botão de envio (ajuste o seletor se necessário)
            Locator botaoEnviar = page.locator("now-button-bar")
                .locator("button:has-text('Enviar')");

            botaoEnviar.click();

            System.out.println("Chamado criado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao criar o chamado:");
            e.printStackTrace();
        }
    }
}
