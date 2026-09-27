
package com.mycompany.actividadcrawler4j;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.net.URI;
import java.util.Locale;

public class CrawlerCETYS extends WebCrawler {

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {

        try {
            URI uri = URI.create(url.getURL());

            String host = uri.getHost();

            if (host == null) {
                return false;
            }

            host = host.toLowerCase(Locale.ROOT);

            boolean dominioValido =
                    host.equals("books.toscrape.com");


            String ruta = uri.getPath().toLowerCase(Locale.ROOT);

            boolean archivoExcluido = ruta.matches(
                    ".*\\.(css|js|jpg|jpeg|png|gif|svg|webp|pdf|zip|mp3|mp4)$"
            );

            return dominioValido && !archivoExcluido;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void visit(Page page) {

        if (!(page.getParseData() instanceof HtmlParseData)) {
            return;
        }

        HtmlParseData html =
                (HtmlParseData) page.getParseData();

        String url = page.getWebURL().getURL();

        // Convertir el HTML en un documento Jsoup
        Document pagina = Jsoup.parse(html.getHtml(), url);

        // Eliminar contenido técnico y navegación repetitiva
        pagina.select(
                "script, style, noscript, iframe, svg, " +
                "nav, footer, aside, form, " +
                ".side_categories, .breadcrumb, .pagination, " +
                ".alert-warning"
        ).remove();

        // Extraer texto del cuerpo de la página
        if (pagina.body() == null) {
            return;
        }

        String texto = pagina.body()
                .text()
                .replaceAll("\\s+", " ")
                .trim();

        // No indexar documentos sin texto
        if (texto.isBlank()) {
            return;
        }

        // Registrar el documento en el índice invertido
        int id = InvertedIndex.getInstancia()
                .agregarDocumento(url, texto);

        if (id == -1) {
            return;
        }

        int enlaces = html.getOutgoingUrls().size();

        System.out.println("--------------------------");
        System.out.println("Documento ID: " + id);
        System.out.println("URL: " + url);
        System.out.println("Caracteres limpios: " + texto.length());
        System.out.println("Enlaces encontrados: " + enlaces);
    }

}
