package fr.insee.cspa.sa.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("x13")
public class X13Resource {

	@GET
	public Response goToPost() {
 
		String output = "Please use POST requests for X13 service";
 
		return Response.status(200).entity(output).build();
 
	}
}
