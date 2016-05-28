package ru.ifmo.ctddev.tolmachev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Bad args");
            return;
        }

        Path inputFilePath = Paths.get(args[0]);

        try (BufferedReader reader = Files.newBufferedReader(inputFilePath, StandardCharsets.UTF_8)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8)) {
                String line = null;
                while (true) {
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        System.out.println("Error while reading input file");
                    }
                    if (line == null) break;
                    Files.walkFileTree(Paths.get(line), new MyFileVisitor(writer));
                }
            } catch (FileNotFoundException e) {
                System.out.println("Can't open output file");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error with output: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Can't open input file");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error with input: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
