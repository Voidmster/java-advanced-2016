package ru.ifmo.ctddev.tolmachev.walk;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by daniil on 13.02.16.
 */
public class MyFileVisitor extends SimpleFileVisitor<Path> {
    public static final String ZERO_STRING = "00000000000000000000000000000000 ";
    private OutputStreamWriter out;

    public MyFileVisitor(OutputStreamWriter out) {
        this.out = out;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();

            byte[] part = new byte[1024];
            int count;

            while ((count = inputStream.read(part)) != -1) {
                md.update(part, 0, count);
            }

            out.write(DatatypeConverter.printHexBinary(md.digest()) + " " + file.toString() + "\n");
        } catch (IOException e) {
            System.out.println("Error while calculate MD5 hash: " + file.toString() + e.getMessage());
            out.write(ZERO_STRING + file.toString() + "\n");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("MD5 not supported.");
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        out.write(ZERO_STRING + file.toString() + "\n");

        if (Files.notExists(file)) {
            System.out.println("No such file: " + file.toString());
        } else if (!Files.isReadable(file)) {
            System.out.println("File is not readable: " + file.toString() + exc.getMessage());
        } else {
            System.out.println("Visitor error: " + exc.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }
}