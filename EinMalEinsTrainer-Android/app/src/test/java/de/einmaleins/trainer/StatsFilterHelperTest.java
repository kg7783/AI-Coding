package de.einmaleins.trainer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class StatsFilterHelperTest {

    @Test
    public void testDateRangeData_creation() {
        long start = System.currentTimeMillis() - 86400000;
        long end = System.currentTimeMillis();
        
        StatsFilterHelper.DateRangeData dates = new StatsFilterHelper.DateRangeData(start, end);
        
        assertEquals(start, dates.startDate);
        assertEquals(end, dates.endDate);
    }

    @Test
    public void testOnFilterChangedListener_interface() {
        final boolean[] called = {false};
        final StatisticsCalculator.TimeFilter[] receivedFilter = {null};
        
        StatsFilterHelper.OnFilterChangedListener listener = filter -> {
            called[0] = true;
            receivedFilter[0] = filter;
        };
        
        listener.onFilterChanged(StatisticsCalculator.TimeFilter.TODAY);
        
        assertTrue(called[0]);
        assertEquals(StatisticsCalculator.TimeFilter.TODAY, receivedFilter[0]);
    }

    @Test
    public void testOnDateRangeChangedListener_interface() {
        final boolean[] called = {false};
        final long[] receivedDate = {0};
        final boolean[] receivedIsStart = {false};
        
        StatsFilterHelper.OnDateRangeChangedListener listener = (newDate, isStartDate) -> {
            called[0] = true;
            receivedDate[0] = newDate;
            receivedIsStart[0] = isStartDate;
        };
        
        long testDate = System.currentTimeMillis();
        listener.onDateRangeChanged(testDate, true);
        
        assertTrue(called[0]);
        assertEquals(testDate, receivedDate[0]);
        assertEquals(true, receivedIsStart[0]);
    }

    @Test
    public void testTimeFilterEnum_values() {
        StatisticsCalculator.TimeFilter[] filters = {
            StatisticsCalculator.TimeFilter.ALL,
            StatisticsCalculator.TimeFilter.TODAY,
            StatisticsCalculator.TimeFilter.WEEK,
            StatisticsCalculator.TimeFilter.MONTH,
            StatisticsCalculator.TimeFilter.CUSTOM
        };
        
        assertEquals(5, filters.length);
        assertEquals("ALL", StatisticsCalculator.TimeFilter.ALL.name());
        assertEquals("TODAY", StatisticsCalculator.TimeFilter.TODAY.name());
        assertEquals("WEEK", StatisticsCalculator.TimeFilter.WEEK.name());
        assertEquals("MONTH", StatisticsCalculator.TimeFilter.MONTH.name());
        assertEquals("CUSTOM", StatisticsCalculator.TimeFilter.CUSTOM.name());
    }
}
