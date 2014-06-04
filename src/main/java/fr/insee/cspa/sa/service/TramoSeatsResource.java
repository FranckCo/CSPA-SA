package fr.insee.cspa.sa.service;

import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.xml.information.XmlInformationSet;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.timeseries.simplets.TsData;
import fr.insee.cspa.sa.content.PreSpecificationEnum;
import fr.insee.cspa.sa.content.TSRequest;

@Path("ts")
public class TramoSeatsResource {

	@GET
	public Response goToGet() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GET /type => Detail of predefined Specification [type] where type = RSA0 ... RSA5<br/>");
		buffer.append("POST => Tramo-Seats service analysis");
		return Response.status(200).entity(buffer.toString()).build();
	}

	/**
	 * Get predefined specifications
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{type}")
	public Response getTSSpecification(final @PathParam("type") PreSpecificationEnum type) {
		
		XmlTramoSeatsSpecification xmlSpecification = new XmlTramoSeatsSpecification();
		xmlSpecification.copy(type.getTramoSeatsSpecification());
		
		return Response.status(200).entity(xmlSpecification).build();
	}
	
	/** 
	 * Tramoseats analysis 
	 */
	@POST
	public Response runTramoSeatsAnalysis(TSRequest request) {
		
		// Get request elements
		TsData series = request.getSeries().create();
		PreSpecificationEnum typePre = request.getPreSpecification();
		TramoSeatsSpecification specification = request.getSpecification().create();
		List<String> outputFilter = request.getOutputFilter();
		//XmlTsVariable[] xmlVariables = request.getUserRegressors();
	
		// Merge preSpecification and specification	
		specification = merge(specification, typePre);
		
		// Analysis
		CompositeResults results = TramoSeatsProcessingFactory.process(series, specification); // TODO exception management ?
		
		// Get outputs
		InformationSet outputs = InformationSetHelper.fromProcResults(results, new TreeSet<String>(outputFilter));
		XmlInformationSet xmlOutputs = new XmlInformationSet();
		xmlOutputs.copy(outputs);
	
		return Response.status(200).entity(xmlOutputs).build();
	}
	
	/**
	 * Merge specification with prespecification
	 */	
	private TramoSeatsSpecification merge(TramoSeatsSpecification specification, PreSpecificationEnum typePre) {

		if (typePre == null && specification == null) specification = new TramoSeatsSpecification();
		else if (typePre != null) {
			if (specification == null) specification = typePre.getTramoSeatsSpecification();
			else  {
				TramoSeatsSpecification preSpecification = typePre.getTramoSeatsSpecification();
				TramoSeatsSpecification reference = new TramoSeatsSpecification();
				
				InformationSet infoSpec = specification.write(true);
				InformationSet infoPre = preSpecification.write(true);
				InformationSet infoRef = reference.write(true);
			
				specification.read(UtilResource.merge(infoSpec, infoPre, infoRef));
			}
		}
		return specification;
	}
}
