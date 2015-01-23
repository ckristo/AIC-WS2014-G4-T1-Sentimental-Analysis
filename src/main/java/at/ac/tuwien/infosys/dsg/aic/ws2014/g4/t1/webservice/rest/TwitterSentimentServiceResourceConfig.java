package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.webservice.rest;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Resource config for the Twitter Sentiment RESTful Web Service.
 */
public class TwitterSentimentServiceResourceConfig extends ResourceConfig {

	public TwitterSentimentServiceResourceConfig() {
		register(TwitterSentimentServiceExceptionMapper.class);
		register(CORSResponseFilter.class);

		packages(TwitterSentimentServiceResourceConfig.class.getPackage().getName());
	}

}
