package com.myapp.music;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AuthorizationActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42; // 请求码
    private String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        // 获取用户邮箱
        userEmail = getIntent().getStringExtra("email");
        // 启动文件选择器请求用户选择存储权限
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 处理返回结果
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri treeUri = data.getData(); // 获取选择的 Uri

                if (treeUri != null) {
                    // 持久化授权权限
                    getContentResolver().takePersistableUriPermission(
                            treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );

                    // 保存路径到 SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    sharedPreferences.edit().putString("tree_uri", treeUri.toString()).apply();
                    Intent intent = new Intent(AuthorizationActivity.this, MainActivity.class);
//                    intent.putExtra("tree_uri", treeUri.toString());
                    intent.putExtra("email", userEmail); // 传递用户的 email
                    startActivity(intent); // 启动 MainActivity
                    finish(); // 结束当前的授权活动
                } else {
                    Toast.makeText(this, "未选择有效路径", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
