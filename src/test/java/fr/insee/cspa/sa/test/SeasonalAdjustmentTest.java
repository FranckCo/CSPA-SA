package fr.insee.cspa.sa.test;


import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ec.satoolkit.GenericSaProcessingFactory;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.seats.SeatsSpecification.EstimationMethod;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.ucarima.UcarimaModel;

public class SeasonalAdjustmentTest {

	private static final double[] g_exports = {
        9568.3, 9920.3, 11353.5, 9247.5, 10114.2, 10763.1, 8456.1, 8071.6, 10328, 10551.4, 10186.1, 8821.6,
        9841.3, 10233.6, 10794.6, 10289.3, 10513.4, 10607.6, 9707.4, 8103.5, 10982.6, 11836.9, 10517.5, 9810.5,
        10374.8, 10855.3, 11671.3, 11901.2, 10846.4, 11917.5, 11362.8, 9314.5, 12605.9, 12815.1, 11254.5, 11111.8,
        11282.9, 11554.5, 12935.6, 12146.3, 11615.3, 13214.8, 11735.5, 9522.3, 12694.8, 12317.6, 11450, 11380.9,
        10604.6, 10972.2, 13331.5, 11733.1, 11284.7, 13295.8, 11881.4, 10374.2, 13828, 13490.5, 13092.2, 13184.4,
        12398.4, 13882.3, 15861.5, 13286.1, 15634.9, 14211, 13646.8, 12224.6, 15916.4, 16535.9, 15796, 14418.6,
        15044.5, 14944.2, 16754.8, 14254, 15454.9, 15644.8, 14568.3, 12520.2, 14803, 15873.2, 14755.3, 12875.1,
        14291.1, 14205.3, 15859.4, 15258.9, 15498.6, 15106.5, 15023.6, 12083, 15761.3, 16943, 15070.3, 13659.6,
        14768.9, 14725.1, 15998.1, 15370.6, 14956.9, 15469.7, 15101.8, 11703.7, 16283.6, 16726.5, 14968.9, 14861,
        14583.3, 15305.8, 17903.9, 16379.4, 15420.3, 17870.5, 15912.8, 13866.5, 17823.2, 17872, 17420.4, 16704.4,
        15991.5, 16583.6, 19123.4, 17838.8, 17335.3, 19026.9, 16428.6, 15337.4, 19379.8, 18070.5, 19563, 18190.6,
        17658, 18437.9, 21510.4, 17111, 19732.7, 20221.8};
	
	public static final TsData exportsTS = new TsData(TsFrequency.Monthly, 1995, 0, g_exports, false);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGenericTramoSeats() {

		// Create the specifications
		TramoSeatsSpecification specification = TramoSeatsSpecification.RSA4.clone();
		// Allow benchmarking (for example)
		specification.getBenchmarkingSpecification().setEnabled(true);

		// The long way: generate the processing, using the Tramo-Seats factory
		// IProcessing<TsData, ?> processing = new TramoSeatsProcessingFactory().generateProcessing(mySpec);
		// IProcResults results = processing.process(exportsTS);
		// The short way
		IProcResults results = TramoSeatsProcessingFactory.process(exportsTS, specification);
		TsData sa = results.getData("sa", TsData.class);
		System.out.println(sa);
		TsData sabench = results.getData("benchmarking.result", TsData.class);
		System.out.println(sabench);

		// All the possible results are defined in the dictionary of "results"
		Map<String, Class> dictionary = results.getDictionary();
		for (Entry<String, Class> entry : dictionary.entrySet()) {
			System.out.println(entry.getKey() + " (" + entry.getValue().toString() + ")");
		}

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGenericX13() {
		// Create the specifications
		X13Specification specification = X13Specification.RSA4.clone();
		// Allow benchmarking
		specification.getBenchmarkingSpecification().setEnabled(true);
		// Create the input (for example...)

		// The long way: generate the processing, using the X13 factory
		// IProcessing<TsData, ?> processing =  new X13ProcessingFactory().generateProcessing(specification);
		// IProcResults results = processing.process(exportsTS);
		// The short way
		IProcResults results = X13ProcessingFactory.process(exportsTS, specification);
		TsData sa = results.getData("sa", TsData.class);
		System.out.println(sa);
		TsData sabench = results.getData("benchmarking.result", TsData.class);
		System.out.println(sabench);

		// Dictionary
		Map<String, Class> dictionary = results.getDictionary();
		for (Entry<String, Class> entry : dictionary.entrySet()) {
			System.out.println(entry.getKey() + " (" + entry.getValue().toString() + ")");
		}
	}

	@Test
	public void testTramoSpec() {

		// Create the specifications
		TramoSeatsSpecification tramoSpec = TramoSeatsSpecification.RSA4.clone();
		// New Easter variable
		tramoSpec.getTramoSpecification().getRegression().getCalendar().getEaster().setOption(Type.IncludeEasterMonday);
		// Automatic detection of the "best" td specification (working days or trading days)
		tramoSpec.getTramoSpecification().getRegression().getCalendar().getTradingDays().setAutomatic(true);
		// Kalman smoother estimates
		tramoSpec.getSeatsSpecification().setMethod(EstimationMethod.KalmanSmoother);
		// Allow benchmarking
		tramoSpec.getBenchmarkingSpecification().setEnabled(true);

		CompositeResults results = TramoSeatsProcessingFactory.process(exportsTS, tramoSpec);

		// Gets the different parts of the results and some of their specific output.
		//SingleTsData input = results.get(GenericSaProcessingFactory.INPUT, SingleTsData.class);
		//assertTrue(input != null);
		//TsData original = input.getSeries();
		//assertTrue(original != null);
		PreprocessingModel regarima = results.get(GenericSaProcessingFactory.PREPROCESSING, PreprocessingModel.class);
		assertTrue(regarima != null);
		TsData residuals = regarima.getFullResiduals();
		assertTrue(residuals != null);
		SeatsResults seats = results.get(GenericSaProcessingFactory.DECOMPOSITION, SeatsResults.class);
		assertTrue(seats != null);
		UcarimaModel ucModel = seats.getUcarimaModel();
		assertTrue(ucModel != null);
		ISeriesDecomposition finalDecomposition = results.get(GenericSaProcessingFactory.FINAL, ISeriesDecomposition.class);
		assertTrue(finalDecomposition != null);
		SaBenchmarkingResults bench = results.get(GenericSaProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
		assertTrue(bench != null);
		TsData target = bench.getTarget();
		assertTrue(target != null);
	}

	@Test
	public void testX13Spec() {

		// Create the specifications
		X13Specification x13Spec = X13Specification.RSA4.clone();
		// Change the default test for regression variables
		x13Spec.getRegArimaSpecification().getRegression().setAICCDiff(2);
		// Set the length of the Henderson filter
		x13Spec.getX11Specification().setHendersonFilterLength(17);
		// Set specific seasonal filters. Just to test
		SeasonalFilterOption[] filters = new SeasonalFilterOption[exportsTS.getFrequency().intValue()];
		for (int i = 0; i < 2; ++i) filters[i] = SeasonalFilterOption.S3X15;
		for (int i = 2; i < filters.length; ++i) filters[i] = SeasonalFilterOption.S3X5;
		x13Spec.getX11Specification().setSeasonalFilters(filters);
		// Allow benchmarking
		x13Spec.getBenchmarkingSpecification().setEnabled(true);

		CompositeResults results = X13ProcessingFactory.process(exportsTS, x13Spec);

		// Gets the different parts of the results and some of their specific output.
		//SingleTsData input = results.get(GenericSaProcessingFactory.INPUT, SingleTsData.class);
		//assertTrue(input != null);
		//TsData original = input.getSeries();
		//assertTrue(original != null);

		PreprocessingModel regarima = results.get(GenericSaProcessingFactory.PREPROCESSING, PreprocessingModel.class);
		assertTrue(regarima != null);
		TsData residuals = regarima.getFullResiduals();
		assertTrue(residuals != null);
		X11Results x11 = results.get(GenericSaProcessingFactory.DECOMPOSITION, X11Results.class);
		assertTrue(x11 != null);
		//double q = x11.getMStatistics().getQ();
		//System.out.println("q = " + q);
		List<Information<TsData>> btables = x11.getInformation().deepSelect("b*", TsData.class);
		for (Information<TsData> info : btables) {
			System.out.println(info.name + " = " + info.value);
		}
		ISeriesDecomposition finalDecomposition = results.get(GenericSaProcessingFactory.FINAL, ISeriesDecomposition.class);
		assertTrue(finalDecomposition != null);
		SaBenchmarkingResults bench = results.get(GenericSaProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
		assertTrue(bench != null);
		TsData target = bench.getTarget();
		assertTrue(target != null);
	}
}
