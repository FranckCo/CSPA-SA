package fr.insee.cspa.sa.config;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
 
/**
 * Filtre CORS pour Jersey, voir http://www.codingpedia.org/ama/how-to-add-cors-support-on-the-server-side-in-java-with-jersey/
 * */
public class CorsFilter implements ContainerResponseFilter {
	
	static final Logger logger = Logger.getLogger(CorsFilter.class);
 
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
 
    	logger.info("Filtering response for Cors support.");
    	
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
 
        headers.add("Access-Control-Allow-Origin", "*");        
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");            
        headers.add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
    }
 
}
