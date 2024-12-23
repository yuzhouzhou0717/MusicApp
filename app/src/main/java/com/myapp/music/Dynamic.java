package com.myapp.music;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Dynamic {
    private int id;
    private String userEmail;
    private String content;
    private String timestamp;
    private String songName;
    private int music_id;
    private String artistName;
    private String songImage;
    private int likesCount;  // 点赞数量
    public Dynamic(int id,String userEmail, String content, int music_id,String timestamp,
     String songName, String artistName, String songImage) {
        this.id = id;
        this.userEmail = userEmail;
        this.content = content;
        this.music_id = music_id;
        this.timestamp = timestamp;
        this.songName = songName;
        this.artistName = artistName;
        this.songImage = songImage;

    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public String getArtistName() {
        return artistName;
    }
    public String getSongName() {
        return songName;
    }
    public String getSongImage() {
        return songImage;
    }
    public String getUserEmail() {
        return userEmail;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }
    public int getMusicid() {
        return music_id;
    }
    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
