// LoginActivity.java
package com.myapp.music;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private UserDatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化数据库助手
        dbHelper = new UserDatabaseHelper(this);

        // 获取控件
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        // 获取 SharedPreferences 实例
        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);

        // 读取存储的账号信息并填充
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");

        if (!savedEmail.isEmpty()) {
            emailEditText.setText(savedEmail);
        }
        if (!savedPassword.isEmpty()) {
            passwordEditText.setText(savedPassword);
        }

        // 登录按钮点击事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请输入电子邮件和密码", Toast.LENGTH_SHORT).show();
                } else {
                    if (dbHelper.validateUser(email, password)) {
//                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // 保存账号信息
                        saveAccountInfo(email, password);

                        // 跳转到主页面
                        Intent intent = new Intent(LoginActivity.this, AuthorizationActivity.class);
                        intent.putExtra("email", email); // 传递用户的 email
                        startActivity(intent);
                        finish(); // 关闭登录页面
                    } else {
                        Toast.makeText(LoginActivity.this, "无效的电子邮件或密码", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 注册按钮点击事件
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // 保存账号信息到 SharedPreferences
    private void saveAccountInfo(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();  // 提交保存
    }
}
