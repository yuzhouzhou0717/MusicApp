package com.myapp.music;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SongDetailActivity extends AppCompatActivity {
    private TextView songNameTextView, artistTextView, albumTextView, likeCountTextView;
    private ImageView songImage1;
    private ImageButton playPauseButton333, likeButton;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isLiked = false;
    private int likeCount = 0;
    private String userEmail,email; // 当前用户
    private int userId,songId; // 当前歌曲 ID
    private UserDatabaseHelper dbHelper; // 数据库助手
    private List<Song> playlist = new ArrayList<>();
    private int currentSongIndex = 0;  // 当前播放的歌曲索引
    private PlaylistAdapter playlistAdapter;
    private ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);
        dbHelper = new UserDatabaseHelper(this); // 确保这里正确初始化
        // 获取歌曲的图片名称
        playlist = getIntent().getParcelableArrayListExtra("playlist");
        if (playlist == null) {
            playlist = new ArrayList<>(); // 确保不为空
        }
        Song song = getIntent().getParcelableExtra("song");
        back = findViewById(R.id.detailBackButton);
        // 设置点击事件监听器
        back.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("updatedPlaylist", (ArrayList<Song>) playlist);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
// 根据图片名称获取资源 ID
        // 获取当前登录用户的 email
        RecyclerView commentRecyclerView = findViewById(R.id.commentRecyclerView);
        EditText commentEditText = findViewById(R.id.commentEditText);
        Button commentSubmitButton = findViewById(R.id.commentSubmitButton);
        ImageView imageView = findViewById(R.id.songImage1);
        // 获取当前活动布局的根视图并找到相关控件
        // 从资源文件加载图片
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_good2);
//        if (imageView == null) { Log.e("SongDetailActivity", "ImageView is null!");}

//        imageView.setImageBitmap(bitmap);
//        if (imageView != null) {
//            Log.e("SongDetailActivity", "ImageView is not null");
//        } else {
//            Log.e("SongDetailActivity", "ImageView is null");
//        }
//        playPauseButton333 = findViewById(R.id.playPauseButton333);
        // 从 Intent 获取传递的数据
        userEmail = getIntent().getStringExtra("email"); // 从 Intent 中获取传递的 email 字符串
        userId = getIntent().getIntExtra("user_id", -1); //
        songId = getIntent().getIntExtra("song_id", -1);
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "用户未登录或没有正确传递邮箱", Toast.LENGTH_SHORT).show();
            return;
        }

//        if (userId == -1) {
//            // 如果没有传递有效的 userId，显示错误或提示用户
//            Toast.makeText(this, "未能获取用户信息", Toast.LENGTH_SHORT).show();
//            finish();  // 退出活动，防止后续操作
//        }
        playlistAdapter = new PlaylistAdapter(playlist, this::onRemoveFromPlaylistClick);
        String songName = getIntent().getStringExtra("song_name");
        String artist = getIntent().getStringExtra("artist");
        String album = getIntent().getStringExtra("album");
        String filePath = getIntent().getStringExtra("file_path");
        String imageName = getIntent().getStringExtra("songImage");
        Log.d("imageName", "imageName: " + imageName);
        int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        imageView.setImageResource(imageResId);
        playPauseButton333 = findViewById(R.id.playPauseButton333);
        // 设置添加到播放列表按钮的点击事件
//        onAddToPlaylistClick.setOnClickListener(v -> {
//            addToPlaylist(song);
//            Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show();
//        });
        playPauseButton333.setOnClickListener(v -> onAddToPlaylistClick(song));

//        // 根据歌曲 ID 从数据库获取图片资源 ID
//        int imageResId = dbHelper.getSongImageResource(songId);
//        Log.d("SongDetailActivity", "dbHelper: " + (dbHelper == null ? "null" : "initialized"));
//        Log.d("SongDetailActivity", "imageResId: " + imageResId);
//        // 设置图片到 ImageView
//        if (imageResId != -1) {
////            songImage1.setImageResource(imageResId);  // 设置图片资源
//            songImage1.setImageResource(R.drawable.ic_cha);  // 默认图片
//        } else {
//            songImage1.setImageResource(R.drawable.ic_cha);  // 默认图片
//        }

//        if (imageName != null && !imageName.isEmpty()) {
//            int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
//            // 打印 imageResId 的值
//            Log.d("SongDetailActivity", "imageResId: " + imageResId);
//            String resourceName = getResources().getResourceEntryName(imageResId);
//            Log.d("SongDetailActivity", "Resource Name: " + resourceName);
//            if (imageResId != 0) {
//                // 设置图片到 ImageView
//                songImageView.setImageResource(imageResId);
//                if (songImageView == null) {
//                    Log.e("SongDetailActivity", "songImageView is null. Check your layout file and ID.");
//                }
//            } else {
//                // 如果找不到对应的图片资源，设置默认图片
//                songImageView.setImageResource(R.drawable.ic_pause); // 用你自己的默认图片
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageResId);
////            Log.d("SongDetailActivity", "songImageView: " + (songImage1 == null ? "null" : "not null"));
//
//            if (bitmap == null) {
//                Log.d("SongDetailActivity",     "Bitmap is null, image resource may be invalid.");
//                songImage1.setImageResource(R.drawable.ic_cha);
////                Log.d("SongDetailActivity", "songImageView: " + (songImage1 == null ? "null" : "not null"));
//            } else {
//                songImage1.setImageBitmap(bitmap);
//                Log.d("SongDetailActivity", "songImageView: " + (songImage1 == null ? "null" : "not null"));
//            }
//        } else {
//            // 如果没有图片名称，使用默认图片
//            songImage1.setImageResource(R.drawable.ic_cha);
        // 初始化视图组件
        songNameTextView = findViewById(R.id.songNameTextView);
        artistTextView = findViewById(R.id.artistTextView);
        albumTextView = findViewById(R.id.albumTextView);

        likeButton = findViewById(R.id.likeButton);
        likeCountTextView = findViewById(R.id.likeCountTextView);
        // 初始化数据库助手
        dbHelper = new UserDatabaseHelper(this);

        // 获取评论数据        adapter = new SongAdapter(searchResults, playlist, this, this::onSongItemClick, this::onPlayButtonClick, this::onAddToPlaylistClick);
        List<Comment> comments = dbHelper.getCommentsForSong(songId);

        // 修改 CommentAdapter 的构造函数调用
        CommentAdapter adapter = new CommentAdapter(this,comments, new CommentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Comment comment) {

            }
        }, new CommentAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Comment comment, int position) {

            }

        });

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setAdapter(adapter);

        // 设置歌曲信息
        songNameTextView.setText(songName);
        artistTextView.setText(artist);
        albumTextView.setText(album);
        // 获取并显示点赞状态和总数
        initializeLikeState(userEmail, songId);

//        // 播放按钮点击事件
//        playPauseButton333.setOnClickListener(v -> togglePlayPause(filePath));

        // 点赞按钮点击事件
        likeButton.setOnClickListener(v -> toggleLike(userEmail, songId));

        // 提交评论按钮点击事件
        commentSubmitButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                // 获取当前用户的 email
                dbHelper.addComment(songId, userEmail, commentText);
                // 更新评论列表
                comments.add(0, new Comment(0, songId, commentText, "刚刚",userEmail));
                adapter.notifyItemInserted(0);
                commentEditText.setText(""); // 清空输入框
            }
        });
    }
    private void addToPlaylist(Song song) {
        if (playlist != null && !playlist.contains(song)) {
            playlist.add(song); // 将歌曲添加到播放列表
        }
    }
    // 初始化点赞状态
    private void initializeLikeState(String userEmail, int songId) {
        isLiked = dbHelper.isSongLikedByUser(userEmail, songId); // 检查用户是否已点赞
        likeCount = dbHelper.getLikeCount(songId); // 获取歌曲总点赞数
        updateLikeButton();
        likeCountTextView.setText(String.valueOf(likeCount));
    }

    // 播放/暂停切换
    private void togglePlayPause(String filePath) {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButton333.setImageResource(R.drawable.ic_play);
                isPlaying = false;
            } else {
                mediaPlayer.start();
                playPauseButton333.setImageResource(R.drawable.ic_pause);
                isPlaying = true;
            }
        } else {
            playSong(filePath);
        }
    }

    // 播放歌曲
    private void playSong(String filePath) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            playPauseButton333.setImageResource(R.drawable.ic_pause);
            isPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 点赞或取消点赞
    private void toggleLike(String userEmail, int songId) {
        if (isLiked) {
            // 取消点赞
            if (dbHelper.unlikeSong(userEmail, songId)) {
                likeCount--;
                isLiked = false;
            }
        } else {
            // 点赞
            if (dbHelper.likeSong(userEmail, songId)) {
                likeCount++;
                isLiked = true;
            }
        }

        // 更新 UI
        likeCountTextView.setText(String.valueOf(likeCount));
        updateLikeButton();

        // 更新歌曲总点赞数到数据库
        dbHelper.updateLikesInDatabase(songId, likeCount);
    }

    // 更新点赞按钮状态
    private void updateLikeButton() {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_love1); // 已点赞图标
        } else {
            likeButton.setImageResource(R.drawable.ic_love); // 默认图标
        }
    }
    // 点击添加到播放列表按钮时的处理逻辑
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
    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("updatedPlaylist", (ArrayList<Song>) playlist);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    private void onRemoveFromPlaylistClick(Song song) {
        playlist.remove(song);
        playlistAdapter.notifyDataSetChanged();

        if (MediaPlayerManager.getCurrentSong() != null && MediaPlayerManager.getCurrentSong().equals(song)) {
            MediaPlayerManager.release();
            Toast.makeText(this, "已从播放列表中移除并停止播放", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
