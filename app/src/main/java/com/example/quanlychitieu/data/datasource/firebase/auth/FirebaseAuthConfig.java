package com.example.quanlychitieu.data.datasource.firebase.auth;

import com.example.quanlychitieu.data.datasource.firebase.auth.impl.FirebaseAuthDataSourceImpl;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Configuration class for Firebase Authentication.
 * This class provides factory methods for creating Firebase auth related
 * components.
 */
public class FirebaseAuthConfig {

  private static volatile FirebaseAuthDataSource authDataSource;

  /**
   * Get singleton instance of FirebaseAuthDataSource
   *
   * @return FirebaseAuthDataSource instance
   */
  public static FirebaseAuthDataSource getAuthDataSource() {
    if (authDataSource == null) {
      synchronized (FirebaseAuthConfig.class) {
        if (authDataSource == null) {
          FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
          authDataSource = new FirebaseAuthDataSourceImpl(firebaseAuth);
        }
      }
    }
    return authDataSource;
  }

  /**
   * Reset the singleton instance (mainly for testing)
   */
  public static void reset() {
    authDataSource = null;
  }

  /**
   * Check if a user is currently signed in
   *
   * @return true if a user is signed in, false otherwise
   */
  public static boolean isUserSignedIn() {
    return FirebaseAuth.getInstance().getCurrentUser() != null;
  }

  /**
   * Get the current user ID
   *
   * @return user ID if signed in, null otherwise
   */
  public static String getCurrentUserId() {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
  }
}
