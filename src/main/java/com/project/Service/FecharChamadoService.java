package com.project.Service;

import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class FecharChamadoService {
    
    public void fecharChamado(String numeroChamado, String anotacoesResolucao) {
    boolean sucesso = false;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();

            // 1. Navega para a tela de lista de chamados
            page.navigate("https://hiaeprod.service-now.com.mcas.ms/now/sow/list/params/list-id/310947c0fbe2ee10d612f9d38eefdc59/tiny-id/1AUamOudzbOzJX7WbDaG1GRY9VAqzg2o");

            Locator searchInput = page.locator("input[placeholder='Pesquisar']");
            searchInput.click(new Locator.ClickOptions().setForce(true));
            page.keyboard().type(numeroChamado, new Keyboard.TypeOptions().setDelay(100));
            page.waitForTimeout(5000);
            page.keyboard().press("Enter");
            page.waitForTimeout(3000);

            Locator botaoDesignar = page.locator("button:has-text('Designar a mim')");

            // Verifica se o botão existe
            if (botaoDesignar.count() > 0) {
                System.out.println("Botão 'Designar a mim' encontrado. Clicando...");
                botaoDesignar.first().click();
                page.waitForTimeout(1500); // Pequena pausa após clique
            } else {
                System.out.println("Botão 'Designar a mim' não encontrado. Seguindo para 'Resolver'...");
            }

            // Continua o fluxo clicando em "Resolver"
            Locator botaoResolver = page.locator("button.now-button.-primary:has-text('Resolver')");
            botaoResolver.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            botaoResolver.click();
            page.waitForTimeout(1000);

            // 1. Abre o combobox "Código de resolução"
            Locator comboCodigoResolucao = page.locator("button[aria-label='Código de resolução']");
            comboCodigoResolucao.waitFor();
            comboCodigoResolucao.click();

            // 2. Aguarda o dropdown aparecer
            page.waitForTimeout(500); // pequeno delay para o dropdown abrir

            // 3. Usa teclado para selecionar a 3ª opção (duas setas ↓, depois Enter)
            page.keyboard().press("ArrowDown");
            page.waitForTimeout(200); // pequeno delay entre teclas
            page.keyboard().press("ArrowDown");
            page.waitForTimeout(200);
            page.keyboard().press("Enter");

            // 7. Preenche as anotações de resolução
            page.locator("textarea[aria-label*='Anotações de resolução']").fill(anotacoesResolucao);

            // 8. Clica no botão final "Resolver"
            page.locator("now-button[id='item-save_button']").click();

            System.out.println("Chamado resolvido com sucesso.");
            sucesso = true;

        } catch (Exception e) {
            System.err.println("Erro ao tentar resolver o chamado:");
            e.printStackTrace();
        }
    }

}
