package com.myapp.music;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private int id;
    private String name;
    private String artist;
    private String album;
    private int likes;
    private String filePath; // 歌曲文件路径
    private String songImage; // 新增属性

    // 构造函数
    public Song(int id, String name, String artist, String album, int likes, String filePath, String songImage) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.likes = likes;
        this.filePath = filePath;
        this.songImage = songImage;
    }

    // Getters 和 Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSongImage() {
        return songImage;
    }

    public void setSongImage(String songImage) {
        this.songImage = songImage;
    }

    // Parcelable 接口的实现

    // 构造函数，用于从 Parcel 中恢复对象
    protected Song(Parcel in) {
        id = in.readInt();
        name = in.readString();
        artist = in.readString();
        album = in.readString();
        likes = in.readInt();
        filePath = in.readString();
        songImage = in.readString();
    }

    // CREATOR 必须实现
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(artist);
        parcel.writeString(album);
        parcel.writeInt(likes);
        parcel.writeString(filePath);
        parcel.writeString(songImage);
    }
}
