package ru.ifmo.ctddev.tolmachev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


/**
 * This class recursively downloads links with given restrictions.
 * <p>
 * This class uses {@code WebCrawlerResources}, {@code DownloadWorker}, {@code ExtractWorker} to download
 * links links with restrictions : depth of links extracting, threads per host.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see ru.ifmo.ctddev.tolmachev.crawler.WebCrawlerResources
 * @see ru.ifmo.ctddev.tolmachev.crawler.ExtractorWorker
 * @see ru.ifmo.ctddev.tolmachev.crawler.DownloadWorker
 */
public class WebCrawler implements Crawler {
    private WebCrawlerController controller;

    private static final String format = "WebCrawler url [downloads [extractors [perHost]]]";

    /**
     * Constructs and runs {@code WebCrawler} with given parameters.
     * <p>
     * Format of {@code args} should be : WebCrawler url [downloads [extractors [perHost]]].
     *
     * @param args {@code String} array with parameters.
     */
    public static void main(String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).filter(x -> x == null).count() > 0) {
            System.out.println("Use format : " + format);
        } else {
            try {
                Downloader downloader = new CachingDownloader(new File("./temp"));
                WebCrawler crawler = new WebCrawler(downloader, Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                crawler.download(args[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs {@code WebCrawler}.
     *
     * @param downloader  {@code Downloader} which will be used to download links.
     * @param downloaders {@code int} count of downloading threads.
     * @param extractors  {@code int} count of extracting threads.
     * @param perHost     {@code int} restriction of downloaders per host.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        controller = new WebCrawlerController(downloader, downloaders, extractors, perHost);
    }

    /**
     * Downloads links from given {@code URL} representing as a {@code String} with given depth.
     *
     * @param url   {@code String} representation of {@code URL}
     * @param depth {@code int} maximum depth of searching.
     * @return {@code Result} {@code List} of downloaded links.
     */
    @Override
    public Result download(String url, int depth) {
        final WebCrawlerResources resources = new WebCrawlerResources();

        resources.getLocker().lock();
        try {
            controller.getDownloadingService()
                    .submit(new DownloadWorker(url, 1, depth, resources, controller));

            while (resources.getCounter().get() > 0) {
                resources.getIsDone().await();
            }
        } catch (InterruptedException ignored) {

        } finally {
            resources.getLocker().unlock();
        }

        resources.getDownloadedLinks().removeAll(resources.getErrors().keySet());
        return new Result(new ArrayList<>(resources.getDownloadedLinks()), resources.getErrors());
    }

    /**
     * Stops services which load and extract links.
     */
    @Override
    public void close() {
        controller.getDownloadingService().shutdown();
        controller.getExtractingService().shutdown();
    }
}
