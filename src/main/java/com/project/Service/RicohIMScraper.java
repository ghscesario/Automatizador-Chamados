package com.project.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RicohIMScraper {
    
    private static final int WIDTH_MAXIMO = 160;

    public static Map<String, Integer> obterNiveisDeToner(String ip) {
        Map<String, Integer> niveis = new HashMap<>();
        String url = "http://" + ip + "/web/guest/pt/websys/webArch/getStatus.cgi#linkToner";

        try {
            Document doc = Jsoup.connect(url) 
                                .timeout(5000)
                                .get();

            // Busca pelo <img> da barra de toner preto
            Element barraPreta = doc.selectFirst("img[src*=deviceStTnBarK.gif]");

            if (barraPreta != null) {
                String widthStr = barraPreta.attr("width");
                int width = Integer.parseInt(widthStr.trim());

                int percentual = (int) Math.round((width / (double) WIDTH_MAXIMO) * 100);
                niveis.put("Preto", percentual);
            }

        } catch (IOException e) {
            System.out.printf("Erro ao acessar Ricoh %s: %s%n", ip, e.getMessage());
        }

        return niveis;
    }

}
