package com.example.leadp2pdirect.servers;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;

public class FileModel implements Serializable {
    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_PDF = 2;
    public static final int TYPE_APPLICATION = 3;
    public static final int TYPE_H5P = 4;
    public static final int TYPE_ZIP = 5;
    public static final int TYPE_DOC = 6;
    public static final int TYPE_COMMON = 7;

    private Long id;
    private File file;
    private String absoluteFilePath;
    private int type;
    private Uri uri;
    private Long fileLength;
    private String fileName;
    private String mimeType;
    private boolean isTransfered = false;

    public FileModel() {
    }

    public FileModel(File file) {
        this.file = file;
    }

    public FileModel(Uri uri, Long fileLength, String fileName) {
        this.uri = uri;
        this.fileLength = fileLength;
        this.fileName = fileName;
    }

    public FileModel(File file, int type) {
        this.file = file;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isTransfered() {
        return isTransfered;
    }

    public void setTransfered(boolean transfered) {
        isTransfered = transfered;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public void setAbsoluteFilePath(String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
