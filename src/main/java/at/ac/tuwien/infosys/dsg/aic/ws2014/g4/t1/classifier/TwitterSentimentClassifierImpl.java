package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.*;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.IPreprocessor;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.ITokenizer;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.PreprocessorImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.TokenizerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.*;

/**
 * Class implementing our custom Twitter sentiment detection classifier.
 */
public class TwitterSentimentClassifierImpl implements ITwitterSentimentClassifier {
	
	/**
	 * The classifier name.
	 */
	private static final String CLASSIFIER_NAME = "SentimentClassificationProblem";
	
	/**
	 * The name of the output file for the trained classifier model.
	 */
	private static final String OUT_FILE_CLASSIFIER = "tmp/"+CLASSIFIER_NAME+".classifier";
	
	/**
	 * The name of the output file for the training set.
	 */
	private static final String OUT_FILE_TRAININGDATA = "tmp/"+CLASSIFIER_NAME+".trainingdata";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(TwitterSentimentClassifierImpl.class);
	
	/**
	 * Tokenizer used for Tweet processing.
	 */
	private final ITokenizer tokenizer = new TokenizerImpl();
	
	/**
	 * Preprocessor used for Tweet processing.
	 */
	private final IPreprocessor preprocessor = new PreprocessorImpl();
	
	/**
	 * The training data used to train the classifier.
	 */
	private Instances trainingData = null;
	
	/**
	 * The Weka classifier used for sentiment classification.
	 */
	private Classifier classifier = null;
	
	@Override
	public void train(Map<Status, Sentiment> trainingSet) throws ClassifierException {
		// create vector of attributes
		FastVector attributes = new FastVector(100);

		// add class attribute
		FastVector classValues = new FastVector();
		classValues.addElement(Sentiment.NEGATIVE.toString());
		classValues.addElement(Sentiment.NEUTRAL.toString());
		classValues.addElement(Sentiment.POSITIVE.toString());
		Attribute classAttr = new Attribute("@@class@@", classValues);
		attributes.addElement(classAttr);
		
		Map<Status, List<String>> processedTweets = new HashMap<>();
		Set<String> allWords = new HashSet<>();
		
		logger.debug("## Preprocess all tweets of training set.");
		
		// process tweets of training set
		for(Map.Entry<Status, Sentiment> entry : trainingSet.entrySet()) {
			List<String> tWords = processTweet(entry.getKey());
			processedTweets.put(entry.getKey(), tWords);
			allWords.addAll(tWords);
		}
		
		// create attributes for all occurring words
		for (String w : allWords) {
			attributes.addElement(new Attribute(w));
		}
		
		// NOTE: do not alter attributes after the next step!
		
		trainingData = new Instances(CLASSIFIER_NAME, attributes, 100);
		trainingData.setClass(classAttr);
		
		double[] zeros = new double[trainingData.numAttributes()];
		
		// create instances for the processed tweets and put them into the training data set
		for(Map.Entry<Status, List<String>> entry : processedTweets.entrySet()) {
			SparseInstance inst = new SparseInstance(trainingData.numAttributes());
			inst.setDataset(trainingData);
			
			// set each occurring word (= binary feature) in the instance vector to 1
			for (String w : entry.getValue()) {
				inst.setValue(trainingData.attribute(w), 1.0);
			}
			// set all other values in the instance vector to 0
			inst.replaceMissingValues(zeros);
			
			// set class value
			inst.setClassValue(trainingSet.get(entry.getKey()).toString());
			
			trainingData.add(inst);
		}
		
		logger.debug("## Train the classifier.");
		
		// train the classifier
		classifier = new SMO();
		try {
			classifier.buildClassifier(trainingData);
		} catch (Exception ex) {
			logger.error("Couldn't build classifier.", ex);
			throw new ClassifierException("Failed on building the classifier", ex);
		}
		
		// write used training set to file
		try {
			writeTrainingDataToFile();
		} catch (IOException ex) {
			logger.warn("Couldn't write training data to file.", ex);
			//TODO: check if we have to remove a corrupted file
		}
		// write trained classifier to file
		try {
			writeClassifierToFile();
		} catch (IOException ex) {
			logger.warn("Couldn't write trained classifier to file.", ex);
			//TODO: check if we have to remove a corrupted file
		}
	}
	
	@Override
	public Double[] classify(Status tweet) throws IllegalStateException, ClassifierException {
		tryRestoringPrevTrainedClassifier();
		
		if (classifier == null) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		
		// create vector of attributes
		FastVector attributes = new FastVector(100);
		
		// add class attribute
		FastVector classValues = new FastVector();
		classValues.addElement(Sentiment.NEGATIVE.toString());
		classValues.addElement(Sentiment.NEUTRAL.toString());
		classValues.addElement(Sentiment.POSITIVE.toString());
		Attribute classAttr = new Attribute("@@class@@", classValues);
		attributes.addElement(classAttr);
		
		// set all attributes to zero
		Enumeration<Attribute> enumeratedAttrs = trainingData.enumerateAttributes();
		while (enumeratedAttrs.hasMoreElements()) {
			Attribute attr = enumeratedAttrs.nextElement();
			attributes.addElement(attr);
		}
		
		Instances testData = new Instances("SentimentClassificationProblemTest", attributes, 100);
		testData.setClass(classAttr);
		
		SparseInstance inst = new SparseInstance(testData.numAttributes());
		inst.setDataset(testData);
		
		double[] zeros = new double[testData.numAttributes()];
		
		// set attributes to 1
		List<String> words = processTweet(tweet);
		for (String w : words) {
			if (testData.attribute(w) != null)
				inst.setValue(testData.attribute(w), 1.0);
		}
		// set all other values in the instance vector to 0
		inst.replaceMissingValues(zeros);
			
		double[] classifyValues;
		try {
			// classify instance
			classifyValues = classifier.distributionForInstance(inst);
		} catch (Exception ex) {
			logger.error("Couldn't classify instance.", ex);
			throw new ClassifierException("Couldn't classify instance", ex);
		}
		
        return ArrayUtils.toObject(classifyValues);
	}
	
	@Override
	public Map<Status, Double[]> classify(Collection<Status> testSet) throws IllegalStateException, ClassifierException {
		Map<Status, Double[]> results = new HashMap<>();
		for (Status s : testSet) {
			results.put(s, classify(s));
		}
		return results;
	}
	
	/**
	 * Returns the feature-relevant words of a Tweet.
	 * @param tweet the tweet to prepare.
	 * @return a list of feature-relevant words (= tokenized and preprocessed text of Tweet).
	 */
	private List<String> processTweet(Status tweet) {
		List<String> tokens = tokenizer.tokenize(tweet.getText());
		preprocessor.preprocess(tokens);
		return tokens;
	}
	
	/**
	 * Write trained classifier to file.
	 */
	private void writeClassifierToFile() throws IOException {
		// write trained classifier to file
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(OUT_FILE_CLASSIFIER))) {
			modelOutStream.writeObject(classifier);
		}
	}
	
	/**
	 * Write training set to file.
	 */
	private void writeTrainingDataToFile() throws IOException {
		// write training data object to file
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(OUT_FILE_TRAININGDATA))) {
			modelOutStream.writeObject(trainingData);
		}
	}
	
	/**
	 * Read trained classifier from file.
	 * @return the read classifier object.
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private Classifier readClassifierFromFile() throws IOException, ClassNotFoundException {
		try (ObjectInputStream modelInStream = new ObjectInputStream(new FileInputStream(OUT_FILE_CLASSIFIER))) {
			Classifier cl = (Classifier) modelInStream.readObject();
			return cl;
		}
	}
	
	/**
	 * Read test data from from file.
	 * @return the read test data (instances object).
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private Instances readTrainingDataFromFile() throws IOException, ClassNotFoundException {
		try (ObjectInputStream modelInStream = new ObjectInputStream(new FileInputStream(OUT_FILE_TRAININGDATA))) {
			Instances insts = (Instances) modelInStream.readObject();
			return insts;
		}
	}
	
	/**
	 * Tries to restore a prev. trained classifier.
	 */
	private void tryRestoringPrevTrainedClassifier() {
		// try to load existing training data set
		if (new File(OUT_FILE_CLASSIFIER).exists()) {
			try {
				trainingData = readTrainingDataFromFile();
			} catch (IOException | ClassNotFoundException ex) {
				logger.warn("Couldn't read training data from file.", ex);
			}
		}
		// try to load existing trained classifier
		if (new File(OUT_FILE_CLASSIFIER).exists()) {
			try {
				classifier = readClassifierFromFile();
			} catch (IOException | ClassNotFoundException ex) {
				logger.warn("Couldn't read prev. trained classifier from file.", ex);
			}
		}
	}
}
