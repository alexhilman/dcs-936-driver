package com.alexhilman.dlink.dcs936.model;

import com.alexhilman.dlink.dcs936.Dcs936Client;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.alexhilman.dlink.dcs936.Dcs936Client.FILE_DATE_FORMAT;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class DcsFile {
    private final String cameraName;
    private final String parentPath;
    private final String fileName;
    private final DcsFileType fileType;
    private final Instant createdInstant;
    private final Dcs936Client dcs936Client;

    DcsFile(final String cameraName,
            final String parentPath,
            final String fileName,
            final DcsFileType fileType, final Dcs936Client dcs936Client) {
        this.cameraName = checkNotNull(cameraName, "cameraName cannot be null");
        this.parentPath = checkNotNull(parentPath, "parentPath cannot be null");
        this.fileName = checkNotNull(fileName, "fileName cannot be null");
        this.fileType = checkNotNull(fileType, "fileType cannot be null");
        this.dcs936Client = checkNotNull(dcs936Client, "dcs936Client cannot be null");
        if (fileType == DcsFileType.File) {
            this.createdInstant = LocalDateTime.parse(fileName.substring(0, 15), FILE_DATE_FORMAT)
                                               .atZone(ZoneId.systemDefault())
                                               .toInstant();
        } else {
            this.createdInstant = null;
        }
    }


    public String getFileName() {
        return fileName;
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

    public InputStream open() throws IOException {
        return dcs936Client.open(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DcsFile dcsFile = (DcsFile) o;

        if (!cameraName.equals(dcsFile.cameraName)) return false;
        if (!parentPath.equals(dcsFile.parentPath)) return false;
        if (!fileName.equals(dcsFile.fileName)) return false;
        if (fileType != dcsFile.fileType) return false;
        return createdInstant != null ? createdInstant.equals(dcsFile.createdInstant) : dcsFile.createdInstant == null;
    }

    @Override
    public int hashCode() {
        int result = cameraName.hashCode();
        result = 31 * result + parentPath.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + fileType.hashCode();
        result = 31 * result + (createdInstant != null ? createdInstant.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsFile{" +
                "cameraName='" + cameraName + '\'' +
                ", parentPath='" + parentPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", createdInstant=" + createdInstant +
                '}';
    }

    public String getAbsoluteFileName() {
        return cameraName + parentPath + getFileName();
    }

    public String getPathInCamera() {
        return parentPath + getFileName();
    }

    public Instant getCreatedInstant() {
        return createdInstant;
    }

    public static class Builder {
        private String cameraName;
        private String parentPath;
        private String fileName;
        private DcsFileType fileType;

        public String getCameraName() {
            return cameraName;
        }

        public Builder setCameraName(final String cameraName) {
            this.cameraName = cameraName;
            return this;
        }

        public String getParentPath() {
            return parentPath;
        }

        public Builder setParentPath(final String parentPath) {
            this.parentPath = parentPath;
            return this;
        }

        public String getFileName() {
            return fileName;
        }

        public Builder setFileName(final String fileName) {
            this.fileName = fileName;
            return this;
        }

        public DcsFileType getFileType() {
            return fileType;
        }

        public Builder setFileType(final DcsFileType fileType) {
            this.fileType = fileType;
            return this;
        }

        public DcsFile build(final Dcs936Client client) {
            return new DcsFile(cameraName, parentPath, fileName, fileType, client);
        }

        public boolean isDirectory() {
            return fileType == DcsFileType.Directory;
        }

        public boolean isFile() {
            return fileType == DcsFileType.File;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Builder builder = (Builder) o;

            if (cameraName != null ? !cameraName.equals(builder.cameraName) : builder.cameraName != null) return false;
            if (parentPath != null ? !parentPath.equals(builder.parentPath) : builder.parentPath != null) return false;
            if (fileName != null ? !fileName.equals(builder.fileName) : builder.fileName != null) return false;
            return fileType == builder.fileType;
        }

        @Override
        public int hashCode() {
            int result = cameraName != null ? cameraName.hashCode() : 0;
            result = 31 * result + (parentPath != null ? parentPath.hashCode() : 0);
            result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
            result = 31 * result + (fileType != null ? fileType.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "cameraName='" + cameraName + '\'' +
                    ", parentPath='" + parentPath + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", fileType=" + fileType +
                    '}';
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
