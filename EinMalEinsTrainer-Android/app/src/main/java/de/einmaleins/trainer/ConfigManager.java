package de.einmaleins.trainer;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class ConfigManager {
    private static final String PREFS_NAME = "EinMalEinsConfig";
    private static final String KEY_BASE_NUMBERS = "baseNumbers";
    private static final String KEY_MULTIPLIERS = "multipliers";
    private static final String KEY_CUSTOM_START_DATE = "custom_start_date";
    private static final String KEY_CUSTOM_END_DATE = "custom_end_date";
    private static final String KEY_LAST_FILTER = "last_filter";

    private SharedPreferences prefs;

    public ConfigManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveConfig(EinmaleinsConfig config) {
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder baseNumbersBuilder = new StringBuilder();
        boolean first = true;
        for (int num : config.baseNumbers) {
            if (!first) baseNumbersBuilder.append(",");
            baseNumbersBuilder.append(num);
            first = false;
        }
        editor.putString(KEY_BASE_NUMBERS, baseNumbersBuilder.toString());

        for (int i = 1; i <= 10; i++) {
            StringBuilder multBuilder = new StringBuilder();
            boolean firstMult = true;
            for (int mult : config.multipliers[i]) {
                if (!firstMult) multBuilder.append(",");
                multBuilder.append(mult);
                firstMult = false;
            }
            editor.putString(KEY_MULTIPLIERS + i, multBuilder.toString());
        }

        editor.apply();
    }

    public EinmaleinsConfig loadConfig() {
        EinmaleinsConfig config = new EinmaleinsConfig();

        String baseNumbersStr = prefs.getString(KEY_BASE_NUMBERS, "");
        if (!baseNumbersStr.isEmpty()) {
            String[] parts = baseNumbersStr.split(",");
            for (String part : parts) {
                try {
                    int num = Integer.parseInt(part.trim());
                    config.baseNumbers.add(num);
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers
                }
            }
        }

        for (int i = 1; i <= 10; i++) {
            String multStr = prefs.getString(KEY_MULTIPLIERS + i, "");
            if (!multStr.isEmpty()) {
                String[] parts = multStr.split(",");
                for (String part : parts) {
                    try {
                        int mult = Integer.parseInt(part.trim());
                        config.multipliers[i].add(mult);
                    } catch (NumberFormatException e) {
                        // Ignore invalid numbers
                    }
                }
            }
        }

        if (config.baseNumbers.isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                config.baseNumbers.add(i);
                for (int j = 1; j <= 10; j++) {
                    config.multipliers[i].add(j);
                }
            }
        }

        return config;
    }

    public boolean hasConfig() {
        return prefs.contains(KEY_BASE_NUMBERS);
    }

    public void saveCustomDateRange(long startDate, long endDate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_CUSTOM_START_DATE, startDate);
        editor.putLong(KEY_CUSTOM_END_DATE, endDate);
        editor.apply();
    }

    public long getCustomStartDate() {
        long defaultStart = getStartOfDay(7);
        return prefs.getLong(KEY_CUSTOM_START_DATE, defaultStart);
    }

    public long getCustomEndDate() {
        long defaultEnd = getStartOfDay(0);
        return prefs.getLong(KEY_CUSTOM_END_DATE, defaultEnd);
    }

    public void saveLastFilter(String filterName) {
        prefs.edit().putString(KEY_LAST_FILTER, filterName).apply();
    }

    public String getLastFilter() {
        return prefs.getString(KEY_LAST_FILTER, "ALL");
    }

    private long getStartOfDay(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
