package com.example.jobsearchapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobsearchapp.DatabaseHelper;
import com.example.jobsearchapp.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword;
    private RadioButton rbCandidate, rbEmployer;
    private Button btnRegister;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        rbCandidate = findViewById(R.id.rbCandidate);
        rbEmployer = findViewById(R.id.rbEmployer);

        btnRegister = findViewById(R.id.btnRegister);

        databaseHelper = new DatabaseHelper(this);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        String role = "";

        if (rbCandidate.isChecked()) {
            role = "candidate";
        } else if (rbEmployer.isChecked()) {
            role = "employer";
        }

        if (TextUtils.isEmpty(name)
                || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(role)) {

            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (databaseHelper.checkEmail(email)) {

            Toast.makeText(
                    this,
                    "Email đã tồn tại",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        boolean result = databaseHelper.insertUser(
                name,
                email,
                password,
                role
        );

        if (result) {

            Toast.makeText(
                    this,
                    "Đăng ký thành công",
                    Toast.LENGTH_SHORT
            ).show();

            edtName.setText("");
            edtEmail.setText("");
            edtPassword.setText("");

        } else {

            Toast.makeText(
                    this,
                    "Đăng ký thất bại",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}