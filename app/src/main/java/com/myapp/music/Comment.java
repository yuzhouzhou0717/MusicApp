package com.myapp.music;
public class Comment {
    private int id;
    private int songId;
    private String commentText;
    private String timestamp;
    private String email;
    public Comment(int id, int songId, String commentText, String timestamp,String email) {
        this.id = id;
        this.songId = songId;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.email = email;  // 初始化邮箱字段
    }

    public int getId() {
        return id;
    }

    public int getSongId() {
        return songId;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public String getEmail() {
        return email;  // 返回用户邮箱
    }
}
