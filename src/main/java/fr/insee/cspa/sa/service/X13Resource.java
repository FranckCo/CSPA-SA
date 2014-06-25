package fr.insee.cspa.sa.service;

import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x13.X13Specification;
import ec.tss.xml.information.XmlInformationSet;
import ec.tss.xml.x13.XmlX13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.timeseries.simplets.TsData;
import fr.insee.cspa.sa.content.PreSpecificationEnum;
import fr.insee.cspa.sa.content.X13Request;

@Path("x13")
public class X13Resource {

	@GET
	public Response goToGet() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GET /type => Detail of predefined Specification [type] where type = RSA0 ... RSA5<br/>");
		buffer.append("POST => X13 service analysis");
		return Response.status(200).entity(buffer.toString()).build();
	}

	/**
	 * Get predefined specifications
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{type}")
	public Response getX13Specification(final @PathParam("type") String type) {
		
		try {
			PreSpecificationEnum prespec = PreSpecificationEnum.valueOf(type.toUpperCase());
			XmlX13Specification xmlSpecification = new XmlX13Specification();
			xmlSpecification.copy(prespec.getX13Specification());
			
			return Response.status(200).entity(xmlSpecification).build();
		}
		catch(IllegalArgumentException e) {
			return Response.status(404).build();
		}
	}
	
	/** 
	 * X13 analysis 
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runTramoSeatsAnalysis(X13Request request) {
		
		// Get request elements
		TsData series = request.getSeries().create();
		PreSpecificationEnum typePre = request.getPreSpecification();
		X13Specification specification = request.getSpecification().create();
		List<String> outputFilter = request.getOutputFilter();
		//XmlTsVariable[] xmlVariables = request.getUserRegressors();
	
		// Merge preSpecification and specification	
		specification = merge(specification, typePre);
		
		// Analysis
		CompositeResults results;
		try {results = X13ProcessingFactory.process(series, specification);}
		catch(Exception e) {return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();}
				
		// Get outputs
		XmlInformationSet xmlOutputs = new XmlInformationSet();
		InformationSet outputs = (outputFilter == null) ? InformationSetHelper.fromProcResults(results)
													: InformationSetHelper.fromProcResults(results, new TreeSet<String>(outputFilter));
		xmlOutputs.copy(outputs);
	
		return Response.status(200).entity(xmlOutputs).build();
	}
	
	/**
	 * Merge specification with prespecification
	 */	
	private X13Specification merge(X13Specification specification, PreSpecificationEnum typePre) {

		if (typePre == null && specification == null) specification = new X13Specification();
		else if (typePre != null) {
			if (specification == null) specification = typePre.getX13Specification();
			else  {
				X13Specification preSpecification = typePre.getX13Specification();
				X13Specification reference = new X13Specification();
				
				InformationSet infoSpec = specification.write(true);
				InformationSet infoPre = preSpecification.write(true);
				InformationSet infoRef = reference.write(true);
			
				specification.read(UtilResource.merge(infoSpec, infoPre, infoRef));
			}
		}
		return specification;
	}
}
