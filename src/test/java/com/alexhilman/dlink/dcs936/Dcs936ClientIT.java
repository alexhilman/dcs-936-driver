package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.GuiceTestInjectorRule;
import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.google.inject.Inject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Dcs936ClientIT {
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
}