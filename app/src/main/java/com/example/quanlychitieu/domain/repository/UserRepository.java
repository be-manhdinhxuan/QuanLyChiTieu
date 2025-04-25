package com.example.quanlychitieu.domain.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.quanlychitieu.domain.model.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Interface định nghĩa các thao tác trên user repository
 */
public interface UserRepository {
    /**
     * Lấy thông tin người dùng từ repository theo userId
     */
    Task<User> getUser(String userId);

    /**
     * Lấy thông tin người dùng hiện tại
     */
    Task<User> getCurrentUser();

    /**
     * Tạo người dùng mới
     */
    Task<Void> createUser(User user);

    /**
     * Cập nhật thông tin người dùng
     */
    Task<Void> updateUser(User user, Uri imageUri);

    /**
     * Xóa tài khoản người dùng
     */
    Task<Void> deleteUser();

    /**
     * Cập nhật tiền hàng tháng của người dùng
     */
    Task<Void> updateUserMoney(int money);

    /**
     * Kiểm tra xem email đã tồn tại chưa
     */
    Task<Boolean> isEmailExists(String email);
}