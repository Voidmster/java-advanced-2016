package ru.ifmo.ctddev.tolmachev.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.Semaphore;

/**
 * This class is implementation of {@code Runnable} which use to download {@code Document} from given link.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see Document
 */
public class DownloadWorker implements Runnable {
    private final String url;
    private final int depth;
    private final int maxDepth;
    private final WebCrawlerResources resources;
    private final WebCrawlerController controller;

    /**
     * Constructs {@code DownloadWorker}.
     *
     * @param url       {@code String} given {@code URL}.
     * @param depth     {@code int} current searching depth.
     * @param maxDepth  {@code int} current maximum depth.
     * @param resources {@code WebCrawlerResources} of {@code WebCrawler}.
     */
    public DownloadWorker(String url, int depth, int maxDepth, WebCrawlerResources resources, WebCrawlerController controller) {
        this.url = url;
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.resources = resources;
        this.controller = controller;
        resources.getCounter().incrementAndGet();
    }

    private void mainPart() throws IOException {
        Document document = controller.download(url);
        release(url);

        if (depth < maxDepth) {
            controller.getExtractingService().submit(new ExtractorWorker(document, depth + 1, maxDepth, resources, controller));
        }
    }

    /**
     * Runs downloading of {@code Document} from given link.
     */
    @Override
    public void run() {
        try {
            int flag = 0;
            synchronized (resources.getDownloadedLinks()) {
                if (!resources.getDownloadedLinks().contains(url)) {
                    if (tryAcquire(url)) {
                        resources.getDownloadedLinks().add(url);
                        flag = 2;
                    } else {
                        flag = 1;
                    }
                }
            }

            if (flag == 2) {
                mainPart();
            } else if (flag == 1) {
                controller.getDownloadingService()
                        .submit(new DownloadWorker(url, depth, maxDepth, resources, controller));
            }
        } catch (IOException e) {
            resources.getDownloadedLinks().add(url);
            resources.getErrors().put(url, e);
        } finally {
            resources.checkedDec();
        }
    }

    private boolean tryAcquire(String url) throws MalformedURLException {
        String host = URLUtils.getHost(url);

        synchronized (resources.getHosts()) {
            resources.getHosts().putIfAbsent(host, new Semaphore(controller.getPerHost()));
        }

        return resources.getHosts().get(host).tryAcquire();
    }

    private void acquire(String url) throws MalformedURLException {
        try {
            String host = URLUtils.getHost(url);

            synchronized (resources.getHosts()) {
                resources.getHosts().putIfAbsent(host, new Semaphore(controller.getPerHost()));
            }

            resources.getHosts().get(host).acquire();
        } catch (InterruptedException ignored) {

        }
    }

    private void release(String url) throws MalformedURLException {
        synchronized (resources.getHosts()) {
            resources.getHosts().get(URLUtils.getHost(url)).release();
        }
    }
}