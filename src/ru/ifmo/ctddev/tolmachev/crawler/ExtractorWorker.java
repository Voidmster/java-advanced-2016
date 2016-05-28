package ru.ifmo.ctddev.tolmachev.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;

import java.io.IOException;
import java.util.List;

/**
 * This class is implementation of {@code Runnable} which use to extract all links from {@code Document}.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see Document
 */
public class ExtractorWorker implements Runnable {
    private final Document document;
    private final int maxDepth;
    private final int depth;
    private final WebCrawlerResources resources;
    private final WebCrawlerController controller;

    /**
     * Constructs {@code ExtractorWorker}.
     *
     * @param document  {@code Document} from which extract links.
     * @param depth     {@code int} current searching depth.
     * @param maxDepth  {@code int} current maximum depth.
     * @param resources {@code WebCrawlerResources} of {@code WebCrawler}.
     */
    public ExtractorWorker(Document document, int depth, int maxDepth, WebCrawlerResources resources, WebCrawlerController controller) {
        this.document = document;
        this.maxDepth = maxDepth;
        this.depth = depth;
        this.resources = resources;
        this.controller = controller;
        resources.getCounter().incrementAndGet();
    }

    /**
     * Runs extracting of links from given {@code Document}.
     */
    @Override
    public void run() {
        try {
            List<String> links = document.extractLinks();

            for (String link : links) {
                controller.getDownloadingService()
                        .submit(new DownloadWorker(link, depth, maxDepth, resources, controller));
            }

        } catch (IOException ignored) {

        } finally {
            resources.checkedDec();
        }
    }
}