package com.myapp.music;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class SearchResultsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> searchResults;
    private PlaylistAdapter playlistAdapter;
    private List<Song> playlist = new ArrayList<>();
    private TextView songNameTextView;
    private ImageButton playPauseButton;
    private int currentSongIndex = 0;
    private String userEmail,songName1;
    private EditText searchBox;
    private Button searchButton;
    private FrameLayout playlistContainer;
    private ImageButton playlistButton, imageButton3,loopModeButton2;
    private TextView StextView;
   private boolean isSingleLoop = false; // 用来记录当前是否为单曲循环模式
    private List<Song> songList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        ImageButton backButton = findViewById(R.id.SbackButton);

//        songName1 = getIntent().getStringExtra("songName");
//        songName1.setText(song.getName() + " - " + song.getArtist());
        // Get the user email and playlist passed through the intent
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "用户未登录或没有正确传递邮箱", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        songNameTextView = findViewById(R.id.StextView);
        playPauseButton = findViewById(R.id.SimageButton2);
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        // 检查当前播放状态并更新 UI
        if (MediaPlayerManager.isPlaying()) {
            Song currentSong = MediaPlayerManager.getCurrentSong();
            if (currentSong != null) {
                songNameTextView.setText(currentSong.getName() + " - " + currentSong.getArtist());
            }
            playPauseButton.setImageResource(R.drawable.ic_pause); // 设置为暂停图标
        } else {
            songNameTextView.setText("未播放任何歌曲");
            playPauseButton.setImageResource(R.drawable.ic_play0); // 设置为播放图标
        }
        playlist = getIntent().getParcelableArrayListExtra("playlist");
        songList = getIntent().getParcelableArrayListExtra("songList");
        searchResults = getIntent().getParcelableArrayListExtra("searchResults");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.SrecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(searchResults, playlist, this, this::onSongItemClick, this::onPlayButtonClick, this::onAddToPlaylistClick);
        recyclerView.setAdapter(adapter);

        // Initialize Playlist RecyclerView
        playlistAdapter = new PlaylistAdapter(playlist, this::onRemoveFromPlaylistClick);
        RecyclerView playlistRecyclerView = findViewById(R.id.SplaylistRecyclerView);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistRecyclerView.setAdapter(playlistAdapter);

        // Initialize Playlist Button
        playlistButton = findViewById(R.id.SimageButton);
        playlistContainer = findViewById(R.id.SplaylistContainer);
        // 初始化按钮
        loopModeButton2 = findViewById(R.id.loopModeButton2);
        // 接收播放模式参数
        Intent intent = getIntent();
        // 删除局部变量声明，直接使用类级别变量
        isSingleLoop = intent.getBooleanExtra("isSingleLoop", false);

        // 根据播放模式更新按钮状态
        updateLoopModeButton();

        playlistButton.setOnClickListener(v -> togglePlaylistVisibility());

        loopModeButton2.setOnClickListener(v -> {
            // 切换播放模式
            isSingleLoop = !isSingleLoop;
            updateLoopModeButton(); // 更新按钮显示

            // 提示用户当前模式
            String mode = isSingleLoop ? "单曲循环" : "顺序播放";
            Toast.makeText(this, "播放模式切换为：" + mode, Toast.LENGTH_SHORT).show();
        });

        // Initialize Song Name TextView and Play/Pause Button

        imageButton3 = findViewById(R.id.SimageButton3);
        // 设置点击事件监听器
        imageButton3.setOnClickListener(v -> playNextSong());
        searchBox = findViewById(R.id.SsearchBox);
        searchButton = findViewById(R.id.SsearchButton);
        searchButton.setOnClickListener(v -> performSearch());
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("updatedPlaylist", (ArrayList<Song>) playlist);
            resultIntent.putExtra("isSingleLoop", isSingleLoop); // 传递播放模式
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void onSongItemClick(Song song) {
        Intent intent = new Intent(SearchResultsActivity.this, SongDetailActivity.class);
        intent.putExtra("songImage", song.getSongImage());
        intent.putExtra("email", userEmail);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("song_name", song.getName());
        intent.putExtra("artist", song.getArtist());
        intent.putExtra("album", song.getAlbum());
        intent.putExtra("file_path", song.getFilePath());
        startActivity(intent);
    }

    private void onPlayButtonClick(Song song) {
        if (playlist.isEmpty()) {
            Toast.makeText(this, "播放列表为空，无法播放", Toast.LENGTH_SHORT).show();
            return;
        }
        playSong(song);
    }

    private void onAddToPlaylistClick(Song song) {
        if (!playlist.contains(song)) {
            playlist.add(song);
            playlistAdapter.notifyDataSetChanged();
            Toast.makeText(this, "已将歌曲添加到播放列表", Toast.LENGTH_SHORT).show();
        }
    }

    private void playSong(Song song) {
        MediaPlayerManager.playSong(this, song, playlist,songNameTextView);

        songNameTextView.setText(song.getName() + " - " + song.getArtist());
        playPauseButton.setImageResource(R.drawable.ic_pause);
    }

    private void togglePlayPause() {
        if (playlist.isEmpty()) {
            Toast.makeText(this, "播放列表为空，无法播放", Toast.LENGTH_SHORT).show();
            return;
        }

        Song currentSong = MediaPlayerManager.getCurrentSong();
        if (currentSong == null) {
            playSong(playlist.get(currentSongIndex));
        } else if (MediaPlayerManager.isPlaying()) {
            MediaPlayerManager.pause();
            playPauseButton.setImageResource(R.drawable.ic_play0);
            Toast.makeText(this, "音乐已暂停", Toast.LENGTH_SHORT).show();
        } else {
            MediaPlayerManager.resume();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            Toast.makeText(this, "音乐正在播放", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlaylistVisibility() {
        if (playlistContainer.getVisibility() == View.VISIBLE) {
            playlistContainer.setVisibility(View.GONE);
        } else {
            playlistContainer.setVisibility(View.VISIBLE);
            playlistAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("updatedPlaylist", (ArrayList<Song>) playlist);
        resultIntent.putExtra("isSingleLoop", isSingleLoop); // 传递播放模式
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        MediaPlayerManager.release();
    }
    private void playNextSong() {
        if (playlist.isEmpty()) {
            Toast.makeText(this, "播放列表为空，无法播放下一首", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSingleLoop) {
            // 单曲循环模式，继续播放当前歌曲
            Song currentSong = playlist.get(currentSongIndex);
            playSong(currentSong);  // 播放当前歌曲
            Toast.makeText(this, "单曲循环：" + currentSong.getName(), Toast.LENGTH_SHORT).show();
        } else {
            // 顺序播放下一首歌曲
            currentSongIndex = (currentSongIndex + 1) % playlist.size();
            Song nextSong = playlist.get(currentSongIndex);
            playSong(nextSong);
            Toast.makeText(this, "顺序播放下一首：" + nextSong.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLoopModeButton() {
        if (isSingleLoop) {
            loopModeButton2.setImageResource(R.drawable.ic_single2); // 单曲循环图标
        } else {
            loopModeButton2.setImageResource(R.drawable.ic_all2); // 顺序播放图标
        }
    }
    private void performSearch() {
        String query = searchBox.getText().toString().toLowerCase();
        searchResults.clear();  // 清空旧的搜索结果

        // 如果搜索框为空，显示所有结果
        if (query.isEmpty()) {
            searchResults.addAll(songList);  // 你也可以根据需要直接显示所有歌曲
        } else {
            // 根据歌曲名称和歌手名称进行过滤
            for (Song song : songList) {
                if (song.getName().toLowerCase().contains(query) || song.getArtist().toLowerCase().contains(query)) {
                    searchResults.add(song);
                }
            }
        }
        // 通知 Adapter 更新数据
        adapter.notifyDataSetChanged();

        // 如果没有匹配项，给用户提示
        if (searchResults.isEmpty()) {
            Toast.makeText(this, "没有找到匹配的歌曲", Toast.LENGTH_SHORT).show();
        }
    }

        private void onRemoveFromPlaylistClick(Song song) {
        playlist.remove(song);
        playlistAdapter.notifyDataSetChanged();

        if (MediaPlayerManager.getCurrentSong() != null && MediaPlayerManager.getCurrentSong().equals(song)) {
            MediaPlayerManager.release();
            Toast.makeText(this, "已从播放列表中移除并停止播放", Toast.LENGTH_SHORT).show();
        }
    }
}

