package ru.ifmo.ctddev.tolmachev.crawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class contains all {@code WebCrawler} resources.
 *
 * @author Tolmachev Daniil (Voidmaster)
 * @see WebCrawler
 */
public class WebCrawlerResources {
    private ConcurrentHashMap<String, IOException> errors;
    private ConcurrentSkipListSet<String> downloadedLinks;
    private Map<String, Semaphore> hosts;
    private ReentrantLock locker;
    private Condition isDone;
    private AtomicInteger activeTasks;

    /**
     * Constructs {@code WebCrawlerResources}.
     */
    public WebCrawlerResources() {
        downloadedLinks = new ConcurrentSkipListSet<>();
        errors = new ConcurrentHashMap<>();
        hosts = new HashMap<>();
        locker = new ReentrantLock();
        isDone = locker.newCondition();
        activeTasks = new AtomicInteger();
    }

    /**
     * Returns {@code ReentrantLock} which used to control process.
     *
     * @return {@code ReentrantLock} which used to control process.
     * @see ReentrantLock
     */
    public ReentrantLock getLocker() {
        return locker;
    }

    /**
     * Returns counter of active tasks.
     *
     * @return {@code AtomicInteger} counter of active tasks.
     * @see AtomicInteger
     */
    public AtomicInteger getCounter() {
        return activeTasks;
    }

    /**
     * Returns {@code Condition} which used to control process.
     *
     * @return {@code Condition} which used to control process.
     * @see Condition
     */
    public Condition getIsDone() {
        return isDone;
    }

    /**
     * Returns {@code ConcurrentHashMap} which used to control errors in process.
     *
     * @return {@code ConcurrentHashMap} which used to control errors in process.
     * @see ConcurrentHashMap
     */
    public ConcurrentHashMap<String, IOException> getErrors() {
        return errors;
    }

    /**
     * Returns {@code ConcurrentSkipListSet} which contains all downloaded links.
     *
     * @return {@code ConcurrentSkipListSet} which contains all downloaded links.
     * @see ConcurrentSkipListSet
     */
    public ConcurrentSkipListSet<String> getDownloadedLinks() {
        return downloadedLinks;
    }

    /**
     * Returns {@code Map} which used to control count of downloaders in process.
     *
     * @return {@code Map} which used to control count of downloaders in process.
     * @see Map
     */
    public Map<String, Semaphore> getHosts() {
        return hosts;
    }

    /**
     * Decrements and checks if result is ready.
     */
    public void checkedDec() {
        if (activeTasks.decrementAndGet() == 0) {
            try {
                locker.lock();
                isDone.signal();
            } finally {
                locker.unlock();
            }
        }
    }
}
