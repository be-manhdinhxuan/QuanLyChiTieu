package com.example.quanlychitieu.data.datasource.firebase.storage.impl;

import android.net.Uri;
import android.util.Log;

import com.example.quanlychitieu.data.datasource.firebase.storage.FirebaseStorageDataSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import javax.inject.Inject;

/**
 * Implementation of FirebaseStorageDataSource
 * Handles all Firebase Storage operations
 */
public class FirebaseStorageDataSourceImpl implements FirebaseStorageDataSource {
    private static final String TAG = "FirebaseStorageDataSource";
    private final FirebaseStorage storage;

    @Inject
    public FirebaseStorageDataSourceImpl() {
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public Task<String> uploadFile(String path, Uri fileUri) {
        if (fileUri == null) {
            return Tasks.forException(new IllegalArgumentException("File URI cannot be null"));
        }

        StorageReference fileRef = storage.getReference().child(path);
        
        return fileRef.putFile(fileUri)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error uploading file", task.getException());
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            })
            .continueWith(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting download URL", task.getException());
                    throw task.getException();
                }
                return task.getResult().toString();
            });
    }

    @Override
    public Task<String> getDownloadUrl(String path) {
        StorageReference fileRef = storage.getReference().child(path);
        
        return fileRef.getDownloadUrl()
            .continueWith(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting download URL", task.getException());
                    throw task.getException();
                }
                return task.getResult().toString();
            });
    }

    @Override
    public Task<Void> deleteFile(String path) {
        StorageReference fileRef = storage.getReference().child(path);
        
        return fileRef.delete()
            .continueWith(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error deleting file", task.getException());
                    throw task.getException();
                }
                return null;
            });
    }
}