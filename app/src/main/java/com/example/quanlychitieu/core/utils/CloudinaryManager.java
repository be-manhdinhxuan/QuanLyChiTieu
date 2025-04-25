package com.example.quanlychitieu.core.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

// Thay đổi import từ com.cloudinary.android.core.FileUtil sang FileUtil của project
import com.example.quanlychitieu.core.utils.FileUtil;

public class CloudinaryManager {
    private static final String CLOUD_NAME = "dhhdfhm3q"; // Lấy từ dashboard
    private static final String API_KEY = "755342635491431"; // Lấy từ dashboard
    private static final String API_SECRET = "BJVw7aRe6qTEZiu4ax8D1BhYrIU"; // Lấy từ dashboard
    private static final String UPLOAD_PRESET = "quanlychitieu_unsigned"; // Tạo trong dashboard

    private static CloudinaryManager instance;
    private final Cloudinary cloudinary;
    private final MediaManager mediaManager;

    private CloudinaryManager(Context context) {
        // Khởi tạo Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);
        cloudinary = new Cloudinary(config);

        // Khởi tạo MediaManager cho Android
        MediaManager.init(context, config);
        mediaManager = MediaManager.get();
    }

    public static synchronized CloudinaryManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudinaryManager(context.getApplicationContext());
        }
        return instance;
    }

    public void uploadImage(Context context, Uri imageUri, String folder, UploadCallback callback) {
        try {
            File imageFile = FileUtil.getFileFromUri(context, imageUri);

            // Upload async
            new Thread(() -> {
                try {
                    Map<String, Object> options = new HashMap<>();
                    options.put("folder", folder);
                    options.put("resource_type", "image");
                    options.put("upload_preset", UPLOAD_PRESET);

                    Map uploadResult = cloudinary.uploader().upload(imageFile, options);
                    String imageUrl = (String) uploadResult.get("secure_url");

                    new Handler(Looper.getMainLooper()).post(() ->
                        callback.onSuccess(imageUrl)
                    );
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(e)
                    );
                } finally {
                    imageFile.delete(); // Cleanup cache
                }
            }).start();
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }
}