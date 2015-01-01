package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

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
	 * Adds the given training set (consisting of Statuses and corresponding
	 * Sentiments) to the classifier.
	 * @param trainingSet the labeled training data for the classifier
	 * @throws java.lang.IllegalStateException if the classifier was already
	 * built (i.e. {@link #train()} was called)
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

	void addSentiData(List<SentiData> trainingData) throws IllegalStateException;

	/**
	 * Trains the classifier using all previously supplied training data.
	 * @throws ClassifierException
	 */
	public void train() throws ClassifierException;

	/**
	 * Saves the training dataset to a file.
	 * @param path the file path to write the data to
	 * @throws IllegalStateException if the dataset does not exist
	 * @throws IOException if an error occured while writing
	 */
	public void save(String path) throws IllegalStateException, IOException;

	/**
	 * Loads a training dataset from a file.
	 * @param path the file path to load from
	 * @throws IOException if an error occured while reading
	 */
	void load(String path) throws IOException, ClassifierException;

	/**
	 * Determines the sentiment of a tweet.
	 * @param tweet the Twitter4J status object to classify.
	 * @return the sentiment class determined by the classifier.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public double classify(Status tweet) throws IllegalStateException, ClassifierException;
	
	/**
	 * Determines the sentiment of a given set of tweets.
	 * @param testSet the Twitter4J status objects to classify.
	 * @return a map with the determined sentiment classes for the tweets.
	 * @throws IllegalStateException if the classifier wasn't trained properly.
	 */
	public double classify(Set<Status> testSet) throws IllegalStateException, ClassifierException;
	
}
