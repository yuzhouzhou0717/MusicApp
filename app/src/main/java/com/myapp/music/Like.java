package com.myapp.music;

public class Like {
    private int likeId;
    private String email;
    private int songId;

    // Constructor
    public Like(int likeId, String email, int songId) {
        this.likeId = likeId;
        this.email = email;
        this.songId = songId;
    }

    // Getters and Setters
    public int getLikeId() {
        return likeId;
    }

    public void setLikeId(int likeId) {
        this.likeId = likeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }
}
