package com.project.Service;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.Model.Printer;
import com.project.Model.PrinterCall;
import com.project.Repository.PrinterCallRepository;
import com.project.Repository.PrinterRepository;

import jakarta.annotation.PostConstruct;

@Service
public class PrinterMonitorService {

    @Autowired 
    private ChamadoService chamadoService;

    @Autowired
    private PrinterCallRepository printerCallRepository;

    @Autowired
    private PrinterRepository printerRepository;

    //Kyocera Scraper
    @Autowired
    private FrameScraperService frameScraperService;

    @Autowired
    private EpsonScraper epsonScraper;

    private List<String> printerIps;      // SP3710
    private List<String> ricohC3003Ips;  // C3003/C3004
    private List<String> kyoceraIps;    // Kyocera
    private List<String> epsonIps;     // Epson L5290
    private List<String> im430Ips;    // Ricoh IM430

    @PostConstruct
    @SuppressWarnings("unused")
    private void initIpLists() {
        printerIps     = printerRepository.findByTipo("SP3710")
                                        .stream().map(Printer::getIp).toList();
        ricohC3003Ips  = printerRepository.findByTipo("C300X")
                                        .stream().map(Printer::getIp).toList();
        kyoceraIps  = printerRepository.findByTipo("Kyocera")
                                        .stream().map(Printer::getIp).toList();
        epsonIps = printerRepository.findByTipo("Epson")
                                        .stream().map(Printer::getIp).toList();
        im430Ips = printerRepository.findByTipo("im430")
                                        .stream().map(Printer::getIp).toList();

        System.out.printf("IPs SP3710  : %s%n", printerIps);
        System.out.printf("IPs Ricoh C : %s%n", ricohC3003Ips);
    }

    private final Set<String> impressorasComChamadoAberto = ConcurrentHashMap.newKeySet();
    private static final int LIMITE_CHAMADO = 25;

    /* ----------RICOH SP3710---------- */
    @Scheduled(fixedRate = 1800000)
    @SuppressWarnings("CallToPrintStackTrace")
    public void checkAllPrinters() {
        this.printerIps = printerRepository.findAllByTipo("SP3710")
            .stream().map(Printer::getIp).toList();

        this.ricohC3003Ips = printerRepository.findAllByTipo("C300X")
            .stream().map(Printer::getIp).toList();

        this.kyoceraIps = printerRepository.findAllByTipo("Kyocera")
            .stream().map(Printer::getIp).toList();
        
        this.epsonIps = printerRepository.findAllByTipo("Epson")
            .stream().map(Printer::getIp).toList();

        this.im430Ips = printerRepository.findAllByTipo("im430")
            .stream().map(Printer::getIp).toList();

        im430Ips.forEach(ip -> tryRun(() -> {
            try {
                checkRicoh(ip);
            } catch (Exception e) {
                System.out.println("IM430 " + ip + " indisponível ou erro.");
            }
        }, ip));
        this.printerIps.forEach(ip -> tryRun(() -> {
            try {
                checkPrinterPreto(ip);
            } catch (Exception e) {
                System.out.println("Impressora de IP: " + ip + " se encontra indisponível ou offline!");
            }
        }, ip));
        this.ricohC3003Ips.forEach(ip -> tryRun(() -> {
            try {
                checkPrinterC3003(ip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ip));
        this.kyoceraIps.forEach(ip -> tryRun(() -> {
            try {
                checkKyocera(ip);
            } catch (Exception e) {
                System.out.println("Impressora de IP: " + ip + " se encontra indisponível ou offline!");
            }
        }, ip));
        epsonIps.forEach(ip -> tryRun(() -> {
            try {
                checkEpson(ip);
            } catch (Exception e) {
                System.out.println("Epson " + ip + " indisponível ou erro.");
            }
        }, ip));
        
        

    }

    /* =====================================================
       IMPRESSORAS SP3710
       ===================================================== */
    private void checkPrinterPreto(String ip) throws Exception {
        int pct = extrairTonerPreto(ip);
        if (pct == -1) {
            System.out.printf("IP %s sem leitura%n", ip);
            return;
        }

        if (pct == 0) {
            System.out.printf("IP %s sem barra de toner. Abrindo chamado.%n", ip);
            if (!impressorasComChamadoAberto.contains(ip)) {
                abrirChamado(ip, 0);
                addCalledPrinter(ip); 
            }
            return;
        }

        if (impressorasComChamadoAberto.contains(ip)) {
            if (pct >= LIMITE_CHAMADO) {
                removeCalledPrinter(ip);
                System.out.printf("IP %s Toner recuperado (%d%%).%n", ip, pct);
            }
            return;
        }

        System.out.printf("IP %s Toner preto %d%%%n", ip, pct);
        if (pct < LIMITE_CHAMADO) {
            abrirChamado(ip, pct);
            addCalledPrinter(ip); 
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

        StringBuilder coresAbaixo = new StringBuilder();
        if (cl.bk >= 0 && cl.bk < LIMITE_CHAMADO) coresAbaixo.append("Preto, ");
        if (cl.c  >= 0 && cl.c  < LIMITE_CHAMADO) coresAbaixo.append("Ciano, ");
        if (cl.m  >= 0 && cl.m  < LIMITE_CHAMADO) coresAbaixo.append("Magenta, ");
        if (cl.y  >= 0 && cl.y  < LIMITE_CHAMADO) coresAbaixo.append("Amarelo, ");

        String coresStr = "";
        if (coresAbaixo.length() > 0) {
            coresStr = coresAbaixo.substring(0, coresAbaixo.length() - 2);
        }

        if (aberto) {
            if (coresStr.isEmpty()) {
                removeCalledPrinter(ip); // remover do banco
                System.out.printf("IP %s Toners recuperados.%n", ip);
            }
            return;
        }

        if (!coresStr.isEmpty()) {
            abrirChamadoColorido(ip, coresStr);
            addCalledPrinter(ip); // persistência no banco
        }
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
                        Kyocera
       ===================================================== */
    public void checkKyocera(String ip) throws Exception {
        String valor = frameScraperService.capturarValorDoFrame(ip);

        if (valor == null) {
            System.out.printf("Kyocera %s → sem leitura ou erro%n", ip);
            return;
        }

        int pct = parsePercent(valor);
        if (pct == -1) {
            System.out.printf("Kyocera %s → valor inválido '%s'%n", ip, valor);
            return;
        }

        boolean aberto = impressorasComChamadoAberto.contains(ip);

        if (aberto) {
            if (pct >= LIMITE_CHAMADO) {
                removeCalledPrinter(ip);
                System.out.printf("Kyocera %s Toner recuperado (%d%%)%n", ip, pct);
            } else {
                System.out.printf("Kyocera %s Chamado aberto — toner ainda baixo (%d%%)%n", ip, pct);
            }
            return;
        }

        System.out.printf("Kyocera %s Toner %d%%%n", ip, pct);

        if (pct < LIMITE_CHAMADO) {
            abrirChamado(ip, pct);
            addCalledPrinter(ip);
        }
    }

    /* =====================================================
                        Epson
       ===================================================== */
        private void checkEpson(String ip) {
        final int ALTURA_MAXIMA = 50; // 50 = 100%
        var alturas = epsonScraper.obterNiveisDeTinta(ip);

        if (alturas.isEmpty()) {
            System.out.printf("Epson %s sem leitura%n", ip);
            return;
        }

        System.out.printf("Epson %s Níveis de tinta: %n", ip);
        Map<String, Integer> niveisConvertidos = new HashMap<>();

        alturas.forEach((cor, altura) -> {
            int percentual = (int) Math.round((altura / (double) ALTURA_MAXIMA) * 100);
            niveisConvertidos.put(cor, percentual);
            System.out.printf("   - %s: %d%%%n", cor, percentual);
        });

        StringBuilder coresBaixas = new StringBuilder();

        niveisConvertidos.forEach((cor, nivel) -> {
            if (nivel < LIMITE_CHAMADO) {
                coresBaixas.append(cor).append(", ");
            }
        });

        boolean aberto = impressorasComChamadoAberto.contains(ip);

        if (coresBaixas.length() > 0) {
            String coresStr = coresBaixas.substring(0, coresBaixas.length() - 2);

            if (!aberto) {
                abrirChamadoColorido(ip, coresStr);
                addCalledPrinter(ip);
            } else {
                System.out.printf("Epson %s Chamado já aberto. Tinta(s) baixa(s): %s%n", ip, coresStr);
            }
        } else if (aberto) {
            System.out.printf("Epson %s Todas tintas recuperadas%n", ip);
            removeCalledPrinter(ip);
        } else {
            System.out.printf("");
        }
    }

     /* =====================================================
                        Ricoh IM430
       ===================================================== */
    private void checkRicoh(String ip) {
        var niveis = RicohIMScraper.obterNiveisDeToner(ip);

        if (niveis.isEmpty()) {
            System.out.printf("Ricoh %s sem leitura%n", ip);
            return;
        }

        try {
            niveis.forEach((cor, nivel) -> {
                System.out.printf("%s Toner %s: %d%%%n", ip, cor, nivel);

                boolean aberto = impressorasComChamadoAberto.contains(ip);

                if (nivel < LIMITE_CHAMADO) {
                    if (!aberto) {
                        abrirChamado(ip, nivel);
                        addCalledPrinter(ip);
                    } else {
                        System.out.printf("Ricoh %s Chamado já aberto. Toner baixo%n", ip);
                    }
                } else if (aberto) {
                    System.out.printf("Ricoh %s Toner recuperado%n", ip);
                    removeCalledPrinter(ip);
                } else {
                    System.out.printf("");
                }
            });
        } catch (Exception e) {
            System.out.printf("Erro ao processar toner da impressora %s: %s%n", ip, e.getMessage());
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

    @PostConstruct
    @SuppressWarnings("unused")
    private void loadCalledPrinters() {
        printerCallRepository.findAll()
            .forEach(pc -> impressorasComChamadoAberto.add(pc.getIp()));
        System.out.printf("IPs com chamado no banco: %s%n", impressorasComChamadoAberto);
    }

    private void addCalledPrinter(String ip) {
        if (impressorasComChamadoAberto.add(ip)) {
            printerCallRepository.save(new PrinterCall(ip));
        }
    }
    private void removeCalledPrinter(String ip) {
        if (impressorasComChamadoAberto.remove(ip)) {
            printerCallRepository.deleteById(ip);
        }
    }

        private void tryRun(Runnable r, String ip){
        try { r.run(); } catch(Exception e){
            System.err.printf("Erro IP %s → %s%n", ip, e.getMessage());
        }
    }

    private void abrirChamado(String ip, int nivel) {
        // 1) Descobre o nome da impressora no banco
        String nome = printerRepository.findById(ip)
                        .map(Printer::getName)
                        .orElse("(sem nome)");

        System.out.printf("Chamado IP %s (%s) toner %d%%%n", ip, nome, nivel);

        // 2) Passa IP + nome ao ChamadoService
        chamadoService.criarChamadoImpressora(ip, nome);
    }

    private void abrirChamadoColorido(String ip, String cores) {
        String nome = printerRepository.findById(ip)
                        .map(Printer::getName)        
                        .orElse("(sem nome)");
        System.out.printf("Chamado Colorido IP %s toner(s) %s%n", ip, cores);
        chamadoService.criarChamadoImpressoraColorida(ip, cores, nome);
    }

}
