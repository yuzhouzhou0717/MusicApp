package com.myapp.music;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private Context context;
    private PlaylistAdapter playlistAdapter;
    private List<Song> playlist = new ArrayList<>();
    private List<Song> Playlist = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private TextView songNameTextView;
    private ImageButton playPauseButton, playlistButton,play1Button,repeat1Button,imageButton3,previousButton;
    private boolean isPlaying = false;
    private FrameLayout playlistContainer;
    private String userEmail;
    private String FilePath;
    private ViewPager2 viewPager2;
    private int currentPage = 0;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42; // SAF 请求代码
    UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);  // 传递 Context
    private int currentSongIndex = 0;  // 当前播放的歌曲索引
    private EditText searchBox;
    private Button searchButton;
    private List<Song> searchResults;  // 搜索结果
    private boolean isSingleLoop = false; // 用来记录当前是否为单曲循环模式
    private boolean isRandom = false; // 当前是否为随机播放模式
    private ImageButton loopModeButton; // 用来显示循环模式的按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String treeUriString = sharedPreferences.getString("tree_uri", null);
        // 创建 UserDatabaseHelper 实例，并传递当前 Activity 的 Context
        dbHelper = new UserDatabaseHelper(this);
//        dbHelper.deleteSong(11);
//        dbHelper.deletelikeById(5);
//        dbHelper.deleteSong(3);
//        根据动态 ID 删除
//        dbHelper.deleteDynamicById(8);

//
//        UserDatabaseHelper dbHelper = new UserDatabaseHelper(context);
//        SQLiteDatabase db = dbHelper.getWritableDatabase(); // 获取可写数据库对象


        // 获取用户邮箱
        userEmail = getIntent().getStringExtra("email");
        // 获取用户邮箱
        FilePath= getIntent().getStringExtra("file_path");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "用户未登录或没有正确传递邮箱", Toast.LENGTH_SHORT).show();
            return;
        }

        // 初始化播放器控制
        songNameTextView = findViewById(R.id.textView);
        playPauseButton = findViewById(R.id.imageButton2);
        imageButton3 = findViewById(R.id.imageButton3);
        previousButton = findViewById(R.id.previousButton);
        playPauseButton.setOnClickListener(v -> togglePlayPause());
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
        // 获取 ViewPager2 实例
        viewPager2 = findViewById(R.id.viewPager);
        int[] images = new int[]{R.drawable.joker1, R.drawable.joker3};
        ImageAdapter imageAdapter = new ImageAdapter(images);
        viewPager2.setAdapter(imageAdapter);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == images.length) {
                    currentPage = 0;
                }
                viewPager2.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);

        // 初始化 RecyclerView 和适配器
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 播放列表适配器
        playlistAdapter = new PlaylistAdapter(playlist, this::onRemoveFromPlaylistClick);
        RecyclerView playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistRecyclerView.setAdapter(playlistAdapter);
        playlistButton = findViewById(R.id.imageButton);
        playlistContainer = findViewById(R.id.playlistContainer);
//
// 初始化按钮
        loopModeButton = findViewById(R.id.loopModeButton);
        updateLoopModeButton(); // 设置初始状态按钮文本

        loopModeButton.setOnClickListener(v -> {
            // 切换播放模式
            if (isRandom) {
                // 如果当前是随机播放，切换到单曲循环
                isRandom = false;
                isSingleLoop = true;
            } else if (isSingleLoop) {
                // 如果当前是单曲循环，切换到顺序播放
                isSingleLoop = false;
            } else {
                // 如果当前是顺序播放，切换到随机播放
                isRandom = true;
            }

            updateLoopModeButton(); // 更新按钮显示
        });

        Intent intent3 = getIntent();
        if (intent3 != null && intent3.hasExtra("playlist")) {
            Playlist = intent3.getParcelableArrayListExtra("playlist");
            isSingleLoop = intent3.getBooleanExtra("isSingleLoop", false);
            // 根据播放模式更新按钮状态
            updateLoopModeButton();
//            Log.d("MainActivity", "Received playlist size: " + Playlist.size());
            if (playlist != null) {
                playlist.clear();
                playlist.addAll(Playlist);
                playlistAdapter.notifyDataSetChanged();
            }
        }

        List<Song> songs = dbHelper.getAllSongs();  // 获取歌曲列表
        sortSongsByLikes(songs);  // 按点赞数量排序
        songAdapter = new SongAdapter(songs, playlist,MainActivity.this, this::onSongItemClick, this::onPlayButtonClick, this::onAddToPlaylistClick);

        recyclerView.setAdapter(songAdapter);

        playlistButton.setOnClickListener(v -> togglePlaylistVisibility());


        // 设置点击事件监听器
        imageButton3.setOnClickListener(v -> playNextSong());

        // 设置点击事件监听器
        previousButton.setOnClickListener(v -> playPreviousSong());

        searchBox = findViewById(R.id.searchBox);
        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> performSearch());
        // 接收 Uri
        // 接收 Uri
        // 启动文件选择器请求用户选择存储权限

        if (treeUriString == null) {
            // 未找到授权路径，跳转到授权页面
            Intent intent = new Intent(this, AuthorizationActivity.class);
            startActivity(intent);
            finish();
        } else {
            Uri treeUri = Uri.parse(treeUriString);

            // 验证权限是否仍有效
            boolean hasPermission = false;
            for (UriPermission uriPermission : getContentResolver().getPersistedUriPermissions()) {
                if (uriPermission.getUri().equals(treeUri) && uriPermission.isReadPermission()) {
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                // 权限失效，重新授权
                Toast.makeText(this, "授权路径失效，请重新授权", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AuthorizationActivity.class);
                startActivity(intent);
                finish();
            } else {
                // 使用已授权的路径加载文件
                loadMp3FilesFromStorageAccessFramework(treeUri);
            }
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // 设置动态按钮为选中状态


        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
//                // 跳转到动态页面
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra("email", userEmail);
//                startActivity(intent);
                return true;

            } else if (item.getItemId() == R.id.nav_note) {
                // 跳转到动态页面
                Intent intent= new Intent(this, DynamicActivity.class);
                intent.putExtra("currentSong", MediaPlayerManager.getCurrentSong());
                intent.putParcelableArrayListExtra("playlist", new ArrayList<>(playlist));
                intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
                intent.putExtra("isSingleLoop", isSingleLoop);  // 传递播放模式
                intent.putExtra("email", userEmail);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_mine) {
                // 处理我的库页面逻辑
                // 跳转到动态页面
                Intent intent= new Intent(this, MineActivity.class);
                intent.putParcelableArrayListExtra("playlist", new ArrayList<>(playlist));
                intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
                intent.putExtra("isSingleLoop", isSingleLoop);  // 传递播放模式
                intent.putExtra("email", userEmail);
                startActivity(intent);
                return true;
            } else {
                // 其他处理
            }
            return true;
        });
        // 检查权限
// 检查权限
//        // 在首次请求权限时设置标志
//        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
//        boolean isPermissionGranted = prefs.getBoolean("storage_permission_granted", false);
//
//        if (!isPermissionGranted) {
//            // 请求权限
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    if (Environment.isExternalStorageManager()) {
//                        // 已授权，直接加载音乐
//
//                        // 保存权限已授予
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putBoolean("storage_permission_granted", true);
//                        editor.apply();
//                    } else {
//                        // 未授权，请求权限
//                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
//                    }
//                } else {
//                    // Android 10设备，直接加载
//                    loadMp3FilesFromStorageAccessFramework(null);
//                }
//            }
//        } else {
//            // 已授权，直接加载音乐
//            loadMp3FilesFromStorageAccessFramework(null);
//        }
}

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
//                loadMp3FilesFromMediaStore(); // 权限授予后加载音乐文件
            } else {
                // 权限拒绝
                Toast.makeText(this, "未授予存储权限，无法访问音乐文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 处理 SAF 权限请求结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                loadMp3FilesFromStorageAccessFramework(treeUri); // 使用 SAF 加载音乐文件
            }
        }
    }

    // 使用 SAF 加载 MP3 文件
    private void loadMp3FilesFromStorageAccessFramework(Uri uri) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        if (pickedDir != null && pickedDir.isDirectory()) {
            DocumentFile[] files = pickedDir.listFiles();
            List<Song> songs = new ArrayList<>();

            for (DocumentFile file : files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    String name = file.getName();
                    if (name != null) {
                        name = name.replace(".mp3", "");
                    } else {
                        name = "未知"; // 给 name 设置一个默认值
                    }
                    String filePath = file.getUri().toString();
                    String artist = "未知"; // 你可以使用 ID3 标签获取实际艺术家信息
                    String album = "未知"; // 使用 ID3 标签获取专辑信息
                    String songImage = "res/drawable/ic_music.png";
                    // 使用 Toast 打印每个文件路径
//                    Toast.makeText(this, "加载文件: " + filePath, Toast.LENGTH_SHORT).show();
                    // 创建歌曲对象
                    Song song = new Song(1, name, artist, album, 0, filePath,songImage);

                    // 插入到数据库
                    UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);

                    // 如果歌曲不存在，插入
                    if (!dbHelper.isSongExist(filePath)) {
                        dbHelper.insertSong(song);  // 插入数据库
                        songs.add(song);  // 添加到列表
                        Log.d("Database", "插入歌曲: " + name);
                    } else {
                        Log.d("Database", "歌曲已存在: " + name);
                    }
                }
            }

            // 在 MainActivity 中获取所有歌曲数据
            List<Song> songsFromDb = dbHelper.getAllSongs();
            songList.clear();
            songList.addAll(songsFromDb);
            songAdapter.notifyDataSetChanged(); // 通知适配器更新 UI
        }
    }
    // 排序函数
    private void sortSongsByLikes(List<Song> songs) {
        // 根据点赞数量进行降序排序
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song s1, Song s2) {
                return Integer.compare(s2.getLikes(), s1.getLikes());  // 降序排列
            }
        });
    }
    private void playNextSong() {
        if (playlist.size() > 0) {
            if (isSingleLoop) {
                // 单曲循环模式下，不切换歌曲，继续播放当前歌曲
                Song currentSong = playlist.get(currentSongIndex);
                playSong(currentSong);  // 播放当前歌曲
            } else if (isRandom) {
                // 随机播放模式：随机播放下一首歌曲
                int randomIndex = (int) (Math.random() * playlist.size());  // 生成随机索引
                Song randomSong = playlist.get(randomIndex);  // 获取随机歌曲
                Log.d("MediaPlayer", "随机播放: 选择的随机歌曲索引: " + randomIndex);
                playSong(randomSong);  // 播放随机歌曲
            } else {
                // 顺序播放模式：播放下一首歌曲
                currentSongIndex = (currentSongIndex + 1) % playlist.size();
                Song nextSong = playlist.get(currentSongIndex);
                playSong(nextSong);
            }
        } else {
            Toast.makeText(this, "播放列表为空，无法播放下一首", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPreviousSong() {
        if (playlist.size() > 0) {
            if (isSingleLoop) {
                // 单曲循环模式下，不切换歌曲，继续播放当前歌曲
                Song currentSong = playlist.get(currentSongIndex);
                playSong(currentSong);  // 播放当前歌曲
            } else if (isRandom) {
                // 随机播放上一首歌曲
                int randomIndex = (int) (Math.random() * playlist.size());  // 生成随机索引
                Song randomSong = playlist.get(randomIndex);  // 获取随机歌曲
                Log.d("MediaPlayer", "随机播放上一首: 选择的随机歌曲索引: " + randomIndex);
                playSong(randomSong);  // 播放随机歌曲
            } else {
                // 顺序播放上一首歌曲
                if (currentSongIndex == 0) {
                    currentSongIndex = playlist.size() - 1;  // 如果是第一首歌，切换到最后一首
                } else {
                    currentSongIndex = (currentSongIndex - 1) % playlist.size();
                }
                Song previousSong = playlist.get(currentSongIndex);
                playSong(previousSong);
            }
        } else {
            Toast.makeText(this, "播放列表为空，无法播放上一首", Toast.LENGTH_SHORT).show();
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
    // 点击添加到播放列表按钮
    private void onAddToPlaylistClick(Song song) {
        if (!playlist.contains(song)) {
            playlist.add(song);  // 将歌曲添加到播放列表
            printPlaylistWithIndexes1();
//            playSong(song);
            playlistAdapter.notifyDataSetChanged();  // 通知适配器更新播放列表视图
            Toast.makeText(this, "已将歌曲添加到播放列表", Toast.LENGTH_SHORT).show();
        }
    }
    private void printPlaylistWithIndexes1() {
        // 遍历播放列表并打印每首歌的编号（索引）
        for (int i = 0; i < playlist.size(); i++) {
            Song song = playlist.get(i);
            Log.d("Playlist", "Song " + (i + 1) + ": " + song.getName() + " (ID: " + song.getId() + ")");

            // 如果是当前正在播放的歌曲，标记它
            if (i == currentSongIndex) {
                Log.d("Playlist", "Currently playing: " + song.getName() + " (Index: " + i + ")");
            }
        }
    }

    private void onSongCompletion() {
        if (isSingleLoop) {
            // 如果是单曲循环，播放当前歌曲
            Song currentSong = playlist.get(currentSongIndex);
            playSong(currentSong);
        } else {
            // 顺序播放下一首歌
            currentSongIndex++;
            if (currentSongIndex < playlist.size()) {
                // 播放下一首歌曲
                Song nextSong = playlist.get(currentSongIndex);
                playSong(nextSong);
            } else {
                // 如果播放完所有歌曲，重置回第一首歌
                Toast.makeText(this, "播放列表已结束", Toast.LENGTH_SHORT).show();
                currentSongIndex = 0;  // 重置播放到第一首歌
                playSong(playlist.get(currentSongIndex));  // 循环播放
            }
        }}
    private void onSongItemClick(Song song) {
        Intent intent = new Intent(MainActivity.this, SongDetailActivity.class);
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



    // 显示或隐藏播放列表
    private void togglePlaylistVisibility() {
        if (playlistContainer.getVisibility() == View.VISIBLE) {
            playlistContainer.setVisibility(View.GONE);
        } else {
            playlistContainer.setVisibility(View.VISIBLE);
            playlistAdapter.notifyDataSetChanged();  // 每次显示播放列表时都刷新视图
        }
    }
    // 从播放列表中移除歌曲
    private void onRemoveFromPlaylistClick(Song song) {
        int removedIndex = playlist.indexOf(song);
        playlist.remove(song);  // 从播放列表中移除歌曲
        playlistAdapter.notifyDataSetChanged();  // 通知适配器更新视图
        Toast.makeText(this, "已从播放列表中移除", Toast.LENGTH_SHORT).show();

        // 如果删除的歌曲是当前播放的歌曲，则停止播放并更新索引
        if (removedIndex == currentSongIndex) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;  // 清空播放器实例
            }

            // 如果播放列表为空，恢复播放器视图到初始状态
            if (playlist.isEmpty()) {
                currentSongIndex = 0;  // 重置索引
                songNameTextView.setText("暂无歌曲播放");  // 显示提示文本
                playPauseButton.setImageResource(R.drawable.ic_play0);  // 显示播放按钮
                isPlaying = false;  // 停止播放状态
                Toast.makeText(this, "播放列表为空，无法播放", Toast.LENGTH_SHORT).show();
            } else {
                // 如果播放列表中仍有歌曲，播放当前索引歌曲
                currentSongIndex = 0;  // 或者重置为播放列表中的第一首歌
                playSong(playlist.get(currentSongIndex));  // 播放第一首歌
            }
        } else if (removedIndex < currentSongIndex) {
            // 如果删除的歌曲在当前播放索引之前，更新索引
            currentSongIndex--;
        }
    }
//    public void showTooltip(View v) {
//        ImageButton button = (ImageButton) v;
//        String tooltipText;
//
//        if (button.getId() == R.id.playButton) {
//            tooltipText = "列表循环";
//        } else if (button.getId() == R.id.imageButton3) {
//            tooltipText = "单曲循环";
//        } else {
//            return;
//        }
//
//        Toast.makeText(this, tooltipText, Toast.LENGTH_SHORT).show();
//    }
private void updateLoopModeButton() {
    if (isRandom) {
        loopModeButton.setImageResource(R.drawable.ic_suiji); // 随机播放图标
//        Toast.makeText(this, "已开启随机播放", Toast.LENGTH_SHORT).show(); // 显示提示
    } else if (isSingleLoop) {
        loopModeButton.setImageResource(R.drawable.ic_single2); // 单曲循环图标
//        Toast.makeText(this, "已开启单曲循环", Toast.LENGTH_SHORT).show(); // 显示提示
    } else {
        loopModeButton.setImageResource(R.drawable.ic_all2); // 顺序播放图标
//        Toast.makeText(this, "已开启顺序播放", Toast.LENGTH_SHORT).show(); // 显示提示
    }
}



private void performSearch() {
    String query = searchBox.getText().toString().toLowerCase();
    searchResults = new ArrayList<>();

    // 根据歌曲名称和歌手名称进行过滤
    for (Song song : songList) {
        if (song.getName().toLowerCase().contains(query) || song.getArtist().toLowerCase().contains(query)) {
            searchResults.add(song);
        }
    }

    // 跳转到搜索结果页面，传递搜索结果
    Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
    intent.putExtra("email", userEmail);
    // 传递当前的播放列表
    intent.putParcelableArrayListExtra("playlist", new ArrayList<>(playlist));
    intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
    intent.putExtra("isSingleLoop", isSingleLoop);  // 传递播放模式

    intent.putParcelableArrayListExtra("searchResults", (ArrayList<Song>) searchResults);
    searchActivityLauncher.launch(intent);
}
    private final ActivityResultLauncher<Intent> searchActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<Song> updatedPlaylist = result.getData().getParcelableArrayListExtra("updatedPlaylist");
                    if (updatedPlaylist != null) {
                        playlist.clear();
                        playlist.addAll(updatedPlaylist);
                        playlistAdapter.notifyDataSetChanged();
                    }
                    // 从搜索页面返回时，获取播放模式
                    boolean updatedIsSingleLoop = result.getData().getBooleanExtra("isSingleLoop", false);
                    // 更新主页面的播放模式
                    isSingleLoop = updatedIsSingleLoop;

                    // 更新按钮的状态
                    updateLoopModeButton();
                    Song currentSong = MediaPlayerManager.getCurrentSong();
                    // 检查当前播放状态并更新 UI
                    if (currentSong != null) {
                        // 有歌曲正在播放
                        songNameTextView.setText(currentSong.getName() + " - " + currentSong.getArtist());
                        if (MediaPlayerManager.isPlaying()) {
                            playPauseButton.setImageResource(R.drawable.ic_pause);  // 设置为暂停图标
                        } else {
                            playPauseButton.setImageResource(R.drawable.ic_play0);  // 设置为播放图标
                        }
                    } else {
                        songNameTextView.setText("未播放任何歌曲");
                        playPauseButton.setImageResource(R.drawable.ic_play0); // 设置为播放图标
                    }
                }
            });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPlayerManager.release(); // 确保释放资源
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
        }
}

