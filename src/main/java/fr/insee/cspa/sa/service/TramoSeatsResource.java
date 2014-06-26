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

import org.apache.log4j.Logger;

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
	
	private Logger logger = Logger.getRootLogger();
	
	/**
	 * Information about services
	 */
	@GET
	public Response goToGet() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GET /type => Detail of predefined Specification [type] where type = RSA0 ... RSA5<br/>");
		buffer.append("POST => Tramo-Seats service analysis");
		return Response.status(Response.Status.OK).entity(buffer.toString()).build();
	}
	
	/**
	 * Get predefined specifications
	 */
	@GET
	@Path("/{type}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getTSSpecification(final @PathParam("type") String type) {
		
		try {
			PreSpecificationEnum prespec = PreSpecificationEnum.valueOf(type.toUpperCase());
		
			XmlTramoSeatsSpecification xmlSpecification = new XmlTramoSeatsSpecification();
			xmlSpecification.copy(prespec.getTramoSeatsSpecification());
		
			logger.info("Send tramoseats pre-specification "+type);
			return Response.status(Response.Status.OK).entity(xmlSpecification).build();
		}
		catch (IllegalArgumentException e) {
			logger.info("Unknown tramoseats pre-specification "+type);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}		
	}
	
	/** 
	 * Tramoseats analysis 
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response runTramoSeatsAnalysis(TSRequest request) throws Exception {
	
		// Get request elements
		TsData series = request.getSeries().create();
		PreSpecificationEnum typePre = request.getPreSpecification();
		TramoSeatsSpecification specification = request.getSpecification().create();
		List<String> outputFilter = request.getOutputFilter();
		//XmlTsVariable[] xmlVariables = request.getUserRegressors();

		// Merge preSpecification and specification	
		specification = merge(specification, typePre); 
		
		// Analysis
		CompositeResults results;
		try {
			results = TramoSeatsProcessingFactory.process(series, specification);
		}
		catch(Exception e) {
			logger.error("Tramoseats analysis error : "+e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
			
		// Get outputs	
		XmlInformationSet xmlOutputs = new XmlInformationSet();
		InformationSet outputs = (outputFilter == null) ? InformationSetHelper.fromProcResults(results) 
													: InformationSetHelper.fromProcResults(results, new TreeSet<String>(outputFilter));
		xmlOutputs.copy(outputs);
	
		logger.info("Tramoseats analysis done");
		return Response.status(Response.Status.OK).entity(xmlOutputs).build();
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
			
				InformationSet infoMerge = UtilResource.merge(infoSpec, infoPre, infoRef);
				
				specification.read(infoMerge);
			}
		}
		return specification;
	}
}
