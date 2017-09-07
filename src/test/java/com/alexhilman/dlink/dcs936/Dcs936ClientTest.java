package com.alexhilman.dlink.dcs936;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Dcs936ClientTest {
    @Test
    public void shouldGetBaseName() {
        assertThat(Dcs936Client.basename("/abc/123/"), is("123"));
        assertThat(Dcs936Client.basename("/abc/123"), is("123"));
        assertThat(Dcs936Client.basename("123"), is("123"));
        assertThat(Dcs936Client.basename("/123"), is("123"));
    }
}