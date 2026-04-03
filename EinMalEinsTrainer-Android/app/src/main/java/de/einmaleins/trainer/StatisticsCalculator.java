package de.einmaleins.trainer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class StatisticsCalculator {

    public enum TimeFilter {
        ALL, TODAY, WEEK, MONTH, CUSTOM
    }

    public static class Statistics {
        public int totalSessions;
        public int totalAttempts;
        public int correctAnswers;
        public int wrongAnswers;
        public double averageAccuracy;
        public double bestAccuracy;
        public double worstAccuracy;
        public ProgressSession bestSession;
        public ProgressSession worstSession;
    }

    public static class SeriesStatistics {
        public HashMap<Integer, Double> multAccuracy;
        public HashMap<Integer, Double> divAccuracy;
        public HashMap<Integer, Integer> multCorrect;
        public HashMap<Integer, Integer> multWrong;
        public HashMap<Integer, Integer> divCorrect;
        public HashMap<Integer, Integer> divWrong;
    }

    public static Statistics calculate(List<ProgressSession> sessions, TimeFilter filter,
                                       long customStartDate, long customEndDate) {
        List<ProgressSession> filteredSessions = filterSessions(sessions, filter, customStartDate, customEndDate);
        
        Statistics stats = new Statistics();
        stats.totalSessions = filteredSessions.size();
        
        if (filteredSessions.isEmpty()) {
            stats.averageAccuracy = 0;
            stats.bestAccuracy = 0;
            stats.worstAccuracy = 0;
            return stats;
        }

        int totalCorrect = 0;
        int totalWrong = 0;
        double totalAccuracy = 0;
        double bestAcc = -1;
        double worstAcc = 101;
        ProgressSession bestSession = null;
        ProgressSession worstSession = null;

        for (ProgressSession session : filteredSessions) {
            totalCorrect += session.getCorrectAnswers();
            totalWrong += session.getWrongAnswers();
            
            double accuracy = session.getAccuracy();
            totalAccuracy += accuracy;
            
            if (accuracy > bestAcc) {
                bestAcc = accuracy;
                bestSession = session;
            }
            if (accuracy < worstAcc && session.getTotalAttempts() > 0) {
                worstAcc = accuracy;
                worstSession = session;
            }
        }

        stats.totalAttempts = totalCorrect + totalWrong;
        stats.correctAnswers = totalCorrect;
        stats.wrongAnswers = totalWrong;
        stats.averageAccuracy = filteredSessions.isEmpty() ? 0 : totalAccuracy / filteredSessions.size();
        stats.bestAccuracy = bestAcc;
        stats.worstAccuracy = worstAcc;
        stats.bestSession = bestSession;
        stats.worstSession = worstSession;

        return stats;
    }

    public static Statistics calculate(List<ProgressSession> sessions, TimeFilter filter) {
        return calculate(sessions, filter, 0, 0);
    }

    public static SeriesStatistics calculateSeriesStats(List<ProgressSession> sessions, TimeFilter filter) {
        return calculateSeriesStats(sessions, filter, 0, 0);
    }

    private static List<ProgressSession> filterSessions(List<ProgressSession> sessions, TimeFilter filter,
                                                        long customStartDate, long customEndDate) {
        if (filter == TimeFilter.ALL) {
            return new ArrayList<>(sessions);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startOfToday = calendar.getTimeInMillis();
        long startOfWeek = startOfToday - (6 * 24 * 60 * 60 * 1000L);
        long startOfMonth = startOfToday - (29 * 24 * 60 * 60 * 1000L);

        List<ProgressSession> filtered = new ArrayList<>();
        
        for (ProgressSession session : sessions) {
            long sessionTime = session.getStartTimestamp();
            boolean matches = false;
            
            switch (filter) {
                case TODAY:
                    matches = sessionTime >= startOfToday;
                    break;
                case WEEK:
                    matches = sessionTime >= startOfWeek;
                    break;
                case MONTH:
                    matches = sessionTime >= startOfMonth;
                    break;
                case CUSTOM:
                    if (customStartDate > 0 && customEndDate > 0) {
                        matches = sessionTime >= customStartDate && sessionTime <= customEndDate;
                    }
                    break;
            }
            
            if (matches) {
                filtered.add(session);
            }
        }
        
        return filtered;
    }

    public static SeriesStatistics calculateSeriesStats(List<ProgressSession> sessions, TimeFilter filter,
                                                        long customStartDate, long customEndDate) {
        List<ProgressSession> filteredSessions = filterSessions(sessions, filter, customStartDate, customEndDate);
        
        SeriesStatistics seriesStats = new SeriesStatistics();
        seriesStats.multAccuracy = new HashMap<>();
        seriesStats.divAccuracy = new HashMap<>();
        seriesStats.multCorrect = new HashMap<>();
        seriesStats.multWrong = new HashMap<>();
        seriesStats.divCorrect = new HashMap<>();
        seriesStats.divWrong = new HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
            seriesStats.multCorrect.put(i, 0);
            seriesStats.multWrong.put(i, 0);
            seriesStats.divCorrect.put(i, 0);
            seriesStats.divWrong.put(i, 0);
        }
        
        for (ProgressSession session : filteredSessions) {
            for (int series = 1; series <= 10; series++) {
                int multC = session.getMultCorrectForSeries(series);
                int multW = session.getMultWrongForSeries(series);
                int divC = session.getDivCorrectForSeries(series);
                int divW = session.getDivWrongForSeries(series);
                
                seriesStats.multCorrect.put(series, seriesStats.multCorrect.get(series) + multC);
                seriesStats.multWrong.put(series, seriesStats.multWrong.get(series) + multW);
                seriesStats.divCorrect.put(series, seriesStats.divCorrect.get(series) + divC);
                seriesStats.divWrong.put(series, seriesStats.divWrong.get(series) + divW);
            }
        }
        
        for (int i = 1; i <= 10; i++) {
            int multTotal = seriesStats.multCorrect.get(i) + seriesStats.multWrong.get(i);
            if (multTotal > 0) {
                seriesStats.multAccuracy.put(i, (double) seriesStats.multCorrect.get(i) / multTotal * 100);
            } else {
                seriesStats.multAccuracy.put(i, 0.0);
            }
            
            int divTotal = seriesStats.divCorrect.get(i) + seriesStats.divWrong.get(i);
            if (divTotal > 0) {
                seriesStats.divAccuracy.put(i, (double) seriesStats.divCorrect.get(i) / divTotal * 100);
            } else {
                seriesStats.divAccuracy.put(i, 0.0);
            }
        }
        
        return seriesStats;
    }
}
