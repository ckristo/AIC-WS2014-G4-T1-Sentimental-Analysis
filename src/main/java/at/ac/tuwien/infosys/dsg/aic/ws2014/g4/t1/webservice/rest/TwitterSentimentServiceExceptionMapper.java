package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.webservice.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Twitter Sentiment service exception mapper.
 */
@Provider
public class TwitterSentimentServiceExceptionMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(WebApplicationException exception) {
		return Response.status(exception.getResponse().getStatus()).entity(exception.getMessage()).build();
	}
}
