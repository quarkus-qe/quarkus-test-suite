package io.quarkus.ts.http.advanced.reactive;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Brotli4JCompressionUtil {
    public static void compressAFile(String inputFilePath, String outFilePath) throws IOException {
        // Load the native library
        Brotli4jLoader.ensureAvailability();

        FileInputStream inFile = new FileInputStream(inputFilePath);
        FileOutputStream outFile = new FileOutputStream(outFilePath + ".br");

        Encoder.Parameters params = new Encoder.Parameters().setQuality(4);

        BrotliOutputStream brotliOutputStream = new BrotliOutputStream(outFile, params);

        int read = inFile.read();
        while (read > -1) {
            brotliOutputStream.write(read);
            read = inFile.read();
        }

        // Close the BrotliOutputStream. This also closes the FileOutputStream.
        brotliOutputStream.close();
        inFile.close();
    }

    public static void deCompressAFile(String compressedFilePath, String decompressedFilePath) throws IOException {
        // Load the native library
        Brotli4jLoader.ensureAvailability();

        FileInputStream inFile = new FileInputStream(compressedFilePath);
        FileOutputStream outFile = new FileOutputStream(decompressedFilePath);

        BrotliInputStream brotliInputStream = new BrotliInputStream(inFile);

        int read = brotliInputStream.read();
        while (read > -1) {
            outFile.write(read);
            read = brotliInputStream.read();
        }

        // Close the BrotliInputStream. This also closes the FileInputStream.
        brotliInputStream.close();
        outFile.close();
    }
}