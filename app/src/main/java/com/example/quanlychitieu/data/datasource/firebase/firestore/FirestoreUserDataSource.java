package com.example.quanlychitieu.data.datasource.firebase.firestore;

import android.net.Uri;

import com.example.quanlychitieu.domain.model.user.User;
import com.google.android.gms.tasks.Task;

/**
 * Interface for user related operations in Firestore
 */
public interface FirestoreUserDataSource {
    /**
     * Create a new user in Firestore. Firestore should generate the ID.
     * @param user User object with initial data (ID field might be ignored).
     * @return Task containing the created User object with the generated ID,
     * or null/exception if the operation fails.
     */
    Task<User> createUser(User user); // <-- THAY ĐỔI: Trả về Task<User> thay vì Task<Void>

    /**
     * Get user details from Firestore
     * @param userId Firebase Auth user ID (or the document ID in 'users' collection)
     * @return Task containing User object, or null/exception if not found or error.
     */
    Task<User> getUser(String userId);

    /**
     * Update user information in Firestore.
     * @param userId The ID of the user document to update.
     * @param user Updated user object (should contain the data to be updated).
     * @return Task for operation completion.
     */
    Task<Void> updateUser(String userId, User user);

    /**
     * Upload avatar image (e.g., to Firebase Storage or Cloudinary)
     * and potentially update the user profile immediately or return the URL.
     * (Consider if this belongs here or in a separate StorageDataSource)
     * @param userId Firebase Auth user ID (or user document ID).
     * @param imageUri URI of the image to upload.
     * @return Task containing the download URL of the uploaded image.
     */
    Task<String> uploadUserAvatar(String userId, Uri imageUri);

    /**
     * Update only the user avatar URL field in Firestore.
     * (Useful after uploading the image separately).
     * @param userId Firebase Auth user ID (or user document ID).
     * @param avatarUrl New avatar URL.
     * @return Task for operation completion.
     */
    Task<Void> updateUserAvatar(String userId, String avatarUrl);

    /**
     * Update only the user's money balance field in Firestore.
     * @param userId Firebase Auth user ID (or user document ID).
     * @param amount New money amount.
     * @return Task for operation completion.
     */
    Task<Void> updateUserMoney(String userId, int amount);

    /**
     * Delete user data from Firestore.
     * (Consider if related data like spendings should also be deleted here or elsewhere).
     * @param userId Firebase Auth user ID (or user document ID).
     * @return Task for operation completion.
     */
    Task<Void> deleteUser(String userId);

    /**
     * Check if an email exists in the user collection.
     * @param email The email to check.
     * @return Task containing Boolean (true if exists, false otherwise).
     */
    Task<Boolean> isEmailExists(String email);

    // getCurrentUser() might not be needed here as the Repository usually gets the ID from FirebaseAuthDataSource
    // and then calls getUser(userId). If needed, define it:
    /*
    Task<User> getCurrentUser();
    */
}