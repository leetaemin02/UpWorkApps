package com.example.jobsearchapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobsearchapp.DatabaseHelper;
import com.example.jobsearchapp.R;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

        databaseHelper = new DatabaseHelper(this);

        // Chuyển sang màn hình đăng ký
        txtRegister.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            LoginActivity.this,
                            RegisterActivity.class
                    )
            );

        });

        // Đăng nhập
        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password)) {

                Toast.makeText(
                        LoginActivity.this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            boolean success =
                    databaseHelper.loginUser(email, password);

            if (success) {

                Toast.makeText(
                        LoginActivity.this,
                        "Đăng nhập thành công",
                        Toast.LENGTH_SHORT
                ).show();

                startActivity(
                        new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        )
                );

                finish();

            } else {

                Toast.makeText(
                        LoginActivity.this,
                        "Sai email hoặc mật khẩu",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}