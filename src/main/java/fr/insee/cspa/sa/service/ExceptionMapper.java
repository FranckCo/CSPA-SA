package fr.insee.cspa.sa.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {
	
	private Logger logger = Logger.getRootLogger();
	
	public ExceptionMapper() {}
	
	public Response toResponse(Exception e) {
		logger.error("Error while converting request : "+e.getMessage());
		return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
	}
}
