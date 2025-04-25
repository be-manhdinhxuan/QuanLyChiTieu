package com.example.quanlychitieu.data.datasource.firebase.firestore;

import android.net.Uri;

import com.example.quanlychitieu.domain.model.user.User;
import com.google.android.gms.tasks.Task;

/**
 * Interface for user related operations in Firestore
 */
public interface FirestoreUserDataSource {
    /**
     * Create a new user in Firestore
     * @param userId Firebase Auth user ID
     * @param user User object with initial data
     * @return Task for operation completion
     */
    Task<Void> createUser(String userId, User user);

    /**
     * Get user details from Firestore
     * @param userId Firebase Auth user ID
     * @return Task containing User object
     */
    Task<User> getUser(String userId);

    /**
     * Update user information in Firestore
     * @param userId Firebase Auth user ID
     * @param user Updated user object
     * @return Task for operation completion
     */
    Task<Void> updateUser(String userId, User user);

    /**
     * Upload avatar image to Firebase Storage and update user profile
     * @param userId Firebase Auth user ID
     * @param imageUri URI of the image to upload
     * @return Task containing the download URL of the uploaded image
     */
    Task<String> uploadUserAvatar(String userId, Uri imageUri);

    /**
     * Update user avatar URL in Firestore
     * @param userId Firebase Auth user ID
     * @param avatarUrl New avatar URL
     * @return Task for operation completion
     */
    Task<Void> updateUserAvatar(String userId, String avatarUrl);

    /**
     * Update user's money balance
     * @param userId Firebase Auth user ID
     * @param amount New money amount
     * @return Task for operation completion
     */
    Task<Void> updateUserMoney(String userId, int amount);

    /**
     * Delete user from Firestore and all related data
     * @param userId Firebase Auth user ID
     * @return Task for operation completion
     */
    Task<Void> deleteUser(String userId);

    /**
     * Get the current logged-in user
     * @return Task containing the current User object
     */
    Task<User> getCurrentUser();
}
