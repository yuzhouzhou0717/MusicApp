package com.myapp.music;

// RegisterActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private UserDatabaseHelper dbHelper;
    private ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化数据库助手
        dbHelper = new UserDatabaseHelper(this);

        // 获取控件
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        // 找到返回按钮
        back = findViewById(R.id.backArrow);
        // 设置点击事件监听器
        back.setOnClickListener(v -> {
            // 返回到登录页面
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // 结束当前活动
        });
        // 注册按钮点击事件
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "请输入电子邮件和密码", Toast.LENGTH_SHORT).show();
                } else {
                    if (!dbHelper.checkUserExists(email)) {
                        dbHelper.addUser(email, password);
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        finish(); // 关闭注册页面
                    } else {
                        Toast.makeText(RegisterActivity.this, "电子邮件已注册", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
