package com.example.quanlychitieu;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d("MainActivity", "User logged in: " + currentUser.getEmail());
            Log.d("MainActivity", "Provider: " + currentUser.getProviderId());
        } else {
            Log.d("MainActivity", "No user logged in");
            // Redirect to login if needed
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}