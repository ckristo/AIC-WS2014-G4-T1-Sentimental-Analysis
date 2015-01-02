package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.rest;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ClassifierException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Customers;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.SentimentAnalyzer;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path("/sentiment")
public class SentimentImpl {

	@POST
	@Path("{name}")
	public Response register(@PathParam("name") String name) {
		boolean newreg = Customers.register(name);
		return Response.status(newreg ? Response.Status.CREATED : Response.Status.OK).build();
	}

	@GET
	@Path("{name}/query")
	public Response query(@PathParam("name") String name, @QueryParam("from") Long from, @QueryParam("to") Long to) {
		if(!Customers.exists(name)) {
			return Response.status(Response.Status.NOT_FOUND).entity("customer "+name+" is not registered").build();
		}
		try {
			double sentiment = SentimentAnalyzer.analyze(name, from == null ? null : new Date(from*1000), to == null ? null : new Date(to*1000));
			return Response.status(Response.Status.OK).entity(Double.toString(sentiment)).build();
		} catch (ClassifierException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
}
