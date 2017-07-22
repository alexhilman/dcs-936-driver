package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.GuiceTestInjectorRule;
import com.google.inject.Inject;
import org.junit.Rule;
import org.junit.Test;

public class ClientIT {
    @Rule
    public final GuiceTestInjectorRule guiceTestInjectorRule = GuiceTestInjectorRule.forTestSuite(this);

    @Inject
    private Client client;

    @Test
    public void shouldGetDirectoryList() {
        client.getRootDirectory();
    }
}