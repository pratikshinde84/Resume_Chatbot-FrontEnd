package com.pratik.resumechatbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pratik.resumechatbot.api.RetrofitClient;
import com.pratik.resumechatbot.fragment.ChatFragment;
import com.pratik.resumechatbot.fragment.ManagerFragment;
import com.pratik.resumechatbot.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ChatbotPrefs";
    private static final String KEY_IP = "backend_ip";
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setSupportActionBar(findViewById(R.id.toolbar));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedIp = prefs.getString(KEY_IP, null);

        if (savedIp != null) {
            RetrofitClient.setBaseUrl(savedIp);
        }

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_manager) {
                selected = new ManagerFragment();
            } else if (item.getItemId() == R.id.nav_chat) {
                selected = new ChatFragment();
            }
            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ManagerFragment())
                    .commit();
            
            if (savedIp == null) {
                showIpDialog();
            } else {
                viewModel.fetchResumes();
            }
        }
    }

    private void showIpDialog() {
        EditText input = new EditText(this);
        input.setHint("10.204.192.51");
        new AlertDialog.Builder(this)
                .setTitle("Enter Backend IP")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String ip = input.getText().toString().trim();
                    if (!ip.isEmpty()) {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(KEY_IP, ip)
                                .apply();
                        RetrofitClient.setBaseUrl(ip);
                        viewModel.fetchResumes();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
