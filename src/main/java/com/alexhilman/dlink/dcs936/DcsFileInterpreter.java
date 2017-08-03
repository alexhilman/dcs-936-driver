package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Interprets XML streams which the user would get when clicking through the terrible "SD Management" web interface. I
 * don't know of another integration point with this camera to get the files.
 */
public class DcsFileInterpreter {
    public List<DcsFile> interpret(final InputStream stream) {
        final Document document = parseXmlStream(stream);

        final NodeList configNodes = document.getElementsByTagName("config");
        if (configNodes == null || configNodes.getLength() == 0) {
            throw new RuntimeException("No config node found");
        }
        final Element config = (Element) configNodes.item(0);

        final NodeList playbackNodes = config.getElementsByTagName("playback");
        if (playbackNodes == null || playbackNodes.getLength() == 0) {
            throw new RuntimeException("No playback node found");
        }
        final Element playback = (Element) playbackNodes.item(0);

        final NodeList folderStringNodes = playback.getElementsByTagName("folderstring");
        if (folderStringNodes == null || folderStringNodes.getLength() == 0) {
            throw new RuntimeException("No folderstring nodes found");
        }
        final Element folderString = (Element) folderStringNodes.item(0);

        return parseFolderStringValue(folderString.getTextContent());
    }

    private List<DcsFile> parseFolderStringValue(final String folderString) {
        assert folderString != null;
        final List<DcsFile> files = new ArrayList<>();
        final StringTokenizer stringTokenizer = new StringTokenizer(folderString, "*", false);
        while (stringTokenizer.hasMoreTokens()) {
            files.add(DcsFile.fromDelimitedString(stringTokenizer.nextToken()));
        }
        return files;
    }

    //20170801:d:-1*20170731:d:-1*20170730:d:-1*20170729:d:-1*20170728:d:-1*

    private Document parseXmlStream(final InputStream stream) {
        final DocumentBuilder documentBuilder = getDocumentBuilder();
        try {
            return documentBuilder.parse(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse XML document", e);
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Cannot parse XML documents", e);
        }
    }

}
