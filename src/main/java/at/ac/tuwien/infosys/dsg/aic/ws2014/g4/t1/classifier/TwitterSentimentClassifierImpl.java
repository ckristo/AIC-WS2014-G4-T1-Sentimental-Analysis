package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Config;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.IPreprocessor;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.ITokenizer;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.PreprocessorImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.TokenizerImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.*;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
	 * The export file path for the training data.
	 */
	private final String OUTPUT_FILEPATH;

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
	private Classifier cls = null;

	/**
	 * The class of the classifier.
	 */
	private Class<? extends Classifier> clsType = IBk.class;

	/**
	 * Constructor.
	 */
	public TwitterSentimentClassifierImpl() {
		Config config = Config.getInstance();

		String classifierName = config.getClassifierName();
		if (classifierName == null) {
			String defaultName = getClass().getName();
			logger.warn("Classifier name not specified in the configuration file -- setting to '"+defaultName+"'");
			CLASSIFIER_NAME = defaultName;
		} else {
			CLASSIFIER_NAME = classifierName;
		}

		EXPORT_TRAINED_CLASSIFIER = config.getExportTrainedClassifierToFile();
		if (EXPORT_TRAINED_CLASSIFIER) {
			String outputFilename = config.getClassifierOutputDirectory()
					+ File.separator
					+ CLASSIFIER_NAME;
			OUTPUT_FILEPATH = outputFilename + ".arff";
		} else {
			OUTPUT_FILEPATH = null;
		}
	}

	private Attribute attrSentiment;
	private Instances ts;

	private List<SentiData> traindata = new ArrayList<>();

	public void addTrainingData(Map<Status, Double> trainingSet) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		for(Map.Entry<Status, Double> entry : trainingSet.entrySet()) {
			List<String> twtoks = processTweet(entry.getKey()), filtered = new ArrayList<>();
			for(String t : twtoks) {
				if(t != null && !t.equals("__++USERNAME++__") && !t.equals("H")) filtered.add(t.toLowerCase());
			}
			traindata.add(new SentiData(filtered, entry.getValue()));
		}
	}

	@Override
	public void addSentiData(List<SentiData> trainingData) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		traindata.addAll(trainingData);
		System.out.printf("added %d SentiData entries\n", trainingData.size());
	}

	@Override
	public void addSentiments(Map<Status, Sentiment> trainingData) throws IllegalStateException {
		Map<Status, Double> tset = new HashMap<>();
		for(Map.Entry<Status, Sentiment> e : trainingData.entrySet()) {
			tset.put(e.getKey(), e.getValue().toDouble());
		}
		addTrainingData(tset);
	}

	@Override
	public void train() throws ClassifierException {
		FastVector sentiments = new FastVector();
		for(Integer i = 0; i <= 10; i++) {
			sentiments.addElement("s"+i.toString());
			System.out.println("sentiment class " + i.toString());
		}
		attrSentiment = new Attribute("__sentiment__", sentiments);

		Set<String> tokens = new HashSet<>();
		for(SentiData sd : traindata) {
			tokens.addAll(sd.getTokens());
		}

		// build our own hash map with the attribute indices (WEKA performs a
		// linear search otherwise...)
		Map<String, Integer> attrmap = new HashMap<>();

		FastVector attrs = new FastVector(tokens.size()+1);
		attrs.addElement(attrSentiment);

		int i = 1;
		for(String t : tokens) {
			if(t == null) continue;
			Attribute attr = new Attribute(t);
			attrs.addElement(attr);
			attrmap.put(t, i);
			i++;
		}


		ts = new Instances("twitter-sentiments", attrs, traindata.size());
		ts.setClassIndex(0);

		System.out.printf("training using sparse instances with %d attributes\n", ts.numAttributes());

		double[] defaults = new double[ts.numAttributes()];
		Arrays.fill(defaults, 0.0);

		for(SentiData sd : traindata) {
			List<String> twtoks = sd.getTokens();

			SparseInstance inst = new SparseInstance(ts.numAttributes());
			inst.setDataset(ts);

			Integer snt = new Integer((int)Math.round(sd.getSentiment() * 10.0));
			inst.setValue(attrSentiment, "s"+snt.toString());
			inst.setDataset(ts);
			System.out.println("adding tokens "+ StringUtils.join(twtoks, ", ")+" with sentiment "+snt);

			// set contained tokens to 1
			for(String t : twtoks) {
				if(t == null) continue;
				inst.setValue(attrmap.get(t), 1.0);
			}
			// set all other tokens to 0
			inst.replaceMissingValues(defaults);

			//System.out.println("instance: "+inst);

			//inst.setWeight(10);	//TODO: custom weight, e.g. for manually classified training data
			ts.add(inst);
		}

		ts.setClassIndex(0);	// class is the sentiment

		System.out.println("building classifier...");

		try {
			cls = clsType.newInstance();
			cls.buildClassifier(ts);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}
	}

	@Override
	public void save() throws IllegalStateException, IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(ts);
		createDirectory(OUTPUT_FILEPATH);
		saver.setFile(new File(OUTPUT_FILEPATH));
		saver.writeBatch();
	}

	@Override
	public void load() throws IOException, ClassifierException {
		ArffLoader loader = new ArffLoader();
		File f = new File(OUTPUT_FILEPATH);
		if(!f.canRead()) {
			throw new IOException("can't load classifier from "+OUTPUT_FILEPATH+": file is not readable");
		}
		loader.setFile(f);
		ts = loader.getDataSet();
		ts.setClassIndex(0);

		attrSentiment = ts.attribute(0);

		try {
			cls = clsType.newInstance();
			cls.buildClassifier(ts);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}

		logger.info("training data from "+f.getPath()+" loaded successfully");
	}

	@Override
	public Double classify(Status tweet) throws IllegalStateException, ClassifierException {
		if(cls == null)
			throw new IllegalStateException("classifier has not been trained yet");


		List<String> twtoks = processTweet(tweet);

		Instances testset = new Instances(ts, 1);

		Instance inst = new SparseInstance(testset.numAttributes());
		inst.setDataset(testset);

		System.out.println("tweet: "+tweet.getText());
		System.out.print("classify: ");
		for(String t : twtoks) {
			if(t == null) continue;
			System.out.print(t.toLowerCase());
			Attribute attr = testset.attribute(t.toLowerCase());
			if(attr != null) {
				inst.setValue(attr, 1.0);
				System.out.print(" [exists]");
			}
			System.out.print(", ");
		}
		System.out.println("");
		double[] defaults = new double[inst.numAttributes()];
		Arrays.fill(defaults, 0.0);
		inst.replaceMissingValues(defaults);

		System.out.println("instance: "+inst);
		testset.add(inst);
		testset.setClassIndex(0);

		try {
			double sclass = cls.classifyInstance(inst);
			//System.out.println("class: "+testset.classAttribute().value((int)sclass));
			return sclass/(attrSentiment.numValues()-1);
		} catch (Exception e) {
			throw new ClassifierException(e);
		}
	}

	@Override
	public Map<Status, Double> classify(Collection<Status> testSet) throws IllegalStateException, ClassifierException {
		Map<Status, Double> results = new HashMap<>();
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
}
