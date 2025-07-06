package com.project.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PrinterMonitorService {

    private final List<String> printerIps = List.of(
        "10.239.20.70",
        "10.239.20.82",
        "10.239.20.84",
        "10.239.20.37",
        "10.239.20.74"
    );

    @Scheduled(fixedRate = 1800000)
    public void checkAllPrinters() {
        for (String ip : printerIps) {
            try {
                checkPrinter(ip);
            } catch (Exception e) {
                System.err.println("Erro ao verificar IP: " + ip + " → " + e.getMessage());
            }
        }
    }

    public void checkPrinter(String ip) throws Exception {
        String url = "https://" + ip + "/main.asp?Lang=en-us";

        Document doc;
        try {
            doc = getHtmlIgnoringSSL(url);
        } catch (Exception e) {
            throw new Exception("Falha ao acessar IP " + ip + ": " + e.getMessage());
        }

        Element tonerTable = doc.selectFirst("table.toner");

        if (tonerTable == null) {
            int nivel = 0; // assume toner vazio
            System.out.println("IP " + ip + " - Barra de toner não encontrada. Possivelmente toner vazio (0%).");
            abrirChamado(ip, nivel);
            return;
        }

        String widthAttr = tonerTable.attr("width");
        int percent = parsePercent(widthAttr);

        if (percent >= 0) {
            System.out.println("IP " + ip + " - Toner preto em " + percent + "%");
            if (percent < 10) {
                abrirChamado(ip, percent);
            }
        } else {
            System.out.println("IP " + ip + " - Não foi possível extrair a porcentagem do toner.");
        }
    }

    private int parsePercent(String width) {
        try {
            return Integer.parseInt(width.replace("%", "").trim());
        } catch (Exception e) {
            System.err.println("Erro ao converter atributo width para número: " + e.getMessage());
            return -1;
        }
    }

    private void abrirChamado(String ip, int nivel) {
        System.out.println("Abrindo chamado para IP " + ip + " com toner em " + nivel + "%");
        // restTemplate.postForObject("http://seuservico/api/chamados", new Chamado(ip, nivel), Void.class);
    }

    private Document getHtmlIgnoringSSL(String url) throws Exception {
        SSLUtil.disableSSLCertificateChecking();

        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setInstanceFollowRedirects(true);

        try (InputStream in = connection.getInputStream()) {
            return Jsoup.parse(in, "UTF-8", url);
        }
    }
}
