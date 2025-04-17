package com.example.quanlychitieu;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputEditText etPassword;
    private MaterialButton btnGui;
    private Toolbar toolbar;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseManager = FirebaseManager.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etPassword = findViewById(R.id.etPassword);
        btnGui = findViewById(R.id.btnGui);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupListeners() {
        btnGui.setOnClickListener(v -> performPasswordChange());
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void performPasswordChange() {
        String newPassword = etPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, 
                            "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, 
                            "Lỗi: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            // Chuyển về màn hình đăng nhập
            finish();
        }
    }
}