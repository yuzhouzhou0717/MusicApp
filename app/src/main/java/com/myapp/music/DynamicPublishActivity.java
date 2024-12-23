package com.myapp.music;

import static com.myapp.music.Dynamic.getCurrentTimestamp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 动态发布页：发布动态
public class DynamicPublishActivity extends AppCompatActivity {
    private EditText contentEditText;
    private TextView selectedMusicTextView1, selectedMusicTextView;
    private ImageView selectedMusicfilePath;
    private Button addMusicButton, publishButton;
    private int selectedMusicId = -1;
    private UserDatabaseHelper database; // 声明数据库变量、
    private String userEmail;
    private RecyclerView recyclerView;
    private List<Song> playlist = new ArrayList<>();
    private List<Song> songList = new ArrayList<>();
    private UserDatabaseHelper songDatabaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_publish);
        playlist = getIntent().getParcelableArrayListExtra("playlist");
        songList = getIntent().getParcelableArrayListExtra("songList");
        database = new UserDatabaseHelper(this);
        contentEditText = findViewById(R.id.dynamicContentEditText);
        selectedMusicTextView = findViewById(R.id.selectedMusicTextView);
        selectedMusicTextView1 = findViewById(R.id.selectedMusicTextView1);
        selectedMusicfilePath = findViewById(R.id.selectedMusicfilePath);
        addMusicButton = findViewById(R.id.addMusicButton);
        publishButton = findViewById(R.id.publishButton);
        // 初始化 SongDatabaseHelper
        songDatabaseHelper = new UserDatabaseHelper(this);
        // 初始化 RecyclerView 和适配器
        // 获取用户邮箱
        userEmail = getIntent().getStringExtra("email");
//        addMusicButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, MusicSelectActivity.class);
//            startActivityForResult(intent, 1);
//        });
        // 找到返回按钮
        ImageButton back = findViewById(R.id.backArrow22);
        // 设置点击事件监听器
        back.setOnClickListener(v -> {
            // 返回
            Intent intent = new Intent(this, DynamicActivity.class);
//            intent.putParcelableArrayListExtra("Playlist", (ArrayList<Song>) playlist);
            intent.putExtra("email", userEmail);
            setResult(RESULT_OK, intent);

            finish(); // 返回动态页面
        });

        publishButton.setOnClickListener(v -> {
            String content = contentEditText.getText().toString();
            if (content.isEmpty()) {
                Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 假设你生成了时间戳
            String timestamp = getCurrentTimestamp(); // 获取当前时间戳
            // 获取歌曲信息
            Song selectedSong = songDatabaseHelper.getSongById(selectedMusicId); // 根据歌曲ID获取歌曲信息
//            if (selectedSong == null) {
//                selectedSong = new Song(0, "Unknown Song", "Unknown Artist", "default_image");
//            }
            String songName = "默认歌曲名";
            String artistName = "默认歌手";
            String songImage = "默认图片路径";
            // 首先检查歌曲名是否为空
            if (selectedSong != null && selectedSong.getName() != null && !selectedSong.getName().isEmpty()) {
                // 如果歌曲名不为空，获取歌曲名、歌手名和图片
                songName = selectedSong.getName();
                artistName = selectedSong.getArtist();
                songImage = selectedSong.getSongImage();
            }


            // 调用构造器时传递时间戳
            database.addDynamic(new Dynamic(0, userEmail, content, selectedMusicId,timestamp,songName, artistName,songImage));


//            Log.d("DynamicPublishActivity", "动态发布页面Song Name: " + songName);
//            Log.d("DynamicPublishActivity", "Artist Name: " + artistName);
//            Log.d("DynamicPublishActivity", "Song Image: " + songImage);
            // 返回结果给动态展示页面
            Intent intent = new Intent();
            intent.putExtra("email", userEmail);
            intent.putExtra("songName", songName);
            intent.putExtra("artistName", artistName);
            intent.putExtra("songImage", songImage);
            setResult(RESULT_OK, intent);

            finish(); // 返回动态页面
        });

        addMusicButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MusicSelectActivity.class);
            // 你可以传递一些额外的信息到 MusicSelectActivity（如果需要）
            intent.putParcelableArrayListExtra("songList", (ArrayList<Song>) songList);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            selectedMusicId = data.getIntExtra("musicId", -1);
            String musicName = data.getStringExtra("songName");
            String artist = data.getStringExtra("artistName");
            String songImageName = data.getStringExtra("songImage"); // 这里是数据库中保存的文件名，如 "song_zm"

            selectedMusicTextView.setText(musicName);
            selectedMusicTextView1.setText(artist);
            int imageResId = getResources().getIdentifier(songImageName, "drawable", getPackageName());
            selectedMusicfilePath.setImageResource(imageResId);

        }

    }

}