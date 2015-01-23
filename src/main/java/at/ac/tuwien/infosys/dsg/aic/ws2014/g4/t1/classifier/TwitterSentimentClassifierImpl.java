package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 * Class implementing our custom Twitter sentiment detection classifier.
 */
public class TwitterSentimentClassifierImpl extends AbstractTwitterSentimentClassifier {

	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(TwitterSentimentClassifierImpl.class);

	/**
	 * The initial capacity for the attributes vector.
	 */
	private static final int INIT_ATTRIBUTES_CAPACITY = 100;

	/**
	 * The index where the class attribute can be found in the attributes
	 * vector.
	 */
	private static final int CLASS_ATTRIBUTE_INDEX = 0;

	/**
	 * defines the mapping between classifier model type and Weka class.
	 */
	private static final Class<? extends Classifier>[] CLASSIFIER_MODEL_MAPPING;

	static {
		CLASSIFIER_MODEL_MAPPING = new Class[ClassifierModel.values().length];
		CLASSIFIER_MODEL_MAPPING[ClassifierModel.SVM.ordinal()] = weka.classifiers.functions.SMO.class;
		CLASSIFIER_MODEL_MAPPING[ClassifierModel.Bayes.ordinal()] = weka.classifiers.bayes.NaiveBayes.class;
		CLASSIFIER_MODEL_MAPPING[ClassifierModel.kNN.ordinal()] = weka.classifiers.lazy.IBk.class;
	}

	/**
	 * The classifier name.
	 */
	private String classifierName;

	/**
	 * The classifier model to use.
	 */
	private ClassifierModel classifierModel;

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
	 * Creates a twitter sentiment classifier with defaults: - uses SMO as
	 * Machine Learning approach - doesn't export / import a trained classifier
	 */
	public TwitterSentimentClassifierImpl() {
		classifierName = getClass().getSimpleName();
		classifierModel = DEFAULT_CLASSIFIER_MODEL;
	}

	/**
	 * Constructor.
	 *
	 * @param config the application configuration to use for setup the
	 * classifier.
	 */
	public TwitterSentimentClassifierImpl(ApplicationConfig config) {
		String name = config.getClassifierName();
		ClassifierModel model = config.getClassifierModel();

		classifierName = (name != null) ? name : getClass().getSimpleName();
		classifierModel = (model != null) ? model : DEFAULT_CLASSIFIER_MODEL;
		exportDirectory = config.getClassifierExportDirectory();
		exportTrainedClassifier = config.getExportTrainedClassifierToFile();
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
	 * Processes the training data and creates a training data set for the Weka
	 * classifier.
	 *
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
		for (Map.Entry<Status, Sentiment> entry : trainingSet.entrySet()) {
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
		for (Map.Entry<Status, List<String>> entry : processedTweets.entrySet()) {
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
	 * Processes the test data and creates a test data set for evaluating the
	 * Weka classifier.
	 *
	 * @param testSet the input test data
	 * @throws IllegalStateException if the classifier wasn't trained yet
	 */
	public void processTestSet(Map<Status, Sentiment> testSet) throws IllegalStateException {
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}

		testData = new Instances(classifierName, attributes, 100);
		testData.setClassIndex(CLASS_ATTRIBUTE_INDEX);

		double[] zeros = new double[testData.numAttributes()];

		logger.debug("## Preprocess all tweets of test set.");

		// process each tweet and create instances
		for (Map.Entry<Status, Sentiment> entry : testSet.entrySet()) {
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
	 *
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
			logger.error("Couldn't instantiate classifier of type '" + classifierModel + "'", ex);
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
	 * Evaluates the classifier with a prev. processed test set and prints an
	 * evaluation summary.
	 *
	 * @throws ClassifierException if an exception occurred during evaluation
	 * @throws IllegalStateException if either the classifier wasn't trained or
	 * no processed test data is available
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
	 *
	 * @param testSet the test set to use for evaluation
	 * @throws ClassifierException if an exception occurred during evaluation
	 * @throws IllegalStateException if the classifier wasn't trained
	 */
	public void evaluate(Map<Status, Sentiment> testSet) throws ClassifierException, IllegalStateException {
		processTestSet(testSet);
		evaluate();
	}

	@Override
	public Sentiment classify(Status tweet) throws IllegalStateException, ClassifierException {
		double[] classDist = classifyWithProbabilities(tweet);
		return getSentiment(classDist);
	}

	@Override
	public Map<Status, Sentiment> classify(Collection<Status> tweets) throws IllegalStateException, ClassifierException {
		Map<Status, Sentiment> results = new LinkedHashMap<>();
		for (Status s : tweets) {
			results.put(s, classify(s));
		}
		return results;
	}

	@Override
	public double[] classifyWithProbabilities(Status tweet) throws ClassifierException {
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}

		Instances data = new Instances(classifierName, attributes, 100);
		data.setClassIndex(CLASS_ATTRIBUTE_INDEX);

		SparseInstance inst = new SparseInstance(data.numAttributes());
		inst.setDataset(data);

		double[] zeros = new double[data.numAttributes()];

		// set attributes to 1
		List<String> words = processTweet(tweet);
		for (String w : words) {
			if (data.attribute(w) != null) {
				inst.setValue(data.attribute(w), 1.0);
			}
		}
		// set all other values in the instance vector to 0
		inst.replaceMissingValues(zeros);

		try {
			// classify instance
			return classifier.distributionForInstance(inst);

		} catch (Exception ex) {
			logger.error("Couldn't classify instance.", ex);
			throw new ClassifierException("Couldn't classify instance", ex);
		}
	}

	@Override
	public Map<Status, double[]> classifyWithProbabilities(Collection<Status> tweets) throws ClassifierException {
		Map<Status, double[]> results = new LinkedHashMap<>();
		for (Status s : tweets) {
			results.put(s, classifyWithProbabilities(s));
		}
		return results;
	}

	@Override
	public void useClassifierModel(ClassifierModel model) {
		classifierModel = model;
	}

	@Override
	public ClassifierModel getClassifierModel() {
		return classifierModel;
	}

	/**
	 * Exports the processed training data to an ARFF file. Caution: If the file
	 * already exists, it will get overwritten!
	 *
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
	 * Exports the processed test data to an ARFF file. Caution: If the file
	 * already exists, it will get overwritten!
	 *
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
	 *
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
	 *
	 * @param inputArffFile the ARFF file to load.
	 * @throws IOException
	 */
	public void loadProcessedTestDataFromArffFile(File inputArffFile) throws IOException {
		testData = loadInstancesFromArffFile(inputArffFile);
		testData.setClassIndex(CLASS_ATTRIBUTE_INDEX);
	}

	/**
	 * Instantiates the classifier of the set type.
	 *
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void instantiateClassifier() throws InstantiationException, IllegalAccessException {
		if (classifierModel == null) {
			throw new IllegalStateException("Classifier type not set");
		}
		classifier = CLASSIFIER_MODEL_MAPPING[classifierModel.ordinal()].newInstance();
	}

	/**
	 * Creates an attribute with all classes.
	 *
	 * @return an attribute with all classes.
	 */
	private Attribute createClassAttribute() {
		FastVector classValues = new FastVector(Sentiment.values().length);
		for (Sentiment s : Sentiment.values()) {
			classValues.addElement(s.toString());
		}
		return new Attribute("__class__", classValues);
	}

	/**
	 * Returns the feature-relevant words of a Tweet.
	 *
	 * @param tweet the tweet to prepare.
	 * @return a list of feature-relevant words (= tokenized and preprocessed
	 * text of Tweet).
	 */
	private List<String> processTweet(Status tweet) {
		List<String> tokens = tokenizer.tokenize(tweet.getText());
		preprocessor.preprocess(tokens);
		return tokens;
	}

	/**
	 * Exports the trained classifier to files.
	 *
	 * @throws IOException
	 */
	private void exportTrainedClassifier() throws IOException {
		if (!isTrained()) {
			throw new IllegalStateException("classifier hasn't been trained yet");
		}
		exportObject(attributes, getAttributesOutputFile());
		exportObject(classifier, getClassifierOuptutFile());
	}

	/**
	 * Exports an object to a file.
	 *
	 * @param obj the object to export
	 * @param out the output file -- will be overwritten if it exists already!
	 * @throws IOException
	 */
	private void exportObject(Object obj, File outputFile) throws IOException {
		createExportDirectory();
		try (ObjectOutputStream modelOutStream = new ObjectOutputStream(new FileOutputStream(outputFile))) {
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
				throw new IOException("Couldn't create export directory '" + exportDirectory.getPath() + "'");
			}
		}
	}

	/**
	 * Reads an object from a file.
	 *
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
				logger.info("Restored attributes from prev. trained classifier file (" + attributesOutputFile.getPath() + ")");
			} catch (IOException | ClassNotFoundException ex) {
				attributes = null;
				logger.warn("Couldn't read attributes from prev. trained classifier file (" + attributesOutputFile.getPath() + ")", ex);
			}
		}
		// try to load existing trained classifier
		if (classifierOutputFile.exists()) {
			try {
				classifier = readObject(classifierOutputFile, Classifier.class);
				logger.info("Restored classifier from prev. trained classifier file (" + classifierOutputFile.getPath() + ")");
			} catch (IOException | ClassNotFoundException ex) {
				attributes = null;
				classifier = null;
				logger.warn("Couldn't read classifier from prev. trained classifier file (" + classifierOutputFile.getPath() + ")", ex);
			}
		}
	}

	/**
	 * Exports Weka instances data to an ARFF file.
	 *
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
	 *
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
	 * Returns the name of the classifier.
	 *
	 * @return the name of the classifier.
	 */
	public String getName() {
		return classifierName;
	}

	/**
	 * Sets the classifier name.
	 *
	 * @param classifierName the new classifier name.
	 */
	public void setName(String classifierName) {
		this.classifierName = classifierName;
	}

	/**
	 * Returns whether the classifier will export the classifier to files after
	 * training.
	 *
	 * @return true if the classifier will export the classifier after training,
	 * false otherwise.
	 */
	public boolean getExportTrainedClassifier() {
		return exportTrainedClassifier;
	}

	/**
	 * Sets the flag whether the classifier will export the classifier to files
	 * after training.
	 *
	 * @param val the new flag value
	 */
	public void setExportTrainedClassifier(boolean val) {
		exportTrainedClassifier = val;
	}

	/**
	 * Returns whether the classifier will import a previously trained
	 * classifier from files.
	 *
	 * @return true if the classifier will import a previously trained
	 * classifier, false otherwise.
	 */
	public boolean getImportTrainedClassifier() {
		return importTrainedClassifier;
	}

	/**
	 * Sets the flag whether the classifier will import a previously trained
	 * classifier from files.
	 *
	 * @param val the new flag value
	 */
	public void setImportTrainedClassifier(boolean val) {
		importTrainedClassifier = val;
	}

	/**
	 * Returns the directory used by the classifier to export files.
	 *
	 * @return the directory used by the classifier to export files.
	 */
	public File getExportDirectory() {
		return exportDirectory;
	}

	/**
	 * Sets the directory used by the classifier to export files.
	 *
	 * @param dir the new export directory
	 */
	public void setExportDirectory(File dir) {
		exportDirectory = dir;
	}

	/**
	 * Returns a file object of the output file for attributes data.
	 * ('[classifierName].attributes')
	 *
	 * @return a file object of the output file for attributes data.
	 */
	public File getAttributesOutputFile() {
		return new File(exportDirectory, classifierName + ".attributes");
	}

	/**
	 * Returns a file object of the output file for the trained classifier.
	 * ('[classifierName]-[classifierModel].classifier')
	 *
	 * @return a file object of the output file for the trained classifier.
	 */
	public File getClassifierOuptutFile() {
		return new File(exportDirectory, classifierName + "-" + classifierModel + ".classifier");
	}
}
