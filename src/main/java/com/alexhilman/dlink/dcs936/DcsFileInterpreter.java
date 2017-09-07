package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.alexhilman.dlink.dcs936.model.DcsFileType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Interprets XML streams which the user would get when clicking through the terrible "SD Management" web interface. I
 * don't know of another integration point with this camera to get the files.
 */
public class DcsFileInterpreter {
    private List<DcsFile.Builder> interpret(final Document document) {
        checkNotNull(document, "document cannot be null");

        final NodeList cameraNameNodes = document.getElementsByTagName("cameraName");
        if (cameraNameNodes == null || cameraNameNodes.getLength() == 0) {
            throw new RuntimeException("No cameraName node found");
        }
        final String cameraName = cameraNameNodes.item(0).getTextContent();

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

        final NodeList folderPathNodes = playback.getElementsByTagName("folderpath");
        final String folderPath;
        if (folderPathNodes == null || folderPathNodes.getLength() == 0) {
            folderPath = "/";
        } else {
            folderPath = "/" + folderPathNodes.item(0).getTextContent();
        }

        final NodeList folderStringNodes = playback.getElementsByTagName("folderstring");
        if (folderStringNodes == null || folderStringNodes.getLength() == 0) {
            throw new RuntimeException("No folderstring nodes found");
        }
        final Element folderString = (Element) folderStringNodes.item(0);

        return parseFolderStringValue(cameraName, folderPath, folderString.getTextContent());
    }

    List<DcsFile.Builder> interpret(final InputStream stream) {
        return interpret(parseXmlStream(stream));
    }

    List<DcsFile.Builder> interpret(final String responseBody) {
        return interpret(parseXmlString(responseBody));
    }

    private List<DcsFile.Builder> parseFolderStringValue(final String cameraName, final String folderPath,
                                                         final String folderString) {
        assert folderString != null;
        final List<DcsFile.Builder> files = new ArrayList<>();
        final StringTokenizer stringTokenizer = new StringTokenizer(folderString, "*", false);
        while (stringTokenizer.hasMoreTokens()) {
            final String delimitedString = stringTokenizer.nextToken();

            final String[] split = delimitedString.split(":");
            checkArgument(split.length == 3, "invalid delimitedString format; expected x:y:z");

            files.add(DcsFile.builder()
                             .setCameraName(cameraName)
                             .setParentPath(toFolderPath(folderPath))
                             .setFileName(split[0])
                             .setFileType(DcsFileType.fromCharacter(split[1].charAt(0))));
        }
        return files;
    }

    private static String toFolderPath(final String path) {
        final StringBuilder newPath = new StringBuilder("/");

        if (path.startsWith("/")) {
            newPath.append(path.substring(1));
        } else {
            newPath.append(path);
        }

        if (newPath.charAt(newPath.length() - 1) == '/') {
            return newPath.toString();
        }
        return newPath.append("/").toString();
    }

    private Document parseXmlStream(final InputStream stream) {
        final DocumentBuilder documentBuilder = getDocumentBuilder();
        try {
            return documentBuilder.parse(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse XML document", e);
        }
    }

    private Document parseXmlString(final String xml) {
        return parseXmlStream(new ByteArrayInputStream(xml.getBytes()));
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Cannot parse XML documents", e);
        }
    }
}
