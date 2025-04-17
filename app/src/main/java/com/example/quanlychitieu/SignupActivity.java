package com.example.quanlychitieu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private TextView tvBirthday, tvLoginNow;
    private LinearLayout layoutGenderMale, layoutGenderFemale;
    private MaterialButton btnSignUp;
    private FirebaseManager firebaseManager;
    private String selectedGender = "";
    private boolean isMaleSelected = false;
    private boolean isFemaleSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseManager = FirebaseManager.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvBirthday = findViewById(R.id.tvBirthday);
        layoutGenderMale = findViewById(R.id.layoutGenderMale);
        layoutGenderFemale = findViewById(R.id.layoutGenderFemale);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLoginNow = findViewById(R.id.tvLoginNow);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> performSignup());
        
        tvLoginNow.setOnClickListener(v -> {
            finish(); // Quay lại màn hình login
        });

        tvBirthday.setOnClickListener(v -> showDatePicker());

        layoutGenderMale.setOnClickListener(v -> {
            isMaleSelected = true;
            isFemaleSelected = false;
            selectedGender = "Nam";
            updateGenderSelection();
        });

        layoutGenderFemale.setOnClickListener(v -> {
            isFemaleSelected = true;
            isMaleSelected = false;
            selectedGender = "Nữ";
            updateGenderSelection();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", 
                    dayOfMonth, month + 1, year);
                tvBirthday.setText(selectedDate);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateGenderSelection() {
        // Cập nhật giao diện khi chọn giới tính
        layoutGenderMale.setBackgroundResource(isMaleSelected ? 
            R.drawable.bg_gender_selected : R.drawable.bg_gender_unselected);
        layoutGenderFemale.setBackgroundResource(isFemaleSelected ? 
            R.drawable.bg_gender_selected : R.drawable.bg_gender_unselected);
    }

    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String birthday = tvBirthday.getText().toString().trim();

        if (fullName.isEmpty()) {
            showError("Vui lòng nhập họ tên");
            return false;
        }

        if (email.isEmpty()) {
            showError("Vui lòng nhập email");
            return false;
        }

        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu");
            return false;
        }

        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu không khớp");
            return false;
        }

        if (birthday.equals("Ngày sinh")) {
            showError("Vui lòng chọn ngày sinh");
            return false;
        }

        if (selectedGender.isEmpty()) {
            showError("Vui lòng chọn giới tính");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void performSignup() {
        if (!validateInput()) {
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Disable signup button
        btnSignUp.setEnabled(false);

        firebaseManager.registerUser(email, password, task -> {
            btnSignUp.setEnabled(true);
            
            if (task.isSuccessful()) {
                // TODO: Lưu thông tin user vào Firestore/Realtime Database
                Toast.makeText(SignupActivity.this, 
                    "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();
            } else {
                String errorMessage = task.getException() != null ? 
                    task.getException().getMessage() : 
                    "Đăng ký thất bại";
                Toast.makeText(SignupActivity.this, 
                    "Lỗi: " + errorMessage, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}