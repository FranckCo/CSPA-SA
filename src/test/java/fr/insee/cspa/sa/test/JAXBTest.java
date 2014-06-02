package fr.insee.cspa.sa.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.AfterClass;
import org.junit.Assert;
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
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import fr.insee.cspa.sa.content.PreSpecificationEnum;
import fr.insee.cspa.sa.content.TSRequest;
import fr.insee.cspa.sa.content.X13Request;

public class JAXBTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/** Internal time series reader */
	private TsData readTsData(String filename) throws Exception {
		
		BufferedReader buffer; String line; String sep = ";";
	    ArrayList<Double> values = new ArrayList<Double>(); 
	    ArrayList<Date> dates = new ArrayList<Date>();
	    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	    
		buffer = new BufferedReader(new FileReader(filename));
		try {
			buffer.readLine();
			while ((line = buffer.readLine()) != null) {
				String[] elements = line.split(sep);
				if (elements.length < 2) continue; 
				dates.add(format.parse(elements[0]));
				values.add(Double.parseDouble(elements[1].replace(",",".")));
			}
		}
		catch(Exception e) {buffer.close(); throw e;}
		buffer.close();
		
		if (dates.size()<2) throw new Exception("too short file");
		int duration = (int)((dates.get(1).getTime() - dates.get(0).getTime())/(1000.*3600*24*28));
		TsFrequency frequency = TsFrequency.valueOf(12/duration);
		TsPeriod start = new TsPeriod(frequency, dates.get(0));
		double[] data = new double[values.size()];
		int i=0; for (Double d : values) data[i++]=d.doubleValue(); 
		TsData series = new TsData(start, data, true);
		return series;
	}
	
	
	/** Test reading XML formatted time series */ 
	@Test
	public void testUnmarshallTsData() throws Exception {
	
    	JAXBContext context = JAXBContext.newInstance(XmlTsData.class);
		
		TsData reference = readTsData("src/test/resources/RF061.csv");
		XmlTsData xmlReference = new XmlTsData();
		xmlReference.copy(reference);

		FileOutputStream stream = new FileOutputStream("src/test/resources/RF061.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlReference, writer); 
		writer.flush();
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlTsData xmlSeries = (XmlTsData) unmarshaller.unmarshal(new File("src/test/resources/RF061.xml"));
		TsData series = xmlSeries.create();

		Assert.assertEquals(0.0,series.distance(reference),1e-12);
	}

    /** Test reading TSRequest */
	@Test
	public void testUnmarshallTSRequest() throws Exception {

		JAXBContext context = JAXBContext.newInstance(TSRequest.class);
		
		TSRequest tsRequest = new TSRequest();

		TsData reference = readTsData("src/test/resources/RF061.csv");
		XmlTsData xmlSeries = new XmlTsData();
		xmlSeries.copy(reference);
		
		TsData reg1 = readTsData("src/test/resources/REG1LPY.csv");
		TsVariable tsVariable = new TsVariable("REG1", reg1);
		XmlTsVariable xmlTsVariable = new XmlTsVariable();
		xmlTsVariable.copy(tsVariable);
		xmlTsVariable.name = "REG1LPY";
		tsRequest.setUserRegressors(new XmlTsVariable[] {xmlTsVariable});
		
		TramoSeatsSpecification specification = PreSpecificationEnum.RSA5.getTramoSeatsSpecification().clone();
		//TsVariableDescriptor descriptor = new TsVariableDescriptor("REG1LPY");  // bug in XmlTsVariableDescriptor
		//descriptor.setEffect(ComponentType.Series); 
		//specification.getTramoSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[] {descriptor});
		XmlTramoSeatsSpecification xmlSpec = new XmlTramoSeatsSpecification();
		xmlSpec.copy(specification);

		tsRequest.setSeries(xmlSeries);
		tsRequest.setPreSpecification(PreSpecificationEnum.RSA3);
		tsRequest.setSpecification(xmlSpec);
		
		tsRequest.setOutputFilter(Arrays.asList("y", "ycal", "tde"));

		FileOutputStream stream = new FileOutputStream("src/test/resources/tsrequest.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(tsRequest,writer);
		writer.flush();

		Unmarshaller unmarshaller = context.createUnmarshaller();
		TSRequest xmlTSRequest = (TSRequest) unmarshaller.unmarshal(new File("src/test/resources/tsrequest.xml"));
		TsData series = xmlTSRequest.getSeries().create();

		Assert.assertEquals(0.0,series.distance(reference),1e-12);
	}

    /** Test easter in tramoseats specification */	
	@Test
	public void testEasterTSSpecification() throws Exception {

		JAXBContext context = JAXBContext.newInstance(XmlTramoSeatsSpecification.class);
		
		TramoSeatsSpecification reference = PreSpecificationEnum.RSA5.getTramoSeatsSpecification().clone();
		reference.getTramoSpecification().getRegression().getCalendar().getEaster().setDuration(2);
		XmlTramoSeatsSpecification xmlReference = new XmlTramoSeatsSpecification();
		xmlReference.copy(reference);

		FileOutputStream stream = new FileOutputStream("src/test/resources/tseaster.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlReference,writer);
		writer.flush();
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlTramoSeatsSpecification xmlSpec = (XmlTramoSeatsSpecification) unmarshaller.unmarshal(new File("src/test/resources/tseaster.xml"));
		TramoSeatsSpecification specification = xmlSpec.create();
		
		Assert.assertEquals(2,specification.getTramoSpecification().getRegression().getCalendar().getEaster().getDuration());	
	}	

    /** Test reading X13Request */
	@Test
	public void testUnmarshallX13Request() throws Exception {

		JAXBContext context = JAXBContext.newInstance(X13Request.class);
		
		X13Request x13request = new X13Request();

		TsData reference = readTsData("src/test/resources/RF061.csv");
		XmlTsData xmlSeries = new XmlTsData();
		xmlSeries.copy(reference);
		
		TsData reg1 = readTsData("src/test/resources/REG1LPY.csv");
		TsVariable tsVariable = new TsVariable("REG1", reg1);
		XmlTsVariable xmlTsVariable = new XmlTsVariable();
		xmlTsVariable.copy(tsVariable);
		xmlTsVariable.name = "REG1LPY";
		x13request.setUserRegressors(new XmlTsVariable[] {xmlTsVariable});
		
		X13Specification specification = PreSpecificationEnum.RSA5.getX13Specification().clone();
		//TsVariableDescriptor descriptor = new TsVariableDescriptor("REG1LPY");  // bug in XmlTsVariableDescriptor
		//descriptor.setEffect(ComponentType.Series); 
		//specification.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[] {descriptor});
		XmlX13Specification xmlSpec = new XmlX13Specification();
		xmlSpec.copy(specification);

		x13request.setSeries(xmlSeries);
		x13request.setPreSpecification(PreSpecificationEnum.RSA3);
		x13request.setSpecification(xmlSpec);
		
		x13request.setOutputFilter(Arrays.asList("y", "ycal", "tde"));

		FileOutputStream stream = new FileOutputStream("src/test/resources/x13request.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		writer.flush();
		
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(x13request,writer);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		X13Request xmlX13Request = (X13Request) unmarshaller.unmarshal(new File("src/test/resources/x13request.xml"));
		TsData series = xmlX13Request.getSeries().create();

		Assert.assertEquals(0.0,series.distance(reference),1e-12);
	}
 
	/** Test writing outputs in TS analysis */
	@Test
	public void testMarshalTSOutputs() throws Exception {

		JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		// Tramo-Seats processing
		TsData series = readTsData("src/test/resources/RF061.csv");
		TramoSeatsSpecification specification = PreSpecificationEnum.RSA5.getTramoSeatsSpecification().clone();
		CompositeResults results = TramoSeatsProcessingFactory.process(series,specification);

		// Complete serialization
		InformationSet info = InformationSetHelper.fromProcResults(results);
		XmlInformationSet xmlInfo = new XmlInformationSet();
		xmlInfo.copy(info);

		FileOutputStream stream = new FileOutputStream("src/test/resources/tsout.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(xmlInfo, writer);
		writer.flush();

		// Partial serialization: only the main decomposition and the m-statistics (using wildcards)
		// Only the necessary output will be computed
		String[] filters = new String[] {"s", "sa", "t", "i", "s_f", "sa_f", "t_f", "i_f","regression*"};
		Set<String> set = new HashSet<String>(Arrays.asList(filters));

		InformationSet partialInfo = InformationSetHelper.fromProcResults(results, set);
		XmlInformationSet xmlPartialInfo = new XmlInformationSet();
		xmlPartialInfo.copy(partialInfo);

		stream = new FileOutputStream("src/test/resources/tsoutp.xml");
		writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(xmlPartialInfo, writer);
		writer.flush();
	}


    /** Test writing outputs in X13 analysis */
	@Test
	public void testMarshalX13Outputs() throws Exception {

		JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		// X13 processing
		TsData series = readTsData("src/test/resources/RF061.csv");
		X13Specification specification = PreSpecificationEnum.RSA5.getX13Specification().clone();
		CompositeResults results = X13ProcessingFactory.process(series,specification);
		
		// Complete serialization
		InformationSet info = InformationSetHelper.fromProcResults(results);
		XmlInformationSet xmlInfo = new XmlInformationSet();
		xmlInfo.copy(info);

		FileOutputStream stream = new FileOutputStream("src/test/resources/x13out.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(xmlInfo, writer);
		writer.flush();

		// Partial serialization: only the main decomposition and the m-statistics (using wildcards)
		// Only the necessary output will be computed

		String[] filters = new String[] {"s", "sa", "t", "i", "s_f", "sa_f", "t_f", "i_f", "regression*"};
		Set<String> set = new HashSet<String>(Arrays.asList(filters));

		InformationSet partialInfo = InformationSetHelper.fromProcResults(results, set);
		XmlInformationSet xmlPartialInfo = new XmlInformationSet();
		xmlPartialInfo.copy(partialInfo);

		stream = new FileOutputStream("src/test/resources/x13outp.xml");
		writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(xmlPartialInfo, writer);
		writer.flush();
	}
	
/*	@Test
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
*/
/*	@Test
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

	}*/
}
