package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Map;
import java.util.Set;
import twitter4j.Status;

/**
 * Interface for our custom Twitter sentiment detection classifier.
 */
public interface ITwitterSentimentClassifier {
	
	/**
	 * Adds the given training set (consisting of Statuses and corresponding
	 * Sentiments) to the classifier.
	 * @param trainingSet the labeled training data for the classifier
	 * @throws java.lang.IllegalStateException if the classifier was already
	 * built (i.e. {@link #train()} was called)
	 */
	public void addTrainingData(Map<Status,Sentiment> trainingSet) throws IllegalStateException;

	/**
	 * Adds the given training set (consisting of strings and corresponding
	 * sentiment values, where 0 is negative and 1 is positive) to the
	 * classifier.
	 * @param trainingSet the labeled training data for the classifier
	 * @throws java.lang.IllegalStateException if the classifier was already
	 * built (i.e. {@link #train()} was called)
	 */
	public void addTrainingWords(Map<String,Double> trainingSet) throws IllegalStateException;

	/**
	 * Trains the classifier using all previously supplied training data.
	 * @throws ClassifierException
	 */
	public void train() throws ClassifierException;
	
	/**
	 * Determines the sentiment of a tweet.
	 * @param tweet the Twitter4J status object to classify.
	 * @return the sentiment class determined by the classifier.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public Sentiment classify(Status tweet) throws IllegalStateException, ClassifierException;
	
	/**
	 * Determines the sentiment of a given set of tweets.
	 * @param testSet the Twitter4J status objects to classify.
	 * @return a map with the determined sentiment classes for the tweets.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public Map<Status,Sentiment> classify(Set<Status> testSet) throws IllegalStateException;
	
}
