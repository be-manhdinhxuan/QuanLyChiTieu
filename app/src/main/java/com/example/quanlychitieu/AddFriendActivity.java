package com.example.quanlychitieu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.graphics.Color;
import android.view.inputmethod.EditorInfo;

public class AddFriendActivity extends AppCompatActivity {
    private EditText friendInput;
    private RecyclerView friendsList;
    private FriendsAdapter adapter;
    private List<String> friends = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        
        // Nhận data từ intent
        friends = getIntent().getStringArrayListExtra("friends");
        colors = getIntent().getIntegerArrayListExtra("colors");
        
        setupViews();
        setupFriendsList();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friendInput = findViewById(R.id.friendInput);
        friendInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addNewFriend();
                return true;
            }
            return false;
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            Intent result = new Intent();
            result.putStringArrayListExtra("friends", new ArrayList<>(friends));
            result.putIntegerArrayListExtra("colors", new ArrayList<>(colors));
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void addNewFriend() {
        String name = friendInput.getText().toString().trim();
        if (!name.isEmpty()) {
            Random random = new Random();
            int color = Color.rgb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
            );
            
            friends.add(name);
            colors.add(color);
            adapter.notifyItemInserted(friends.size() - 1);
            
            friendInput.setText("");
        }
    }

    private void setupFriendsList() {
        friendsList = findViewById(R.id.friendsList);
        adapter = new FriendsAdapter(friends, colors, position -> {
            friends.remove(position);
            colors.remove(position);
            adapter.notifyItemRemoved(position);
        });
        friendsList.setAdapter(adapter);
        friendsList.setLayoutManager(new LinearLayoutManager(this));
    }
}
