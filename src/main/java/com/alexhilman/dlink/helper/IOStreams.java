package com.alexhilman.dlink.helper;

import java.io.ByteArrayInputStream;
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

    public static InputStream copyToByteArrayInputStream(final InputStream inputStream) throws IOException {
        final byte[] buffer = new byte[4096];
        byte[] streamCopy = new byte[0];

        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
            if (bytesRead == 0) {
                continue;
            }

            final byte[] newStreamCopy = new byte[streamCopy.length + bytesRead];

            System.arraycopy(streamCopy, 0, newStreamCopy, 0, streamCopy.length);
            System.arraycopy(buffer, 0, newStreamCopy, streamCopy.length, bytesRead);

            streamCopy = newStreamCopy;
        }

        return new ByteArrayInputStream(streamCopy);
    }
}
