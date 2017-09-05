package com.alexhilman.dlink.dcs936.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class DcsFile {
    private final String cameraName;
    private final String parentPath;
    private final String fileName;
    private final DcsFileType fileType;
    private volatile int size = -1;

    DcsFile(final String cameraName,
            final String parentPath,
            final String fileName,
            final DcsFileType fileType) {
        this.cameraName = cameraName;
        this.parentPath = checkNotNull(parentPath, "parentPath cannot be null");
        this.fileName = checkNotNull(fileName, "fileName cannot be null");
        this.fileType = checkNotNull(fileType, "fileType cannot be null");
    }

    public static DcsFile fromDelimitedString(final String cameraName,
                                              final String parentPath,
                                              final String delimitedString) {
        checkNotNull(cameraName, "cameraName cannot be null");
        checkNotNull(parentPath, "parentPath cannot be null");
        checkNotNull(delimitedString, "delimitedString cannot be null");

        final String[] split = delimitedString.split(":");
        checkArgument(split.length == 3, "invalid delimitedString format; expected x:y:z");

        return new DcsFile(cameraName,
                           toFolderPath(parentPath),
                           split[0],
                           DcsFileType.fromCharacter(split[1].charAt(0)));
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

    public String getFileName() {
        return fileName;
    }

    public int getSize() {
        return size;
    }

    public DcsFile setSize(final int size) {
        this.size = size;
        return this;
    }

    public DcsFileType getFileType() {
        return fileType;
    }

    public boolean isDirectory() {
        return fileType == DcsFileType.Directory;
    }

    public boolean isFile() {
        return fileType == DcsFileType.File;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getCameraName() {
        return cameraName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DcsFile dcsFile = (DcsFile) o;

        if (size != dcsFile.size) return false;
        if (!cameraName.equals(dcsFile.cameraName)) return false;
        if (!parentPath.equals(dcsFile.parentPath)) return false;
        if (!fileName.equals(dcsFile.fileName)) return false;
        return fileType == dcsFile.fileType;
    }

    @Override
    public int hashCode() {
        int result = cameraName.hashCode();
        result = 31 * result + parentPath.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + fileType.hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "DcsFile{" +
                "cameraName='" + cameraName + '\'' +
                ", parentPath='" + parentPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", size=" + size +
                '}';
    }

    public String getAbsoluteFileName() {
        return cameraName + parentPath + getFileName();
    }

    public String getPathInCamera() {
        return parentPath + getFileName();
    }
}
