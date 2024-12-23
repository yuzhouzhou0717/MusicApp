package com.myapp.music;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
public class MineActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 1;
    private String userEmail;
    private TextView userE;
    private RecyclerView recyclerViewDYComments, recyclerViewComments, likedSongsRecyclerView,dynamicsRecyclerView;
    private CommentAdapter commentAdapter;
    private DyCommentAdapter DycommentAdapter;
    private UserDatabaseHelper dbHelper;
    private List<Comment> comments; // 评论列表
    private List<DynamicComment> Dycomments; // 评论列表
    private LikeSongsAdapter likeSongsAdapter;
    private DynamicAdapter dynamicsAdapter;
    private List<Song> likedSongsList;
    private List<Dynamic> userDynamics;
    private Button selectedButton = null; // 当前选中的按钮
    private List<Song> playlist = new ArrayList<>();
    private List<Song> songList = new ArrayList<>();
    private boolean isSingleLoop = false; // 用来记录当前是否为单曲循环模式
    private PlaylistAdapter playlistAdapter;
    private ImageButton playlistButton, imageButton3,loopModeButton2;
    private FrameLayout playlistContainer;
    private int currentSongIndex = 0;  // 当前播放的歌曲索引
    private TextView songNameTextView;
    private ImageButton playPauseButton,btnOptions;
    private Button btnLikes;
    private TextView textUserName, textUserProfile, textLikedSongsCount, textCommentsCount, textDynamicsCount;
    private ImageView imageProfile;
    private String currentPhotoPath;  // 用于存储拍摄的照片路径
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        // 初始化数据库
        dbHelper = new UserDatabaseHelper(this);
        Intent intent = getIntent();






        // 获取用户邮箱
        userEmail = getIntent().getStringExtra("email");
        userName = dbHelper.getUserNameByEmail(userEmail);
        userE = findViewById(R.id.textEmail);
        btnOptions = findViewById(R.id.btnOptions);
        textUserProfile = findViewById(R.id.textUserProfile);
        textLikedSongsCount = findViewById(R.id.textLikedSongsCount);
        textCommentsCount = findViewById(R.id.textCommentsCount);
        textDynamicsCount = findViewById(R.id.textDynamicsCount);
        imageProfile = findViewById(R.id.imageProfile);
//        userE.setText(userEmail);
        userE.setText(userName);
        ImageButton btnEditProfile = findViewById(R.id.btnEditProfile);
        EditText textUserProfile = findViewById(R.id.textUserProfile);
        AtomicBoolean isEditing = new AtomicBoolean(false);  // 使用 AtomicBoolean 来代替 boolean
        // 获取用户信息
        User user = dbHelper.getUserByEmail(userEmail); // 假设有getUserByEmail方法
        btnEditProfile.setOnClickListener(v -> {
            if (isEditing.get()) {
                // 保存资料
                String updatedProfile = textUserProfile.getText().toString();
                user.setProfile(updatedProfile); // 更新 User 对象的 profile
                // 保存更新的资料，例如保存到数据库或共享偏好设置
                dbHelper.updateUser(user);
                // 改回编辑模式
                btnEditProfile.setImageResource(R.drawable.ic_bianji1); // 编辑图标
                textUserProfile.setFocusable(false);
                textUserProfile.setClickable(false);
                textUserProfile.setFocusableInTouchMode(false);  // 禁用输入
            } else {
                // 开始编辑资料
                btnEditProfile.setImageResource(R.drawable.ic_tijiao); // 保存图标
                textUserProfile.setFocusableInTouchMode(true);  // 允许编辑
                textUserProfile.setClickable(true);  // 允许点击
                textUserProfile.requestFocus();  // 获取焦点
            }

            // 切换编辑状态
            isEditing.set(!isEditing.get());
        });
        // 设置点击事件
        btnOptions.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MineActivity.this, view);
            getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());
            popupMenu.show();

            // 处理菜单项点击事件
            popupMenu.setOnMenuItemClickListener(item -> {
                // 使用 if-else 判断菜单项的 ID
                if (item.getItemId() == R.id.option_1) {
                    // 处理 option 1 的点击事件
                    showChangePasswordDialog();
                    // TODO: 执行相关操作
                    return true;
                } else if (item.getItemId() == R.id.option_2) {
                    // 处理 option 2 的点击事件
                    // TODO: 执行相关操作
                    // 点击修改用户名，弹出修改用户名的对话框
                    showChangeUsernameDialog();
                    return true;
                } else if (item.getItemId() == R.id.option_3) {
                    // 处理 option 2 的点击事件
                    // TODO: 执行相关操作
                    return true;
                } else if (item.getItemId() == R.id.option_4) {
                    // 处理 option 3 的点击事件
                    // TODO: 执行相关操作
                    logout();
                    return true;
                } else {
                    // 未处理的菜单项
                    return false;
                }
            });
        });
        if (user != null) {
            textUserProfile.setText(user.getProfile() != null ? user.getProfile() : "未设置个人资料");
            String avatarPath = dbHelper.getUserAvatarPathByEmail(userEmail);  // 获取头像路径

            imageProfile.setImageResource(R.drawable.default_avatar1);

            // 获取用户统计信息
            int likedSongsCount = dbHelper.getLikedSongsCountByUser(userEmail); // 获取点赞歌曲数
            int commentsCount = dbHelper.getTotalCommentsCountByUser(userEmail); // 获取评论数
            int dynamicsCount = dbHelper.getDynamicsCountByUser(userEmail); // 获取动态数

            textLikedSongsCount.setText("你喜欢了 " + likedSongsCount + " 首歌曲");
            textCommentsCount.setText("你发布了 " + commentsCount + " 条评论");
            textDynamicsCount.setText("你发布了 " + dynamicsCount + " 条动态");
        }

        playlist = getIntent().getParcelableArrayListExtra("playlist");
        songList = getIntent().getParcelableArrayListExtra("songList");
        // 初始化播放器控制
        // Initialize Playlist Button
        // 播放列表适配器
        playlistAdapter = new PlaylistAdapter(playlist, this::onRemoveFromPlaylistClick);
        RecyclerView playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistRecyclerView.setAdapter(playlistAdapter);
        playlistContainer = findViewById(R.id.playlistContainer22);
        playlistButton = findViewById(R.id.imageButton22);
        songNameTextView = findViewById(R.id.textView22);
        loopModeButton2 = findViewById(R.id.loopModeButton22);
        playPauseButton = findViewById(R.id.imageButton11);
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        // 接收播放模式参数
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
            } else {
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
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistRecyclerView.setAdapter(playlistAdapter);

        // 初始化 RecyclerView
        likedSongsRecyclerView = findViewById(R.id.likedSongsRecyclerView);
        likedSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        likedSongsRecyclerView.setVisibility(View.VISIBLE); // 点赞

        // 获取当前用户dynamicsRecyclerView
        userDynamics = dbHelper.getUserDynamics(userEmail);
//        Log.d("MineActivity", "User dynamics size: " + userDynamics.size());

        // 初始化 RecyclerView
        // 初始化 RecyclerView 和适配器
        dynamicsRecyclerView = findViewById(R.id.dynamicsRecyclerView);
        dynamicsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

// 初始化动态列表和适配器
        userDynamics = dbHelper.getUserDynamics(userEmail);
        dynamicsAdapter = new DynamicAdapter(this, userDynamics, userEmail, new DynamicAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Dynamic dynamic, int position) {
                showDeleteConfirmationDialog1(dynamic, position);
            }
        });
        dynamicsRecyclerView.setAdapter(dynamicsAdapter);

// 点击显示动态
        Button btnDynamics = findViewById(R.id.btnDynamics);
        btnDynamics.setOnClickListener(v -> {
            // 判断动态列表是否已经显示
            if (dynamicsRecyclerView.getVisibility() == View.VISIBLE) {
                // 如果已显示，点击时隐藏
                selectButton(btnDynamics, -1);
                dynamicsRecyclerView.setVisibility(View.GONE);
            } else {
                selectButton(btnDynamics, 0);
                dynamicsRecyclerView.setVisibility(View.VISIBLE);
                recyclerViewDYComments.setVisibility(View.GONE);
                likedSongsRecyclerView.setVisibility(View.GONE);
                recyclerViewComments.setVisibility(View.GONE);

                // 重新获取动态列表并更新适配器
                userDynamics = dbHelper.getUserDynamics(userEmail);
                Log.d("MineActivity", "User dynamics size: " + userDynamics.size());

                // 更新适配器
                dynamicsAdapter.notifyDataSetChanged();  // 确保数据刷新
            }
        });

        // 获取当前用户点赞的所有歌曲
        likedSongsList = dbHelper.getLikedSongsByUser(userEmail);

        // 初始化适配器
        likeSongsAdapter = new LikeSongsAdapter(likedSongsList, new LikeSongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Song song) {
                // 跳转到歌曲详情页面
                Intent intent = new Intent(MineActivity.this, SongDetailActivity.class);
                intent.putExtra("email", userEmail);
                intent.putExtra("song_id", song.getId());
                intent.putExtra("song_name", song.getName());
                intent.putExtra("artist", song.getArtist());
                intent.putExtra("album", song.getAlbum());
                intent.putExtra("file_path", song.getFilePath());
                intent.putExtra("songImage", song.getSongImage());
                startActivity(intent);
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewMine);


        // 设置动态按钮为选中状态
        bottomNavigationView.setSelectedItemId(R.id.nav_mine);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // 跳转到动态页面
                Intent intent3 = new Intent(this, MainActivity.class);
                intent3.putExtra("email", userEmail);
                intent3.putParcelableArrayListExtra("playlist", (ArrayList<Song>) playlist);
                intent3.putParcelableArrayListExtra("songList", (ArrayList<Song>) songList);
                intent3.putExtra("isSingleLoop", isSingleLoop); // 传递播放模式
                setResult(RESULT_OK, intent3);
                startActivity(intent3);
                return true;
            } else if (item.getItemId() == R.id.nav_note) {
                // 跳转到动态页面
                Intent intent3 = new Intent(this, DynamicActivity.class);
                intent3.putParcelableArrayListExtra("playlist", new ArrayList<>(playlist));
                intent3.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
                Log.d("MineActivity", "yourCollection is null: " + (songList == null));

                intent3.putExtra("isSingleLoop", isSingleLoop);  // 传递播放模式
                intent3.putExtra("email", userEmail);
                setResult(RESULT_OK, intent3);
                startActivity(intent3);
                return true;
            } else if (item.getItemId() == R.id.nav_mine) {
                return true;

            } else {
                // 其他处理
            }
            return true;
        });
        // 设置适配器给 RecyclerView
        likedSongsRecyclerView.setAdapter(likeSongsAdapter);

        Button btnComments = findViewById(R.id.btnComments);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);


        // 点击显示评论
        btnComments.setOnClickListener(v -> {
            // 判断评论列表是否已经显示
            if (recyclerViewComments.getVisibility() == View.VISIBLE) {
                // 如果已显示，点击时隐藏
                selectButton(btnComments, -1);
                recyclerViewComments.setVisibility(View.GONE);
            } else {
                selectButton(btnComments, 0);
                recyclerViewComments.setVisibility(View.VISIBLE);
                recyclerViewDYComments.setVisibility(View.GONE);
                likedSongsRecyclerView.setVisibility(View.GONE);
                dynamicsRecyclerView.setVisibility(View.GONE);// 隐藏点赞

                comments = dbHelper.getUserComments(userEmail); // 获取评论列表

                // 设置适配器
                commentAdapter = new CommentAdapter(this, comments, new CommentAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Comment comment) {
                        // 跳转到歌曲详情页面
                        Intent intent = new Intent(MineActivity.this, SongDetailActivity.class);
                        int songId = comment.getSongId();
                        Song song = dbHelper.getSongById(songId); // 获取歌曲信息
                        intent.putExtra("song", song);
                        intent.putExtra("songImage", song.getSongImage());
                        intent.putExtra("email", userEmail);
                        intent.putExtra("song_id", song.getId());
                        intent.putExtra("song_name", song.getName());
                        intent.putExtra("artist", song.getArtist());
                        intent.putExtra("album", song.getAlbum());
                        intent.putExtra("file_path", song.getFilePath());
                        intent.putExtra("songId", comment.getSongId());
                        startActivity(intent);
                    }
                }, new CommentAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(Comment comment, int position) {
                        showDeleteConfirmationDialog(comment, position);
                    }
                });

                recyclerViewComments.setAdapter(commentAdapter);
                recyclerViewComments.setLayoutManager(new LinearLayoutManager(MineActivity.this));
            }
        });


        Button btnDYComments = findViewById(R.id.btnDYComments);
        recyclerViewDYComments = findViewById(R.id.recyclerViewDYComments);

// 点击显示评论
        btnDYComments.setOnClickListener(v -> {
            // 判断评论列表是否已经显示
            if (recyclerViewDYComments.getVisibility() == View.VISIBLE) {
                // 如果已显示，点击时隐藏
                selectButton(btnDYComments, -1);
                recyclerViewDYComments.setVisibility(View.GONE);
            } else {
                selectButton(btnDYComments, 0);
                recyclerViewDYComments.setVisibility(View.VISIBLE);
                recyclerViewComments.setVisibility(View.GONE);
                likedSongsRecyclerView.setVisibility(View.GONE);
                dynamicsRecyclerView.setVisibility(View.GONE); // 隐藏点赞

                Dycomments = dbHelper.getUserDYComments(userEmail); // 获取评论列表

                // 设置适配器
                DycommentAdapter = new DyCommentAdapter(this, Dycomments, new DyCommentAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(DynamicComment Dycomment) {
                        // 跳转到动态详情页面
                        int dynamicId = Dycomment.getDynamicId();
                        Dynamic dynamic = dbHelper.getDynamicById(dynamicId); // 获取歌曲信息
                        Intent intent = new Intent(MineActivity.this, DynamicDetailActivity.class); // 使用 DynamicDetailActivity
                        intent.putExtra("dynamicId", dynamic.getId()); // 传递动态ID
                        intent.putExtra("userEmail", dynamic.getUserEmail()); // 传递动态ID
                        intent.putExtra("content", dynamic.getContent()); // 传递动态内容
                        intent.putExtra("timestamp", dynamic.getTimestamp()); // 传递时间戳
                        intent.putExtra("songName", dynamic.getSongName()); // 传递歌曲名称
                        intent.putExtra("artistName", dynamic.getArtistName()); // 传递艺术家名称
                        intent.putExtra("songImage", dynamic.getSongImage()); // 传递歌曲图片
                        startActivity(intent); // 跳转
                    }
                }, new DyCommentAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(DynamicComment Dycomment, int position) {
                        // 弹出删除确认对话框
                        showDeleteConfirmationDialog3(Dycomment, position);
                    }
                });

                recyclerViewDYComments.setAdapter(DycommentAdapter);
                recyclerViewDYComments.setLayoutManager(new LinearLayoutManager(MineActivity.this));
            }
        });

        // 显示点赞歌曲列表
        btnLikes = findViewById(R.id.btnLikes);
        btnLikes.setOnClickListener(v -> {
            // 判断点赞列表是否已经显示
            if (likedSongsRecyclerView.getVisibility() == View.VISIBLE) {
                // 如果已显示，点击时隐藏
                selectButton(btnLikes, -1);
                likedSongsRecyclerView.setVisibility(View.GONE);
            } else {
                selectButton(btnLikes, 0);
                likedSongsRecyclerView.setVisibility(View.VISIBLE);
                recyclerViewDYComments.setVisibility(View.GONE);
                recyclerViewComments.setVisibility(View.GONE);
                dynamicsRecyclerView.setVisibility(View.GONE);
                // 获取点赞歌曲列表
                likedSongsList = dbHelper.getLikedSongsByUser(userEmail);

                // 更新适配器
                likeSongsAdapter.updateLikedSongs(likedSongsList); // 更新已点赞歌曲列表
//
////                // 显示播放按钮
////                Button playButton = findViewById(R.id.btnPlayLikedSongs);
//                playButton.setVisibility(View.VISIBLE);  // 显示播放按钮
//
//                // 设置播放按钮点击事件
//                playButton.setOnClickListener(v1 -> {
//                    // 将所有喜欢的歌曲添加到播放列表
//                    MediaPlayerManager.setPlaylist(likedSongsList, 0);  // 从第一个歌曲开始播放
//                    // 开始播放第一首歌曲
//                    MediaPlayerManager.playSong(this, likedSongsList.get(0), likedSongsList, songNameTextView);
//                });
            }
        });
    }
//    private void setPlayButtonForLikedSongs() {
//        // 设置播放按钮点击事件
//        Button playButton = findViewById(R.id.btnPlayLikedSongs);
//        playButton.setVisibility(View.VISIBLE);  // 确保播放按钮可见
//        playButton.setOnClickListener(v -> {
//            // 将所有喜欢的歌曲添加到播放列表
//            MediaPlayerManager.setPlaylist(likedSongsList, 0);  // 从第一个歌曲开始播放
//            // 开始播放歌曲
//            MediaPlayerManager.playSong(this, likedSongsList.get(0), likedSongsList, songNameTextView);
//        });
//    }
    private void showChangeUsernameDialog() {
        // 创建一个输入框对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改用户名");

        // 设置输入框
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // 设置按钮
// 设置按钮
        builder.setPositiveButton("确认", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (newUsername.length() == 0) {
                Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                input.setText(""); // 清空输入框内容
            } else if (newUsername.length() > 10) {
                Toast.makeText(this, "昵称不能超过10个字符", Toast.LENGTH_SHORT).show();
                input.setText(""); // 清空输入框内容
            } else {
                // 检查用户名是否已经被占用
                if (dbHelper.isUsernameTaken(newUsername)) {
                    Toast.makeText(this, "该昵称已被占用，请选择其他昵称", Toast.LENGTH_SHORT).show();
                    input.setText(""); // 清空输入框内容
                } else {
                    // 更新数据库中的用户名
                    dbHelper.updateUsername(newUsername, userEmail);
                    Toast.makeText(this, "昵称修改成功", Toast.LENGTH_SHORT).show();
                    userE.setText(newUsername);  // 设置新的用户名
                    dialog.dismiss(); // 成功时关闭对话框
                }
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    // 实现退出登录功能
    private void logout() {
        // 清除 SharedPreferences 中的当前用户邮箱
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("currentUserEmail"); // 删除当前用户的登录信息
        editor.apply();

        // 跳转到登录页面
        Intent intent = new Intent(MineActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // 关闭当前页面，防止返回到当前页面
    }
    // 显示修改密码的弹窗
    private void showChangePasswordDialog() {
        // 创建输入框布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        // 创建旧密码输入框
        final EditText oldPasswordEditText = new EditText(this);
        oldPasswordEditText.setHint("请输入旧密码");
        oldPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordEditText);

        // 创建新密码输入框
        final EditText newPasswordEditText = new EditText(this);
        newPasswordEditText.setHint("请输入新密码");
        newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordEditText);

        // 创建弹窗
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改密码")
                .setView(layout)
                .setPositiveButton("确认", (dialog, which) -> {
                    String oldPassword = oldPasswordEditText.getText().toString();
                    String newPassword = newPasswordEditText.getText().toString();
                    if (isValidPassword(oldPassword, newPassword)) {
                        updatePassword(oldPassword, newPassword);
                    } else {
                        Toast.makeText(MineActivity.this, "密码无效或不匹配", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    // 验证密码是否合法
    private boolean isValidPassword(String oldPassword, String newPassword) {
        // 你可以根据需要进一步增强验证逻辑
        return !oldPassword.isEmpty() && !newPassword.isEmpty() && newPassword.length() >= 6;
    }

    // 更新密码的方法
    private void updatePassword(String oldPassword, String newPassword) {
        // 假设使用 SQLite 数据库
        SQLiteDatabase db = openOrCreateDatabase("musicAppDB", MODE_PRIVATE, null);

        // 查询旧密码是否匹配
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE email = ?", new String[]{userEmail});
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            if (storedPassword.equals(oldPassword)) {
                // 更新密码
                ContentValues values = new ContentValues();
                values.put("password", newPassword);
                int rowsUpdated = db.update("users", values, "email = ?", new String[]{userEmail});
                if (rowsUpdated > 0) {
                    Toast.makeText(MineActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MineActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MineActivity.this, "旧密码错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog1(Dynamic dynamic, int position) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("你确定要删除这条动态吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDynamicFromDatabase(dynamic, position);
                        // 从动态列表中删除该动态
                        userDynamics.remove(position);

                        // 通知适配器更新 UI
                        dynamicsAdapter.notifyItemRemoved(position);
                    }
                })
                .setNegativeButton("取消", null) // 取消按钮
                .show();
    }

    private void showDeleteConfirmationDialog3(DynamicComment Dycomment, int position) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("你确定要删除这条动态评论吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 删除评论
                    dbHelper.deleteDynamicComment(Dycomment.getId()); // 通过评论ID删除

                    // 从列表中移除该评论并刷新RecyclerView
                    Dycomments.remove(position);
                    DycommentAdapter.notifyItemRemoved(position);

                    // 提示用户删除成功
            Toast.makeText(MineActivity.this, "评论已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null) // 取消按钮
                .show();
    }
    // 显示删除确认对话框
    private void showDeleteConfirmationDialog(Comment comment, int position) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("你确定要删除这条歌曲评论吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCommentFromDatabase(comment, position);
                    }
                })
                .setNegativeButton("取消", null) // 取消按钮
                .show();
    }

    // 从数据库中删除评论并更新UI
    private void deleteCommentFromDatabase(Comment comment, int position) {
        // 从数据库中删除评论
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("comment", "id = ?", new String[]{String.valueOf(comment.getId())});

        // 从适配器数据中删除该评论
        comments.remove(position);

        // 通知适配器删除项
        commentAdapter.notifyItemRemoved(position);

        // 提示用户删除成功
        Toast.makeText(MineActivity.this, "评论已删除", Toast.LENGTH_SHORT).show();
    }
    private void deleteDynamicFromDatabase(Dynamic dynamic, int position) {
        // 从数据库中删除动态
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("dynamic", "id = ?", new String[]{String.valueOf(dynamic.getId())});

        // 从动态列表中删除该动态
        userDynamics.remove(position);

        // 通知适配器更新 UI
        dynamicsAdapter.notifyItemRemoved(position);

        // 提示用户删除成功
        Toast.makeText(MineActivity.this, "动态已删除", Toast.LENGTH_SHORT).show();
    }

    // 选择按钮的功能，设置当前按钮为选中状态，其他按钮恢复默认状态
    private void selectButton(Button selected,int key) {
        if(selectedButton == selected && key==-1){
            selectedButton.setBackgroundResource(R.drawable.btn_selector);
            selectedButton.setTextColor(getResources().getColor(android.R.color.black));
            return; // 结束方法执行
        }
        if(selectedButton == selected && key==-2){
            // 设置当前按钮为选中状态
            selected.setBackgroundResource(R.drawable.selected_button_background); // 自定义蓝色背景
            selected.setTextColor(getResources().getColor(android.R.color.white)); // 文字变白
            selectedButton = selected;
            return; // 结束方法执行
        }
//        // 如果已经选中了该按钮，直接返回
//        else if (selectedButton == selected) return;

        // 恢复之前选中的按钮背景为默认
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.btn_selector);
            selectedButton.setTextColor(getResources().getColor(android.R.color.black));
        }

        // 设置当前按钮为选中状态
        selected.setBackgroundResource(R.drawable.selected_button_background); // 自定义蓝色背景
        selected.setTextColor(getResources().getColor(android.R.color.white)); // 文字变白
        selectedButton = selected;
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
}
