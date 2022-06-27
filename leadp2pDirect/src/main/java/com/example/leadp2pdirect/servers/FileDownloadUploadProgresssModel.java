package com.example.leadp2pdirect.servers;

public class FileDownloadUploadProgresssModel {
    public enum SendingOrReceiving {
        Sending,
        Receiving;
    }

    private String fileName;
    private String sendingOrReceiving;
    private long dataIncrement;
    private long totalProgress;
    private long fileLength;
    private int progressPercentage;

    public FileDownloadUploadProgresssModel() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDataIncrement() {
        return dataIncrement;
    }

    public void setDataIncrement(long dataIncrement) {
        this.dataIncrement = dataIncrement;
    }

    public long getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(long totalProgress) {
        this.totalProgress = totalProgress;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getSendingOrReceiving() {
        return sendingOrReceiving;
    }

    public void setSendingOrReceiving(String sendingOrReceiving) {
        this.sendingOrReceiving = sendingOrReceiving;
    }
}
