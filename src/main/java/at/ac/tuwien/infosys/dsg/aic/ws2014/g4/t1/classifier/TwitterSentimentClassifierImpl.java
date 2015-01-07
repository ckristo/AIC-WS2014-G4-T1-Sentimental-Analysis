package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
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
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.core.*;
import weka.core.converters.ArffSaver;

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
	private final String classifierName;
	
	/**
	 * The classifier type.
	 */
	private final Class<?> classifierType;
	
	/**
	 * Indicator whether to export the trained classifier to a file.
	 */
	private boolean exportTrainedClassifier = false;
	
	/**
	 * Indicator whether to import the trained classifier from a file.
	 */
	private boolean importTrainedClassifier = false;
	
	/**
	 * Indicator whether to export the training data to an ARFF file.
	 */
	private boolean exportTrainingData = false;
	
	/**
	 * The export file path for the attributes data.
	 */
	private File attributesOutputFile = null;
	
	/**
	 * The export file path for the classifier data.
	 */
	private File classifierOutputFile = null;
	
	/**
	 * The export file path for the training data.
	 */
	private File trainingDataOutputFile = null;
	
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
	private FastVector attributes = null;
	
	/**
	 * The Weka classifier used for sentiment classification.
	 */
	private Classifier classifier = null;
	
	/**
	 * Creates a twitter sentiment classifier with defaults:
	 * - uses SMO as Machine Learning approach
	 * - doesn't export training data or the trained classifier
	 */
	public TwitterSentimentClassifierImpl() {
		classifierName = getClass().getName();
		classifierType = SMO.class;
	}
	
	/**
	 * Constructor.
	 * @param config the application configuration to use for setup the classifier.
	 */
	public TwitterSentimentClassifierImpl(ApplicationConfig config) {
		if (config.getClassifierName() == null) {
			classifierName = getClass().getName();
			logger.warn("Classifier name not specified in the configuration file -- setting to '"+getClass().getName()+"'");
		} else {
			classifierName = config.getClassifierName();
		}
		
		classifierType = config.getClassifierType();
		
		String exportPath = config.getClassifierOutputDirectory() + File.separator;
		
		exportTrainedClassifier = config.getExportTrainedClassifierToFile();
		if (exportTrainedClassifier) {
			String outputFilename = exportPath + classifierName + "-" + classifierType.getName();
			attributesOutputFile = new File(outputFilename + ".attributes");
			classifierOutputFile = new File(outputFilename + ".classifier");
		} else {
			attributesOutputFile = null;
			classifierOutputFile = null;
		}
		
		importTrainedClassifier = config.getImportTrainedClassifierToFile();
		
		exportTrainingData = config.getExportTrainingDataToArffFile();
		if (exportTrainingData) {
			trainingDataOutputFile = new File(exportPath + "trainingdata.arff");
		} else {
			trainingDataOutputFile = null;
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
		
		Instances trainingData = new Instances(classifierName, wekaAttrs, 100);
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
		
		// export training data
		if (exportTrainingData) {
			try {
				exportInstancesToArffFile(trainingData, trainingDataOutputFile);
			} catch (IOException ex) {
				logger.warn("Couldn't write attributes to file.", ex);
			}
		}
		
		// export trained classifier
		if (exportTrainedClassifier) {
			try {
				exportObject(attributes, attributesOutputFile);
				exportObject(classifier, classifierOutputFile);
			} catch (IOException ex) {
				logger.warn("Couldn't export attributes and trained classifier to files.", ex);
			}
		}
	}
	
	@Override
	public boolean isTrained() {
		if (classifier == null) {
			if (importTrainedClassifier) {
				tryRestoringTrainedClassifier();
			}
		}
		return (classifier != null);
	}
	
	@Override
	public Double[] classify(Status tweet) throws IllegalStateException, ClassifierException {
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		
		// create vector of attributes
		FastVector wekaAttrs = new FastVector(100);
		
		// add class attribute
		Attribute classAttr = createClassAttribute();
		wekaAttrs.addElement(classAttr);
		
		// set all attributes to zero
		wekaAttrs.appendElements(attributes);
		
		Instances testData = new Instances(classifierName, wekaAttrs, 100);
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
	 * Creates a directory path if it doesn't exist yet.
	 * @param path the directory path
	 * @throws IllegalArgumentException if the path doesn't point to a directory
	 * @throws IOException if the path doesn't exist but couldn't be created
	 */
	private void createDirectoryPath(File path) throws IOException {
		if (!path.exists()) {
			if (!path.mkdirs()) {
				throw new IOException("Couldn't create directory path '"+path.getPath()+"'");
			}
		}
	}
	
	/**
	 * Exports an instances object to an ARFF file.
	 * @param instances the instances to export
	 * @param out the output file -- will be overwritten if it exists already!
	 */
	private void exportInstancesToArffFile(Instances instances, File out) throws IOException {
		createDirectoryPath(out.getParentFile());
		
		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setInstances(instances);
		arffSaver.setFile(out);
		arffSaver.writeBatch();
	}
	
	/**
	 * Exports an object to a file.
	 * @param obj the object to export
	 * @param out the output file -- will be overwritten if it exists already!
	 * @throws IOException 
	 */
	private void exportObject(Object obj, File out) throws IOException {
		createDirectoryPath(out.getParentFile());
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(out))) {
			modelOutStream.writeObject(obj);
		}
	}
	
	/**
	 * Reads an object from a file.
	 * @param <T> the object's class
	 * @param in the file to read
	 * @param cls the class of the object to read
	 * @return the read object
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private <T> T readObject(File in, Class<T> cls) throws IOException, ClassNotFoundException {
		try (ObjectInputStream modelInStream = new ObjectInputStream(new FileInputStream(in))) {
			return (T) modelInStream.readObject();
		}
	}
	
	/**
	 * Tries to restore a previously trained classifier.
	 */
	private void tryRestoringTrainedClassifier() {
		// try to load existing training data set
		if (attributesOutputFile.exists()) {
			try {
				attributes = readObject(attributesOutputFile, FastVector.class);
			} catch (IOException | ClassNotFoundException ex) {
				logger.warn("Couldn't read attributes from prev. trained classifier from file.", ex);
			}
		}
		// try to load existing trained classifier
		if (classifierOutputFile.exists()) {
			try {
				classifier = readObject(classifierOutputFile, Classifier.class);
			} catch (IOException | ClassNotFoundException ex) {
				logger.warn("Couldn't read prev. trained classifier from file.", ex);
			}
		}
	}
	
	/**
	 * Returns whether the classifier will export the classifier to files after training.
	 * @return true if the classifier will export the classifier after training, false otherwise.
	 */
	public boolean getExportTrainedClassifier() {
		return exportTrainedClassifier;
	}
	
	/**
	 * Sets the flag whether the classifier will export the classifier to files after training.
	 * @param val the new flag value
	 */
	public void setExportTrainedClassifier(boolean val) {
		exportTrainedClassifier = val;
	}
	
	/**
	 * Returns whether the classifier will import a previously trained classifier from files.
	 * @return true if the classifier will import a previously trained classifier, false otherwise.
	 */
	public boolean getImportTrainedClassifier() {
		return exportTrainedClassifier;
	}
	
	/**
	 * Sets the flag whether the classifier will import a previously trained classifier from files.
	 * @param val the new flag value
	 */
	public void setImportTrainedClassifier(boolean val) {
		exportTrainedClassifier = val;
	}
	
	/**
	 * Returns whether the classifier will export the training data to an ARFF file before training.
	 * @return true if the classifier will export the training data before training, false otherwise.
	 */
	public boolean getExportTrainingData() {
		return exportTrainingData;
	}
	
	/**
	 * Sets the flag whether the classifier will export the training data to an ARFF file before training.
	 * @param val the new flag value
	 */
	public void setExportTrainingData(boolean val) {
		exportTrainingData = val;
	}
	
	/**
	 * Returns a file object of the output file for attributes data.
	 * @return a file object of the output file for attributes data.
	 */
	public File getAttributesOutputFile() {
		return attributesOutputFile;
	}
	
	/**
	 * Sets the output file for attributes data.
	 * @param out the new output file
	 */
	public void setAttributesOutputFile(File out) {
		attributesOutputFile = out;
	}
	
	/**
	 * Returns a file object of the output file for the trained classifier.
	 * @return a file object of the output file for the trained classifier.
	 */
	public File getClassifierOuptutFile() {
		return classifierOutputFile;
	}
	
	/**
	 * Sets the output file for the trained classifier.
	 * @param out the new output file
	 */
	public void setClassifierOuptutFile(File out) {
		classifierOutputFile = out;
	}
	
	/**
	 * Returns a file object of the output file for attributes.
	 * @return a file object of the output file for attributes.
	 */
	public File getTrainingDataOutputFile() {
		return trainingDataOutputFile;
	}
	
	/**
	 * Sets the output file for the training data AIFF file.
	 * @param out the new output file
	 */
	public void setTrainingDataOutputFile(File out) {
		trainingDataOutputFile = out;
	}
	
}
