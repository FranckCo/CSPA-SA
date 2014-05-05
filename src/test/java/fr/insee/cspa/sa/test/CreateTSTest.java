package fr.insee.cspa.sa.test;


import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

public class CreateTSTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testDirect() {

		double[] values = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

		// The monthly series will start in January 2012
		TsPeriod start = new TsPeriod(TsFrequency.Monthly, 2012, 0);
		// Create the time domain for the series
		TsDomain domain = new TsDomain(start, values.length);

		// Creates directly the first series. The last parameter indicates that the data are cloned.
		TsData series1 = new TsData(start, values, true);

		// Creates a series from its domain
		TsData series2 = new TsData(domain);
		// All the data are missing
		assertTrue(series2.getObsCount() == 0);
		// Initialize the data for series2
		for (int index = 0;  index < values.length; ++index) series2.set(index, values[index]);
		// The series are now identical
		assertTrue(series1.equals(series2));

		// Creates a third series by cloning series1
		TsData series3 = series1.clone();
		// The series are identical
		assertTrue(series1.equals(series3));

		System.out.println(series3.toString());
	}

	@Test
	public void testIndirect() {

		TsDataCollector collector = new TsDataCollector();
		int nObs = 100;
		Day day = Day.toDay();
		Random rnd = new Random();
		TsData series = null;
		for (int index = 0; index < nObs; ++index) {
			// Add a new observation (date, value)
			collector.addObservation(day.toCalendar().getTime(), index);
			// Creates a new time series. The most suitable frequency is automatically choosen
			// The creation will fail if the collector contains less than 2 observations
			series = collector.make(TsFrequency.Undefined, TsAggregationType.None);
			if (index >= 2) {
				assertTrue(series != null);
				assertTrue(series.getLength() >= index + 1);
				}
			day = day.plus(31 + rnd.nextInt(10));
		}

		collector = new TsDataCollector();
		nObs = 10000;
		day = Day.toDay();
		for (int i = 0; i < nObs; ++i) {
			// Add a new observation (date, value)
			// New observation may belong to the same period (month, quarter...)
			collector.addObservation(day.toCalendar().getTime(), i);
			day = day.plus(rnd.nextInt(3));
			}
		// Creates a new time series. The frequency is specified
		// The observations belonging to the same period are aggregated following the specified method
		series =  collector.make(TsFrequency.Quarterly, TsAggregationType.Sum);
		DescriptiveStatistics stats = new DescriptiveStatistics(series);
		assertTrue(Math.round(stats.getSum()) == nObs*(nObs - 1)/2);

	}
}
