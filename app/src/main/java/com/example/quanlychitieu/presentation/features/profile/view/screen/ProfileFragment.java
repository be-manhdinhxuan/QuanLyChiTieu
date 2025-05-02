package com.example.quanlychitieu.presentation.features.profile.view.screen;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.quanlychitieu.R;
import com.example.quanlychitieu.core.setting.localization.AppLocalizationsSetup;
import com.example.quanlychitieu.databinding.FragmentProfileBinding;
import com.example.quanlychitieu.domain.model.user.User;
import com.example.quanlychitieu.presentation.features.auth.login.screen.LoginActivity;
import com.example.quanlychitieu.presentation.features.history.screen.HistoryActivity;
import com.example.quanlychitieu.presentation.features.main.navigation.screen.MainActivity;
import com.example.quanlychitieu.presentation.features.profile.about.screen.AboutActivity;
import com.example.quanlychitieu.presentation.features.profile.edit.screen.EditProfileActivity;
import com.example.quanlychitieu.presentation.features.profile.password.screen.ChangePasswordActivity;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private int language = 0;
    private boolean darkMode = false;
    private boolean loginMethod = false;
    private NumberFormat numberFormat;
    public static final String BALANCE_UPDATED = "balance_updated";
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các biến
        db = FirebaseFirestore.getInstance();
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Thiết lập các chức năng
        loadUserData();
        setupViews();
        setupListeners();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        binding.textEmail.setText(userEmail);

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = User.fromFirebase(documentSnapshot);

                    // Cập nhật UI
                    binding.textName.setText(currentUser.getName() != null && !currentUser.getName().isEmpty()
                        ? currentUser.getName() : "Người dùng");
                    binding.textMoney.setText(numberFormat.format(currentUser.getMoney()));

                    // Broadcast thông báo số dư đã được cập nhật
                    Intent intent = new Intent(BALANCE_UPDATED);
                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

                    // Load avatar
                    String avatarUrl = currentUser.getAvatar();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.imageAvatar);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupViews() {
        // Lấy cài đặt từ SharedPreferences
        language = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("language", 0); // 0 = Vietnamese, 1 = English
        darkMode = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("darkMode", false);
        
        // Kiểm tra phương thức đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String providerId = currentUser.getProviderData().get(1).getProviderId();
            boolean isEmailLogin = "password".equals(providerId);
            
            // Cập nhật UI dựa trên phương thức đăng nhập
            if (binding.itemChangePassword != null) {
                binding.itemChangePassword.setVisibility(isEmailLogin ? View.VISIBLE : View.GONE);
            }
        }

    }

    private void setupListeners() {
        binding.itemAccount.setOnClickListener(v -> navigateToEditProfile());
        binding.itemChangePassword.setOnClickListener(v -> navigateToChangePassword());
        binding.itemLanguage.setOnClickListener(v -> showLanguageBottomSheet());
        binding.itemHistory.setOnClickListener(v -> navigateToHistory());
        binding.itemAbout.setOnClickListener(v -> navigateToAbout());
        binding.buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            navigateToLogin();
        });


    }

    private void navigateToEditProfile() {
        if (currentUser != null) {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "Đang tải thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToChangePassword() {
        startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
    }

    private void navigateToHistory() {
        startActivity(new Intent(requireContext(), HistoryActivity.class));
    }

    private void navigateToAbout() {
        startActivity(new Intent(requireContext(), AboutActivity.class));
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showLanguageBottomSheet() {
        // Hiển thị dialog chọn ngôn ngữ
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.dialog_language);

        // Lấy các view trong dialog
        RadioButton radioVietnamese = dialog.findViewById(R.id.radioVietnamese);
        RadioButton radioEnglish = dialog.findViewById(R.id.radioEnglish);
        View layoutVietnamese = dialog.findViewById(R.id.layoutVietnamese);
        View layoutEnglish = dialog.findViewById(R.id.layoutEnglish);

        // Set trạng thái RadioButton dựa trên ngôn ngữ hiện tại
        // language = 0 là tiếng Việt, language = 1 là tiếng Anh
        if (language == 0) {
            radioVietnamese.setChecked(true);
            radioEnglish.setChecked(false);
        } else {
            radioVietnamese.setChecked(false);
            radioEnglish.setChecked(true);
        }

        // Xử lý sự kiện click vào layout tiếng Việt
        layoutVietnamese.setOnClickListener(v -> {
            if (language != 0) {
                changeLanguage(0);
                dialog.dismiss();
            }
        });

        // Xử lý sự kiện click vào layout tiếng Anh
        layoutEnglish.setOnClickListener(v -> {
            if (language != 1) {
                changeLanguage(1);
                dialog.dismiss();
            }
        });

        // Xử lý sự kiện click vào RadioButton tiếng Việt
        radioVietnamese.setOnClickListener(v -> {
            if (language != 0) {
                changeLanguage(0);
                dialog.dismiss();
            }
        });

        // Xử lý sự kiện click vào RadioButton tiếng Anh
        radioEnglish.setOnClickListener(v -> {
            if (language != 1) {
                changeLanguage(1);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeLanguage(int lang) {
        if (lang != language) {
            language = lang;
            // Lưu ngôn ngữ mới vào SharedPreferences
            requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putInt("language", language)
                .apply();

            // Thay đổi ngôn ngữ
            Locale newLocale = new Locale(lang == 0 ? "vi" : "en");
            Locale.setDefault(newLocale);
            Configuration config = new Configuration();
            config.locale = newLocale;
            requireContext().getResources().updateConfiguration(config, 
                requireContext().getResources().getDisplayMetrics());

            // Khởi động lại ứng dụng
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Reload user data when returning from EditProfileActivity
        loadUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}