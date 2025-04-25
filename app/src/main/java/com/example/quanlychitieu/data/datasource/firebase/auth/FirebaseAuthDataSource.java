package com.example.quanlychitieu.data.datasource.firebase.auth;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public interface FirebaseAuthDataSource {
    /**
     * Sign in user with email and password
     */
    Task<AuthResult> signIn(String email, String password);

    /**
     * Create new user with email and password
     */
    Task<AuthResult> signUp(String email, String password);

    /**
     * Update user display name
     */
    Task<Void> updateProfile(String displayName);

    /**
     * Send password reset email
     */
    Task<Void> sendPasswordResetEmail(String email);

    /**
     * Sign out current user
     */
    Task<Void> signOut();

    /**
     * Check if user is currently signed in
     */
    boolean isUserSignedIn();

    /**
     * Get current Firebase user
     */
    FirebaseUser getCurrentUser();

    /**
     * Get current user ID
     */
    String getCurrentUserId();
}
