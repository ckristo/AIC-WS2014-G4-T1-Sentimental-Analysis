package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.webservice.rest;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.ClassifierModel;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.IRegistrationService;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.ITwitterSentimentService;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.ITwitterSentimentService.TrainingConfig;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.RegistrationException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.RegistrationService;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.TwitterSentimentService;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.TwitterSentimentServiceException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.UserAlreadyRegisteredException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service.UserSession;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;

/**
 * Resource for the Twitter Sentiment RESTful Web Service.
 */
@Singleton
@Path("/twitter_sentiment_service")
public class TwitterSentimentServiceResource {

	private static final Logger logger = LogManager.getLogger(TwitterSentimentServiceResource.class);

	private static final DateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private final IRegistrationService registrationService = RegistrationService.getInstance();

	private final ITwitterSentimentService twitterSentimentService = TwitterSentimentService.getInstance();

	@POST
	@Path("register")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject register(String username) {
		if (username == null) {
			throw new BadRequestException("Parameter 'username' is missing");
		}

		UserSession session = null;
		try {
			session = registrationService.register(username);
		} catch (UserAlreadyRegisteredException ex1) {
			throw new BadRequestException("There's already an open session for user '" + username + "'");
		} catch (RegistrationException ex2) {
			logger.error("Couldn't perform registration for user '" + username, ex2);
			throw new InternalServerErrorException("Couldn't perform registration");
		}

		return Json.createObjectBuilder()
				.add("username", username)
				.add("token", session.getSessionToken())
				.add("expires", session.getExpirationDate().getTime())
				.build();
	}

	@GET
	@Path("query")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject query(@QueryParam("token") String token,
			@QueryParam("from") String from,
			@QueryParam("to") String to,
			@QueryParam("classifierModel") @DefaultValue("SVM") ClassifierModel classifierModel,
			@QueryParam("trainingConfig") @DefaultValue("Sentiment140_100k") TrainingConfig trainingConf) {
		if (token == null) {
			throw new BadRequestException("Parameter 'token' is missing");
		} else if (from == null) {
			throw new BadRequestException("Parameter 'from' is missing");
		} else if (to == null) {
			throw new BadRequestException("Parameter 'to' is missing");
		}

		// convert date parameters to Date objects
		Date dateFrom, dateTo;
		try {
			dateFrom = dayDateFormat.parse(from);
		} catch (ParseException ex) {
			throw new BadRequestException("Parameter 'from' must be a date string in the format <year>-<month>-<day>");
		}
		try {
			dateTo = dayDateFormat.parse(to);
		} catch (ParseException ex) {
			throw new BadRequestException("Parameter 'to' must be a date string in the format <year>-<month>-<day>");
		}

		// get user session
		UserSession session = registrationService.getActiveSessionBySessionToken(token);
		if (session == null) {
			throw new NotAuthorizedException("Invalid user token -- please register first");
		}

		// search for tweets
		List<Status> tweets = null;
		try {
			tweets = twitterSentimentService.searchForTweets(session.getUser().getName(), dateFrom, dateTo);
		} catch (TwitterSentimentServiceException ex) {
			logger.error("Failed to call twitter search api", ex);
			throw new InternalServerErrorException("Couldn't call Twitter Search API");
		}

		// determine sentiment for all found tweets
		Map<Status, double[]> tweetSentiments;
		try {
			tweetSentiments = twitterSentimentService.classifyTweetsWithProbabilities(tweets, classifierModel, trainingConf);
		} catch (TwitterSentimentServiceException ex) {
			logger.error("Failed to classify tweets", ex);
			throw new InternalServerErrorException("Couldn't classify Tweets");
		}

		// aggregate sentiment probabilities
		double[] avgSentimentProbabilities = twitterSentimentService.aggregateSentimentProbabilities(tweetSentiments.values());

		// -- create JSON return value -----------------------------------------
		
		JsonObjectBuilder jsonAvgSentiment = Json.createObjectBuilder();
		for (Sentiment s : twitterSentimentService.getAllSentiments()) {
			jsonAvgSentiment.add(s.toString(), avgSentimentProbabilities[s.ordinal()]);
		}

		JsonArrayBuilder jsonTweetArray = Json.createArrayBuilder();
		for (Map.Entry<Status, double[]> entry : tweetSentiments.entrySet()) {
			Status tweet = entry.getKey();
			double[] sentimentProbabilities = entry.getValue();

			JsonObjectBuilder jsonObj = Json.createObjectBuilder()
					.add("id", tweet.getId())
					.add("created_at", tweet.getCreatedAt().getTime())
					.add("text", tweet.getText())
					.add("retweet_count", tweet.getRetweetCount())
					.add("favorite_count", tweet.getFavoriteCount())
					.add("sentiment", twitterSentimentService.getSentiment(sentimentProbabilities).toString());

			// TODO: use same structure / form as Twitter Search API (e.g. created_at is a timestamp)
			// TODO: add more / all information of a tweet
			
			jsonTweetArray.add(jsonObj.build());
		}

		return Json.createObjectBuilder()
				.add("aggregated_sentiment", jsonAvgSentiment.build())
				.add("tweets", jsonTweetArray.build())
				.build();
	}
}
