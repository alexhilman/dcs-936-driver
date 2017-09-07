package com.alexhilman.dlink;

import com.alexhilman.dlink.dcs936.Dcs936Client;
import com.alexhilman.dlink.inject.DcsModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

