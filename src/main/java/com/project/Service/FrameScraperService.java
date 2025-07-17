package com.project.Service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FrameScraperService {

  public String capturarValorDoFrame(String ip) {
    Playwright playwright = null;
    Browser browser = null;

    try {
      playwright = Playwright.create();
      browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions().setHeadless(true)
      );

      BrowserContext context = browser.newContext(
        new Browser.NewContextOptions().setIgnoreHTTPSErrors(true)
      );

      Page page = context.newPage();
      page.navigate("https://"+ip+"/opt1/index.htm");

      page.waitForLoadState(LoadState.DOMCONTENTLOADED);

      // Primeiro iframe: wlmframe
      Frame frame1 = page.frames().stream()
        .filter(f -> "wlmframe".equals(f.name()))
        .findFirst()
        .orElse(null);

      if (frame1 == null) {
        System.err.println("Frame 'wlmframe' não encontrado.");
        return null;
      }

      // Segundo iframe: Hme_Paper.htm (dentro de wlmframe)
      Frame frame2 = frame1.childFrames().stream()
        .filter(f -> f.url().contains("Hme_Paper.htm"))
        .findFirst()
        .orElse(null);

      if (frame2 == null) {
        System.err.println("Frame interno 'Hme_Paper.htm' não encontrado.");
        return null;
      }

      // Localiza todos os elementos correspondentes
      Locator paperCells = frame2.locator("td[data-bind*='PaperLevel']");
      int count = paperCells.count();

      for (int i = 0; i < count; i++) {
        String rawText = paperCells.nth(i).innerText().trim();

        //Usa regex para encontrar valores tipo "100%"
        Pattern pattern = Pattern.compile("(\\d+\\s*%)");
        Matcher matcher = pattern.matcher(rawText);

        if (matcher.find()) {
          return matcher.group(1).replace(" ", ""); // Ex: "100%"
        }
      }

      System.err.println("Nenhum valor em % encontrado.");
      return null;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (browser != null) browser.close();
      if (playwright != null) playwright.close();
    }
  }
}
