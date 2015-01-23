package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.ClassifierModel;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.Sentiment;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import twitter4j.Status;

/**
 * Twitter Sentiment Service interface.
 */
public interface ITwitterSentimentService {

	/**
	 * Enumeration for available training configurations.
	 */
	enum TrainingConfig {
		/**
		 * Sentiment140 data 10k (5k positive, 5k negative)
		 */
		Sentiment140_10k,
		/**
		 * Sentiment140 data 20k (10k positive, 10k negative)
		 */
		Sentiment140_20k,
		/**
		 * Sentiment140 data 100k (50k positive, 50k negative)
		 */
		Sentiment140_100k,
		/**
		 * Sentiment140 data 200k (100k positive, 100k negative)
		 */
		Sentiment140_200k
	}

	/**
	 * Searches for tweets using Twitter search API.
	 *
	 * @param username the username
	 * @param start the start date
	 * @param end the end date
	 * @return a list of found tweets.
	 * @throws TwitterSentimentServiceException if an exception occurred when
	 * calling the Twitter search API.
	 */
	List<Status> searchForTweets(String username, Date start, Date end) throws TwitterSentimentServiceException;

	/**
	 * Detects the sentiment of a list of tweets
	 *
	 * @param tweets the list of tweets
	 * @param model the classifier model to use for classification
	 * @param trainingConf the training config to use for classification
	 * @return a map of tweets with the sentiment probabilities (index =
	 * Sentiment ordinal)
	 * @throws TwitterSentimentServiceException if an exception occurred when
	 * using the Twitter Sentiment classifier.
	 */
	Map<Status, double[]> classifyTweetsWithProbabilities(List<Status> tweets, ClassifierModel model, TrainingConfig trainingConf) throws TwitterSentimentServiceException;

	/**
	 * Returns an array of all sentiment classes.
	 *
	 * @return an array of all sentiment classes.
	 */
	Sentiment[] getAllSentiments();

	/**
	 * Returns the sentiment for a given probabilities array.
	 *
	 * @param probabilities the probabilities for each class (e.g. determined by
	 * classifyTweetsWithProbabilities) (Sentiment ordinal = array index)
	 * @return the sentiment for the given probabilities array -- determined as
	 * follows: 
	 *   - if positive value > 0.5 -> POSITIVE 
	 *   - if negative value > 0.5 -> NEGATIVE 
	 *   - else NEUTRAL
	 */
	Sentiment getSentiment(double[] probabilities);

	/**
	 * Aggregates the sentiment probabilities of a given collection of
	 * probabilities.
	 *
	 * @param probabilitiesCollection the collection of probabilities
	 * @return the aggregated sentiment probabilities
	 */
	double[] aggregateSentimentProbabilities(Collection<double[]> probabilitiesCollection);
}
