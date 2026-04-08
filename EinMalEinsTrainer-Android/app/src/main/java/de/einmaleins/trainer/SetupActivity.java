package de.einmaleins.trainer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SetupActivity extends AppCompatActivity {
    private ConfigManager configManager;
    private EinmaleinsConfig workingConfig;

    private TableLayout tableLayout;
    private Button btnSave;
    private Switch switchAutoSwitch;

    private boolean[][] multipliersSelected = new boolean[10][10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        configManager = new ConfigManager(this);
        workingConfig = configManager.loadConfig();

        ImageButton btnBack = findViewById(R.id.btnBack);
        tableLayout = findViewById(R.id.tableLayout);
        btnSave = findViewById(R.id.btnSave);
        switchAutoSwitch = findViewById(R.id.switchAutoSwitch);

        btnBack.setOnClickListener(v -> finish());

        loadConfigToArrays();
        buildTable();

        switchAutoSwitch.setChecked(workingConfig.autoSwitchMode);

        btnSave.setOnClickListener(v -> saveConfig());
    }

    private void loadConfigToArrays() {
        for (int i = 0; i < 10; i++) {
            int num = i + 1;
            for (int j = 0; j < 10; j++) {
                int mult = j + 1;
                multipliersSelected[i][j] = workingConfig.multipliers[num].contains(mult);
            }
        }
    }

    private void buildTable() {
        tableLayout.removeAllViews();

        // Header Row
        TableRow headerRow = new TableRow(this);
        headerRow.setGravity(Gravity.CENTER);

        TextView emptyCell = new TextView(this);
        emptyCell.setText("");
        TableRow.LayoutParams emptyParams = new TableRow.LayoutParams(
            dpToPx(30), dpToPx(30));
        emptyParams.setMargins(4, 4, 4, 4);
        emptyCell.setLayoutParams(emptyParams);
        headerRow.addView(emptyCell);

        for (int i = 1; i <= 10; i++) {
            TextView numCell = new TextView(this);
            numCell.setText(String.valueOf(i));
            numCell.setTextSize(12);
            numCell.setTextColor(Color.BLACK);
            numCell.setGravity(Gravity.CENTER);
            TableRow.LayoutParams numParams = new TableRow.LayoutParams(
                0, dpToPx(30));
            numParams.setMargins(2, 4, 2, 4);
            numParams.weight = 1;
            numCell.setLayoutParams(numParams);
            headerRow.addView(numCell);
        }

        tableLayout.addView(headerRow);

        // Data Rows
        for (int row = 0; row < 10; row++) {
            int num = row + 1;

            TableRow dataRow = new TableRow(this);
            dataRow.setGravity(Gravity.CENTER);

            TextView labelCell = new TextView(this);
            labelCell.setText(num + ":");
            labelCell.setTextSize(12);
            labelCell.setTextColor(Color.BLACK);
            labelCell.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            TableRow.LayoutParams labelParams = new TableRow.LayoutParams(
                dpToPx(30), dpToPx(30));
            labelParams.setMargins(4, 4, 4, 4);
            labelCell.setLayoutParams(labelParams);
            dataRow.addView(labelCell);

            for (int col = 0; col < 10; col++) {
                int mult = col + 1;
                Button btn = createConfigButton(num, mult);
                dataRow.addView(btn);
            }

            tableLayout.addView(dataRow);
        }
    }

    private Button createConfigButton(final int baseNum, final int mult) {
        final int row = baseNum - 1;
        final int col = mult - 1;

        Button btn = new Button(this);
        btn.setText("");
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setAllCaps(false);
        btn.setPadding(0, 0, 0, 0);

        updateButtonStyle(btn, multipliersSelected[row][col], row);

        final int buttonSize = dpToPx(28);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, buttonSize);
        params.setMargins(2, 4, 2, 4);
        params.weight = 1;
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            multipliersSelected[row][col] = !multipliersSelected[row][col];
            updateButtonStyle(btn, multipliersSelected[row][col], row);
        });

        return btn;
    }

    private void updateButtonStyle(Button btn, boolean selected, int row) {
        int[] rowColors = {
            R.color.row1_color, R.color.row2_color, R.color.row3_color, R.color.row4_color,
            R.color.row5_color, R.color.row6_color, R.color.row7_color, R.color.row8_color,
            R.color.row9_color, R.color.row10_color
        };

        int bgColor = selected ?
            ContextCompat.getColor(this, rowColors[row]) :
            ContextCompat.getColor(this, R.color.unselected_color);
        int textColor = selected ? Color.WHITE : Color.DKGRAY;

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(4));
        drawable.setColor(bgColor);
        btn.setBackground(drawable);
        btn.setTextColor(textColor);
    }

    private void saveConfig() {
        EinmaleinsConfig config = new EinmaleinsConfig();

        for (int i = 0; i < 10; i++) {
            int num = i + 1;

            for (int j = 0; j < 10; j++) {
                int mult = j + 1;
                if (multipliersSelected[i][j]) {
                    config.multipliers[num].add(mult);
                }
            }

            if (!config.multipliers[num].isEmpty()) {
                config.baseNumbers.add(num);
            }
        }

        if (config.baseNumbers.isEmpty()) {
            Toast.makeText(this, "Bitte mindestens einen Wert auswählen!", Toast.LENGTH_SHORT).show();
            return;
        }

        config.autoSwitchMode = switchAutoSwitch.isChecked();

        configManager.saveConfig(config);
        Toast.makeText(this, "Gespeichert!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
