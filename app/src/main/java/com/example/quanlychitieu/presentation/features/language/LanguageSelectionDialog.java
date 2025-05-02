package com.example.quanlychitieu.presentation.features.language;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.example.quanlychitieu.databinding.DialogLanguageBinding;
import java.util.Locale;

public class LanguageSelectionDialog extends DialogFragment {
    private DialogLanguageBinding binding;
    private LanguageSelectedCallback callback;

    public interface LanguageSelectedCallback {
        void onLanguageSelected(int language);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        binding = DialogLanguageBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);

        // Lấy ngôn ngữ hiện tại
        int currentLanguage = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("language", 0); // Mặc định là tiếng Việt (0)

        // Set trạng thái cho radio buttons
        binding.radioVietnamese.setChecked(currentLanguage == 0);
        binding.radioEnglish.setChecked(currentLanguage == 1);

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
        // Lưu cài đặt ngôn ngữ
        requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putInt("language", lang)
            .putBoolean("first_time", false)
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
}