package com.alexhilman.dlink.inject;

import com.alexhilman.dlink.dcs936.Client;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * TODO: update JavaDoc
 */
public class DcsModule extends AbstractModule {
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("dcs.username"))
                          .toInstance("admin");
        bind(String.class).annotatedWith(Names.named("dcs.password"))
                          .toInstance("");
        bind(URL.class).annotatedWith(Names.named("dcs.baseUrl"))
                       .toInstance(buildUrl("http://192.168.1.113/eng/admin/adv_sdcard.cgi"));
        bind(Client.class);
    }

    private URL buildUrl(final String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot start up: invalid base URL", e);
        }
    }
}
