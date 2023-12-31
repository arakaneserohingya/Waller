package com.blogger.waller.room.table;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/*
 * To save notification
 */

@Entity(tableName = "notification")
public class NotificationEntity implements Serializable {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "image")
    public String image;

    @ColumnInfo(name = "link")
    public String link;

    @ColumnInfo(name = "object")
    public String object;

    // extra attribute
    @ColumnInfo(name = "read")
    public Boolean read = false;

    @ColumnInfo(name = "created_at")
    public Long created_at;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

//    public static NotificationEntity entity(Notification notification) {
//        NotificationEntity entity = new NotificationEntity();
//        entity.setId(notification.id);
//        entity.setTitle(notification.title);
//        entity.setContent(notification.content);
//        entity.setType(notification.type);
//        entity.setObject(notification.object);
//        entity.setImage(notification.image);
//        entity.setLink(notification.link);
//        entity.setRead(notification.read);
//        entity.setCreated_at(notification.created_at);
//        return entity;
//    }
}
