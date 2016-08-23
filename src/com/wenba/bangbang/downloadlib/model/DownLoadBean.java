package com.wenba.bangbang.downloadlib.model;

/**
 * Created by silvercc on 16/7/7.
 */
public class DownLoadBean {
    private String userID;
    private String taskID;
    private String url;
    private String filePath;
    private String fileName;
    private long fileSize;
    private long downloadSize;
    private boolean isSupportBreakpoint;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public boolean isSupportBreakpoint() {
        return isSupportBreakpoint;
    }

    public void setIsSupportBreakpoint(boolean isSupportBreakpoint) {
        this.isSupportBreakpoint = isSupportBreakpoint;
    }
}
