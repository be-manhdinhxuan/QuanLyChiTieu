package com.example.quanlychitieu.data.datasource.firebase.auth.impl;

import android.util.Log;
import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseAuthDataSourceImpl implements FirebaseAuthDataSource {
  private static final String TAG = "FirebaseAuthDataSource";
  private final FirebaseAuth auth;

  @Inject
  public FirebaseAuthDataSourceImpl(FirebaseAuth auth) {
    this.auth = auth;
  }

  @Override
  public Task<AuthResult> signIn(String email, String password) {
    return auth.signInWithEmailAndPassword(email, password);
  }

  @Override
  public Task<AuthResult> signUp(String email, String password) {
    return auth.createUserWithEmailAndPassword(email, password);
  }

  @Override
  public Task<Void> updateProfile(String displayName) {
    FirebaseUser user = auth.getCurrentUser();
    if (user == null) {
      Log.e(TAG, "updateProfile: No user is signed in");
      return null;
    }

    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
        .setDisplayName(displayName)
        .build();

    return user.updateProfile(profileUpdates);
  }

  @Override
  public Task<Void> sendPasswordResetEmail(String email) {
    return auth.sendPasswordResetEmail(email);
  }

  @Override
  public Task<Void> signOut() {
    auth.signOut();
    // Since Firebase signOut() doesn't return a Task, we need to create a completed
    // task
    return Tasks.forResult(null);
  }

  @Override
  public boolean isUserSignedIn() {
    return auth.getCurrentUser() != null;
  }

  @Override
  public FirebaseUser getCurrentUser() {
    return auth.getCurrentUser();
  }

  @Override
  public String getCurrentUserId() {
    FirebaseUser user = auth.getCurrentUser();
    return user != null ? user.getUid() : null;
  }
}
