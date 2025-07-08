package com.project.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PrinterMonitorService {

    @Autowired
    private ChamadoService chamadoService;

    private final List<String> printerIps = List.of(
        "10.239.20.12",
        "10.239.20.15",
        "10.239.20.16",
        "10.239.20.17",
        "10.239.20.18",
        "10.239.20.19",
        "10.239.20.20",
        "10.239.20.21",
        "10.239.20.22",
        "10.239.20.23",
        "10.239.20.24",
        "10.239.20.25",
        "10.239.20.26",
        "10.239.20.27",
        "10.239.20.28",
        "10.239.20.30",
        "10.239.20.31",
        "10.239.20.32",
        "10.239.20.33",
        "10.239.20.34",
        "10.239.20.35",
        "10.239.20.36",
        "10.239.20.37",
        "10.239.20.38",
        "10.239.20.39",
        "10.239.20.40",
        "10.239.20.41",
        "10.239.20.42",
        "10.239.20.43",
        "10.239.20.44",
        "10.239.20.45",
        "10.239.20.46",
        "10.239.20.47",
        "10.239.20.48",
        "10.239.20.49",
        "10.239.20.50",
        "10.239.20.51",
        "10.239.20.52",
        "10.239.20.55",
        "10.239.20.56",
        "10.239.20.57",
        "10.239.20.58",
        "10.239.20.59",
        "10.239.20.60",
        "10.239.20.61",
        "10.239.20.62",
        "10.239.20.63",
        "10.239.20.64",
        "10.239.20.66",
        "10.239.20.67",
        "10.239.20.68",
        "10.239.20.69",
        "10.239.20.70",
        "10.239.20.71",
        "10.239.20.72",
        "10.239.20.73",
        "10.239.20.74",
        "10.239.20.75",
        "10.239.20.76",
        "10.239.20.77",
        "10.239.20.78",
        "10.239.20.79",
        "10.239.20.80",
        "10.239.20.81",
        "10.239.20.82",
        "10.239.20.83",
        "10.239.20.84",
        "10.239.20.85",
        "10.239.20.86",
        "10.239.20.87",
        "10.239.20.88",
        "10.239.20.89",
        "10.239.20.90",
        "10.239.20.91",
        "10.239.20.92",
        "10.239.20.93",
        "10.239.20.94",
        "10.239.20.95",
        "10.239.20.97",
        "10.239.20.98",
        "10.239.20.99",
        "10.239.20.205",
        "10.239.20.206",
        "10.239.20.207",
        "10.239.20.208",
        "10.239.20.209"
    );

    // Conjunto thread-safe para IPs com chamado aberto
    private final Set<String> impressorasComChamadoAberto = ConcurrentHashMap.newKeySet();

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
        // Se já tem chamado aberto, verifica só se o toner recuperou
        if (impressorasComChamadoAberto.contains(ip)) {
            Document doc = getHtmlIgnoringSSL("https://" + ip + "/main.asp?Lang=en-us");
            Element tonerTable = doc.selectFirst("table.toner");

            int percent = 0;
            if (tonerTable != null) {
                String widthAttr = tonerTable.attr("width");
                percent = parsePercent(widthAttr);
            }
            if (percent >= 10) {
                impressorasComChamadoAberto.remove(ip);
                System.out.println("IP " + ip + " - Nível de toner recuperado (" + percent + "%). Monitoramento normal retomado.");
            } else {
                System.out.println("IP " + ip + " - Chamado já aberto. Toner em " + percent + "%. Aguardando recuperação.");
            }
            return;
        }

        // Fluxo normal para impressora sem chamado aberto
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
            impressorasComChamadoAberto.add(ip);
            return;
        }

        String widthAttr = tonerTable.attr("width");
        int percent = parsePercent(widthAttr);

        if (percent >= 0) {
            System.out.println("IP " + ip + " - Toner preto em " + percent + "%");
            if (percent < 25) {
                abrirChamado(ip, percent);
                impressorasComChamadoAberto.add(ip);
            }
        } else {
            System.out.println("IP " + ip + " - Não foi possível extrair a porcentagem do toner.");
        }
    }

    @SuppressWarnings("UseSpecificCatch")
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
        //Descomentar linha abaixo para abrir chamados automaticamente quando os níveis de toner estiverem baixos
        chamadoService.criarChamadoImpressora(ip);
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
