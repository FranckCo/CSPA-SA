package fr.insee.cspa.sa.test;


import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.ChangeOfRegime;
import ec.tstoolkit.timeseries.regression.ChangeOfRegimeType;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

public class RegressionModelTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testVariableList() {

		TsVariableList X = new TsVariableList();
		GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
		X.add(td);
		X.add(new LeapYearVariable(LengthOfPeriodType.LeapYear));
		X.add(new ChangeOfRegime(td, ChangeOfRegimeType.ZeroStarted, new Day(2000, Month.January, 0)));
		X.add(new SeasonalDummies(TsFrequency.Monthly));

		assertTrue(X.getVariablesCount() == 6 + 1 + 6 + 11);

		// Gets all the variables for 40 years
		Matrix matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 1980, 0, 480));
		// The matrix is full rank
		assertTrue(matrix.rank() == X.getVariablesCount());
		// Gets all the variables since 1/1/2000
		matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 2000, 0, 240));
		// The matrix is rank-deficient (the TD and the TD with change of regime are identical)
		assertTrue(matrix.rank() == X.getVariablesCount() - 6);
		// TD, TD with change of regime, LP (or 6 + 6 + 1)
		assertTrue(X.selectCompatible(ICalendarVariable.class).getVariablesCount() == 13);
		// TD only
		assertTrue(X.select(ITradingDaysVariable.class).getVariablesCount() == 6);
		// description of the regression variables
		for (ITsVariable var : X.items()) {
			System.out.println(var.getDescription());
			if (var.getDim() > 1)
				for (int j = 0; j < var.getDim(); ++j) System.out.println("    " + var.getItemDescription(j));
		}
	}

	@Test
	public void testCustomVariable() {

		TsVariableList X=new TsVariableList();
		X.add(new EasterVariable());
		X.add(new JulianEasterVariable());
		Matrix matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 1900, 0, 2400));
		// Prints the monthly regression variables for years from 1900 to 2100
		System.out.println(matrix);
	}

	@Test
	public void testCustomModifier() {

		TsData x = SeasonalAdjustmentTest.exportsTS;
		TsDomain xdom = x.getDomain().extend(24, 60);
		TsVariableList vars = new TsVariableList();
		vars.add(new SeasonalDummies(TsFrequency.Monthly));
		vars.add(new TramoExpander(x, TramoSpecification.TR4));
		// Series extended with 24 backcasts and 60 forecasts, mixed with other variables
		Matrix matrix = vars.all().matrix(xdom);
		System.out.println(matrix);
	}
}
