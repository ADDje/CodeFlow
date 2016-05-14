package com.team10.codeflow;

import java.io.Serializable;

/**
 * Object for storing project information data, pretty self explanatory.
 * Created by Adam on 08/05/2016.
 */
public class ProjectInfo implements Serializable {

    private String title;
    private String date;
    private String desc;

    public ProjectInfo(String title, String date, String desc) {
        this.setTitle(title);
        this.setDate(date);
        this.setDesc(desc);
    }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return title; };

    public void setDate(String date) { this.date = date; }

    public String getDate() { return date; };

    public void setDesc(String desc) { this.desc = desc; }

    public String getDesc() { return desc; };

}
