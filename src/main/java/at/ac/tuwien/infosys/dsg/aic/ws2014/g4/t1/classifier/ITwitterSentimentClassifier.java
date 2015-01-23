package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Collection;
import java.util.Map;
import twitter4j.Status;

/**
 * Interface for our custom Twitter sentiment detection classifier.
 */
public interface ITwitterSentimentClassifier {

	/**
	 * An enumeration for the sentiment classes.
	 */
	public enum Sentiment {
		NEGATIVE {
			@Override
			public String toString() {
				return "negative";
			}
		},
		NEUTRAL {
			@Override
			public String toString() {
				return "neutral";
			}
		},
		POSITIVE {
			@Override
			public String toString() {
				return "positive";
			}
		},
	}

	/**
	 * Enumeration of classifier models.
	 */
	public enum ClassifierModel {
		SVM {
			@Override
			public String toString() {
				return "SVM";
			}
		},
		Bayes {
			@Override
			public String toString() {
				return "Bayes";
			}
		},
		kNN {
			@Override
			public String toString() {
				return "kNN";
			}
		}
	}

	/**
	 * The default model to use.
	 */
	public ClassifierModel DEFAULT_CLASSIFIER_MODEL = ClassifierModel.SVM;

	/**
	 * Allows to determine which model should be used.
	 *
	 * @param model the model to use
	 */
	public void useClassifierModel(ClassifierModel model);

	/**
	 * Returns the model currently used.
	 *
	 * @return the model currently used.
	 */
	public ClassifierModel getClassifierModel();

	/**
	 * Trains the classifier using a given training set
	 *
	 * @param trainingSet the labeled training data for the classifier
	 * @throws ClassifierException if the classifier couldn't be trained
	 * properly.
	 */
	public void train(Map<Status, Sentiment> trainingSet) throws ClassifierException;

	/**
	 * Checks if the classifier was previously trained or not.
	 *
	 * @return true if the classifier was previously trained, false otherwise.
	 */
	public boolean isTrained();

	/**
	 * Determines the sentiment of a given tweet.
	 *
	 * @param tweet the Twitter4J status object to classify.
	 * @return the sentiment class determined by the classifier.
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws ClassifierException if the classifier couldn't classify the
	 * instance.
	 */
	public Sentiment classify(Status tweet) throws IllegalStateException, ClassifierException;

	/**
	 * Determines the probability for each sentiment of a given tweet.
	 *
	 * @param tweet the Twitter4J status object to classify.
	 * @return the probability for each class (Sentiment ordinal = array index).
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws ClassifierException if the classifier couldn't classify the
	 * instance.
	 */
	public double[] classifyWithProbabilities(Status tweet) throws IllegalStateException, ClassifierException;

	/**
	 * Determines the sentiment of a given collection of tweets.
	 *
	 * @param tweets the Twitter4J status objects to classify.
	 * @return a map with the determined sentiment classes for the tweets.
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws ClassifierException if the classifier couldn't classify the
	 * instance.
	 */
	public Map<Status, Sentiment> classify(Collection<Status> tweets) throws IllegalStateException, ClassifierException;

	/**
	 * Determines the probability for each sentiment of a given collection of
	 * tweets.
	 *
	 * @param tweets the Twitter4J status objects to classify.
	 * @return a map with the determined probabilities for each class (Sentiment
	 * ordinal = array index).
	 * @throws IllegalStateException if the classifier wasn't trained before.
	 * @throws ClassifierException if the classifier couldn't classify the
	 * instance.
	 */
	public Map<Status, double[]> classifyWithProbabilities(Collection<Status> tweets) throws IllegalStateException, ClassifierException;

	/**
	 * Returns the sentiment for a given probabilities array.
	 *
	 * @param probabilities the probabilities for each class (e.g. determined by
	 * classifyWithProbabilities) (Sentiment ordinal = array index)
	 * @return the sentiment for the given probabilities array -- determined as
	 * follows: 
	 *   - if positive value > 0.5 -> POSITIVE 
	 *   - if negative value > 0.5 -> NEGATIVE 
	 *   - else NEUTRAL
	 */
	public Sentiment getSentiment(double[] probabilities);

}
