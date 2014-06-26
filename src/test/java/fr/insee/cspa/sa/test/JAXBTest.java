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
import ec.tss.xml.regression.XmlTsVariables;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;
import ec.tss.xml.x13.XmlX13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
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
	    
	    // Read lines
		buffer = new BufferedReader(new FileReader(filename));
		try {
			buffer.readLine(); // skip header line
			while ((line = buffer.readLine()) != null) {
				String[] elements = line.split(sep);
				if (elements.length < 2) continue; 
				dates.add(format.parse(elements[0]));
				values.add(Double.parseDouble(elements[1].replace(",",".")));
			}
		}
		catch(Exception e) {buffer.close(); throw e;}
		buffer.close();
		
		// Guess frequency and transform data into TsData format
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
	
    	// Get a series and transform it to XmlTsData
		TsData reference = readTsData("src/test/resources/RF061.csv");
		XmlTsData xmlReference = new XmlTsData();
		xmlReference.copy(reference);

		// Write schema into file 
		FileOutputStream stream = new FileOutputStream("src/test/resources/RF061.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlReference, writer); 
		writer.flush();
		
		// Read schema
		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlTsData xmlSeries = (XmlTsData) unmarshaller.unmarshal(new File("src/test/resources/RF061.xml"));
		TsData series = xmlSeries.create();

		// Test if read and original series are equals
		Assert.assertEquals(0.0,series.distance(reference),1e-12);
	}

    /** Test reading TSRequest */
	@Test
	public void testUnmarshallTSRequest() throws Exception {

		JAXBContext context = JAXBContext.newInstance(TSRequest.class);
		
		// User regressor 
		TsData reg1 = readTsData("src/test/resources/REG1LPY.csv");
		TsVariable tsVariable = new TsVariable("REG1", reg1);
		TsVariables tsVariables = new TsVariables();
		tsVariables.set("REG1LPY",tsVariable);
	
		XmlTsVariables xmlTsVariables = new XmlTsVariables();
		xmlTsVariables.copy(tsVariables);
		xmlTsVariables.name = "regressor";
	
		TsVariableDescriptor descriptor = new TsVariableDescriptor("REG1LPY"); 
		descriptor.setEffect(ComponentType.Series); 
	
		// Specification. Link to user regressor does not work : bug in XmlTsVariableDescriptor
		TramoSeatsSpecification specification = PreSpecificationEnum.RSA5.getTramoSeatsSpecification().clone();
		//specification.getTramoSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[] {descriptor}); 
		XmlTramoSeatsSpecification xmlSpec = new XmlTramoSeatsSpecification();
		xmlSpec.copy(specification);
			
		// Series
		TsData reference = readTsData("src/test/resources/RF061.csv");
		XmlTsData xmlSeries = new XmlTsData();
		xmlSeries.copy(reference);
	
		// Create a TSRequest and add : user regressor, prespecification, specification, series, output filter
		TSRequest tsRequest = new TSRequest();
		
		tsRequest.setPreSpecification(PreSpecificationEnum.RSA3);
		tsRequest.setUserRegressors(xmlTsVariables);
		tsRequest.setSpecification(xmlSpec);
		tsRequest.setSeries(xmlSeries);
		tsRequest.setOutputFilter(Arrays.asList("y", "ycal", "tde"));

		// Write related XML schema into file
		FileOutputStream stream = new FileOutputStream("src/test/resources/tsrequest.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(tsRequest,writer);
		writer.flush();

		// Read XML schema and check series
		Unmarshaller unmarshaller = context.createUnmarshaller();
		TSRequest xmlTSRequest = (TSRequest) unmarshaller.unmarshal(new File("src/test/resources/tsrequest.xml"));
		TsData series = xmlTSRequest.getSeries().create();

		Assert.assertEquals(0.0,series.distance(reference),1e-12);
	}

    /** Test easter in tramoseats specification */	
	@Test
	public void testEasterTSSpecification() throws Exception {

		JAXBContext context = JAXBContext.newInstance(XmlTramoSeatsSpecification.class);
		
		// Create Tramoseats specification
		TramoSeatsSpecification reference = PreSpecificationEnum.RSA5.getTramoSeatsSpecification().clone();
		reference.getTramoSpecification().getRegression().getCalendar().getEaster().setDuration(2);
		XmlTramoSeatsSpecification xmlReference = new XmlTramoSeatsSpecification();
		xmlReference.copy(reference);

		// Write related schema into file
		FileOutputStream stream = new FileOutputStream("src/test/resources/tseaster.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlReference,writer);
		writer.flush();
		
		// Read XML schema and check Easter duration parameter 
		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlTramoSeatsSpecification xmlSpec = (XmlTramoSeatsSpecification) unmarshaller.unmarshal(new File("src/test/resources/tseaster.xml"));
		TramoSeatsSpecification specification = xmlSpec.create();
		
		Assert.assertEquals(2,specification.getTramoSpecification().getRegression().getCalendar().getEaster().getDuration());	
	}	

    /** Test reading X13Request */
	@Test
	public void testUnmarshallX13Request() throws Exception {

		JAXBContext context = JAXBContext.newInstance(X13Request.class);
		
		// User regressor
		TsData reg1 = readTsData("src/test/resources/REG1LPY.csv");
		TsVariable tsVariable = new TsVariable("REG1", reg1);
		TsVariables tsVariables = new TsVariables();
		tsVariables.set("REG1LPY",tsVariable);
		
		XmlTsVariables xmlTsVariables = new XmlTsVariables();
		xmlTsVariables.copy(tsVariables);
		xmlTsVariables.name = "regressor";
		
		TsVariableDescriptor descriptor = new TsVariableDescriptor("REG1LPY"); 
		descriptor.setEffect(ComponentType.Series); 
		
		// Specification. Link to user regressor does not work : bug in XmlTsVariableDescriptor
		X13Specification specification = PreSpecificationEnum.RSA5.getX13Specification().clone();
		//specification.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[] {descriptor});
		XmlX13Specification xmlSpec = new XmlX13Specification();
		xmlSpec.copy(specification);

		// Series
		TsData reference = readTsData("src/test/resources/RF061.csv");
		XmlTsData xmlSeries = new XmlTsData();
		xmlSeries.copy(reference);
		
		// Create X13 Request and add user regressors, specification, prespecification, series and output filter
		X13Request x13request = new X13Request();
		
		x13request.setUserRegressors(xmlTsVariables);
		x13request.setSpecification(xmlSpec);
		x13request.setPreSpecification(PreSpecificationEnum.RSA3);
		x13request.setSeries(xmlSeries);
		x13request.setOutputFilter(Arrays.asList("y", "ycal", "tde"));

		// Write related schema into file
		FileOutputStream stream = new FileOutputStream("src/test/resources/x13request.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
    	Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(x13request,writer);
		writer.flush();

		// Read XML schema and check series
		Unmarshaller unmarshaller = context.createUnmarshaller();
		X13Request xmlX13Request = (X13Request) unmarshaller.unmarshal(new File("src/test/resources/x13request.xml"));
		TsData series = xmlX13Request.getSeries().create();

		Assert.assertEquals(0.0,series.distance(reference),1e-12);
	}
 
	 /** Test easter in tramoseats specification */	
	@Test
	public void testEasterX13Specification() throws Exception {

		JAXBContext context = JAXBContext.newInstance(XmlX13Specification.class);
			
		// Create X13 specification
		X13Specification reference = PreSpecificationEnum.RSA5.getX13Specification().clone();
		reference.getRegArimaSpecification().getRegression().getEaster().setW(2);
		XmlX13Specification xmlReference = new XmlX13Specification();
		xmlReference.copy(reference);

		// Write related schema into file
		FileOutputStream stream = new FileOutputStream("src/test/resources/x13easter.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
	    Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlReference,writer);
		writer.flush();
			
		// Read XML schema and check Easter duration parameter 
		Unmarshaller unmarshaller = context.createUnmarshaller();
		XmlX13Specification xmlSpec = (XmlX13Specification) unmarshaller.unmarshal(new File("src/test/resources/x13easter.xml"));
		X13Specification specification = xmlSpec.create();
			
		Assert.assertEquals(2,specification.getRegArimaSpecification().getRegression().getEaster().getW());	
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
}
