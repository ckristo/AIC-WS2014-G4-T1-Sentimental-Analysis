package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/sentiment")
public class SentimentImpl {

    @GET
    @Produces("text/html")
    @Path("sayHi/{text}")
    public String sayHi(@PathParam("text") String text) {
        return "Hello " + text;
    }
}
