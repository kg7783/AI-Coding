package de.einmaleins.trainer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SessionListActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageButton btnBack;
    private ImageButton btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

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
    }
}
