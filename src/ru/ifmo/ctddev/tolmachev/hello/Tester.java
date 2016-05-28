package ru.ifmo.ctddev.tolmachev.hello;

import java.io.IOException;

public class Tester {
    public static void main(String[] args) throws IOException {
        HelloUDPServer server = new HelloUDPServer();
        HelloUDPClient client = new HelloUDPClient();
        server.start(8080, 1);
        client.start("localhost", 8080, "NU,,U,UIIMkkkk9ллл99л", 5, 2);
    }
}
