package com.blogger.waller.room.table;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.blogger.waller.model.Wallpaper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

@Entity(tableName = "favorite")
public class EntityListing implements Serializable {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "published")
    public String published;

    @ColumnInfo(name = "updated")
    public String updated;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "link")
    public String link;

    @ColumnInfo(name = "image")
    public String image;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "saved_date")
    private Long saved_date;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getSaved_date() {
        return saved_date;
    }

    public void setSaved_date(Long saved_date) {
        this.saved_date = saved_date;
    }

    public static EntityListing entity(Wallpaper data) {
        EntityListing entity = new EntityListing();
        entity.setId(data.id);
        entity.setType(data.type);
        entity.setTitle(data.title);
        entity.setContent(data.content);
        entity.setLink(data.link);
        entity.setImage(data.image);
        entity.setPublished(data.published);
        entity.setUpdated(data.updated);
        entity.setCategory(new Gson().toJson(data.category));
        return entity;
    }

    public Wallpaper original() {
        Wallpaper l = new Wallpaper();
        l.id = getId();
        l.type = getType();
        l.title = getTitle();
        l.content = getContent();
        l.link = getLink();
        l.image = getImage();
        l.published = getPublished();
        l.updated = getUpdated();
        l.category = new Gson().fromJson(getCategory(), new TypeToken<List<String>>(){}.getType());
        return l;
    }
}
