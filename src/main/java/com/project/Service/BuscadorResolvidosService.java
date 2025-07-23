package com.project.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.*;

@Service
public class BuscadorResolvidosService {

    private static final String URL_RESOLVIDOS = "https://hiaeprod.service-now.com.mcas.ms/now/sow/list/params/list-id/31da1d2747fa2250eb41b352036d433c/tiny-id/AoRsXPgAGosilpyEBZjmGvkwbY8LVhNK";

    public List<String> buscarNumerosDeChamadosResolvidos() {
        List<String> numeros = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true) // true para produção
            );

            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(Paths.get("session.json"))
            );

            Page page = context.newPage();
            page.navigate(URL_RESOLVIDOS);
            page.waitForTimeout(6000); // aguarda carregamento

            Locator linhas = page.locator(
                "macroponent-f51912f4c700201072b211d4d8c26010 >> " +
                "#item-snCanvasAppshellMain >> " +
                "sn-canvas-experience-shell >> macroponent-c276387cc331101080d6d3658940ddd2 >> " +
                "#item-wsContent >> main > sn-canvas-screen:nth-child(1) >> " +
                "section > screen-action-transformer-d0f8b9a3c3013010965e070e9140dde0 > macroponent-2c08111d0fc21010036a83fa68767ef6 >> " +
                "#item-resizable_panes > now-record-list-connected >> div > now-record-list >> " +
                "div > div.sn-list-grid-container > div > div > now-grid >> " +
                "table > tbody > tr"
            );

            int total = linhas.count();

            for (int i = 0; i < total; i++) {
                try {
                    Locator linkNumeroChamado = linhas.nth(i).locator("td:nth-child(3) a");
                    String numeroChamado = linkNumeroChamado.textContent().trim();
                    numeros.add(numeroChamado);
                } catch (Exception e) {
                    System.err.println("Erro ao extrair número da linha " + i);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar chamados resolvidos:");
            e.printStackTrace();
        }
        System.out.println(numeros);
        return numeros;
    }
}
