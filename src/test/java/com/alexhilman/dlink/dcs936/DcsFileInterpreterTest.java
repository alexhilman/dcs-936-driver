package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DcsFileInterpreterTest {
    private static final String rootStructureResource = "/com/alexhilman/dlink/dcs936/samples/root-structure.xml";
    private static final String secondaryStructureResource = "/com/alexhilman/dlink/dcs936/samples/secondary-structure.xml";

    private final DcsFileInterpreter dcsFileInterpreter = new DcsFileInterpreter();

    @Test
    public void shouldReadRootFolderStructure() {
        try (final InputStream rootStructureStream = getClass().getResourceAsStream(rootStructureResource)) {
            assertThat(rootStructureStream, is(notNullValue()));

            final List<DcsFile> dcsFiles = dcsFileInterpreter.interpret(rootStructureStream);

            assertThat(dcsFiles, is(notNullValue()));
            assertThat(dcsFiles, hasSize(5));

            final DcsFile firstFile = dcsFiles.get(0);
            final DcsFile secondFile = dcsFiles.get(1);
            final DcsFile thirdFile = dcsFiles.get(2);
            final DcsFile fourthFile = dcsFiles.get(3);
            final DcsFile fifthFile = dcsFiles.get(4);

            assertThat(firstFile.getFileName(), is("20170801"));
            assertThat(secondFile.getFileName(), is("20170731"));
            assertThat(thirdFile.getFileName(), is("20170730"));
            assertThat(fourthFile.getFileName(), is("20170729"));
            assertThat(fifthFile.getFileName(), is("20170728"));

            assertThat(firstFile.isDirectory(), is(true));
            assertThat(secondFile.isDirectory(), is(true));
            assertThat(thirdFile.isDirectory(), is(true));
            assertThat(fourthFile.isDirectory(), is(true));
            assertThat(fifthFile.isDirectory(), is(true));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldReadSecondaryFolderStructure() {
        try (final InputStream rootStructureStream = getClass().getResourceAsStream(secondaryStructureResource)) {
            assertThat(rootStructureStream, is(notNullValue()));

            final List<DcsFile> dcsFiles = dcsFileInterpreter.interpret(rootStructureStream);

            assertThat(dcsFiles, is(notNullValue()));
            assertThat(dcsFiles, hasSize(5));

            final DcsFile firstFile = dcsFiles.get(0);
            final DcsFile secondFile = dcsFiles.get(1);
            final DcsFile thirdFile = dcsFiles.get(2);
            final DcsFile fourthFile = dcsFiles.get(3);
            final DcsFile fifthFile = dcsFiles.get(4);

            assertThat(firstFile.getFileName(), is("21"));
            assertThat(secondFile.getFileName(), is("20"));
            assertThat(thirdFile.getFileName(), is("19"));
            assertThat(fourthFile.getFileName(), is("18"));
            assertThat(fifthFile.getFileName(), is("17"));

            assertThat(firstFile.isDirectory(), is(true));
            assertThat(secondFile.isDirectory(), is(true));
            assertThat(thirdFile.isDirectory(), is(true));
            assertThat(fourthFile.isDirectory(), is(true));
            assertThat(fifthFile.isDirectory(), is(true));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}