package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ClassifierException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.ClassifierModel;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Constants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Twitter Sentiment service.
 */
public class TwitterSentimentService implements ITwitterSentimentService {

	private static final Logger logger = LogManager.getLogger(TwitterSentimentService.class);

	/**
	 * Date format used by the Twitter search API.
	 */
	private static final DateFormat TWITTER_SEARCH_API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * The singleton instance.
	 */
	private static TwitterSentimentService instance;

	/**
	 * The Twitter Sentiment classifier.
	 */
	private final TwitterSentimentClassifierImpl classifier;

	/**
	 * Constructor.
	 *
	 * Do not allow to instantiate objects -- singleton!
	 */
	private TwitterSentimentService() {
		// try to load application config
		TwitterSentimentClassifierImpl classifier;
		try {
			InputStream is = TwitterSentimentService.class.getResourceAsStream(Constants.DEFAULT_CONFIG_FILE_RESOURCE);
			ApplicationConfig config = new ApplicationConfig(is);

			classifier = new TwitterSentimentClassifierImpl(config);
		} catch (IOException ex) {
			logger.warn("Couldn't load application configuration -- instantiate classifier without config!", ex);

			classifier = new TwitterSentimentClassifierImpl();
		}

		this.classifier = classifier;
	}

	/**
	 * Returns the twitter sentiment service singleton instance.
	 *
	 * @return the twitter sentiment service instance.
	 */
	public static TwitterSentimentService getInstance() {
		if (instance == null) {
			instance = new TwitterSentimentService();
		}
		return instance;
	}

	// TODO: cache twitter search
	@Override
	public List<Status> searchForTweets(String username, Date start, Date end) throws TwitterSentimentServiceException {
		// create query string
		String queryStr = String.format("%s since:%s until:%s",
				username,
				TWITTER_SEARCH_API_DATE_FORMAT.format(start),
				TWITTER_SEARCH_API_DATE_FORMAT.format(end));

		// set up twitter instance
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query(queryStr);
		query.lang("en");

		QueryResult result = null;
		try {
			// obtain oauth2 bearer token
			twitter.getOAuth2Token();

			// execute query and print results
			result = twitter.search(query);
		} catch (TwitterException ex) {
			throw new TwitterSentimentServiceException("Couldn't call Twitter search API", ex);
		}

		return result.getTweets();
	}

	@Override
	public Map<Status, double[]> classifyTweetsWithProbabilities(List<Status> tweets, ClassifierModel model, TrainingConfig trainingConf) throws TwitterSentimentServiceException {
		File oldExportDir = classifier.getExportDirectory();

		classifier.useClassifierModel(model);
		classifier.setExportDirectory(new File(oldExportDir, trainingConf.toString()));

		Map<Status, double[]> classifiedTweets;
		try {
			classifiedTweets = classifier.classifyWithProbabilities(tweets);
		} catch (IllegalStateException | ClassifierException ex) {
			throw new TwitterSentimentServiceException("Couldn't classify tweets", ex);
		}

		classifier.setExportDirectory(oldExportDir);

		return classifiedTweets;
	}

	@Override
	public Sentiment[] getAllSentiments() {
		return Sentiment.values();
	}

	@Override
	public Sentiment getSentiment(double[] probabilities) {
		return classifier.getSentiment(probabilities);
	}

	@Override
	public double[] aggregateSentimentProbabilities(Collection<double[]> probabilitiesCollection) {
		Sentiment[] sentiments = getAllSentiments();

		// sum up probabilities first
		double[] avgProbabilities = new double[sentiments.length];
		for (double[] probabilities : probabilitiesCollection) {
			for (Sentiment s : sentiments) {
				avgProbabilities[s.ordinal()] += probabilities[s.ordinal()];
			}
		}

		// finally, calculate average
		if (!probabilitiesCollection.isEmpty()) {
			for (Sentiment s : sentiments) {
				avgProbabilities[s.ordinal()] = (double) (avgProbabilities[s.ordinal()] / probabilitiesCollection.size());
			}
		}

		return avgProbabilities;
	}

}
