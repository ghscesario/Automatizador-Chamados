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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PrinterMonitorService {

    @Autowired private ChamadoService chamadoService;

    /* --------- RICOH SP3710 --------- */
    private final List<String> printerIps = List.of(
        "10.239.20.12","10.239.20.15","10.239.20.70"
        
    );

    /* --------- LISTA de Ricoh MP C3003/C3004 --------- */
    private final List<String> ricohC3003Ips = List.of(
        "10.239.20.30", // C3003
        "10.239.20.81"  // C3004
    );

    private final Set<String> impressorasComChamadoAberto = ConcurrentHashMap.newKeySet();
    private static final int LIMITE_CHAMADO = 25;

    /* ----------RICOH SP3710---------- */
    @Scheduled(fixedRate = 1800000)
    @SuppressWarnings("CallToPrintStackTrace")
    public void checkAllPrinters() {
        printerIps.forEach(ip -> tryRun(() -> {
            try {
                checkPrinterPreto(ip);
            } catch (Exception e) {
                System.out.println("Impressora de ip: "+ip+" se encontra indisponível ou offline!");
            }
        }, ip));
        ricohC3003Ips.forEach(ip -> tryRun(() -> {
            try {
                checkPrinterC3003(ip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ip));
    }
    private void tryRun(Runnable r, String ip){
        try { r.run(); } catch(Exception e){
            System.err.printf("Erro IP %s → %s%n", ip, e.getMessage());
        }
    }

    /* =====================================================
       IMPRESSORAS PRETO / KYOCERA / EPSON
       ===================================================== */
    private void checkPrinterPreto(String ip) throws Exception {
        int pct = extrairTonerPreto(ip);
        if (pct == -1) { 
            System.out.printf("IP %s sem leitura%n", ip); 
            return; 
        }

        if (pct == 0) { // acessível mas sem barra de toner
            System.out.printf("IP %s sem barra de toner. Abrindo chamado.%n", ip);
            if (!impressorasComChamadoAberto.contains(ip)) {
                abrirChamado(ip, 0);
                impressorasComChamadoAberto.add(ip);
            }
            return;
        }

        if (impressorasComChamadoAberto.contains(ip)) {
            if (pct >= LIMITE_CHAMADO) {
                impressorasComChamadoAberto.remove(ip);
                System.out.printf("IP %s Toner recuperado (%d%%).%n", ip, pct);
            }
            return;
        }
        System.out.printf("IP %s Toner preto %d%%%n", ip, pct);
        if (pct < LIMITE_CHAMADO) {
            abrirChamado(ip, pct);
            impressorasComChamadoAberto.add(ip);
        }
    }
    private int extrairTonerPreto(String ip) throws Exception {
        Document doc = fetchFirstAvailable(
            "https://"+ip+"/main.asp?Lang=en-us", "http://"+ip+"/main.asp?Lang=en-us",
            "https://"+ip+"/", "http://"+ip+"/"
        );
        Element t = doc.selectFirst("table.toner");
        if (t != null) return parsePercent(t.attr("width"));
        Element k = doc.selectFirst("td[data-bind*=PaperLevel]");
        if (k != null) return parsePercent(k.text());
        return 0;  // acessível mas sem barra de toner
    }

    /* =====================================================
       RICOH MP C3003 / C3004 – 4 cores
       ===================================================== */
    private void checkPrinterC3003(String ip) throws Exception {
        ColorLevels cl = extrairTonerRicohC3003(ip);
        int menor = cl.menor();
        if (menor == -1) {
            System.out.printf("IP %s sem leitura%n", ip);
            return;
        }

        boolean aberto = impressorasComChamadoAberto.contains(ip);
        System.out.printf("IP %s %s %s%n", ip, cl, aberto ? "(Chamado aberto)" : "");

        // Monta a string com as cores abaixo do limite
        StringBuilder coresAbaixo = new StringBuilder();
        if (cl.bk >= 0 && cl.bk < LIMITE_CHAMADO) coresAbaixo.append("Preto, ");
        if (cl.c >= 0 && cl.c < LIMITE_CHAMADO) coresAbaixo.append("Ciano, ");
        if (cl.m >= 0 && cl.m < LIMITE_CHAMADO) coresAbaixo.append("Magenta, ");
        if (cl.y >= 0 && cl.y < LIMITE_CHAMADO) coresAbaixo.append("Amarelo, ");

        // Remove a última vírgula e espaço, se houver
        String coresStr = "";
        if (coresAbaixo.length() > 0) {
            coresStr = coresAbaixo.substring(0, coresAbaixo.length() - 2);
        }

        if (aberto) {
            // Se chamado aberto, só remove se todas as cores estiverem acima do limite
            if (coresStr.isEmpty()) {
                impressorasComChamadoAberto.remove(ip);
                System.out.printf("IP %s Toners recuperados.%n", ip);
            }
            return;
        }

        if (!coresStr.isEmpty()) {
            abrirChamadoColorido(ip, coresStr);
            impressorasComChamadoAberto.add(ip);
        }
    }

private void abrirChamadoColorido(String ip, String cores) {
    System.out.printf("Chamado Colorido IP %s toner(s) %s%n", ip, cores);
    chamadoService.criarChamadoImpressoraColorida(ip, cores);
}


    private ColorLevels extrairTonerRicohC3003(String ip) throws Exception {
        Document doc = fetchFirstAvailable(
            "http://"+ip+"/web/guest/br/websys/webArch/getStatus.cgi#linkToner",
            "https://"+ip+"/web/guest/br/websys/webArch/getStatus.cgi#linkToner"
        );
        Elements dls = doc.select("div.tonerArea").parents();
        ColorLevels lv = new ColorLevels();
        for(Element dl : dls){
            Element img = dl.selectFirst("div.tonerArea img[src*=/images/deviceStTnBar]");
            Element dt  = dl.selectFirst("dt.listboxdtm");
            if(img == null || dt == null) continue;
            int pct = (int)((parseIntSafe(img.attr("width")) / 160.0) * 100);
            switch(dt.text().trim().toLowerCase()){
                case "preto" -> lv.bk = pct;
                case "ciano" -> lv.c = pct;
                case "magenta" -> lv.m = pct;
                case "amarelo" -> lv.y = pct;
            }
        }
        return lv;
    }
    private static class ColorLevels{
        int bk = -1, c = -1, m = -1, y = -1;
        int menor(){ 
            int min = 100;
            if(bk >= 0) min = Math.min(min, bk);
            if(c >= 0) min = Math.min(min, c);
            if(m >= 0) min = Math.min(min, m);
            if(y >= 0) min = Math.min(min, y);
            return min;
        }
        @SuppressWarnings("override")
        public String toString(){ 
            return String.format("Preto %d%%, Ciano %d%%, Magenta %d%%, Amarelo %d%%", bk, c, m, y);
        }
    }

    /* =====================================================
       FERRAMENTAS GERAIS
       ===================================================== */
    @SuppressWarnings("UseSpecificCatch")
    private Document fetchFirstAvailable(String... urls) throws Exception{
        SSLUtil.disableSSLCertificateChecking();
        for(String u : urls){
            try{
                HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
                c.setConnectTimeout(4000); c.setReadTimeout(4000);
                if(c.getResponseCode() >= 400) continue;
                try(InputStream in = c.getInputStream()){
                    return Jsoup.parse(in, "UTF-8", u);
                }
            }catch(Exception ignore){ }
        }
        throw new Exception("Todas URLs falharam");
    }
    private int parsePercent(String v){ return parseIntSafe(v.replace("%","")); }
    @SuppressWarnings("UseSpecificCatch")
    private int parseIntSafe(String v){ 
        try { 
            return Integer.parseInt(v.trim()); 
        } catch(Exception e){ 
            return -1; 
        } 
    }

    private void abrirChamado(String ip,int nivel){
        System.out.printf("Chamado IP %s toner %d%%%n", ip, nivel);
        chamadoService.criarChamadoImpressora(ip);
    }
}
