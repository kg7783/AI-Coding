package de.einmaleins.trainer;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {

    public static void generateTestSessions(Context context) {
        SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
        Gson gson = new Gson();
        
        List<ProgressSession> sessions = new ArrayList<>();
        Random random = new Random();

        sessions.add(createSession(random, 0, 8, 10, 0, 2));
        sessions.add(createSession(random, 1, 6, 9, 1, 4));
        sessions.add(createSession(random, 7, 7, 10, 0, 3));
        sessions.add(createSession(random, 14, 5, 8, 2, 5));
        sessions.add(createSession(random, 30, 4, 7, 3, 6));
        sessions.add(createSession(random, 60, 3, 6, 4, 7));

        for (int i = 0; i < sessions.size(); i++) {
            ProgressSession session = sessions.get(i);
            HashMap<Integer, Integer> multCorrect = new HashMap<>();
            HashMap<Integer, Integer> multWrong = new HashMap<>();
            HashMap<Integer, Integer> divCorrect = new HashMap<>();
            HashMap<Integer, Integer> divWrong = new HashMap<>();

            for (int series = 1; series <= 10; series++) {
                multCorrect.put(series, 0);
                multWrong.put(series, 0);
                divCorrect.put(series, 0);
                divWrong.put(series, 0);
            }

            int remainingCorrect = session.getCorrectAnswers();
            int remainingWrong = session.getWrongAnswers();

            List<Integer> seriesIndices = new ArrayList<>();
            for (int s = 1; s <= 10; s++) {
                seriesIndices.add(s);
            }
            Collections.shuffle(seriesIndices, random);

            int maxSeries = Math.min(remainingCorrect, 10);
            for (int idx = 0; idx < maxSeries && remainingCorrect > 0; idx++) {
                int series = seriesIndices.get(idx);
                int isMult = random.nextInt(2);
                int val = Math.min(random.nextInt(2) + 1, remainingCorrect);
                if (isMult == 0) {
                    multCorrect.put(series, multCorrect.get(series) + val);
                } else {
                    divCorrect.put(series, divCorrect.get(series) + val);
                }
                remainingCorrect -= val;
            }

            if (remainingCorrect > 0) {
                int series = seriesIndices.get(0);
                int isMult = random.nextInt(2);
                if (isMult == 0) {
                    multCorrect.put(series, multCorrect.get(series) + remainingCorrect);
                } else {
                    divCorrect.put(series, divCorrect.get(series) + remainingCorrect);
                }
            }

            maxSeries = Math.min(remainingWrong, 10);
            for (int idx = 0; idx < maxSeries && remainingWrong > 0; idx++) {
                int series = seriesIndices.get(idx);
                int isMult = random.nextInt(2);
                int val = Math.min(1, remainingWrong);
                if (isMult == 0) {
                    multWrong.put(series, multWrong.get(series) + val);
                } else {
                    divWrong.put(series, divWrong.get(series) + val);
                }
                remainingWrong -= val;
            }

            if (remainingWrong > 0) {
                int series = seriesIndices.get(0);
                int isMult = random.nextInt(2);
                if (isMult == 0) {
                    multWrong.put(series, multWrong.get(series) + remainingWrong);
                } else {
                    divWrong.put(series, divWrong.get(series) + remainingWrong);
                }
            }

            session.setSeriesStats(multCorrect, multWrong, divCorrect, divWrong);
        }

        List<ProgressSession> existingSessions = prefsManager.loadSessions();
        existingSessions.addAll(sessions);
        prefsManager.saveSessions(existingSessions);
    }

    private static ProgressSession createSession(Random random, int daysAgo, 
            int minCorrect, int maxCorrect, int minWrong, int maxWrong) {
        ProgressSession session = new ProgressSession();
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        cal.set(Calendar.HOUR_OF_DAY, random.nextInt(12) + 8);
        cal.set(Calendar.MINUTE, random.nextInt(60));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        long startTime = cal.getTimeInMillis();
        long endTime = startTime + (random.nextInt(30) + 10) * 60 * 1000;

        int correct = random.nextInt(maxCorrect - minCorrect + 1) + minCorrect;
        int wrong = random.nextInt(maxWrong - minWrong + 1) + minWrong;
        int total = correct + wrong;

        session.setStartTimestamp(startTime);
        session.setEndTimestamp(endTime);
        session.setCorrectAnswers(correct);
        session.setWrongAnswers(wrong);
        session.setTotalAttempts(total);

        return session;
    }

    private static class SharedPreferencesManager {
        private static final String PREFS_NAME = "ProgressSessions";
        private static final String KEY_SESSIONS = "sessions";
        private android.content.SharedPreferences prefs;
        private Gson gson;

        SharedPreferencesManager(Context context) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            gson = new Gson();
        }

        List<ProgressSession> loadSessions() {
            String json = prefs.getString(KEY_SESSIONS, null);
            if (json == null) return new ArrayList<>();
            Type type = new TypeToken<List<ProgressSession>>(){}.getType();
            return gson.fromJson(json, type);
        }

        void saveSessions(List<ProgressSession> sessions) {
            String json = gson.toJson(sessions);
            prefs.edit().putString(KEY_SESSIONS, json).apply();
        }
    }
}
