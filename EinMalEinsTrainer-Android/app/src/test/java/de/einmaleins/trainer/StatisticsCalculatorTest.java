package de.einmaleins.trainer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class StatisticsCalculatorTest {

    @Test
    public void testCalculateAll_emptyList() {
        List<ProgressSession> sessions = new ArrayList<>();
        StatisticsCalculator.Statistics stats = 
            StatisticsCalculator.calculate(sessions, StatisticsCalculator.TimeFilter.ALL);
        assertEquals(0, stats.totalSessions);
        assertEquals(0, stats.totalAttempts);
    }

    @Test
    public void testCalculateAll_withSessions() {
        List<ProgressSession> sessions = createSessions(3);
        StatisticsCalculator.Statistics stats = 
            StatisticsCalculator.calculate(sessions, StatisticsCalculator.TimeFilter.ALL);
        assertEquals(3, stats.totalSessions);
        assertTrue(stats.totalAttempts > 0);
    }

    @Test
    public void testCalculateSeriesStats_emptyList() {
        List<ProgressSession> sessions = new ArrayList<>();
        StatisticsCalculator.SeriesStatistics stats = 
            StatisticsCalculator.calculateSeriesStats(sessions, StatisticsCalculator.TimeFilter.ALL);
        assertNotNull(stats.multAccuracy);
        assertNotNull(stats.multCorrect);
        assertEquals(10, stats.multAccuracy.size());
    }

    @Test
    public void testCalculateSeriesStats_withData() {
        ProgressSession session = new ProgressSession(5, 3, 8);
        HashMap<Integer, Integer> multC = new HashMap<>();
        multC.put(1, 3);
        session.setSeriesStats(multC, new HashMap<>(), new HashMap<>(), new HashMap<>());
        
        List<ProgressSession> sessions = Arrays.asList(session);
        StatisticsCalculator.SeriesStatistics stats = 
            StatisticsCalculator.calculateSeriesStats(sessions, StatisticsCalculator.TimeFilter.ALL);
        
        assertEquals(3, (int) stats.multCorrect.get(1));
    }

    @Test
    public void testAccuracyCalculation() {
        ProgressSession session = new ProgressSession(8, 2, 10);
        List<ProgressSession> sessions = Arrays.asList(session);
        StatisticsCalculator.Statistics stats = 
            StatisticsCalculator.calculate(sessions, StatisticsCalculator.TimeFilter.ALL);
        
        assertEquals(8, stats.correctAnswers);
        assertEquals(2, stats.wrongAnswers);
        assertEquals(10, stats.totalAttempts);
    }

    @Test
    public void testCalculateSeriesStats_multipleSessions() {
        ProgressSession session1 = new ProgressSession(5, 2, 7);
        HashMap<Integer, Integer> multC1 = new HashMap<>();
        multC1.put(1, 2);
        multC1.put(2, 3);
        session1.setSeriesStats(multC1, new HashMap<>(), new HashMap<>(), new HashMap<>());
        
        ProgressSession session2 = new ProgressSession(6, 1, 7);
        HashMap<Integer, Integer> multC2 = new HashMap<>();
        multC2.put(1, 4);
        multC2.put(3, 2);
        session2.setSeriesStats(multC2, new HashMap<>(), new HashMap<>(), new HashMap<>());
        
        List<ProgressSession> sessions = Arrays.asList(session1, session2);
        StatisticsCalculator.SeriesStatistics stats = 
            StatisticsCalculator.calculateSeriesStats(sessions, StatisticsCalculator.TimeFilter.ALL);
        
        assertEquals(6, (int) stats.multCorrect.get(1)); // 2 + 4
        assertEquals(3, (int) stats.multCorrect.get(2)); // 3 + 0
        assertEquals(2, (int) stats.multCorrect.get(3)); // 0 + 2
    }

    @Test
    public void testCalculateSeriesStats_accuracyCalculation() {
        ProgressSession session = new ProgressSession(6, 4, 10);
        HashMap<Integer, Integer> multC = new HashMap<>();
        multC.put(1, 4);
        HashMap<Integer, Integer> multW = new HashMap<>();
        multW.put(1, 1);
        session.setSeriesStats(multC, multW, new HashMap<>(), new HashMap<>());
        
        List<ProgressSession> sessions = Arrays.asList(session);
        StatisticsCalculator.SeriesStatistics stats = 
            StatisticsCalculator.calculateSeriesStats(sessions, StatisticsCalculator.TimeFilter.ALL);
        
        assertEquals(80.0, stats.multAccuracy.get(1), 0.01);
    }

    @Test
    public void testCalculateWithDivStats() {
        ProgressSession session = new ProgressSession(6, 4, 10);
        HashMap<Integer, Integer> divC = new HashMap<>();
        divC.put(3, 3);
        divC.put(5, 3);
        HashMap<Integer, Integer> divW = new HashMap<>();
        divW.put(3, 2);
        divW.put(5, 2);
        session.setSeriesStats(new HashMap<>(), new HashMap<>(), divC, divW);
        
        List<ProgressSession> sessions = Arrays.asList(session);
        StatisticsCalculator.SeriesStatistics stats = 
            StatisticsCalculator.calculateSeriesStats(sessions, StatisticsCalculator.TimeFilter.ALL);
        
        assertEquals(3, (int) stats.divCorrect.get(3));
        assertEquals(3, (int) stats.divCorrect.get(5));
        assertEquals(2, (int) stats.divWrong.get(3));
        assertEquals(2, (int) stats.divWrong.get(5));
    }

    @Test
    public void testCalculateAverageAccuracy() {
        ProgressSession session1 = new ProgressSession(10, 0, 10); // 100%
        ProgressSession session2 = new ProgressSession(0, 10, 10); // 0%
        
        List<ProgressSession> sessions = Arrays.asList(session1, session2);
        StatisticsCalculator.Statistics stats = 
            StatisticsCalculator.calculate(sessions, StatisticsCalculator.TimeFilter.ALL);
        
        assertEquals(50.0, stats.averageAccuracy, 0.01);
    }

    private List<ProgressSession> createSessions(int count) {
        List<ProgressSession> sessions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sessions.add(new ProgressSession(5 + i, 2, 7 + i));
        }
        return sessions;
    }
}
