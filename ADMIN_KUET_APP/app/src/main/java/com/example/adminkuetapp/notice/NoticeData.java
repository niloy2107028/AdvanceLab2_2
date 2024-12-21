package com.example.adminkuetapp.notice;

public class NoticeData {
    private String title;
    private String fileUrl;
    private String date;
    private String time;
    private String uniqueKey;
    private String fileType;

    // Default constructor for Firebase
    public NoticeData() {
    }

    // Constructor to initialize the NoticeData object
    public NoticeData(String title, String fileUrl, String date, String time, String uniqueKey, String fileType) {
        this.title = title;
        this.fileUrl = fileUrl;
        this.date = date;
        this.time = time;
        this.uniqueKey = uniqueKey;
        this.fileType = fileType;
    }

    // Getter and Setter methods

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
