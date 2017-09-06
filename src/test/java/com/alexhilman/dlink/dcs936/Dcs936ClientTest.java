package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.alexhilman.dlink.dcs936.model.DcsFileType;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Dcs936ClientTest {
    @Test
    public void shouldGetFileInstant() throws MalformedURLException {
        final Dcs936Client dcs936Client =
                new Dcs936Client(new AccessCredentials("", "", new URL("http://localhost")));

        Instant now = Instant.now();
        now = now.minusMillis(now.toEpochMilli() % 1000); // remove millis from timestamp

        final DcsFile newFile = new DcsFile("dummy",
                                            "/abc/123",
                                            now.atZone(ZoneId.systemDefault())
                                               .format(Dcs936Client.FILE_DATE_FORMAT) + "D.mp4",
                                            DcsFileType.File);

        final Instant fileInstant = dcs936Client.getFileInstant(newFile);

        assertThat(fileInstant, is(now));
    }
}