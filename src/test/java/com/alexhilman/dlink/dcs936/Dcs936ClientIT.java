package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.GuiceTestInjectorRule;
import com.google.inject.Inject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class Dcs936ClientIT {
    @Rule
    public final GuiceTestInjectorRule guiceTestInjectorRule = GuiceTestInjectorRule.forTestSuite(this);

    @Inject
    private Dcs936Client dcs936Client;

    @Test
    @Ignore
    public void shouldGetDirectoryList() {
        dcs936Client.getRootDirectory();
    }
}