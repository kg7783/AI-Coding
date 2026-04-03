package de.einmaleins.trainer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {
    private static final int COLOR_CORRECT = Color.parseColor("#228B22");
    private static final int COLOR_WRONG = Color.parseColor("#DC143C");

    private SessionManager sessionManager;
    private RadioGroup radioGroupFilter;
    private TextView tvTotalSessions, tvTotalAttempts, tvAverageAccuracy;
    private TextView tvCorrectCount, tvWrongCount;
    private PieChart pieChart;
    private BarChart barChart;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }
        
        initViews(view);
        setupCharts();
        setupRadioGroup();
        updateStatistics(StatisticsCalculator.TimeFilter.ALL);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (radioGroupFilter != null && sessionManager != null) {
            StatisticsCalculator.TimeFilter currentFilter = getCurrentFilter();
            updateStatistics(currentFilter);
        }
    }

    private void initViews(View view) {
        radioGroupFilter = view.findViewById(R.id.radioGroupFilter);
        
        tvTotalSessions = view.findViewById(R.id.tvTotalSessions);
        tvTotalAttempts = view.findViewById(R.id.tvTotalAttempts);
        tvAverageAccuracy = view.findViewById(R.id.tvAverageAccuracy);
        tvCorrectCount = view.findViewById(R.id.tvCorrectCount);
        tvWrongCount = view.findViewById(R.id.tvWrongCount);
        
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
    }

    private void setupCharts() {
        if (pieChart == null || barChart == null) return;
        
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getLegend().setEnabled(true);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.getLegend().setEnabled(true);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void setupRadioGroup() {
        if (radioGroupFilter == null) return;
        
        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (sessionManager == null) return;
            
            StatisticsCalculator.TimeFilter filter = StatisticsCalculator.TimeFilter.ALL;
            
            if (checkedId == R.id.radioToday) {
                filter = StatisticsCalculator.TimeFilter.TODAY;
            } else if (checkedId == R.id.radioWeek) {
                filter = StatisticsCalculator.TimeFilter.WEEK;
            } else if (checkedId == R.id.radioMonth) {
                filter = StatisticsCalculator.TimeFilter.MONTH;
            }
            
            updateStatistics(filter);
        });
    }

    private StatisticsCalculator.TimeFilter getCurrentFilter() {
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

    private void updateStatistics(StatisticsCalculator.TimeFilter filter) {
        if (sessionManager == null || tvTotalSessions == null) return;
        
        List<ProgressSession> sessions = sessionManager.loadSessions();
        StatisticsCalculator.Statistics stats = StatisticsCalculator.calculate(sessions, filter);

        tvTotalSessions.setText(String.valueOf(stats.totalSessions));
        tvTotalAttempts.setText(String.valueOf(stats.totalAttempts));
        tvAverageAccuracy.setText(String.format(Locale.GERMAN, "%.0f%%", stats.averageAccuracy));
        tvCorrectCount.setText(String.valueOf(stats.correctAnswers));
        tvWrongCount.setText(String.valueOf(stats.wrongAnswers));

        updatePieChart(stats);
        updateBarChart(stats, sessions, filter);
    }

    private void updatePieChart(StatisticsCalculator.Statistics stats) {
        if (pieChart == null) return;
        
        if (stats.correctAnswers == 0 && stats.wrongAnswers == 0) {
            pieChart.setData(null);
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        if (stats.correctAnswers > 0) {
            entries.add(new PieEntry(stats.correctAnswers, "Richtig"));
            colors.add(COLOR_CORRECT);
        }
        if (stats.wrongAnswers > 0) {
            entries.add(new PieEntry(stats.wrongAnswers, "Falsch"));
            colors.add(COLOR_WRONG);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateBarChart(StatisticsCalculator.Statistics stats, 
                                List<ProgressSession> sessions,
                                StatisticsCalculator.TimeFilter filter) {
        if (barChart == null) return;
        
        List<ProgressSession> filteredSessions = filterSessions(sessions, filter);
        
        if (filteredSessions.isEmpty()) {
            barChart.setData(null);
            barChart.invalidate();
            return;
        }

        List<BarEntry> correctEntries = new ArrayList<>();
        List<BarEntry> wrongEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int maxEntries = Math.min(filteredSessions.size(), 10);
        for (int i = 0; i < maxEntries; i++) {
            ProgressSession session = filteredSessions.get(i);
            correctEntries.add(new BarEntry(i, session.getCorrectAnswers()));
            wrongEntries.add(new BarEntry(i, session.getWrongAnswers()));
            labels.add("S" + (i + 1));
        }

        BarDataSet correctDataSet = new BarDataSet(correctEntries, "Richtig");
        correctDataSet.setColor(COLOR_CORRECT);
        correctDataSet.setValueTextSize(10f);

        BarDataSet wrongDataSet = new BarDataSet(wrongEntries, "Falsch");
        wrongDataSet.setColor(COLOR_WRONG);
        wrongDataSet.setValueTextSize(10f);

        BarData data = new BarData(correctDataSet, wrongDataSet);
        data.setBarWidth(0.35f);
        
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.groupBars(-0.5f, 0.2f, 0.05f);
        barChart.invalidate();
    }

    private List<ProgressSession> filterSessions(List<ProgressSession> sessions, 
                                                StatisticsCalculator.TimeFilter filter) {
        List<ProgressSession> result = new ArrayList<>();
        
        long startOfToday = 0, startOfWeek = 0, startOfMonth = 0;
        if (filter != StatisticsCalculator.TimeFilter.ALL) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            startOfToday = calendar.getTimeInMillis();
            startOfWeek = startOfToday - (6 * 24 * 60 * 60 * 1000L);
            startOfMonth = startOfToday - (29 * 24 * 60 * 60 * 1000L);
        }
        
        for (int i = sessions.size() - 1; i >= 0; i--) {
            ProgressSession session = sessions.get(i);
            
            switch (filter) {
                case ALL:
                    result.add(session);
                    break;
                case TODAY:
                    if (session.getStartTimestamp() >= startOfToday) result.add(session);
                    break;
                case WEEK:
                    if (session.getStartTimestamp() >= startOfWeek) result.add(session);
                    break;
                case MONTH:
                    if (session.getStartTimestamp() >= startOfMonth) result.add(session);
                    break;
            }
        }
        
        return result;
    }
}
