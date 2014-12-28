package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Config;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Constants;
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
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(TwitterSentimentClassifierImpl.class);
	
	/**
	 * The classifier name.
	 */
	private final String CLASSIFIER_NAME;
	
	/**
	 * Indicator whether to export the trained classifier to a file.
	 */
	private final boolean EXPORT_TRAINED_CLASSIFIER;
	
	/**
	 * The export file path for the attributes data.
	 */
	private final String OUTPUT_FILEPATH_ATTRIBUTES;
	
	/**
	 * The export file path for the classifier data.
	 */
	private final String OUTPUT_FILEPATH_CLASSIFIER;
	
	/**
	 * Tokenizer used for Tweet processing.
	 */
	private final ITokenizer tokenizer = new TokenizerImpl();
	
	/**
	 * Preprocessor used for Tweet processing.
	 */
	private final IPreprocessor preprocessor = new PreprocessorImpl();
	
	/**
	 * A set with all word attributes used by the Weka classifier.
	 */
	private FastVector attributes;
	
	/**
	 * The Weka classifier used for sentiment classification.
	 */
	private Classifier classifier = null;
	
	/**
	 * Constructor.
	 */
	public TwitterSentimentClassifierImpl() {
		CLASSIFIER_NAME = Config.getProperty(Constants.CONFIG_KEY_CLASSIFIER_NAME, getClass().getName());
		EXPORT_TRAINED_CLASSIFIER = Config.getPropertyAsBoolean(Constants.CONFIG_KEY_CLASSIFIER_EXPORT_TRAINED_DATA_TO_FILE, false);
		if (EXPORT_TRAINED_CLASSIFIER) {
			String outputFilename = Config.getProperty(Constants.CONFIG_KEY_CLASSIFIER_OUTPUT_DIRECTORY, ".") 
					+ File.separator 
					+ CLASSIFIER_NAME;
			OUTPUT_FILEPATH_ATTRIBUTES = outputFilename + ".attributes";
			OUTPUT_FILEPATH_CLASSIFIER = outputFilename + ".classifier";
		} else {
			OUTPUT_FILEPATH_ATTRIBUTES = null;
			OUTPUT_FILEPATH_CLASSIFIER = null;
		}
	}
	
	@Override
	public void train(Map<Status, Sentiment> trainingSet) throws ClassifierException {
		// create vector of attributes
		FastVector wekaAttrs = new FastVector(100);

		// add class attribute
		Attribute classAttr = createClassAttribute();
		wekaAttrs.addElement(classAttr);
		
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
		attributes = new FastVector(100);
		for (String w : allWords) {
			Attribute attr = new Attribute(w);
			attributes.addElement(attr);
			wekaAttrs.addElement(attr);
		}
		
		// NOTE: do not alter attributes after the next step!
		
		Instances trainingData = new Instances(CLASSIFIER_NAME, wekaAttrs, 100);
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
		
		// export trained classifier
		if (EXPORT_TRAINED_CLASSIFIER) {
			try {
				writeAttributesDataToFile();
			} catch (IOException ex) {
				logger.warn("Couldn't write attributes to file.", ex);
			}
			try {
				writeClassifierToFile();
			} catch (IOException ex) {
				logger.warn("Couldn't write trained classifier to file.", ex);
			}
		}
	}
	
	@Override
	public Double[] classify(Status tweet) throws IllegalStateException, ClassifierException {
		tryRestoringTrainedClassifier();
		
		if (classifier == null) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		
		// create vector of attributes
		FastVector wekaAttrs = new FastVector(100);
		
		// add class attribute
		Attribute classAttr = createClassAttribute();
		wekaAttrs.addElement(classAttr);
		
		// set all attributes to zero
		wekaAttrs.appendElements(attributes);
		
		Instances testData = new Instances(CLASSIFIER_NAME, wekaAttrs, 100);
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
	 * Creates an attribute with all classes.
	 * @return an attribute with all classes.
	 */
	private Attribute createClassAttribute() {
		FastVector classValues = new FastVector();
		classValues.addElement(Sentiment.NEGATIVE.toString());
		classValues.addElement(Sentiment.NEUTRAL.toString());
		classValues.addElement(Sentiment.POSITIVE.toString());
		return new Attribute("@@class@@", classValues);
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
	 * Creates all necessary, non-existing directories for a given file path.
	 * @param path the file path
	 */
	private void createDirectory(String path) {
		File parent = new File(path).getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				logger.warn("Couldn't create parent directories for path '"+path+"'");
			}
		}
	}
	
	/**
	 * Write training set to file.
	 */
	private void writeAttributesDataToFile() throws IOException {
		createDirectory(OUTPUT_FILEPATH_ATTRIBUTES);
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILEPATH_ATTRIBUTES))) {
			modelOutStream.writeObject(attributes);
		}
	}
	
	/**
	 * Write trained classifier to file.
	 */
	private void writeClassifierToFile() throws IOException {
		createDirectory(OUTPUT_FILEPATH_CLASSIFIER);
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILEPATH_CLASSIFIER))) {
			modelOutStream.writeObject(classifier);
		}
	}
	
	/**
	 * Read test data from from file.
	 * @return the read test data (instances object).
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private FastVector readAttributesDataFromFile() throws IOException, ClassNotFoundException {
		try (ObjectInputStream modelInStream = new ObjectInputStream(new FileInputStream(OUTPUT_FILEPATH_ATTRIBUTES))) {
			return (FastVector) modelInStream.readObject();
		}
	}
	
	/**
	 * Read trained classifier from file.
	 * @return the read classifier object.
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private Classifier readClassifierFromFile() throws IOException, ClassNotFoundException {
		try (ObjectInputStream modelInStream = new ObjectInputStream(new FileInputStream(OUTPUT_FILEPATH_CLASSIFIER))) {
			return (Classifier) modelInStream.readObject();
		}
	}
	
	/**
	 * Tries to restore a previously trained classifier.
	 */
	private void tryRestoringTrainedClassifier() {
		// try to load existing training data set
		if (new File(OUTPUT_FILEPATH_ATTRIBUTES).exists()) {
			try {
				attributes = readAttributesDataFromFile();
			} catch (IOException | ClassNotFoundException ex) {
				logger.warn("Couldn't read attributes data from file.", ex);
			}
		}
		// try to load existing trained classifier
		if (new File(OUTPUT_FILEPATH_CLASSIFIER).exists()) {
			try {
				classifier = readClassifierFromFile();
			} catch (IOException | ClassNotFoundException ex) {
				logger.warn("Couldn't read prev. trained classifier from file.", ex);
			}
		}
	}
}
