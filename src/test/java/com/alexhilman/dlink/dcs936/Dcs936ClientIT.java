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
    public void shouldGetDirectoryList() {
        final List<DcsFile> rootFiles = dcs936Client.getRootFiles();

        assertThat(rootFiles, is(notNullValue()));
        assertThat(rootFiles, hasSize(greaterThan(0)));
    }
}