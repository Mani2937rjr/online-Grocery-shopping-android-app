package com.example.shop2order;

public class ModelAPdf {

    String uid,id,date,pdfUrl,timestamp1;

    public ModelAPdf() {
    }

    public ModelAPdf(String uid, String id, String date, String pdfUrl, String timestamp1) {
        this.uid = uid;
        this.id = id;
        this.date = date;
        this.pdfUrl = pdfUrl;
        this.timestamp1 = timestamp1;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getTimestamp1() {
        return timestamp1;
    }

    public void setTimestamp1(String timestamp1) {
        this.timestamp1 = timestamp1;
    }
}
