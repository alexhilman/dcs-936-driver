package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.GuiceTestInjectorRule;
import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.alexhilman.dlink.helper.IOStreams;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Dcs936ClientIT {
    private static final Logger LOG = LogManager.getLogger(Dcs936ClientIT.class);

    @Rule
    public final GuiceTestInjectorRule guiceTestInjectorRule = GuiceTestInjectorRule.forTestSuite(this);

    @Inject
    private Dcs936Client dcs936Client;

    @Test
    @Ignore
    public void shouldDrillDownToMovieFiles() {
        final List<DcsFile> dateFolders = dcs936Client.list("/");
        assertThat(dateFolders, is(notNullValue()));
        assertThat(dateFolders, hasSize(greaterThan(1)));

        final DcsFile firstDateFolder = dateFolders.get(0);
        final List<DcsFile> hourFolders = dcs936Client.list(firstDateFolder);
        assertThat(hourFolders, is(notNullValue()));
        assertThat(hourFolders, hasSize(greaterThan(0)));

        final DcsFile firstHourFolder = hourFolders.get(0);
        final List<DcsFile> movieFiles = dcs936Client.list(firstHourFolder);
        assertThat(movieFiles, is(notNullValue()));
        assertThat(movieFiles, hasSize(greaterThan(0)));
        movieFiles.forEach(file -> {
            assertThat(file.getFileName(), anyOf(endsWith(".jpg"), endsWith(".mp4")));
        });
    }

    @Test
    @Ignore
    public void shouldGetSizeForFile() {
        final List<DcsFile> dateFolders = dcs936Client.list("/");
        assertThat(dateFolders, is(notNullValue()));
        assertThat(dateFolders, hasSize(greaterThan(1)));

        final DcsFile firstDateFolder = dateFolders.get(0);
        final List<DcsFile> hourFolders = dcs936Client.list(firstDateFolder);
        assertThat(hourFolders, is(notNullValue()));
        assertThat(hourFolders, hasSize(greaterThan(0)));

        final DcsFile firstHourFolder = hourFolders.get(0);
        final List<DcsFile> movieFiles = dcs936Client.list(firstHourFolder);
        assertThat(movieFiles, is(notNullValue()));
        assertThat(movieFiles, hasSize(greaterThan(0)));
        movieFiles.forEach(file -> {
            dcs936Client.requestSize(file);
            assertThat(file.getSize(), is(greaterThan(0)));
        });
    }

    @Test
    @Ignore
    public void shouldOpenFile() throws IOException {
        final File tempFile = File.createTempFile("dcs-936-", ".mp4");
        assertThat(tempFile.exists(), is(true));

        final List<DcsFile> dateFolders = dcs936Client.list("/");
        final List<DcsFile> hourFolders = dcs936Client.list(dateFolders.get(0));
        final List<DcsFile> movieFiles = dcs936Client.list(hourFolders.get(0));

        final List<DcsFile> mp4Files = movieFiles.stream()
                                                 .filter(file -> file.getFileName().endsWith(".mp4"))
                                                 .collect(Collectors.toList());

        assertThat(mp4Files, hasSize(greaterThan(0)));

        final DcsFile dcsFile = mp4Files.get(0);
        System.out.println("Attempting to download " + dcsFile.getAbsoluteFileName());

        try (final InputStream inputStream = dcs936Client.open(dcsFile)) {
            assertThat(inputStream, is(notNullValue()));
            try (final FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                IOStreams.redirect(inputStream, outputStream);
            }
        }

        System.out.println("Movie file: " + tempFile.getAbsolutePath());
    }

    @Test
    public void shouldFindNewFilesSinceInstant() {
        final List<DcsFile> files = dcs936Client.findNewMoviesSince(Instant.now().minus(3, ChronoUnit.HOURS));

        assertThat(files, is(notNullValue()));
        assertThat(files, hasSize(greaterThanOrEqualTo(0)));
    }
}