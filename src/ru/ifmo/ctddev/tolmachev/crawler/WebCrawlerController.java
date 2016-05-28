package ru.ifmo.ctddev.tolmachev.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class contains control resources of {@code WebCrawler}.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see WebCrawler
 */
public class WebCrawlerController {
    private final ExecutorService downloadingService;
    private final ExecutorService extractingService;
    private final Downloader downloader;
    private final int perHost;

    /**
     * Constructs {@code WebCrawlerController}.
     *
     * @param downloader  {@code Downloader} which uses for downloading links.
     * @param downloaders {@code int} count of downloading threads.
     * @param extractors  {@code int} count of extracting threads.
     * @param perHost     {@code int} restriction of downloaders per host.
     */
    public WebCrawlerController(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloadingService = Executors.newFixedThreadPool(downloaders);
        extractingService = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
        this.perHost = perHost;
    }

    /**
     * Returns restriction of downloaders per host.
     *
     * @return {@code int} restriction of downloaders per host.
     */
    public int getPerHost() {
        return perHost;
    }

    /**
     * Downloads {@code Document} from given {@code URL} by using {@code Downloader}.
     *
     * @param url {@code String} given {@code URL}.
     * @return {@code Document} which was downloaded.
     * @throws IOException if an error occurred.
     */
    public Document download(String url) throws IOException {
        return downloader.download(url);
    }

    /**
     * Returns {@code ExecutorService} which contains all downloading threads.
     *
     * @return {@code ExecutorService} which contains all downloading threads.
     * @see ExecutorService
     */
    public ExecutorService getDownloadingService() {
        return downloadingService;
    }

    /**
     * Returns {@code ExecutorService} which contains all extracting threads.
     *
     * @return {@code ExecutorService} which contains all extracting threads.
     * @see ExecutorService
     */
    public ExecutorService getExtractingService() {
        return extractingService;
    }
}
