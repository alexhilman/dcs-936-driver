package com.alexhilman.dlink.dcs936.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class DcsFile {
    private final String parentPath;
    private final String fileName;
    private final DcsFileType fileType;
    private final int size;

    DcsFile(final String parentPath,
            final String fileName,
            final DcsFileType fileType,
            final int size) {
        this.parentPath = checkNotNull(parentPath, "parentPath cannot be null");
        this.fileName = checkNotNull(fileName, "fileName cannot be null");
        this.fileType = checkNotNull(fileType, "fileType cannot be null");
        this.size = size;
    }

    public static DcsFile fromDelimitedString(final String parentPath, final String delimitedString) {
        checkNotNull(parentPath, "parentPath cannot be null");
        checkNotNull(delimitedString, "delimitedString cannot be null");

        final String[] split = delimitedString.split(":");
        checkArgument(split.length == 3, "invalid delimitedString format; expected x:y:z");

        return new DcsFile(parentPath, split[0], DcsFileType.fromCharacter(split[1].charAt(0)), Integer.parseInt(split[2]));
    }

    public String getFileName() {
        return fileName;
    }

    public int getSize() {
        return size;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DcsFile dcsFile = (DcsFile) o;

        if (size != dcsFile.size) return false;
        if (!parentPath.equals(dcsFile.parentPath)) return false;
        if (!fileName.equals(dcsFile.fileName)) return false;
        return fileType == dcsFile.fileType;
    }

    @Override
    public int hashCode() {
        int result = parentPath.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + fileType.hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "DcsFile{" +
                "parentPath='" + parentPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", size=" + size +
                '}';
    }
}
