package com.alexhilman.dlink;

import com.alexhilman.dlink.inject.DcsModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 */
public class App {
    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        final Injector injector = Guice.createInjector(new DcsModule());


    }
}
