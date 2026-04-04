package de.einmaleins.trainer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ProgressSessionTest {

    @Test
    public void testGetAccuracy_withCorrectAnswers() {
        ProgressSession session = new ProgressSession(8, 2, 10);
        assertEquals(80.0, session.getAccuracy(), 0.01);
    }

    @Test
    public void testGetAccuracy_withNoAnswers() {
        ProgressSession session = new ProgressSession(0, 0, 0);
        assertEquals(0.0, session.getAccuracy(), 0.01);
    }

    @Test
    public void testGetAccuracy_allCorrect() {
        ProgressSession session = new ProgressSession(10, 0, 10);
        assertEquals(100.0, session.getAccuracy(), 0.01);
    }

    @Test
    public void testGetAccuracy_allWrong() {
        ProgressSession session = new ProgressSession(0, 10, 10);
        assertEquals(0.0, session.getAccuracy(), 0.01);
    }

    @Test
    public void testGetTotalMult_emptySeries() {
        ProgressSession session = new ProgressSession(5, 3, 8);
        assertEquals(0, session.getTotalMult());
    }

    @Test
    public void testGetTotalMult_withData() {
        ProgressSession session = new ProgressSession(6, 2, 8);
        HashMap<Integer, Integer> multC = new HashMap<>();
        HashMap<Integer, Integer> multW = new HashMap<>();
        multC.put(1, 2);
        multC.put(2, 3);
        multW.put(1, 1);
        session.setSeriesStats(multC, multW, new HashMap<>(), new HashMap<>());
        assertEquals(6, session.getTotalMult());
    }

    @Test
    public void testGetTotalDiv_emptySeries() {
        ProgressSession session = new ProgressSession(5, 3, 8);
        assertEquals(0, session.getTotalDiv());
    }

    @Test
    public void testGetTotalDiv_withData() {
        ProgressSession session = new ProgressSession(5, 3, 8);
        HashMap<Integer, Integer> divC = new HashMap<>();
        divC.put(3, 2);
        divC.put(5, 3);
        session.setSeriesStats(new HashMap<>(), new HashMap<>(), divC, new HashMap<>());
        assertEquals(5, session.getTotalDiv());
    }

    @Test
    public void testSetAndGetTimestamp() {
        ProgressSession session = new ProgressSession();
        long now = System.currentTimeMillis();
        session.setStartTimestamp(now);
        session.setEndTimestamp(now + 60000);
        assertEquals(now, session.getStartTimestamp());
        assertEquals(now + 60000, session.getEndTimestamp());
    }

    @Test
    public void testGetMultCorrectForSeries() {
        ProgressSession session = new ProgressSession(5, 3, 8);
        HashMap<Integer, Integer> multC = new HashMap<>();
        multC.put(1, 3);
        multC.put(5, 2);
        session.setSeriesStats(multC, new HashMap<>(), new HashMap<>(), new HashMap<>());
        
        assertEquals(3, session.getMultCorrectForSeries(1));
        assertEquals(2, session.getMultCorrectForSeries(5));
        assertEquals(0, session.getMultCorrectForSeries(3));
    }

    @Test
    public void testGetDivWrongForSeries() {
        ProgressSession session = new ProgressSession(5, 3, 8);
        HashMap<Integer, Integer> divW = new HashMap<>();
        divW.put(2, 1);
        divW.put(7, 2);
        session.setSeriesStats(new HashMap<>(), new HashMap<>(), new HashMap<>(), divW);
        
        assertEquals(1, session.getDivWrongForSeries(2));
        assertEquals(2, session.getDivWrongForSeries(7));
        assertEquals(0, session.getDivWrongForSeries(4));
    }
}
