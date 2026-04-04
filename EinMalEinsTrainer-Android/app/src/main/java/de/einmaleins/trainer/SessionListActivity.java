package de.einmaleins.trainer;

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
                    tab.setText("Sitzungen");
                    break;
                case 1:
                    tab.setText("Auswertung");
                    break;
                case 2:
                    tab.setText("Reihen");
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
                            .setTitle("Alle löschen")
                            .setMessage("Möchtest du wirklich alle Sitzungen löschen?")
                            .setPositiveButton("Löschen", (d, which) -> {
                                de.einmaleins.trainer.SessionManager manager = 
                                        new de.einmaleins.trainer.SessionManager(this);
                                manager.clearAllSessions();
                                fragment.updateSessions();
                            })
                            .setNegativeButton("Abbrechen", null)
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
            Toast.makeText(this, "5 Sekunden halten für Testdaten...", Toast.LENGTH_SHORT).show();
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
                .setTitle("Testdaten generieren")
                .setMessage("Möchtest du 6 Test-Sessions erstellen?")
                .setPositiveButton("Ja", (d, which) -> {
                    TestDataGenerator.generateTestSessions(this);
                    SessionListFragment fragment = (SessionListFragment) getSupportFragmentManager()
                            .findFragmentByTag("f0");
                    if (fragment != null) {
                        fragment.updateSessions();
                    }
                })
                .setNegativeButton("Nein", null)
                .show();
    }
}
