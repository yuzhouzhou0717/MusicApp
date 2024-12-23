package com.myapp.music;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DynamicDetailActivity extends AppCompatActivity {

    private TextView tvUserEmail, tvContent, songNameTextView, artistNameTextView, tvTimestamp;
    private ImageView songImageView;
    private Button commentSubmitButton;
    private EditText commentEditText;
    private UserDatabaseHelper dbHelper;
    private RecyclerView commentsRecyclerView;
    private DyCommentAdapter dyCommentAdapter;
    private List<DynamicComment> dyComments;
    private String userEmail,userName;
    private int dynamicId; // 动态的唯一ID
    private ImageView btnLike;
    private TextView tvLikesCount;
private ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_detail);

        // 获取传递的数据
        userEmail = getIntent().getStringExtra("userEmail");

        String content = getIntent().getStringExtra("content");
        String songName = getIntent().getStringExtra("songName");
        String artistName = getIntent().getStringExtra("artistName");
        String songImage = getIntent().getStringExtra("songImage");
        String timestamp = getIntent().getStringExtra("timestamp");
        dynamicId = getIntent().getIntExtra("dynamicId", -1); // 获取动态ID
//// 使用 Toast 打印 userEmail
//        Toast.makeText(this, "Received userEmail: " + userEmail, Toast.LENGTH_SHORT).show();
//// 打印 userEmail 以检查传递的数据
//        Log.d("DynamicDetailActivity", "Received userEmail: " + userEmail);
        // 初始化视图
        tvUserEmail = findViewById(R.id.tvUserEmail1);
        tvContent = findViewById(R.id.tvContent1);
        songNameTextView = findViewById(R.id.songNameTextView11);
        artistNameTextView = findViewById(R.id.artistNameTextView11);
        songImageView = findViewById(R.id.songImageView11);
        tvTimestamp = findViewById(R.id.tvTimestamp1);
        commentEditText = findViewById(R.id.commentEditText1);
        commentSubmitButton = findViewById(R.id.commentSubmitButton1);
        commentsRecyclerView = findViewById(R.id.commentRecyclerView1);
        back = findViewById(R.id.detailBackButton1);
        // 设置点击事件监听器
        back.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
//            resultIntent.putParcelableArrayListExtra("updatedPlaylist", (ArrayList<Song>) playlist);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 初始化数据库帮助类
        dbHelper = new UserDatabaseHelper(this);
        // 设置数据
        userName = dbHelper.getUserNameByEmail(userEmail);
        tvUserEmail.setText(userName);
//        tvUserEmail.setText(userEmail);
        tvContent.setText(content);
        songNameTextView.setText(songName);
        artistNameTextView.setText(artistName);
        tvTimestamp.setText(timestamp);
        btnLike = findViewById(R.id.likeButton1);
        tvLikesCount = findViewById(R.id.likeCountTextView1);

        // 获取动态的点赞数
        int likesCount = dbHelper.getLikesCountByDynamic(dynamicId);
        tvLikesCount.setText(String.valueOf(likesCount));

        // 判断用户是否已点赞
        boolean isLiked = dbHelper.isLikedByUser(userEmail, dynamicId);
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_good2);  // 已点赞状态的图标
        } else {
            btnLike.setImageResource(R.drawable.ic_good);  // 默认未点赞状态的图标
        }

        btnLike.setOnClickListener(v -> {
            // 获取当前点赞数
            int currentLikesCount = dbHelper.getLikesCountByDynamic(dynamicId);
            if (dbHelper.isLikedByUser(userEmail, dynamicId)) {
                // 用户已点赞，执行取消点赞
                dbHelper.removeLike(userEmail, dynamicId);
                btnLike.setImageResource(R.drawable.ic_good);  // 更新为未点赞状态
                currentLikesCount--;
            } else {
                // 用户未点赞，执行点赞
                dbHelper.addLike(userEmail, dynamicId);
                btnLike.setImageResource(R.drawable.ic_good2);  // 更新为已点赞状态
                currentLikesCount++;
            }

            // 更新点赞数显示
            tvLikesCount.setText(String.valueOf(currentLikesCount));

            // 更新动态表中的点赞数
            dbHelper.updateLikesCount(dynamicId,currentLikesCount);
        });

        // 获取动态评论列表并设置适配器
        dyComments = dbHelper.getDynamicComments(dynamicId);
        dyCommentAdapter = new DyCommentAdapter(this,dyComments, new DyCommentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DynamicComment Dycomment) {
            }
        }, new DyCommentAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(DynamicComment Dycomment, int position) {
            }
        });
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(dyCommentAdapter);

        // 设置歌曲封面
        if (songImage != null) {
            int resId = getResources().getIdentifier(songImage, "drawable", getPackageName());
            if (resId != 0) {
                songImageView.setImageResource(resId);
            } else {
                songImageView.setImageResource(R.drawable.ic_music); // 默认图片
            }
        }

        // 设置提交评论按钮点击事件
        commentSubmitButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                // 将评论插入到动态评论表
                dbHelper.addDynamicComment(dynamicId, userEmail, commentText);
                // 清空评论输入框
                commentEditText.setText("");
                // 刷新评论列表
                refreshComments();
            }
        });
    }

    // 刷新评论列表的方法
    private void refreshComments() {
        // 获取最新的评论列表
        dyComments = dbHelper.getDynamicComments(dynamicId);
        // 更新适配器的数据
        dyCommentAdapter.updateComments(dyComments);
    }
}
