package com.blogger.waller.model;

import java.io.Serializable;
import java.util.List;

public class Wallpaper implements Serializable {

    public String id;
    public String type;
    public String title;
    public String content;
    public String link;
    public String image;
    public boolean multiple;
    public boolean child;
    public List<String> category;
    public List<String> images;
    public String published;
    public String updated;

    public Wallpaper() {
    }

    public Wallpaper(Wallpaper w) {
        this.type = w.type;
        this.title = w.title;
        this.link = w.link;
        this.published = w.published;
        this.updated = w.updated;
        this.category = w.category;
        this.child = true;
    }

    public String getFilename() {
        return String.valueOf(id.hashCode());
    }
}
