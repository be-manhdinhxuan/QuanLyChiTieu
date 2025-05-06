package com.example.quanlychitieu.presentation.features.language;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.DialogLanguageBinding;
import com.example.quanlychitieu.presentation.features.main.navigation.screen.MainActivity;
import java.util.Locale;

public class LanguageSelectionDialog extends DialogFragment {
    private static final String TAG = "LanguageSelectionDialog";
    private DialogLanguageBinding binding;
    private LanguageSelectedCallback callback;
    private boolean showPreselectedLanguage = true;

    public interface LanguageSelectedCallback {
        void onLanguageSelected(int language);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        binding = DialogLanguageBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false); // Không cho phép đóng dialog bằng back button

        // Nếu không hiển thị ngôn ngữ đã chọn trước đó, bỏ chọn cả hai radio button
        if (!showPreselectedLanguage) {
            binding.radioVietnamese.setChecked(false);
            binding.radioEnglish.setChecked(false);
        } else {
            // Lấy ngôn ngữ hiện tại từ SharedPreferences
            int savedLanguage = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getInt("language", -1);
                
            Log.d(TAG, "Saved language from SharedPreferences: " + savedLanguage);
            
            // Nếu chưa có ngôn ngữ được lưu, xác định ngôn ngữ hiện tại từ Locale
            if (savedLanguage == -1) {
                Locale currentLocale;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    currentLocale = requireContext().getResources().getConfiguration().getLocales().get(0);
                } else {
                    currentLocale = requireContext().getResources().getConfiguration().locale;
                }
                
                String languageCode = currentLocale.getLanguage();
                Log.d(TAG, "Current system language code: " + languageCode);
                
                // Xác định ngôn ngữ dựa trên mã ngôn ngữ
                savedLanguage = "vi".equals(languageCode) ? 0 : 1;
                
                // Lưu ngôn ngữ vào SharedPreferences
                requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("language", savedLanguage)
                    .apply();
                    
                Log.d(TAG, "Determined and saved language: " + savedLanguage);
            }
            
            // Chọn radio button tương ứng
            binding.radioVietnamese.setChecked(savedLanguage == 0);
            binding.radioEnglish.setChecked(savedLanguage == 1);
            
            Log.d(TAG, "Set radio buttons: Vietnamese=" + (savedLanguage == 0) + ", English=" + (savedLanguage == 1));
        }

        setupListeners();
        return dialog;
    }

    private void setupListeners() {
        binding.layoutVietnamese.setOnClickListener(v -> selectLanguage(0));
        binding.layoutEnglish.setOnClickListener(v -> selectLanguage(1));
        binding.radioVietnamese.setOnClickListener(v -> selectLanguage(0));
        binding.radioEnglish.setOnClickListener(v -> selectLanguage(1));
    }

    private void selectLanguage(int lang) {
        Log.d(TAG, "Language selected: " + (lang == 0 ? "Vietnamese" : "English"));
        
        // Lưu cài đặt ngôn ngữ và đánh dấu không còn là lần đầu nữa
        requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putInt("language", lang)
            .putBoolean("first_time", false)  // Quan trọng: Đặt first_time thành false
            .apply();

        // Áp dụng ngôn ngữ mới
        Locale newLocale = new Locale(lang == 0 ? "vi" : "en");
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;
        requireContext().getResources().updateConfiguration(config, 
            requireContext().getResources().getDisplayMetrics());

        if (callback != null) {
            callback.onLanguageSelected(lang);
        }
        dismiss();
    }

    public void setCallback(LanguageSelectedCallback callback) {
        this.callback = callback;
    }
    
    // Thêm phương thức mới để kiểm soát việc hiển thị ngôn ngữ đã chọn trước đó
    public void setShowPreselectedLanguage(boolean show) {
        this.showPreselectedLanguage = show;
    }
}