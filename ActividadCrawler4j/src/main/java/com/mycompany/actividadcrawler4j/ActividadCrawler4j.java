
package com.mycompany.actividadcrawler4j;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class ActividadCrawler4j {

    public static void main(String[] args) throws Exception {

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder("datos-crawler");


        config.setMaxPagesToFetch(100);
        config.setPolitenessDelay(1500);
        config.setResumableCrawling(false);

        PageFetcher fetcher = new PageFetcher(config);

        RobotstxtConfig robotsConfig =
                new RobotstxtConfig();

        RobotstxtServer robotsServer =
                new RobotstxtServer(robotsConfig, fetcher);

        CrawlController controller =
                new CrawlController(
                        config,
                        fetcher,
                        robotsServer
                );

        controller.addSeed("https://books.toscrape.com/");

        System.out.println("Iniciando rastreo dominio web...");

        controller.start(CrawlerCETYS::new, 1);

        System.out.println("Rastreo finalizado.");
        
        InvertedIndex indice =
        InvertedIndex.getInstancia();

        System.out.println(
                "Documentos indexados: "
                + indice.totalDocumentos()
        );

        System.out.println(
                "Terminos diferentes: "
                + indice.totalTerminos()
        );

        indice.guardarEn(
                java.nio.file.Path.of("resultados")
        );

    }
}
