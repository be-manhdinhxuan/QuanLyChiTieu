package com.example.quanlychitieu;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private MaterialButton btnGui;
    private Toolbar toolbar;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        firebaseManager = FirebaseManager.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etEmail = findViewById(R.id.etEmail);
        btnGui = findViewById(R.id.btnGui);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupListeners() {
        btnGui.setOnClickListener(v -> performPasswordReset());
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void performPasswordReset() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button
        btnGui.setEnabled(false);

        firebaseManager.resetPassword(email, task -> {
            btnGui.setEnabled(true);
            
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPasswordActivity.this, 
                    "Đã gửi email khôi phục mật khẩu", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMessage = task.getException() != null ? 
                    task.getException().getMessage() : 
                    "Không thể gửi email khôi phục";
                Toast.makeText(ForgotPasswordActivity.this, 
                    "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}