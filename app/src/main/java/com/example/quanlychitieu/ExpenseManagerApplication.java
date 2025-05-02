package com.example.quanlychitieu;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ExpenseManagerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Khởi tạo cài đặt ngôn ngữ mặc định nếu chưa có
        boolean isFirstTime = getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("first_time", true);
            
        if (isFirstTime) {
            // Nếu là lần đầu, mặc định là tiếng Việt
            getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putInt("language", 0) // 0 = Tiếng Việt
                .putBoolean("first_time", true)
                .apply();
                
            // Áp dụng ngôn ngữ mặc định (tiếng Việt)
            Locale locale = new Locale("vi");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        } else {
            // Nếu không phải lần đầu, áp dụng ngôn ngữ đã lưu
            int savedLanguage = getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getInt("language", 0);
            
            Locale locale = new Locale(savedLanguage == 0 ? "vi" : "en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }
}