package ru.ifmo.ctddev.tolmachev.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;

/**
 * This class is implementation of {@code Runnable} which use to sending and receiving UDP packets.
 *
 * @author Tolmachev Daniil (Voidmaster)
 */
public class ServerWorker implements Runnable {
    private final DatagramSocket socket;

    /**
     * Constructs {@code ServerWorker}.
     * @param socket
     */
    public ServerWorker(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Runs {@code ServerWorker}.
     */
    @Override
    public void run() {
        try {
            byte buffer[] = new byte[socket.getReceiveBufferSize()];

            Charset charset = Charset.forName("UTF-8");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    System.out.println("Server waiting for packet");
                    socket.receive(packet);
                    System.out.println("Server got packet");

                    String data = ("Hello, " + new String(packet.getData(), 0, packet.getLength(), charset));
                    byte message[] = data.getBytes(charset);

                    DatagramPacket parcel = new DatagramPacket(message, message.length, packet.getAddress(), packet.getPort());
                    socket.send(parcel);
                    System.out.format("Server send %s\n", data);
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (IOException ignored) {}
    }
}
