package com.myapp.music;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myapp.music.R;
import com.myapp.music.Song;
import com.myapp.music.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicSelectActivity extends AppCompatActivity {

    private RecyclerView songRecyclerView;
    private List<Song> songList = new ArrayList<>();
    private List<Song> playlist = new ArrayList<>();
    private SongAdapter songAdapter;
    private int selectedSongId = -1;  // 用来保存用户选择的歌曲ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_select);

        // 接收传递的歌曲列表
        songList = getIntent().getParcelableArrayListExtra("songList");
        playlist = getIntent().getParcelableArrayListExtra("playlist");
        // 初始化 RecyclerView 和适配器
        songRecyclerView = findViewById(R.id.songRecyclerView);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化 playlist，假设它是当前播放列表中的歌曲


        // 设置点击事件监听器
        SongAdapter.OnSongItemClickListener onSongItemClickListener = song -> {
            // 选择歌曲时的处理
            selectedSongId = song.getId();
            Toast.makeText(this, "已选择歌曲：" + song.getName(), Toast.LENGTH_SHORT).show();
        };

        SongAdapter.OnPlayButtonClickListener onPlayButtonClickListener = song -> {
//            // 播放按钮点击事件
//            Toast.makeText(this, "播放歌曲：" + song.getName(), Toast.LENGTH_SHORT).show();
        };

        SongAdapter.OnPlaylistButtonClickListener onPlaylistButtonClickListener = song -> {
//            // 添加到播放列表按钮点击事件
//            playlist.add(song);
//            Toast.makeText(this, "已添加到播放列表：" + song.getName(), Toast.LENGTH_SHORT).show();
        };

        // 初始化适配器，传递所有需要的参数
        songAdapter = new SongAdapter(songList, playlist, MusicSelectActivity.this,
                onSongItemClickListener, onPlayButtonClickListener, onPlaylistButtonClickListener);

        songRecyclerView.setAdapter(songAdapter);
        // 设置返回按钮
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // 返回时传递选择的歌曲ID
            Intent resultIntent = new Intent();
            resultIntent.putExtra("musicId", selectedSongId);
            // 传递歌曲封面图片路径
            String songImage = (selectedSongId != -1) ? getSongImageById(selectedSongId) : "";
            resultIntent.putExtra("songImage", songImage);
            resultIntent.putExtra("artistName", selectedSongId != -1 ? getArtistById(selectedSongId) : "");
            resultIntent.putExtra("songName", selectedSongId != -1 ? getSongNameById(selectedSongId) : "");
            setResult(RESULT_OK, resultIntent);
            finish();  // 返回
        });
    }

    // 根据歌曲ID获取歌曲名称
    private String getSongNameById(int songId) {
        for (Song song : songList) {
            if (song.getId() == songId) {
                return song.getName();
            }
        }
        return "";
    }
    // 根据歌曲ID获取歌手名称
    private String getArtistById(int songId) {
        for (Song song : songList) {
            if (song.getId() == songId) {
                return song.getArtist();
            }
        }
        return "";
    }

    // 根据歌曲ID获取歌曲封面图片（假设 Song 类中有 getSongImage 方法）
    private String getSongImageById(int songId) {
        for (Song song : songList) {
            if (song.getId() == songId) {
                return song.getSongImage();  // 假设返回图片路径或 URL
            }
        }
        return "";
    }
}

