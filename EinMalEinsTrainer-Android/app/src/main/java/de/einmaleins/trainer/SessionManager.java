package de.einmaleins.trainer;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SessionManager {
    private static final String PREFS_NAME = "ProgressSessions";
    private static final String KEY_SESSIONS = "sessions";
    private SharedPreferences prefs;
    private Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<ProgressSession> loadSessions() {
        String json = prefs.getString(KEY_SESSIONS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ProgressSession>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void startNewSession() {
        List<ProgressSession> sessions = loadSessions();
        sessions.add(0, new ProgressSession());
        saveSessions(sessions);
    }

    public void updateCurrentSession(int correct, int wrong, int total) {
        List<ProgressSession> sessions = loadSessions();
        if (!sessions.isEmpty()) {
            ProgressSession current = sessions.get(0);
            current.setCorrectAnswers(correct);
            current.setWrongAnswers(wrong);
            current.setTotalAttempts(total);
            current.setEndTimestamp(System.currentTimeMillis());
            saveSessions(sessions);
        }
    }

    public void updateCurrentSession(int correct, int wrong, int total,
                                     HashMap<Integer, Integer> multCorrect,
                                     HashMap<Integer, Integer> multWrong,
                                     HashMap<Integer, Integer> divCorrect,
                                     HashMap<Integer, Integer> divWrong) {
        List<ProgressSession> sessions = loadSessions();
        if (!sessions.isEmpty()) {
            ProgressSession current = sessions.get(0);
            current.setCorrectAnswers(correct);
            current.setWrongAnswers(wrong);
            current.setTotalAttempts(total);
            current.setEndTimestamp(System.currentTimeMillis());
            current.setSeriesStats(multCorrect, multWrong, divCorrect, divWrong);
            saveSessions(sessions);
        }
    }

    public void clearAllSessions() {
        prefs.edit().remove(KEY_SESSIONS).apply();
    }

    public void deleteSession(String id) {
        List<ProgressSession> sessions = loadSessions();
        sessions.removeIf(s -> s.getId().equals(id));
        saveSessions(sessions);
    }

    public boolean hasActiveSession() {
        return !loadSessions().isEmpty();
    }

    private void saveSessions(List<ProgressSession> sessions) {
        String json = gson.toJson(sessions);
        prefs.edit().putString(KEY_SESSIONS, json).apply();
    }
}
