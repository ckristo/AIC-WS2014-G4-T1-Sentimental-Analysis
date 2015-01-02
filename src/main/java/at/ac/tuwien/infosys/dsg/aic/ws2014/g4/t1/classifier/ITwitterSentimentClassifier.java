package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Collection;
import java.util.Map;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public void addTrainingData(Map<Status,Double> trainingSet) throws IllegalStateException;

	/**
	 * Adds the given training set (consisting of strings and corresponding
	 * sentiment values, where 0 is negative and 1 is positive) to the
	 * classifier.
	 * @param trainingSet the labeled training data for the classifier
	 * @throws java.lang.IllegalStateException if the classifier was already
	 * built (i.e. {@link #train()} was called)
	 */
	public void addTrainingWords(Map<String,Double> trainingSet) throws IllegalStateException;

	public void addSentiData(List<SentiData> trainingData) throws IllegalStateException;

	public void addSentiments(Map<Status, Sentiment> trainingSet) throws IllegalStateException;

	/**
	 * Trains the classifier using all previously supplied training data.
	 * @throws ClassifierException
	 */
	public void train() throws ClassifierException;

	/**
	 * Saves the training dataset to a file.
	 * @throws IllegalStateException if the dataset does not exist
	 * @throws IOException if an error occured while writing
	 */
	public void save() throws IllegalStateException, IOException;

	/**
	 * Loads a training dataset from a file.
	 * @throws IOException if an error occured while reading
	 */
	void load() throws IOException, ClassifierException;

	/**
	 * Determines the sentiment of a tweet.
	 * @param tweet the Twitter4J status object to classify.
	 * @return the sentiment class determined by the classifier (as double).
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws ClassifierException if the classifier couldn't classify the instance.
	 */
	public Double classify(Status tweet) throws IllegalStateException, ClassifierException;

	/**
	 * Determines the sentiment of a given set of tweets.
	 * @param testSet the Twitter4J status objects to classify.
	 * @return a map with the determined sentiment classes for the tweets.
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public Map<Status, Double> classify(Collection<Status> testSet) throws IllegalStateException, ClassifierException;

}
