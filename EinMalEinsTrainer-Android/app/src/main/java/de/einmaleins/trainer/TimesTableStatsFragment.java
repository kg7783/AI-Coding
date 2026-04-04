package de.einmaleins.trainer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TimesTableStatsFragment extends Fragment {
    private static final int COLOR_GOOD = Color.parseColor("#228B22");
    private static final int COLOR_MEDIUM = Color.parseColor("#FFA500");
    private static final int COLOR_BAD = Color.parseColor("#DC143C");
    private static final int COLOR_NONE = Color.parseColor("#CCCCCC");
    private static final int COLOR_DISABLED = Color.parseColor("#AAAAAA");

    private SessionManager sessionManager;
    private ConfigManager configManager;
    private RadioGroup radioGroupFilter;
    private RadioButton radioCustom;
    private View[] multSeriesViews;
    private View[] divSeriesViews;
    private Button btnStartDate;
    private Button btnEndDate;
    private long customStartDate = 0;
    private long customEndDate = 0;

    public static TimesTableStatsFragment newInstance() {
        return new TimesTableStatsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_times_table_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
            configManager = new ConfigManager(getContext());
        }

        initViews(view);
        setupDateButtons();
        StatsFilterHelper.loadLastFilter(radioGroupFilter, radioCustom, configManager, false);
        setupRadioGroup();
        StatisticsCalculator.TimeFilter currentFilter = getCurrentFilter();
        updateStatistics(currentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null && configManager != null) {
            StatisticsCalculator.TimeFilter currentFilter = getCurrentFilter();
            updateStatistics(currentFilter);
        }
    }

    private void initViews(View view) {
        radioGroupFilter = view.findViewById(R.id.radioGroupFilter);
        radioCustom = view.findViewById(R.id.radioCustom);
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);

        multSeriesViews = new View[10];
        divSeriesViews = new View[10];

        int[] multIds = {
                R.id.series1Mult, R.id.series2Mult, R.id.series3Mult, R.id.series4Mult, R.id.series5Mult,
                R.id.series6Mult, R.id.series7Mult, R.id.series8Mult, R.id.series9Mult, R.id.series10Mult
        };

        int[] divIds = {
                R.id.series1Div, R.id.series2Div, R.id.series3Div, R.id.series4Div, R.id.series5Div,
                R.id.series6Div, R.id.series7Div, R.id.series8Div, R.id.series9Div, R.id.series10Div
        };

        for (int i = 0; i < 10; i++) {
            multSeriesViews[i] = view.findViewById(multIds[i]);
            divSeriesViews[i] = view.findViewById(divIds[i]);
        }
    }

    private void setupRadioGroup() {
        StatsFilterHelper.setupRadioGroup(
            radioGroupFilter, radioCustom, configManager, false, this::updateStatistics
        );
    }

    private StatisticsCalculator.TimeFilter getCurrentFilter() {
        if (radioCustom != null && radioCustom.isChecked()) {
            return StatisticsCalculator.TimeFilter.CUSTOM;
        }

        if (radioGroupFilter == null) return StatisticsCalculator.TimeFilter.ALL;

        int checkedId = radioGroupFilter.getCheckedRadioButtonId();
        if (checkedId == R.id.radioToday) {
            return StatisticsCalculator.TimeFilter.TODAY;
        } else if (checkedId == R.id.radioWeek) {
            return StatisticsCalculator.TimeFilter.WEEK;
        } else if (checkedId == R.id.radioMonth) {
            return StatisticsCalculator.TimeFilter.MONTH;
        }
        return StatisticsCalculator.TimeFilter.ALL;
    }

    private void setupDateButtons() {
        StatsFilterHelper.DateRangeData dates = 
            StatsFilterHelper.initDateRange(configManager, false);
        customStartDate = dates.startDate;
        customEndDate = dates.endDate;
        
        StatsFilterHelper.updateDateButtonsDisplay(btnStartDate, btnEndDate, 
                                                  customStartDate, customEndDate);
        
        btnStartDate.setOnClickListener(v -> 
            StatsFilterHelper.showDatePicker(this, true, customStartDate, this::onDateChanged));
        btnEndDate.setOnClickListener(v -> 
            StatsFilterHelper.showDatePicker(this, false, customEndDate, this::onDateChanged));
    }

    private void onDateChanged(long newDate, boolean isStartDate) {
        if (isStartDate) {
            customStartDate = newDate;
        } else {
            customEndDate = newDate;
        }
        
        StatsFilterHelper.saveDateRange(configManager, false, customStartDate, customEndDate);
        StatsFilterHelper.updateDateButtonsDisplay(btnStartDate, btnEndDate, 
                                                  customStartDate, customEndDate);
        
        if (radioCustom != null) {
            radioCustom.setChecked(true);
        }
        if (radioGroupFilter != null) {
            radioGroupFilter.clearCheck();
        }
        
        updateStatistics(StatisticsCalculator.TimeFilter.CUSTOM);
    }

    private void updateStatistics(StatisticsCalculator.TimeFilter filter) {
        if (sessionManager == null || configManager == null) return;
        if (multSeriesViews == null || divSeriesViews == null) return;

        List<ProgressSession> sessions = sessionManager.loadSessions();

        StatisticsCalculator.SeriesStatistics stats;

        if (filter == StatisticsCalculator.TimeFilter.CUSTOM) {
            stats = StatisticsCalculator.calculateSeriesStats(sessions, filter, customStartDate, customEndDate);
        } else {
            stats = StatisticsCalculator.calculateSeriesStats(sessions, filter);
        }

        EinmaleinsConfig config = configManager.loadConfig();

        updateSeriesViews(multSeriesViews, stats.multAccuracy, stats.multCorrect, stats.multWrong, "×", config);
        updateSeriesViews(divSeriesViews, stats.divAccuracy, stats.divCorrect, stats.divWrong, "÷", config);
    }

    private void updateSeriesViews(View[] seriesViews, HashMap<Integer, Double> accuracyMap,
                                  HashMap<Integer, Integer> correctMap, HashMap<Integer, Integer> wrongMap,
                                  String symbol, EinmaleinsConfig config) {
        for (int i = 0; i < 10; i++) {
            View row = seriesViews[i];
            if (row == null) continue;

            int series = i + 1;
            TextView tvLabel = row.findViewById(R.id.tvSeriesLabel);
            ProgressBar progressBar = row.findViewById(R.id.progressBar);
            ProgressBar progressBarWrong = row.findViewById(R.id.progressBarWrong);
            TextView tvStats = row.findViewById(R.id.tvSeriesStats);

            boolean isConfigured = isSeriesConfigured(series, config);

            if (!isConfigured) {
                tvLabel.setText(series + symbol);
                setProgressBarWeight(progressBar, 0);
                setProgressBarWeight(progressBarWrong, 0);
                tvStats.setText("--");
                tvStats.setTextColor(COLOR_DISABLED);
            } else {
                Integer correct = correctMap != null ? correctMap.getOrDefault(series, 0) : 0;
                Integer wrong = wrongMap != null ? wrongMap.getOrDefault(series, 0) : 0;
                
                int total = correct + wrong;

                tvLabel.setText(series + symbol);
                
                if (total > 0) {
                    int correctPercent = (int) Math.round((double) correct / total * 100);
                    int wrongPercent = 100 - correctPercent;
                    int textColor = getColorForPercent(correctPercent);
                    
                    setProgressBarWeight(progressBar, correctPercent);
                    setProgressBarWeight(progressBarWrong, wrongPercent);
                    
                    progressBar.setMax(100);
                    progressBar.setProgress(100);
                    progressBarWrong.setMax(100);
                    progressBarWrong.setProgress(100);
                    
                    tvStats.setText(String.format(Locale.GERMAN, "%d von %d", correct, total));
                    tvStats.setTextColor(textColor);
                } else {
                    setProgressBarWeight(progressBar, 1);
                    setProgressBarWeight(progressBarWrong, 0);
                    progressBar.setMax(100);
                    progressBar.setProgress(0);
                    progressBarWrong.setMax(100);
                    progressBarWrong.setProgress(0);
                    
                    tvStats.setText("0 von 0");
                    tvStats.setTextColor(COLOR_NONE);
                }
            }
        }
    }

    private void setProgressBarWeight(ProgressBar progressBar, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progressBar.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
        } else {
            params.weight = weight;
        }
        progressBar.setLayoutParams(params);
    }

    private boolean isSeriesConfigured(int series, EinmaleinsConfig config) {
        if (config == null || config.baseNumbers == null) return true;
        return config.baseNumbers.contains(series) && 
               config.multipliers != null && 
               config.multipliers[series] != null && 
               !config.multipliers[series].isEmpty();
    }

    private int getColorForPercent(int percent) {
        if (percent == 0) return COLOR_NONE;
        if (percent >= 80) return COLOR_GOOD;
        if (percent >= 50) return COLOR_MEDIUM;
        return COLOR_BAD;
    }
}
