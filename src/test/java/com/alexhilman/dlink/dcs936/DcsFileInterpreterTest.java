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
    private static final String tertiaryStructureResource = "/com/alexhilman/dlink/dcs936/samples/tertiary-structure-files.xml";

    private final DcsFileInterpreter dcsFileInterpreter = new DcsFileInterpreter();

    @Test
    public void shouldReadRootFolderStructure() {
        try (final InputStream stream = getClass().getResourceAsStream(rootStructureResource)) {
            assertThat(stream, is(notNullValue()));

            final List<DcsFile> dcsFiles = dcsFileInterpreter.interpret(stream);

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

            dcsFiles.forEach(file -> {
                assertThat(file.getParentPath(), is("/"));
                assertThat(file.isDirectory(), is(true));
            });
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldReadSecondaryFolderStructure() {
        try (final InputStream stream = getClass().getResourceAsStream(secondaryStructureResource)) {
            assertThat(stream, is(notNullValue()));

            final List<DcsFile> dcsFiles = dcsFileInterpreter.interpret(stream);

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

            dcsFiles.forEach(file -> {
                assertThat(file.isDirectory(), is(true));
                assertThat(file.getParentPath(), is("/20170801"));
            });
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldReadTertiaryStructureWhichHasFiles() {
        try (final InputStream stream = getClass().getResourceAsStream(tertiaryStructureResource)) {
            assertThat(stream, is(notNullValue()));

            final List<DcsFile> dcsFiles = dcsFileInterpreter.interpret(stream);

            assertThat(dcsFiles, is(notNullValue()));
            assertThat(dcsFiles, hasSize(5));

            final DcsFile firstFile = dcsFiles.get(0);
            final DcsFile secondFile = dcsFiles.get(1);
            final DcsFile thirdFile = dcsFiles.get(2);
            final DcsFile fourthFile = dcsFiles.get(3);
            final DcsFile fifthFile = dcsFiles.get(4);

            assertThat(firstFile.getFileName(), is("20170801_215228D.mp4"));
            assertThat(secondFile.getFileName(), is("20170801_215228D.jpg"));
            assertThat(thirdFile.getFileName(), is("20170801_214814D.mp4"));
            assertThat(fourthFile.getFileName(), is("20170801_214814D.jpg"));
            assertThat(fifthFile.getFileName(), is("20170801_214522D.mp4"));

            dcsFiles.forEach(file -> {
                assertThat(file.isFile(), is(true));
                assertThat(file.getParentPath(), is("/20170801/21"));
            });
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}