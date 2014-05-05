package fr.insee.cspa.sa.test;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.calendars.Utilities;
import ec.tstoolkit.timeseries.regression.AbstractSingleTsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;

public class JulianEasterVariable extends AbstractSingleTsVariable {

	@Override
	public String getDescription() {
		return "Julian Easter";
	}

	@Override
	public boolean isSignificant(TsDomain domain) {

        // Julian Easter (in Gregorian dates) falls either in April or in May
		return domain.getFrequency() == TsFrequency.Monthly || domain.getFrequency() == TsFrequency.QuadriMonthly;
	}

	@Override
	public void data(TsPeriod start, DataBlock data) {

        // Rather inefficient algorithm
		// Create first a series initialized at 0
		TsData var = new TsData(new TsDomain(start, data.getLength()), 0);
		YearIterator iter = new YearIterator(var);
		while (iter.hasMoreElements()) {
			TsDataBlock cur = iter.nextElement();
			Day julianEaster = Utilities.julianEaster(cur.start.getYear(), true);
			// Creates the period that contains Easter
			TsPeriod p=new TsPeriod(start.getFrequency(), julianEaster);
			// search its position in this data block.
			int pos=p.minus(cur.start);
			// the series is NOT CORRECTED for long term mean effect.
			if (pos >= 0 && pos <cur.data.getLength()) {
				cur.data.set(pos, 1);
			}
		}
		data.copy(var);
	}

}
