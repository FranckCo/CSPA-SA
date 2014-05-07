package fr.insee.cspa.sa.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.xml.XmlTsData;
import ec.tss.xml.information.XmlInformationSet;
import ec.tss.xml.regression.XmlTsVariable;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;
import ec.tss.xml.x13.XmlX13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec.Type;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import fr.insee.cspa.sa.content.PreSpecificationEnum;
import fr.insee.cspa.sa.content.TSRequest;

public class JAXBTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	static double[] values = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

	@Test
	public void testMarshallTsData() throws JAXBException {

		TsPeriod start = new TsPeriod(TsFrequency.Monthly, 2012, 0);
		TsData series = new TsData(start, values, true);

		XmlTsData xmlSeries = new XmlTsData();
		xmlSeries.copy(series);

    	JAXBContext context = JAXBContext.newInstance(XmlTsData.class);
    	Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(xmlSeries, new PrintWriter(System.out));
	}

	@Test
	public void testUnmarshallTsData() throws JAXBException, IOException {

		JAXBContext context = JAXBContext.newInstance(XmlTsData.class);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlTsData xmlSeries = (XmlTsData) unmarshaller.unmarshal(new File("src/test/resources/tsdata.xml"));
		TsData series = xmlSeries.create();

		System.out.println(series);
	}

	@Test
	public void testMarshallTSRequest() throws JAXBException {

		TSRequest tsRequest = new TSRequest();

		TsData series = new TsData(new TsPeriod(TsFrequency.Monthly, 2012, 0), values, true);
		XmlTsData xmlSeries = new XmlTsData();
		xmlSeries.copy(series);

		XmlTramoSeatsSpecification xmlSpec = new XmlTramoSeatsSpecification();
		xmlSpec.copy(TramoSeatsSpecification.RSA5.clone());

		tsRequest.setSeries(xmlSeries);
		tsRequest.setPreSpecification(PreSpecificationEnum.RSA3);
		tsRequest.setSpecification(xmlSpec);

		TsVariable tsVariable = new TsVariable("Series One", series);
		XmlTsVariable xmlTsVariable = new XmlTsVariable();
		xmlTsVariable.copy(tsVariable);
		xmlTsVariable.name = "Regressor One";
		tsRequest.setUserRegressors(new XmlTsVariable[] {xmlTsVariable});

		tsRequest.setOutputFilter(Arrays.asList("item1", "item2", "item3"));

		JAXBContext context = JAXBContext.newInstance(TSRequest.class);
    	Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(tsRequest, new PrintWriter(System.out));
	}

	@Test
	public void testUnmarshallTSRequest() throws JAXBException {

		JAXBContext context = JAXBContext.newInstance(TSRequest.class);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		TSRequest xmlTSRequest = (TSRequest) unmarshaller.unmarshal(new File("src/test/resources/tsrequest.xml"));
		TsData series = xmlTSRequest.getSeries().create();

		System.out.println(series);
	}

	@Test
	public void testMarshallTSSpecification() {

		TramoSeatsSpecification specification = TramoSeatsSpecification.RSA5.clone();
		XmlTramoSeatsSpecification xmlSpec = new XmlTramoSeatsSpecification();
		xmlSpec.copy(specification);

		xmlSpec.serialize(new PrintWriter(System.out));
	}

	@Test
	public void testMarshallTSSpecificationRegression() {

		TramoSeatsSpecification specification = TramoSeatsSpecification.RSA5.clone();

		InterventionVariable iv1 = new InterventionVariable();
		iv1.setDelta(3.14);
		iv1.setD1DS(true);
		InterventionVariable iv2 = new InterventionVariable();
		iv2.setDelta(2.79);
		iv2.setD1DS(false);

		specification.getTramoSpecification().getRegression().add(iv1);
		specification.getTramoSpecification().getRegression().add(iv2);

		TsVariableDescriptor var1 = new TsVariableDescriptor();
		var1.setName("var1");
		var1.setLags(1, 3);

		// FIXME Next instruction makes creates truncated serialization
		//specification.getTramoSpecification().getRegression().add(var1);

		Ramp ramp = new Ramp();
		ramp.setStart(new Day(2001, Month.January, 0)); // Day is 0-based
		ramp.setEnd(new Day(2004, Month.December, 30));

		specification.getTramoSpecification().getRegression().add(ramp);

		XmlTramoSeatsSpecification xmlSpec = new XmlTramoSeatsSpecification();
		xmlSpec.copy(specification);

		xmlSpec.serialize(new PrintWriter(System.out));
	}

	@Test
	public void testUnmarshallTSSpecification() throws JAXBException {

		JAXBContext context = JAXBContext.newInstance(XmlTramoSeatsSpecification.class);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlTramoSeatsSpecification xmlSpec = (XmlTramoSeatsSpecification) unmarshaller.unmarshal(new File("src/test/resources/tssa3.xml"));
		TramoSeatsSpecification specification = xmlSpec.create();

		System.out.println(specification.toLongString());
	}

	@Test
	public void testMarshallX13Specification() {

		X13Specification specification = X13Specification.RSA1.clone();
		XmlX13Specification xmlSpec = new XmlX13Specification();
		xmlSpec.copy(specification);

		xmlSpec.serialize(new PrintWriter(System.out));
	}

	@Test
	public void testUnmarshallX13Specification() throws JAXBException, IOException {

		JAXBContext context = JAXBContext.newInstance(ec.tss.xml.x13.XmlX13Specification.class);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlX13Specification xmlSpec = (XmlX13Specification) unmarshaller.unmarshal(new File("src/test/resources/x13sa5.xml"));
		X13Specification specification = xmlSpec.create();

		System.out.println(specification.toLongString());
	}

	@Test
	public void testMarshallX13CalendarSpecification() throws JAXBException {

		RegressionSpec specification = new RegressionSpec();
		specification.setAICCDiff(3.14159);
		// FIXME 
		TradingDaysSpec tradingDays = new TradingDaysSpec();
		tradingDays.setAutoAdjust(true);
		tradingDays.setStockTradingDays(24);
		tradingDays.setTradingDaysType(ec.tstoolkit.timeseries.calendars.TradingDaysType.WorkingDays);
		tradingDays.setHolidays("holiday");
		tradingDays.setChangeOfRegime(new ChangeOfRegimeSpec(new Day(2013, Month.April, 0), Type.Full));
		specification.setTradingDays(tradingDays);

		MovingHolidaySpec mhSpec1 = MovingHolidaySpec.easterSpec(true);
		MovingHolidaySpec mhSpec2 = MovingHolidaySpec.easterSpec(false);

		specification.setMovingHolidays(new MovingHolidaySpec[] {mhSpec1, mhSpec2});

		// Can't marshall directly XmlXmlCalendarSpec
		//XmlCalendarSpec xmlSpec = XmlCalendarSpec.create(specification);
		//JAXBContext context = JAXBContext.newInstance(XmlCalendarSpec.class);
		//Marshaller marshaller = context.createMarshaller();
		//marshaller.marshal(xmlSpec, System.out);

		X13Specification x13Spec = X13Specification.RSA1.clone();
		x13Spec.getRegArimaSpecification().setRegression(specification);

		XmlX13Specification xmlSpec = new XmlX13Specification();
		xmlSpec.copy(x13Spec);

		xmlSpec.serialize(new PrintWriter(System.out));

	}

	@Test
	public void testMarshalTSOutputs() throws Exception {

		// Tramo-Seats processing
		SequentialProcessing<TsData> processing = TramoSeatsProcessingFactory.instance.generateProcessing(TramoSeatsSpecification.RSA4);
		CompositeResults results = processing.process(SeasonalAdjustmentTest.exportsTS);

		// Complete serialization
		InformationSet info = InformationSetHelper.fromProcResults(results);
		XmlInformationSet xmlInfo = new XmlInformationSet();
		xmlInfo.copy(info);

		JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

		FileOutputStream stream = new FileOutputStream("src/test/resources/tsout.xml");

		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlInfo, writer);

		writer.flush();

		// Partial serialization: only the main decomposition and the m-statistics (using wildcards)
		// Only the necessary output will be computed
		String[] filters = new String[] {"s", "sa", "t", "i", "s_f", "sa_f", "t_f", "i_f", "mstat*"};
		Set<String> set = new HashSet<String>(Arrays.asList(filters));

		InformationSet partialInfo = InformationSetHelper.fromProcResults(results, set);
		XmlInformationSet xmlPartialInfo = new XmlInformationSet();
		xmlPartialInfo.copy(partialInfo);

		stream = new FileOutputStream("src/test/resources/tsoutp.xml");
		writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(xmlPartialInfo, writer);
		writer.flush();
	}

	@Test
	public void testMarshalX13Outputs() throws Exception {

		// X13 processing
		SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(X13Specification.RSA4);
		CompositeResults results = processing.process(SeasonalAdjustmentTest.exportsTS);

		// Complete serialization
		InformationSet info = InformationSetHelper.fromProcResults(results);
		XmlInformationSet xmlInfo = new XmlInformationSet();
		xmlInfo.copy(info);

		JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

		FileOutputStream stream = new FileOutputStream("src/test/resources/x13out.xml");

		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlInfo, writer);

		writer.flush();

		// Partial serialization: only the main decomposition and the m-statistics (using wildcards)
		// Only the necessary output will be computed

		String[] filters = new String[] {"s", "sa", "t", "i", "s_f", "sa_f", "t_f", "i_f", "mstat*"};
		Set<String> set = new HashSet<String>(Arrays.asList(filters));

		InformationSet partialInfo = InformationSetHelper.fromProcResults(results, set);
		XmlInformationSet xmlPartialInfo = new XmlInformationSet();
		xmlPartialInfo.copy(partialInfo);

		stream = new FileOutputStream("src/test/resources/x13outp.xml");
		writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(xmlPartialInfo, writer);
		writer.flush();
	}
}
