package com.example.quanlychitieu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.*;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.AccessToken;
import com.example.quanlychitieu.models.User;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn, btnGoogle, btnFacebook;
    private TextView tvForgotPassword, tvRegisterNow;
    private FirebaseManager firebaseManager;
    private CallbackManager callbackManager;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignIn(task);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo CallbackManager trước khi sử dụng
        callbackManager = CallbackManager.Factory.create();

        firebaseManager = FirebaseManager.getInstance();
        firebaseManager.initGoogleSignIn(this);

        checkLoggedInUser();
        // Khởi tạo views TRƯỚC
        initViews();
        setupListeners();
        setupFacebookCallback();
        
        // Sau đó mới kiểm tra silent sign-in
        GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (lastAccount != null && !lastAccount.isExpired()) {
            Log.d(TAG, "Found cached Google account: " + lastAccount.getEmail());
            handleGoogleSignIn(GoogleSignIn.getSignedInAccountFromIntent(null));
        }
        

    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnSignIn.setOnClickListener(v -> performLogin());
        
        tvRegisterNow.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        btnGoogle.setOnClickListener(v -> startGoogleSignIn());

        btnFacebook.setOnClickListener(v -> startFacebookLogin());
    }

    private void checkLoggedInUser() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "User already logged in: " + user.getEmail());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Log.d(TAG, "No user logged in, checking Google account...");
            GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (lastAccount != null && !lastAccount.isExpired()) {
                Log.d(TAG, "Found cached Google account: " + lastAccount.getEmail());
                firebaseManager.handleGoogleSignInResult(null, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        handleSuccessfulLogin(firebaseUser, "google");
                    } else {
                        Log.e(TAG, "Silent Google sign-in failed", task.getException());
                    }
                });
            }
        }
    }


    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);
        
        firebaseManager.loginUser(email, password, task -> {
            showLoading(false);
            
            if (task.isSuccessful()) {
                Log.d(TAG, "Email login successful");
                handleSuccessfulLogin(task.getResult().getUser(), "email");
            } else {
                runOnUiThread(() -> {
                    String errorMessage;
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMessage = "Email hoặc mật khẩu không chính xác";
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        errorMessage = "Tài khoản không tồn tại";
                    } else {
                        errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : 
                            "Đăng nhập thất bại";
                    }
                    
                    Toast.makeText(LoginActivity.this, 
                        "Lỗi: " + errorMessage, 
                        Toast.LENGTH_SHORT).show();
                    
                    // Clear password field for security
                    etPassword.setText("");
                });
            }
        });
    }

    private void startGoogleSignIn() {
        GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (lastAccount != null && !lastAccount.isExpired()) {
            Log.d(TAG, "Using cached Google account");
            handleGoogleSignIn(null); // Pass null for silent sign-in
        } else {
            Intent signInIntent = firebaseManager.getGoogleSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> task) {
        if (task == null || !task.isSuccessful()) {
            Log.e(TAG, "Google sign-in task is null or unsuccessful");
            return;
        }

        showLoading(true);
        
        firebaseManager.handleGoogleSignInResult(task, authResult -> {
            showLoading(false);
            
            if (authResult.isSuccessful()) {
                Log.d(TAG, "Firebase auth successful");
                FirebaseUser user = authResult.getResult().getUser();
                handleSuccessfulLogin(user, "google");
            } else {
                runOnUiThread(() -> {
                    Log.e(TAG, "Firebase auth failed", authResult.getException());
                    String errorMessage = authResult.getException() != null ? 
                        authResult.getException().getMessage() : 
                        "Đăng nhập Google thất bại";
                    Toast.makeText(LoginActivity.this, 
                        "Lỗi: " + errorMessage, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupFacebookCallback() {
        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookLogin(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(LoginActivity.this, 
                        "Đăng nhập Facebook đã bị hủy", 
                        Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(LoginActivity.this,
                        "Lỗi đăng nhập Facebook: " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void startFacebookLogin() {
        btnFacebook.setEnabled(false);
        // Đảm bảo callbackManager đã được khởi tạo
        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create();
            setupFacebookCallback();
        }
        LoginManager.getInstance().logInWithReadPermissions(this,
            Arrays.asList("email", "public_profile"));
    }

    private void handleFacebookLogin(AccessToken token) {
        Log.d(TAG, "Handling Facebook login with token: " + token.getToken());
        
        firebaseManager.handleFacebookAccessToken(token, task -> {
            btnFacebook.setEnabled(true);
            
            if (task.isSuccessful()) {
                Log.d(TAG, "Facebook login successful");
                handleSuccessfulLogin(task.getResult().getUser(), "facebook");
            } else {
                Log.e(TAG, "Facebook login failed", task.getException());
                String errorMessage = task.getException() != null ?
                    task.getException().getMessage() :
                    "Đăng nhập Facebook thất bại";
                Toast.makeText(LoginActivity.this,
                    "Lỗi: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulLogin(FirebaseUser firebaseUser, String provider) {
        Log.d(TAG, "Login successful for " + firebaseUser.getEmail() + " via " + provider);
        Log.d(TAG, "Navigating to MainActivity immediately...");

        // *** 1. CHUYỂN MÀN HÌNH NGAY LẬP TỨC ***
        showLoading(false);
        runOnUiThread(() -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // *** 2. THỰC HIỆN LƯU DỮ LIỆU VÀO FIRESTORE Ở CHẾ ĐỘ NỀN ***
        Log.d(TAG, "Attempting to save user data to Firestore in background...");

        // Tạo user mới với các giá trị mặc định
        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                provider
        );

        // Thiết lập các giá trị mặc định cho các trường mới
        user.setName(firebaseUser.getDisplayName() != null ? 
                firebaseUser.getDisplayName() : 
                firebaseUser.getEmail());
        user.setAvatar(firebaseUser.getPhotoUrl() != null ? 
                firebaseUser.getPhotoUrl().toString() : 
                null); // Constructor sẽ tự set DEFAULT_AVATAR nếu null
        user.setGender(true); // Mặc định là Nam
        user.setMoney(0); // Mặc định là 0
        // Birthday sẽ được người dùng cập nhật sau

        // Kiểm tra xem user đã tồn tại chưa
        firebaseManager.getUserData(firebaseUser.getUid(), userDoc -> {
            if (userDoc.exists()) {
                // Nếu user đã tồn tại, chỉ cập nhật lastLoginAt và các thông tin cơ bản
                User existingUser = User.fromFirestore(userDoc);
                if (existingUser != null) {
                    // Giữ nguyên các thông tin cũ như birthday, gender, money
                    // Chỉ cập nhật các thông tin từ provider mới nếu cần
                    User updatedUser = existingUser.copyWith(
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : existingUser.getName(),
                        existingUser.getBirthday(),
                        firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : existingUser.getAvatar(),
                        existingUser.isGender(),
                        existingUser.getMoney()
                    );
                    updatedUser.setLastLoginAt(System.currentTimeMillis());
                    
                    firebaseManager.saveUserToFirestore(updatedUser, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Updated existing user data: " + updatedUser.getUid());
                        } else {
                            logFirestoreError(task.getException());
                        }
                    });
                }
            } else {
                // Nếu là user mới, lưu toàn bộ thông tin
                firebaseManager.saveUserToFirestore(user, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Created new user: " + user.getUid());
                    } else {
                        logFirestoreError(task.getException());
                    }
                });
            }
        });
    }

    private void logFirestoreError(Exception exception) {
        Log.e(TAG, "Firestore operation failed", exception);
        runOnUiThread(() -> {
            String errorMessage = "Lỗi lưu dữ liệu người dùng.";
            if (exception != null) {
                errorMessage += " Nguyên nhân: " + exception.getMessage();
            }
            Toast.makeText(getApplicationContext(),
                    errorMessage,
                    Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Thêm kiểm tra null
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Thêm phương thức để hiển thị/ẩn loading indicator
    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            if (btnSignIn != null) btnSignIn.setEnabled(!show);
            if (btnGoogle != null) btnGoogle.setEnabled(!show);
            if (btnFacebook != null) btnFacebook.setEnabled(!show);
            if (etEmail != null) etEmail.setEnabled(!show);
            if (etPassword != null) etPassword.setEnabled(!show);
        });
    }
}
