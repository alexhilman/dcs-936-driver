package com.alexhilman.dlink;

import com.alexhilman.dlink.inject.DcsModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * TODO: update JavaDoc
 */
public class GuiceTestInjectorRule implements TestRule {
    private static volatile Injector injector;
    private Object testSuite;

    public GuiceTestInjectorRule(final Object testSuite) {
        this.testSuite = testSuite;
    }

    public static GuiceTestInjectorRule forTestSuite(final Object testSuite) {
        return new GuiceTestInjectorRule(testSuite);
    }

    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                getInjector().injectMembers(testSuite);
                base.evaluate();
            }
        };
    }

    public Injector getInjector() {
        Injector injector = GuiceTestInjectorRule.injector;
        if (injector == null) {
            synchronized (getClass()) {
                injector = GuiceTestInjectorRule.injector;

                if (injector == null) {
                    injector = GuiceTestInjectorRule.injector = Guice.createInjector(new DcsModule());
                }
            }
        }
        return injector;
    }
}
