package fr.insee.cspa.sa.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ec.tss.xml.information.XmlInformationSet;
import fr.insee.cspa.sa.content.TSRequest;
import fr.insee.cspa.sa.content.X13Request;

public class JerseyTest {

	static private String TS_SERVICE_URL = "http://localhost:8080/seasonal-adjustment-service/ts";
	static private String X13_SERVICE_URL = "http://localhost:8080/seasonal-adjustment-service/x13";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void testTsAnalysis() throws Exception {
			
		JAXBContext context = JAXBContext.newInstance(TSRequest.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		TSRequest xmlTSRequest = (TSRequest) unmarshaller.unmarshal(new File("src/test/resources/tsrequest.xml"));
		
		Client client = ClientBuilder.newClient();
		XmlInformationSet outputs = client
									.target(TS_SERVICE_URL)
									.request()
									.post(Entity.entity(xmlTSRequest, MediaType.APPLICATION_XML),XmlInformationSet.class);
		
		JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);		
	
		FileOutputStream stream = new FileOutputStream("src/test/resources/tsjerseyout.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(outputs, writer);
	}
	
	@Test
	public void testX13Analysis() throws Exception {
			
		JAXBContext context = JAXBContext.newInstance(X13Request.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		X13Request xmlX13Request = (X13Request) unmarshaller.unmarshal(new File("src/test/resources/x13request.xml"));
		
		Client client = ClientBuilder.newClient();
		XmlInformationSet outputs = client
									.target(X13_SERVICE_URL)
									.request()
									.post(Entity.entity(xmlX13Request, MediaType.APPLICATION_XML),XmlInformationSet.class);
		
		JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
		Marshaller marshaller = jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);		
	
		FileOutputStream stream = new FileOutputStream("src/test/resources/x13jerseyout.xml");
		OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		marshaller.marshal(outputs, writer);
	}
}
