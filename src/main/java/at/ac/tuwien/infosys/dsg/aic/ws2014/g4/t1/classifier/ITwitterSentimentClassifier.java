package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Collection;
import java.util.Map;
import twitter4j.Status;

/**
 * Interface for our custom Twitter sentiment detection classifier.
 */
public interface ITwitterSentimentClassifier {
	
	/**
	 * Trains the classifier using a given training set
	 * @param trainingSet the labeled training data for the classifier
	 * @throws ClassifierException if the classifier couldn't be trained properly.
	 */
	public void train(Map<Status,Sentiment> trainingSet) throws ClassifierException;
	
	/**
	 * Checks if the classifier was previously trained or not.
	 * @return true if the classifier was previously trained, false otherwise.
	 */
	public boolean isTrained();
	
	/**
	 * Determines the sentiment of a tweet.
	 * @param tweet the Twitter4J status object to classify.
	 * @return the sentiment class determined by the classifier (as double).
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws ClassifierException if the classifier couldn't classify the instance.
	 */
	public Double[] classify(Status tweet) throws IllegalStateException, ClassifierException;
	
	/**
	 * Determines the sentiment of a given set of tweets.
	 * @param testSet the Twitter4J status objects to classify.
	 * @return a map with the determined sentiment classes for the tweets.
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public Map<Status, Double[]> classify(Collection<Status> testSet) throws IllegalStateException, ClassifierException;
	
}
