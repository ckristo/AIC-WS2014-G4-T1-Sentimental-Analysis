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
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.*;
import weka.core.converters.ArffLoader;
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
	 * The initial capacity for the attributes vector.
	 */
	private static final int INIT_ATTRIBUTES_CAPACITY = 100;
	
	/**
	 * The index where the class attribute can be found in the attributes vector.
	 */
	public static final int CLASS_ATTRIBUTE_INDEX = 0;
	
	/**
	 * The classifier name.
	 */
	private final String classifierName;
	/**
	 * The classifier type.
	 */
	private Class<? extends Classifier> classifierType = null;
	/**
	 * Indicator whether to export the trained classifier to a file.
	 */
	private boolean exportTrainedClassifier = false;
	/**
	 * Indicator whether to import the trained classifier from a file.
	 */
	private boolean importTrainedClassifier = false;
	/**
	 * The export directory path.
	 */
	private File exportDirectory = null;
	/**
	 * The file name for exporting the classifier to file.
	 */
	private String classifierOutputFileName = null;
	/**
	 * The file name for exporting the attributes data to file.
	 */
	private String attributesOutputFileName = null;
	
	/**
	 * Tokenizer used for Tweet processing.
	 */
	private final ITokenizer tokenizer = new TokenizerImpl();
	/**
	 * Preprocessor used for Tweet processing.
	 */
	private final IPreprocessor preprocessor = new PreprocessorImpl();
	
	/**
	 * The processed training data used for training the Weka classifier.
	 */
	private Instances trainingData = null;
	/**
	 * The processed test data used for evaluating the Weka classifier.
	 */
	private Instances testData = null;
	
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
	 * - doesn't export / import a trained classifier
	 */
	public TwitterSentimentClassifierImpl() {
		classifierName = getClass().getSimpleName();
		classifierType = SMO.class;
	}
	
	/**
	 * Constructor.
	 * @param config the application configuration to use for setup the classifier.
	 */
	public TwitterSentimentClassifierImpl(ApplicationConfig config) {
		if (config.getClassifierName() == null) {
			classifierName = getClass().getName();
			logger.warn("Classifier name not specified in the configuration file -- setting to '"+getClass().getSimpleName()+"'");
		} else {
			classifierName = config.getClassifierName();
		}
		
		classifierType = config.getClassifierType();
		exportDirectory = new File(config.getClassifierOutputDirectory());
		
		exportTrainedClassifier = config.getExportTrainedClassifierToFile();
		attributesOutputFileName = classifierName + ".attributes";
		classifierOutputFileName = classifierName + "-" + classifierType.getSimpleName() + ".classifier";
		
		importTrainedClassifier = config.getImportTrainedClassifierToFile();
	}
	
	@Override
	public void train(Map<Status, Sentiment> trainingSet) throws ClassifierException {
		processTrainingSet(trainingSet);
		train();
	}
	
	@Override
	public boolean isTrained() {
		if (classifier == null) {
			if (importTrainedClassifier) {
				restoreTrainedClassifier();
			}
		}
		return (attributes != null && classifier != null);
	}
	
	/**
	 * Processes the training data and creates a training data set for the Weka classifier.
	 * @param trainingSet the input training data
	 */
	public void processTrainingSet(Map<Status, Sentiment> trainingSet) {
		// init attributes vector
		attributes = new FastVector(INIT_ATTRIBUTES_CAPACITY);

		// add class attribute
		Attribute classAttr = createClassAttribute();
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
			Attribute attr = new Attribute(w);
			attributes.addElement(attr);
		}
		
		// NOTE: do not alter attributes after the next step!
		
		trainingData = new Instances(classifierName, attributes, 100);
		trainingData.setClassIndex(CLASS_ATTRIBUTE_INDEX);
		
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
	}
	
	/**
	 * Processes the test data and creates a test data set for evaluating the Weka classifier.
	 * @param testSet the input test data
	 * @throws IllegalStateException if the classifier wasn't trained yet
	 */
	public void processTestSet(Map<Status, Sentiment> testSet) throws IllegalStateException{
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		
		testData = new Instances(classifierName, attributes, 100);
		testData.setClassIndex(CLASS_ATTRIBUTE_INDEX);
		
		double[] zeros = new double[testData.numAttributes()];
		
		logger.debug("## Preprocess all tweets of test set.");
		
		// process each tweet and create instances
		for(Map.Entry<Status, Sentiment> entry : testSet.entrySet()) {
			List<String> tWords = processTweet(entry.getKey());
			SparseInstance inst = new SparseInstance(testData.numAttributes());
			inst.setDataset(testData);
			
			// set each word that became an attribute during training in the instance vector to 1
			for (String w : tWords) {
				Attribute attr = testData.attribute(w);
				if (attr != null) {
					inst.setValue(attr, 1.0);
				}
			}
			
			// set all other values in the instance vector to 0
			inst.replaceMissingValues(zeros);
			
			// set class value
			inst.setClassValue(testSet.get(entry.getKey()).toString());
			
			testData.add(inst);
		}
	}
	
	/**
	 * Trains the classifier with the currently set training data.
	 * @throws ClassifierException
	 * @throws IllegalStateException if no processed training data is available
	 */
	public void train() throws ClassifierException, IllegalStateException {
		if (trainingData == null) {
			throw new IllegalStateException("Couldn't train classifier - no processed training data available");
		}
		
		logger.debug("## Train the classifier.");
		
		// instantiate classifier
		try {
			instantiateClassifier();
		} catch (InstantiationException | IllegalAccessException ex) {
			logger.error("Couldn't instantiate classifier of type '"+classifierType.getName()+"'", ex);
			throw new ClassifierException("Couldn't instantiate classifier", ex);
		}
		
		// build (train) the classifier
		try {
			classifier.buildClassifier(trainingData);
		} catch (Exception ex) {
			logger.error("Couldn't build classifier.", ex);
			throw new ClassifierException("Failed on building the classifier", ex);
		}
		
		// export trained classifier
		if (exportTrainedClassifier) {
			try {
				exportTrainedClassifier();
			} catch (IOException ex) {
				logger.warn("Couldn't export trained classifier to files.", ex);
			}
		}
	}
	
	/**
	 * Evaluates the classifier with a prev. processed test set and prints an evaluation summary.
	 * @throws ClassifierException if an exception occurred during evaluation
	 * @throws IllegalStateException if either the classifier wasn't trained or no processed test data is available
	 */
	public void evaluate() throws ClassifierException, IllegalStateException {
		if (!isTrained()) {
			throw new IllegalStateException("Cannot evaluate classifier -- classifier wasn't trained yet");
		}
		if (testData == null) {
			throw new IllegalStateException("Cannot evaluate classifier -- no processed test set available");
		}
		
		Evaluation eval = null;
		try {
			eval = new Evaluation(testData);
			eval.useNoPriors();
			eval.evaluateModel(classifier, testData);
		} catch (Exception ex) {
			throw new ClassifierException("Couldn't evaluate classifier -- exception thrown by Weka", ex);
		}
		
		String evalSummary = eval.toSummaryString();
		String evalSummaryLines[] = evalSummary.split("\\r?\\n");
		
		// print only summary of correctly/incorrectly classified instances
		System.out.println(evalSummaryLines[1]);
		System.out.println(evalSummaryLines[2]);
	}
	
	/**
	 * Evaluates the classifier using a given test set.
	 * @param testSet the test set to use for evaluation
	 * @throws ClassifierException if an exception occurred during evaluation
	 * @throws IllegalStateException if the classifier wasn't trained
	 */
	public void evaluate(Map<Status, Sentiment> testSet) throws ClassifierException, IllegalStateException {
		processTestSet(testSet);
		evaluate();
	}
	
	@Override
	public Double[] classify(Status tweet) throws IllegalStateException, ClassifierException {
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		
		Instances data = new Instances(classifierName, attributes, 100);
		
		SparseInstance inst = new SparseInstance(data.numAttributes());
		inst.setDataset(data);
		
		double[] zeros = new double[data.numAttributes()];
		
		// set attributes to 1
		List<String> words = processTweet(tweet);
		for (String w : words) {
			if (data.attribute(w) != null)
				inst.setValue(data.attribute(w), 1.0);
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
		// TODO: improve for multiple tweets
		Map<Status, Double[]> results = new HashMap<>();
		for (Status s : testSet) {
			results.put(s, classify(s));
		}
		return results;
	}
	
	/**
	 * Exports the processed training data to an ARFF file. 
	 * Caution: If the file already exists, it will get overwritten!
	 * @param outputFileName the output file name
	 * @throws IOException
	 */
	public void exportProcessedTrainingDataToArffFile(String outputFileName) throws IOException {
		if (trainingData == null) {
			throw new IllegalStateException("Couldn't export training data -- no processed training data available");
		}
		exportInstancesToArffFile(trainingData, new File(exportDirectory, outputFileName));
	}
	
	/**
	 * Exports the processed test data to an ARFF file. 
	 * Caution: If the file already exists, it will get overwritten!
	 * @param outputFileName the output file name
	 * @throws IOException
	 */
	public void exportProcessedTestDataToArffFile(String outputFileName) throws IOException {
		if (testData == null) {
			throw new IllegalStateException("Couldn't export test data -- no processed test data available");
		}
		exportInstancesToArffFile(testData, new File(exportDirectory, outputFileName));
	}
	
	/**
	 * Loads processed training data from an ARFF file.
	 * @param inputArffFile the ARFF file to load.
	 * @throws IOException
	 */
	public void loadProcessedTrainingDataFromArffFile(File inputArffFile) throws IOException {
		// load training data
		trainingData = loadInstancesFromArffFile(inputArffFile);
		trainingData.setClassIndex(CLASS_ATTRIBUTE_INDEX);
		
		// re-populate attribute vector
		attributes = new FastVector(INIT_ATTRIBUTES_CAPACITY);
		// * add class attribute
		Attribute classAttr = createClassAttribute();
		attributes.addElement(classAttr);
		// * add other attributes of training set
		for (Enumeration<Attribute> e = trainingData.enumerateAttributes(); e.hasMoreElements();) {
			Attribute attr = e.nextElement();
			attributes.addElement(attr);
		}
	}
	
	/**
	 * Loads processed test data from an ARFF file.
	 * @param inputArffFile the ARFF file to load.
	 * @throws IOException
	 */
	public void loadProcessedTestDataFromArffFile(File inputArffFile) throws IOException {
		testData = loadInstancesFromArffFile(inputArffFile);
		testData.setClassIndex(CLASS_ATTRIBUTE_INDEX);
	}
	
	/**
	 * Instantiates the classifier of the set type.
	 * @throws InstantiationException
	 * @throws IllegalAccessException 
	 */
	private void instantiateClassifier() throws InstantiationException, IllegalAccessException {
		if (classifierType == null) {
			throw new IllegalStateException("Classifier type not set");
		}
		classifier = classifierType.newInstance();
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
		return new Attribute("__class__", classValues);
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
	 * Exports the trained classifier to files.
	 * @throws IOException
	 */
	private void exportTrainedClassifier() throws IOException {
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		exportObject(attributes, attributesOutputFileName);
		exportObject(classifier, classifierOutputFileName);
	}
	
	/**
	 * Exports an object to a file.
	 * @param obj the object to export
	 * @param out the output file -- will be overwritten if it exists already!
	 * @throws IOException 
	 */
	private void exportObject(Object obj, String outputFileName) throws IOException {
		createExportDirectory();
		File out = new File(exportDirectory, outputFileName);
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(out))) {
			modelOutStream.writeObject(obj);
		}
	}
	
	/**
	 * Creates the export directory if it doesn't exist.
	 */
	private void createExportDirectory() throws IOException {
		// create export directory if not exists
		if (!exportDirectory.exists()) {
			if (!exportDirectory.mkdirs()) {
				throw new IOException("Couldn't create export directory '"+exportDirectory.getPath()+"'");
			}
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
	private void restoreTrainedClassifier() {
		File attributesOutputFile = getAttributesOutputFile();
		File classifierOutputFile = getClassifierOuptutFile();
		
		// try to load existing training data set
		if (attributesOutputFile.exists()) {
			try {
				attributes = readObject(attributesOutputFile, FastVector.class);
			} catch (IOException | ClassNotFoundException ex) {
				attributes = null;
				logger.warn("Couldn't read attributes from prev. trained classifier from file.", ex);
			}
		}
		// try to load existing trained classifier
		if (classifierOutputFile.exists()) {
			try {
				classifier = readObject(classifierOutputFile, Classifier.class);
			} catch (IOException | ClassNotFoundException ex) {
				attributes = null;
				classifier = null;
				logger.warn("Couldn't read prev. trained classifier from file.", ex);
			}
		}
	}
	
	/**
	 * Exports Weka instances data to an ARFF file.
	 * @param data the data to export
	 * @param out the file to export
	 * @throws IOException
	 */
	private void exportInstancesToArffFile(Instances data, File out) throws IOException {
		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setInstances(data);
		
		createExportDirectory();
		arffSaver.setFile(out);
		arffSaver.writeBatch();
	}
	
	/**
	 * Loads Weka instances from an ARFF file.
	 * @param in the input file
	 * @return the loaded Weka instances
	 * @throws IOException 
	 */
	private Instances loadInstancesFromArffFile(File in) throws IOException {
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(in);
		return arffLoader.getDataSet();
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
	 * Returns the directory used by the classifier to export files.
	 * @return the directory used by the classifier to export files.
	 */
	public File getExportDirectory() {
		return exportDirectory;
	}
	
	/**
	 * Sets the directory used by the classifier to export files.
	 * @param dir the new export directory
	 */
	public void setExportDirectory(File dir) {
		exportDirectory = dir;
	}
	
	/**
	 * Returns a file object of the output file for attributes data.
	 * @return a file object of the output file for attributes data.
	 */
	public File getAttributesOutputFile() {
		return new File(exportDirectory, attributesOutputFileName);
	}
	
	/**
	 * Returns the file name used to export the attributes data.
	 * @return the file name used to export the attributes data.
	 */
	public String getAttributesOutputFileName() {
		return attributesOutputFileName;
	}
	
	/**
	 * Sets the file name used to export the attributes data.
	 * @param fileName the new file name
	 */
	public void setAttributesOutputFileName(String fileName) {
		attributesOutputFileName = fileName;
	}
	
	/**
	 * Returns a file object of the output file for the trained classifier.
	 * @return a file object of the output file for the trained classifier.
	 */
	public File getClassifierOuptutFile() {
		return new File(exportDirectory, classifierOutputFileName);
	}
	
	/**
	 * Returns the file name used to export the trained classifier.
	 * @return the file name used to export the trained classifier.
	 */
	public String getClassifierOuptutFileName() {
		return classifierOutputFileName;
	}
	
	/**
	 * Sets the file name used to export the trained classifier.
	 * @param fileName the new file name
	 */
	public void setClassifierOutputFileName(String fileName) {
		classifierOutputFileName = fileName;
	}
	
	public Class<? extends Classifier> getClassifierType() {
		return classifierType;
	}
	
	public void setClassifierType(Class<? extends Classifier> type) {
		classifierType = type;
	}
}
