package com.project.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class EpsonScraper {

    public Map<String, Integer> obterNiveisDeTinta(String ip) {
    Map<String, Integer> niveis = new LinkedHashMap<>();

    try {
        // Ignora certificados SSL inválidos
        SSLUtil.disableSSLCertificateChecking();

        String url = "https://" + ip + "/PRESENTATION/HTML/TOP/PRTINFO.HTML";

        // Conexão manual via HttpsURLConnection
        URL endereco = new URL(url);
        HttpsURLConnection conexao = (HttpsURLConnection) endereco.openConnection();
        conexao.setConnectTimeout(5000);
        conexao.setReadTimeout(5000);

        InputStream is = conexao.getInputStream();
        Document doc = Jsoup.parse(is, "UTF-8", url);

        // Aqui começa o parsing real dos níveis de tinta
        Elements elementos = doc.select("img.color");

        for (Element img : elementos) {
            String src = img.attr("src");
            String heightStr = img.attr("height");

            if (heightStr != null && !heightStr.isEmpty()) {
                int altura = Integer.parseInt(heightStr);
                String cor = identificarCor(src); // Método auxiliar
                niveis.put(cor, altura);
            }
        }

    } catch (Exception e) {
        System.out.println("Erro ao acessar Epson " + ip + ": " + e.getMessage());
    }

    return niveis;
}

private String identificarCor(String src) {
    if (src.contains("Ink_K")) return "Preto";
    if (src.contains("Ink_C")) return "Ciano";
    if (src.contains("Ink_M")) return "Magenta";
    if (src.contains("Ink_Y")) return "Amarelo";
    return "Desconhecido";
}

}
