package ru.ifmo.ctddev.tolmachev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class is implementation of {@code HelloClient} which use like client which can sending and receiving UDP packets.
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class HelloUDPClient implements HelloClient {
    /**
     * Starts {@code HelloUDPClient}.
     * @param hostname {@code String} host which would be use to connect.
     * @param port {@code int} port which would be use to connect.
     * @param prefix {@code String} prefix of requests.
     * @param requests {@code int} count of requests.
     * @param threads {@code int} count of threads which would be use.
     */
    @Override
    public void start(String hostname, int port, String prefix, int requests, int threads) {
        final ExecutorService service = Executors.newFixedThreadPool(threads);

        try {
            final InetAddress address = InetAddress.getByName(hostname);

            for (int i = 0; i < threads; i++) {
                service.submit(new ClientWorker(i, address, port, prefix, requests));
            }

            service.shutdown();
            service.awaitTermination(1, TimeUnit.DAYS);
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            service.shutdownNow();
        }
    }
}
