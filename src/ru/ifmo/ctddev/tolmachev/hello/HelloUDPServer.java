package ru.ifmo.ctddev.tolmachev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is implementation of {@code HelloServer} which use like server which can sending and receiving UDP packets.
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class HelloUDPServer implements HelloServer{
    private final Map<Integer, ExecutorService> services;
    private final List<DatagramSocket> sockets;

    /**
     * Constructs {@code HelloUDPServer}.
     */
    public HelloUDPServer() {
        services = new HashMap<>();
        sockets = new ArrayList<>();
    }

    /**
     * Starts {@code HelloUDPServer}.
     * @param port {@code int} port which would be use to connect.
     * @param threads {@code int} count of threads.
     */
    @Override
    public void start(int port, int threads) {
        if (services.containsKey(port)) {
            throw new IllegalStateException("Server already bind on this port");
        }

        try {
            sockets.add(new DatagramSocket(port, InetAddress.getByName("localhost")));
            sockets.get(sockets.size() - 1).setSoTimeout(300);
            services.put(port, Executors.newFixedThreadPool(threads));

            for (int i = 0; i < threads; i++) {
                services.get(port).submit(new ServerWorker(sockets.get(sockets.size() - 1)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Stops {@code HelloUDPServer}.
     */
    @Override
    public void close() {
        services.values().forEach(ExecutorService::shutdownNow);
        sockets.forEach(DatagramSocket::close);
    }
}
