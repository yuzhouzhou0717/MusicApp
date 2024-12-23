package com.myapp.music;

public class DynamicComment {
    private int id;
    private int dynamicId;
    private String email;
    private String comment;
    private String timestamp;
    public DynamicComment(int id, int dynamicId, String email, String comment, String timestamp) {
        this.id = id;
        this.dynamicId = dynamicId;
        this.email = email;
        this.comment = comment;
        this.timestamp = timestamp;
    }
    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDynamicId() {
        return dynamicId;
    }

    public void setDynamicId(int dynamicId) {
        this.dynamicId = dynamicId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
