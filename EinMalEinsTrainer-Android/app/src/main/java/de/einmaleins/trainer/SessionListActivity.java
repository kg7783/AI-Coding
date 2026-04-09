package de.einmaleins.trainer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

public class SessionListActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageButton btnBack;
    private ImageButton btnClearAll;
    private Handler handler;
    private Runnable longClickRunnable;
    private boolean isLongClickTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager cm = new ConfigManager(this);
        applyLanguage(cm.getLanguage());
        setContentView(R.layout.activity_session_list);

        handler = new Handler(Looper.getMainLooper());

        initViews();
        setupViewPager();
        setupTabs();
        setupListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnBack = findViewById(R.id.btnBack);
        btnClearAll = findViewById(R.id.btnClearAll);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                btnClearAll.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_sitzungen);
                    break;
                case 1:
                    tab.setText(R.string.tab_auswertung);
                    break;
                case 2:
                    tab.setText(R.string.tab_reihen);
                    break;
            }
        }).attach();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnClearAll.setOnClickListener(v -> {
            SessionListFragment fragment = (SessionListFragment) getSupportFragmentManager()
                    .findFragmentByTag("f0");
            if (fragment != null) {
                fragment.getView().post(() -> {
                    android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_alle_loeschen_title)
                            .setMessage(R.string.dialog_alle_loeschen_message)
                            .setPositiveButton(R.string.btn_loeschen, (d, which) -> {
                                de.einmaleins.trainer.SessionManager manager = 
                                        new de.einmaleins.trainer.SessionManager(this);
                                manager.clearAllSessions();
                                fragment.updateSessions();
                            })
                            .setNegativeButton(R.string.btn_abbrechen, null)
                            .create();
                    dialog.show();
                });
            }
        });

        longClickRunnable = () -> {
            isLongClickTriggered = true;
            showTestDataDialog();
        };

        btnClearAll.setOnLongClickListener(v -> {
            isLongClickTriggered = false;
            Toast.makeText(this, R.string.toast_testdaten, Toast.LENGTH_SHORT).show();
            handler.postDelayed(longClickRunnable, 5000);
            return true;
        });

        btnClearAll.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP || 
                event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                handler.removeCallbacks(longClickRunnable);
            }
            return false;
        });
    }

    private void showTestDataDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_testdaten_title)
                .setMessage(R.string.dialog_testdaten_message)
                .setPositiveButton(R.string.btn_ja, (d, which) -> {
                    TestDataGenerator.generateTestSessions(this);
                    SessionListFragment fragment = (SessionListFragment) getSupportFragmentManager()
                            .findFragmentByTag("f0");
                    if (fragment != null) {
                        fragment.updateSessions();
                    }
                })
                .setNegativeButton(R.string.btn_nein, null)
                .show();
    }
    
    private void applyLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
