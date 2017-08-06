package com.alexhilman.dlink.inject;

import com.alexhilman.dlink.dcs936.AccessCredentials;
import com.alexhilman.dlink.dcs936.Dcs936Client;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: update JavaDoc
 */
public class DcsModule extends AbstractModule {
    private final Properties properties;

    public DcsModule(final File propertiesFile) {
        this(readPropertiesFrom(propertiesFile));
    }

    public DcsModule(final Properties properties) {
        checkNotNull(properties.getProperty("dcs936.username"), "missing property: dcs936.username");
        checkNotNull(properties.getProperty("dcs936.password"), "missing property: dcs936.password");
        checkNotNull(properties.getProperty("dcs936.endpoint"), "missing property: dcs936.endpoint");
        this.properties = checkNotNull(properties, "properties cannot be null");
    }

    private static Properties readPropertiesFrom(final File propertiesFile) {
        checkNotNull(propertiesFile, "propertiesFile cannot be null");
        checkArgument(propertiesFile.exists(), "propertiesFile must exist");

        final Properties properties = new Properties();
        try (final FileInputStream stream = new FileInputStream(propertiesFile)) {
            properties.load(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load properties from " + propertiesFile.getAbsolutePath(), e);
        }
        return properties;
    }

    protected void configure() {
        bind(AccessCredentials.class).toInstance(
                new AccessCredentials(properties.getProperty("dcs936.username"),
                                      properties.getProperty("dcs936.password"),
                                      sdCardEndpoint())
        );
        bind(String.class).annotatedWith(Names.named("dcs936.username"))
                          .toInstance("admin");
        bind(String.class).annotatedWith(Names.named("dcs936.password"))
                          .toInstance("");
        bind(URL.class).annotatedWith(Names.named("dcs936.baseUrl"))
                       .toInstance(buildUrl("http://192.168.1.113/eng/admin/adv_sdcard.cgi"));
        bind(Dcs936Client.class);
    }

    private URL sdCardEndpoint() {
        final String endpoint = properties.getProperty("dcs936.endpoint");
        try {
            return new URL(new URL(endpoint), "/eng/admin/adv_sdcard.cgi");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed endpoint URL: " + endpoint, e);
        }
    }

    private URL buildUrl(final String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot start up: invalid base URL", e);
        }
    }
}
