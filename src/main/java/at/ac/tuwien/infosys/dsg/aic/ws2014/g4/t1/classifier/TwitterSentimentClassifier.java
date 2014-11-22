package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Map;
import java.util.Set;
import twitter4j.Status;

/**
 * Interface for our custom Twitter sentiment detection classifier.
 */
public interface TwitterSentimentClassifier {
	
	/**
	 * Trains the classifier using a given training set
	 * @param trainingSet the labeled training data for the classifier
	 */
	public void train(Map<Status,Sentiment> trainingSet) throws ClassifierException;
	
	/**
	 * Determines the sentiment of a tweet.
	 * @param tweet the Twitter4J status object to classify.
	 * @return the sentiment class determined by the classifier.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public Sentiment classify(Status tweet) throws IllegalStateException;
	
	/**
	 * Determines the sentiment of a given set of tweets.
	 * @param testSet the Twitter4J status objects to classify.
	 * @return a map with the determined sentiment classes for the tweets.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public Map<Status,Sentiment> classify(Set<Status> testSet) throws IllegalStateException;
	
}
