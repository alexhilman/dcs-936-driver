package com.alexhilman.dlink;

import com.alexhilman.dlink.dcs936.Dcs936Client;
import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.alexhilman.dlink.inject.DcsModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

public class AppTest {
    private static final Logger LOG = LogManager.getLogger(AppTest.class);

    private static final Injector INJECTOR = Guice.createInjector(new DcsModule(testProperties()));
    private File downloadLocation;

    private static File testProperties() {
        return new File(AppTest.class.getResource("/com/alexhilman/dlink/dcs936/access.properties").getFile());
    }

    @Inject
    private Dcs936Client dcs936Client;

    @Before
    public void inject() {
        INJECTOR.injectMembers(this);

        final File userHome = new File(System.getProperty("user.home"));
        downloadLocation = new File(userHome, ".dcs936");
        if (!downloadLocation.exists()) {
            if (!downloadLocation.mkdir()) {
                throw new IllegalStateException("Could not create directory: " + downloadLocation.getAbsolutePath());
            }
        }
    }

    @Ignore
    @Test
    public void shouldGetFiles() {
        recurseForMovies("/");
    }

    private void recurseForMovies(final String path) {
        dcs936Client.list(path)
                    .forEach(f -> {
                        if (f.isDirectory()) {
                            recurseForMovies(f.getPathInCamera());
                        } else if (f.getAbsoluteFileName().endsWith(".mp4")) {
                            download(f, downloadLocation);
                        }
                    });
    }

    private void download(final DcsFile fileToDownload, final File parentFolder) {
        final File folder = new File(parentFolder, fileToDownload.getParentPath());
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + folder.getAbsolutePath());
            }
        }

        try {
            final File newFile = new File(folder, fileToDownload.getFileName());
            if (newFile.exists() && newFile.length() == fileToDownload.getSize()) {
                // all good, but this never gets hit... why? File size is different...
                LOG.info("File {} already exists and matches the camera file size",
                         fileToDownload.getAbsoluteFileName());
            } else {
                LOG.info("Downloading {} to {}", fileToDownload.getAbsoluteFileName(), parentFolder.getAbsolutePath());

                if (newFile.exists()) {
                    if (!newFile.delete()) {
                        throw new IllegalStateException("Could not delete file: " + newFile.getAbsolutePath());
                    }
                }

                if (!newFile.createNewFile()) {
                    throw new IllegalStateException("Could not create new file: " + newFile.getAbsolutePath());
                }

                try (final InputStream inStream = dcs936Client.open(fileToDownload);
                     final OutputStream outStream = new FileOutputStream(newFile)) {
                    IOStreams.redirect(inStream, outStream);
                }
                assert newFile.length() > 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class IOStreams {
        public static void redirect(final InputStream inputStream, final OutputStream outputStream) throws IOException {
            final byte[] buffer = new byte[4096];

            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

}

