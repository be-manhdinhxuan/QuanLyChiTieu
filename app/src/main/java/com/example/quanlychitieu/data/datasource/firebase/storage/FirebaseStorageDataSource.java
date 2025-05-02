package com.example.quanlychitieu.data.datasource.firebase.storage;

import android.net.Uri;
import com.google.android.gms.tasks.Task;

public interface FirebaseStorageDataSource {
    Task<String> uploadFile(String path, Uri fileUri);
    Task<String> getDownloadUrl(String path);
    Task<Void> deleteFile(String path);
}
