package ru.ifmo.ctddev.tolmachev.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;

/**
 * This class is implementation of {@code Runnable} which use to sending and receiving UDP packets.
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class ClientWorker implements Runnable {
    private final int id;
    private final String prefix;
    private final int countOfReq;
    private final InetAddress address;
    private final int port;

    /**
     * Constructs {@code ClientWorker}.
     * @param id {@code int} id of worker.
     * @param address {@code InetAddress} address to connect.
     * @param port {@code int} port to connect.
     * @param prefix {@code String} prefix of requests.
     * @param countOfReq {@code int} count of request which would be send.
     */
    public ClientWorker(int id, InetAddress address, int port, String prefix, int countOfReq) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.prefix = prefix;
        this.countOfReq = countOfReq;
    }

    /**
     * Runs {@code ClientWorker}.
     */
    @Override
    public void run() {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(300);

                Charset charset = Charset.forName("UTF-8");
                byte[] buffer = new byte[socket.getSendBufferSize()];
                int counter = 0;

                while (counter != countOfReq) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    String line = prefix + id + "_" + counter;
                    byte[] message = line.getBytes(charset);
                    DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
//                    System.out.format("Send: %s\n", line);
                    socket.send(packet);

                    try {
                        packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        String response = new String(packet.getData(), 0, packet.getLength(), charset);

                        if (response.equals("Hello, " + line)) {
                            counter++;
                        }
//                        System.out.format("Send: %s\n", line);
                    } catch (SocketTimeoutException ignored) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
