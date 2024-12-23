package com.myapp.music;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

// 动态页面：展示动态列表
public class DynamicActivity extends AppCompatActivity {
    private RecyclerView dynamicRecyclerView;
    private FloatingActionButton addDynamicButton;
    private DynamicAdapter dynamicAdapter;
    private List<Dynamic> dynamicList;
    private UserDatabaseHelper database; // 数据库变量
    private String userEmail;
    private List<Song> playlist = new ArrayList<>();
    private List<Song> songList = new ArrayList<>();
    private PlaylistAdapter playlistAdapter;
    private boolean isSingleLoop = false; // 用来记录当前是否为单曲循环模式
    private ImageButton playlistButton, imageButton3, loopModeButton2;
    private FrameLayout playlistContainer;
    private int currentSongIndex = 0;  // 当前播放的歌曲索引
    private TextView songNameTextView;
    private ImageButton playPauseButton;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);
        // 获取从 DynamicPublishActivity 返回的 Intent
        Intent intent = getIntent();
        String songName = intent.getStringExtra("songName");
        String artistName = intent.getStringExtra("artistName");
        String songImage = intent.getStringExtra("songImage");
        playlist = getIntent().getParcelableArrayListExtra("playlist");
        songList = getIntent().getParcelableArrayListExtra("songList");
        songAdapter = new SongAdapter(songList,  playlist,DynamicActivity.this, this::onSongItemClick, this::onPlayButtonClick, this::onAddToPlaylistClick);
        // 打印 songList 中的歌曲信息
        if (songList != null && !songList.isEmpty()) {
            for (Song song : songList) {
                System.out.println("这是测试的Song ID: " + song.getId() + ", Name: " + song.getName() + ", Artist: " + song.getArtist());
            }
        } else {
            System.out.println("songList is empty or null.");
        }
        // 初始化数据库
        database = new UserDatabaseHelper(this);

        // 初始化 RecyclerView 和布局管理器
        dynamicRecyclerView = findViewById(R.id.dynamicRecyclerView);
        dynamicRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化 FloatingActionButton
        addDynamicButton = findViewById(R.id.addDynamicButton);

        // 获取用户邮箱
        userEmail = getIntent().getStringExtra("email");
//        Toast.makeText(this, "userEmail: " + userEmail, Toast.LENGTH_SHORT).show();
        // 加载动态数据并设置适配器
        dynamicList = loadDynamicsWithLikes();  // 加载并设置点赞数量
        sortDynamicsByLikes(dynamicList);  // 按点赞数量排序
        for (Dynamic dynamic : dynamicList) {
            Log.d("Dynamic", "ID: " + dynamic.getId() + " Likes Count: " + dynamic.getLikesCount());
        }
        dynamicAdapter = new DynamicAdapter(this, dynamicList, userEmail,new DynamicAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Dynamic dynamic, int position) {

            }
        });
        dynamicRecyclerView.setAdapter(dynamicAdapter);
        dynamicAdapter.notifyDataSetChanged();
        // 初始化按钮
        // 初始化播放器控制
        playlistContainer = findViewById(R.id.playlistContainer22);
        playlistButton = findViewById(R.id.imageButton22);
        songNameTextView = findViewById(R.id.textView22);
        loopModeButton2 = findViewById(R.id.loopModeButton22);
        playPauseButton = findViewById(R.id.imageButton11);
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        // 接收播放模式参数
        // 检查当前播放状态并更新 UI
        if (MediaPlayerManager.isPlaying()) {
            Song currentSong = MediaPlayerManager.getCurrentSong();
            if (currentSong != null) {
                songNameTextView.setText(currentSong.getName() + " - " + currentSong.getArtist());
            }
            playPauseButton.setImageResource(R.drawable.ic_pause); // 设置为暂停图标
        } else {
            Song currentSong = MediaPlayerManager.getCurrentSong();
            if (currentSong != null) {
                songNameTextView.setText(currentSong.getName() + " - " + currentSong.getArtist());
                playPauseButton.setImageResource(R.drawable.ic_play0); // 设置为播放图标
            }
            else{
                songNameTextView.setText("未播放任何歌曲");
                playPauseButton.setImageResource(R.drawable.ic_play0); // 设置为播放图标
            }

        }


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
//            Toast.makeText(this, "播放模式切换为：" + mode, Toast.LENGTH_SHORT).show();
        });

        // Initialize Song Name TextView and Play/Pause Button

        imageButton3 = findViewById(R.id.imageButton33);
        // 设置点击事件监听器
        imageButton3.setOnClickListener(v -> playNextSong());

        playlistAdapter = new PlaylistAdapter(playlist, this::onRemoveFromPlaylistClick);
        RecyclerView playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistRecyclerView.setAdapter(playlistAdapter);
        // 点击按钮跳转到发布动态页面

        addDynamicButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(this, DynamicPublishActivity.class);
            intent2.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
            intent2.putExtra("email", userEmail);
            startActivityForResult(intent2, 100);  // 使用 startActivityForResult，并设置请求码
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewNote);
        // 设置动态按钮为选中状态
        bottomNavigationView.setSelectedItemId(R.id.nav_note);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // 跳转到主页面
                Intent intent3 = new Intent(this, MainActivity.class);
                intent3.putExtra("email", userEmail);
                intent3.putParcelableArrayListExtra("playlist", (ArrayList<Song>) playlist);
                intent3.putParcelableArrayListExtra("songList", (ArrayList<Song>) songList);
                intent3.putExtra("isSingleLoop", isSingleLoop); // 传递播放模式
                setResult(RESULT_OK, intent3);
                startActivity(intent3);
                return true;

            } else if (item.getItemId() == R.id.nav_note) {
//                // 跳转到动态页面
//                Intent intent = new Intent(this, DynamicActivity.class);
//                intent.putExtra("email", userEmail);
//                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_mine) {
                // 处理我的库页面
                // 逻辑
                Intent intent3= new Intent(this, MineActivity.class);
                intent3.putParcelableArrayListExtra("playlist", (ArrayList<Song>) playlist);
                intent3.putParcelableArrayListExtra("songList", (ArrayList<Song>) songList);
                intent3.putExtra("isSingleLoop", isSingleLoop);  // 传递播放模式
                intent3.putExtra("email", userEmail);

                setResult(RESULT_OK, intent3);
                startActivity(intent3);
                return true;
            } else {
                // 其他处理
            }
            return true;
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 确保返回的是正确的请求码，并且结果成功
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // 从返回的 Intent 中获取歌曲信息
            String songName = data.getStringExtra("songName");
            String artistName = data.getStringExtra("artistName");
            String songImage = data.getStringExtra("songImage");

            // 打印歌曲信息
            Log.d("DynamicActivity", "Song Name: " + songName);
            Log.d("DynamicActivity", "Artist Name: " + artistName);
            Log.d("DynamicActivity", "Song Image: " + songImage);

            // 根据返回的数据更新UI（如果需要）
            // 例如，你可以在动态列表项中显示歌曲信息
        }
    }
    // 加载动态列表
    private void loadDynamicList() {
        dynamicList.clear();
        dynamicList.addAll(loadDynamicsFromDatabase());
        dynamicAdapter.notifyDataSetChanged();
    }
    private void onRemoveFromPlaylistClick(Song song) {
        playlist.remove(song);
            playlistAdapter.notifyDataSetChanged();

        if (MediaPlayerManager.getCurrentSong() != null && MediaPlayerManager.getCurrentSong().equals(song)) {
            MediaPlayerManager.release();
            Toast.makeText(this, "已从播放列表中移除并停止播放", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateLoopModeButton() {
        if (isSingleLoop) {
            loopModeButton2.setImageResource(R.drawable.ic_single2); // 单曲循环图标
        } else {
            loopModeButton2.setImageResource(R.drawable.ic_all2); // 顺序播放图标
        }
    }
    // 显示或隐藏播放列表
    private void togglePlaylistVisibility() {
        if (playlistContainer.getVisibility() == View.VISIBLE) {
            playlistContainer.setVisibility(View.GONE);
        } else {
            playlistContainer.setVisibility(View.VISIBLE);
            playlistAdapter.notifyDataSetChanged();  // 每次显示播放列表时都刷新视图
        }
    }
    private void sortDynamicsByLikes(List<Dynamic> dynamics) {
        // 根据点赞数量进行降序排序
        Collections.sort(dynamics, new Comparator<Dynamic>() {
            @Override
            public int compare(Dynamic d1, Dynamic d2) {
                    return Integer.compare(d2.getLikesCount(), d1.getLikesCount());  // 按点赞数降序排序
            }
        });
        // 打印排序后的结果，检查排序是否成功
        for (Dynamic dynamic : dynamics) {
            Log.d("Sorted Dynamic", "ID: " + dynamic.getId() + " Likes Count: " + dynamic.getLikesCount());
        }
    }
    private void playNextSong() {
        if (playlist.size() > 0) {
            if (isSingleLoop) {
                // 单曲循环模式下，不切换歌曲，继续播放当前歌曲
                Song currentSong = playlist.get(currentSongIndex);
                playSong(currentSong);  // 播放当前歌曲
            } else {
                // 顺序播放下一首歌曲
                currentSongIndex = (currentSongIndex + 1) % playlist.size();
                Song nextSong = playlist.get(currentSongIndex);
                playSong(nextSong);
            }
        } else {
            Toast.makeText(this, "播放列表为空，无法播放下一首", Toast.LENGTH_SHORT).show();
        }
    }
    // 播放或暂停歌曲
    private void togglePlayPause() {
        if (playlist.isEmpty()) {
            Toast.makeText(this, "播放列表为空，无法播放", Toast.LENGTH_SHORT).show();
            return;
        }

        if (MediaPlayerManager.isPlaying()) {
            MediaPlayerManager.pause();
            playPauseButton.setImageResource(R.drawable.ic_play0);
        } else {
            Song currentSong = playlist.get(currentSongIndex);
            MediaPlayerManager.playSong(this, currentSong,playlist,songNameTextView);
            songNameTextView.setText(currentSong.getName() + " - " + currentSong.getArtist());
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
    }
    private void playSong(Song song) {
        if (playlist.isEmpty()) {
            // 如果播放列表为空，显示提示
            Toast.makeText(this, "播放列表为空，无法播放", Toast.LENGTH_SHORT).show();
            return;
        }

        // 确保传递正确的播放列表
        MediaPlayerManager.playSong(this, song, playlist,songNameTextView);
        songNameTextView.setText(song.getName() + " - " + song.getArtist());
        playPauseButton.setImageResource(R.drawable.ic_pause);
    }
    private void onSongItemClick(Song song) {
        Intent intent = new Intent(DynamicActivity.this, SongDetailActivity.class);
        intent.putExtra("song", song);
        intent.putExtra("songImage",song.getSongImage());
        intent.putExtra("email", userEmail);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("song_name", song.getName());
        intent.putExtra("artist", song.getArtist());
        intent.putExtra("album", song.getAlbum());
        intent.putExtra("file_path", song.getFilePath());
        intent.putParcelableArrayListExtra("playlist", new ArrayList<>(playlist));
//        startActivity(intent);
        detailActivityLauncher.launch(intent);
    }
    private final ActivityResultLauncher<Intent> detailActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<Song> updatedPlaylist = result.getData().getParcelableArrayListExtra("updatedPlaylist");
                    if (updatedPlaylist != null) {
                        playlist.clear();
                        playlist.addAll(updatedPlaylist);
                        playlistAdapter.notifyDataSetChanged();
                    }
                }
            });
    // 处理播放按钮点击事件
    private void onPlayButtonClick(Song song) {
        if (playlist.isEmpty()) {
            // 如果播放列表为空，显示提示
            Toast.makeText(this, "播放列表为空，无法播放", Toast.LENGTH_SHORT).show();
            return;
        }
        // 打印当前传入的歌曲信息
        Log.d("PlayButton", "当前播放的歌曲: " + (song != null ? song.getName() : "null"));
        // 如果歌曲为空或不在播放列表中，显示提示
        if (song == null || !playlist.contains(song)) {
            Toast.makeText(this, "无效的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        // 调用播放函数
        playSong(song);
    }
    // 点击添加到播放列表按钮时的处理逻辑
    private void onAddToPlaylistClick(Song song) {
        if (!playlist.contains(song)) {
            playlist.add(song);  // 将歌曲添加到播放列表
//            printPlaylistWithIndexes1();
//            playSong(song);
            playlistAdapter.notifyDataSetChanged();  // 通知适配器更新播放列表视图
            Toast.makeText(this, "已将歌曲添加到播放列表", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 页面返回时刷新数据
//        loadDynamicList();
        // 从 Intent 中获取当前播放的歌曲和进度
//        Song song = getIntent().getParcelableExtra("currentSong");
//        int currentPosition = getIntent().getIntExtra("currentPosition", 0);
//
//        if (song != null) {
//            MediaPlayerManager.playSong(this,song,playlist,songNameTextView);;
//            mediaPlayer.seekTo(currentPosition);  // 恢复播放进度
//        }
    }
    // 加载动态并设置点赞数量
    private List<Dynamic> loadDynamicsWithLikes() {
        List<Dynamic> dynamics = loadDynamicsFromDatabase();  // 假设这里是从数据库加载动态列表
        for (Dynamic dynamic : dynamics) {
            int likesCount = database.getLikesCountByDynamic(dynamic.getId()); // 获取每个动态的点赞数
            dynamic.setLikesCount(likesCount);  // 设置点赞数
            Log.d("Dynamic", "Dynamic ID: " + dynamic.getId() + " Likes Count: " + likesCount);

        }
        return dynamics;
    }
    // 从数据库加载动态数据
    private List<Dynamic> loadDynamicsFromDatabase() {
        return database.getAllDynamics();
    }
}
