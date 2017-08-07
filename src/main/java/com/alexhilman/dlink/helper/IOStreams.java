package com.alexhilman.dlink.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class IOStreams {
    public static void redirect(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[4096];

        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }
}
