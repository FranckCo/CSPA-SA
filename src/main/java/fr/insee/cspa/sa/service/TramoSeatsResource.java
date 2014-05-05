package fr.insee.cspa.sa.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("ts")
public class TramoSeatsResource {

	@GET
	public Response goToPost() {
 
		String output = "Please use POST requests for Tramo-Seats service";
 
		return Response.status(200).entity(output).build();
 
	}
}
