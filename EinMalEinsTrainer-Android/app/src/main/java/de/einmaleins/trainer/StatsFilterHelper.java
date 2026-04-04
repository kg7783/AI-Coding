package de.einmaleins.trainer;

import android.app.DatePickerDialog;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

public class StatsFilterHelper {

    public interface OnFilterChangedListener {
        void onFilterChanged(StatisticsCalculator.TimeFilter filter);
    }

    public interface OnDateRangeChangedListener {
        void onDateRangeChanged(long newDate, boolean isStartDate);
    }

    // ============================================
    // RADIO GROUP METHODEN
    // ============================================

    public static void setupRadioGroup(
            RadioGroup radioGroup,
            RadioButton radioCustom,
            ConfigManager configManager,
            boolean isStats,
            OnFilterChangedListener listener) {

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            StatisticsCalculator.TimeFilter filter = StatisticsCalculator.TimeFilter.ALL;

            if (checkedId == R.id.radioToday) {
                filter = StatisticsCalculator.TimeFilter.TODAY;
            } else if (checkedId == R.id.radioWeek) {
                filter = StatisticsCalculator.TimeFilter.WEEK;
            } else if (checkedId == R.id.radioMonth) {
                filter = StatisticsCalculator.TimeFilter.MONTH;
            } else if (checkedId == R.id.radioAll) {
                filter = StatisticsCalculator.TimeFilter.ALL;
            }

            if (radioCustom != null) {
                radioCustom.setChecked(false);
            }
            if (configManager != null) {
                if (isStats) {
                    configManager.saveLastFilterStats(filter.name());
                } else {
                    configManager.saveLastFilterSeries(filter.name());
                }
            }
            if (listener != null) {
                listener.onFilterChanged(filter);
            }
        });

        if (radioCustom != null) {
            radioCustom.setOnClickListener(v -> {
                radioGroup.clearCheck();
                radioCustom.setChecked(true);
                if (configManager != null) {
                    if (isStats) {
                        configManager.saveLastFilterStats("CUSTOM");
                    } else {
                        configManager.saveLastFilterSeries("CUSTOM");
                    }
                }
                if (listener != null) {
                    listener.onFilterChanged(StatisticsCalculator.TimeFilter.CUSTOM);
                }
            });
        }
    }

    public static void loadLastFilter(
            RadioGroup radioGroup,
            RadioButton radioCustom,
            ConfigManager configManager,
            boolean isStats) {

        String lastFilter;
        if (isStats) {
            lastFilter = configManager.getLastFilterStats();
        } else {
            lastFilter = configManager.getLastFilterSeries();
        }

        if ("CUSTOM".equals(lastFilter)) {
            if (radioCustom != null) {
                radioCustom.setChecked(true);
            }
            if (radioGroup != null) {
                radioGroup.clearCheck();
            }
        } else {
            if (radioCustom != null) {
                radioCustom.setChecked(false);
            }
            if (radioGroup != null) {
                int radioId = R.id.radioAll;
                if ("TODAY".equals(lastFilter)) {
                    radioId = R.id.radioToday;
                } else if ("WEEK".equals(lastFilter)) {
                    radioId = R.id.radioWeek;
                } else if ("MONTH".equals(lastFilter)) {
                    radioId = R.id.radioMonth;
                }
                radioGroup.check(radioId);
            }
        }
    }

    // ============================================
    // DATE BUTTON METHODEN
    // ============================================

    public static DateRangeData initDateRange(ConfigManager configManager, boolean isStats) {
        long start;
        long end;

        if (isStats) {
            start = configManager.getCustomStartDateStats();
            end = configManager.getCustomEndDateStats();
        } else {
            start = configManager.getCustomStartDateSeries();
            end = configManager.getCustomEndDateSeries();
        }

        if (start == 0) {
            start = getStartOfDay(0);
        }
        if (end == 0) {
            end = getEndOfDay(0);
        }

        return new DateRangeData(start, end);
    }

    public static void showDatePicker(
            Fragment fragment,
            boolean isStartDate,
            long currentDate,
            OnDateRangeChangedListener listener) {

        Calendar cal = Calendar.getInstance();
        if (currentDate > 0) {
            cal.setTimeInMillis(currentDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                fragment.requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    if (isStartDate) {
                        selectedCal.set(year, month, dayOfMonth, 0, 0, 0);
                        selectedCal.set(Calendar.MILLISECOND, 0);
                    } else {
                        selectedCal.set(year, month, dayOfMonth, 23, 59, 59);
                        selectedCal.set(Calendar.MILLISECOND, 999);
                    }
                    if (listener != null) {
                        listener.onDateRangeChanged(selectedCal.getTimeInMillis(), isStartDate);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    public static void updateDateButtonsDisplay(
            Button btnStartDate,
            Button btnEndDate,
            long startDate,
            long endDate) {

        java.text.DateFormat dateFormat =
                java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.GERMAN);
        btnStartDate.setText(dateFormat.format(startDate));
        btnEndDate.setText(dateFormat.format(endDate));
    }

    public static void saveDateRange(
            ConfigManager configManager,
            boolean isStats,
            long startDate,
            long endDate) {

        if (isStats) {
            configManager.saveCustomDateRangeStats(startDate, endDate);
        } else {
            configManager.saveCustomDateRangeSeries(startDate, endDate);
        }
    }

    // ============================================
    // HILFSMETHODEN
    // ============================================

    private static long getStartOfDay(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getEndOfDay(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    // ============================================
    // DATENKLASSE
    // ============================================

    public static class DateRangeData {
        public final long startDate;
        public final long endDate;

        public DateRangeData(long startDate, long endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
