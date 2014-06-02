package fr.insee.cspa.sa.test;

import java.util.List;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.ITsModifier;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

public class TramoExpander implements ITsModifier {

    TsVariable variable;
    PreprocessingModel model;

    TramoExpander(TsVariable variable, TramoSpecification specification) {
    	this.variable = variable;
    	model = specification.build().process(variable.getTsData(), new ModellingContext());
    }

    TramoExpander(TsData series, TramoSpecification specification) {
    	variable = new TsVariable(series);
    	model=specification.build().process(series, new ModellingContext());
    }

	public void data(TsDomain domain, List<DataBlock> data, int start) {

        TsDomain vdom = variable.getTsData().getDomain();
        int nb = vdom.getStart().minus(domain.getStart());
        int nf = domain.getEnd().minus(vdom.getEnd());

        TsData backcast = null, forecast = null;
        if (nb > 0) backcast = model.backcast(nb, false);
        if (nf > 0) forecast = model.forecast(nf, false);

        TsData series = variable.getTsData().fittoDomain(domain);
        if (backcast != null) series = series.update(backcast);
        if (forecast != null) series = series.update(forecast);

        data.get(start).copy(series);
	}

	public TsDomain getDefinitionDomain() {
		return null;
	}

	public TsFrequency getDefinitionFrequency() {
		return variable.getDefinitionFrequency();
	}

	public String getDescription() {
		return variable.getDescription();
	}

	public int getDim() {
		return 1;
	}

	public String getItemDescription(int index) {
		return variable.getItemDescription(index);
	}

	public boolean isSignificant(TsDomain domain) {
		return domain.getFrequency() == variable.getDefinitionFrequency();
	}

	public ITsVariable getVariable() {
		return variable;
	}
}
