package com.example.quanlychitieu;

import android.content.Context;
import android.content.Intent;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.quanlychitieu.models.User;
import android.util.Log;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;

    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void initGoogleSignIn(Context context) {
        this.context = context;
        try {
            String clientId = context.getString(R.string.default_web_client_id);
            Log.d(TAG, "Initializing Google Sign In with client ID: " + clientId);
            
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Sign In", e);
        }
    }

    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User is logged in: " + currentUser.getEmail());
            Log.d(TAG, "Provider: " + currentUser.getProviderId());
            return true;
        }
        Log.d(TAG, "No user is logged in");
        return false;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void registerUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void resetPassword(String email, OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    public Intent getGoogleSignInIntent() {
        if (mGoogleSignInClient == null) {
            throw new IllegalStateException("Google Sign In not initialized");
        }
        return mGoogleSignInClient.getSignInIntent();
    }

    public void handleGoogleSignInResult(Task<GoogleSignInAccount> task, OnCompleteListener<AuthResult> listener) {
        try {
            GoogleSignInAccount account;
            if (task != null && task.isSuccessful()) {
                account = task.getResult(ApiException.class);
            } else {
                if (context == null) {
                    throw new IllegalStateException("Context not initialized. Call initGoogleSignIn first.");
                }
                account = GoogleSignIn.getLastSignedInAccount(context);
                if (account == null) {
                    throw new ApiException(new Status(CommonStatusCodes.SIGN_IN_REQUIRED));
                }
            }
            
            Log.d(TAG, "Google account retrieved: " + account.getEmail());
            
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(listener);
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed with code: " + e.getStatusCode(), e);
            listener.onComplete(Tasks.forException(e));
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during Google sign in", e);
            listener.onComplete(Tasks.forException(e));
        }
    }

    public void handleFacebookAccessToken(AccessToken token, OnCompleteListener<AuthResult> listener) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(listener);
    }

    public void saveUserToFirestore(User user, OnCompleteListener<Void> listener) {
        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnCompleteListener(listener);
    }

    public void updateUserLastLogin(String uid) {
        db.collection("users")
                .document(uid)
                .update("lastLoginAt", System.currentTimeMillis());
    }

    public void signOut() {
        mAuth.signOut();
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
        }
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public void getUserData(String uid, OnSuccessListener<DocumentSnapshot> listener) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error getting user data", e));
    }
}