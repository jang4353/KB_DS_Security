package com.android.insecurebankv2;

import java.util.Date;

public class TalkList_item {
    private int bid;
    private String title;
    private String content;
    private String date;
    private String username;


    public TalkList_item(String title, String content, String date, String username) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.username = username;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
