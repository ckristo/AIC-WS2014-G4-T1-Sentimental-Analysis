package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.*;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.IPreprocessor;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.ITokenizer;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.PreprocessorImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.TokenizerImpl;
import org.apache.commons.lang.StringUtils;
import twitter4j.Status;

import weka.classifiers.Classifier;
import weka.core.*;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;

/**
 * Class implementing our custom Twitter sentiment detection classifier.
 */
public class TwitterSentimentClassifierImpl implements ITwitterSentimentClassifier {
	
	// general TODOs:
	// * tokenize tweet
	// * run list of words through preprocessor
	// * create feature vector for machine learning library based on the list of words
	// * ( add other features )
	// ...

	private Classifier cls = null;
	private Attribute attrSentiment;
	private Instances ts;

	private Set<String> tokens = new HashSet<>();
	private List<SentiData> traindata = new ArrayList<>();

	private class SentiData {
		private List<String> tokens;
		private Sentiment sentiment;

		public SentiData(List<String> toks, Sentiment s) {
			this.tokens = toks;
			this.sentiment = s;
		}

		public Sentiment getSentiment() {
			return sentiment;
		}

		public List<String> getTokens() {
			return tokens;
		}
	}

	public void addTrainingData(Map<Status, Sentiment> trainingSet) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		ITokenizer tokenizer = new TokenizerImpl();
		IPreprocessor preproc = new PreprocessorImpl();

		for(Map.Entry<Status, Sentiment> entry : trainingSet.entrySet()) {
			String text = entry.getKey().getText();
			List<String> twtoks = tokenizer.tokenize(text);

			preproc.preprocess(twtoks);

			tokens.addAll(twtoks);
			traindata.add(new SentiData(twtoks, entry.getValue()));
		}
	}

	public void addTrainingWords(Map<String, Double> trainingSet) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		//TODO
		throw new IllegalStateException("not yet implemented");
	}

	@Override
	public void train() throws ClassifierException {
		FastVector sentiments = new FastVector();
		sentiments.addElement(Sentiment.NEGATIVE.toString());
		sentiments.addElement(Sentiment.NEUTRAL.toString());
		sentiments.addElement(Sentiment.POSITIVE.toString());
		attrSentiment = new Attribute("__sentiment__", sentiments);

		FastVector attrs = new FastVector();
		attrs.addElement(attrSentiment);

		for(String t : tokens) {
			if(t == null) continue;
			Attribute attr = new Attribute(t);
			attrs.addElement(attr);
		}

		ts = new Instances("twitter-sentiments", attrs, traindata.size());

		for(SentiData sd : traindata) {
			List<String> twtoks = sd.getTokens();

			SparseInstance inst = new SparseInstance(ts.numAttributes());
			inst.setValue(attrSentiment, sd.getSentiment().toString());

			// set contained tokens to 1
			for(String t : twtoks) {
				if(t == null) continue;
				inst.setValue(ts.attribute(t), 1.0);
			}
			// set all other tokens to 0
			double[] defaults = new double[inst.numAttributes()];
			Arrays.fill(defaults, 0.0);
			inst.replaceMissingValues(defaults);

			//inst.setWeight(10);	//TODO: custom weight, e.g. for manually classified training data
			ts.add(inst);
		}

		ts.setClassIndex(0);	// class is the sentiment

		int split = (int)(ts.numInstances()*0.7);
		Instances train = new Instances(ts, 0, split);
		train.setClassIndex(0);
		Instances test = new Instances(ts, split+1, ts.numInstances()-split-1);
		test.setClassIndex(0);

		cls = new LibSVM();
		try {
			cls.buildClassifier(train);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}

		Evaluation eval = null;
		try {
			eval = new Evaluation(train);
			eval.evaluateModel(cls, test);
		} catch (Exception e) {
			throw new ClassifierException(e);
		}

		System.out.println(eval.toSummaryString());
	}

	@Override
	public Sentiment classify(Status tweet) throws IllegalStateException, ClassifierException {
		if(cls == null) throw new IllegalStateException("classifier has not been trained");
		Instance inst = new Instance(2);
		inst.setDataset(ts);
		//inst.setValue(attrText, tweet.getText());
		System.out.println("instance: " + inst);

		//TODO: apply filter?
		double sclass = 0;
		try {
			sclass = cls.classifyInstance(inst);
		} catch (Exception e) {
			throw new ClassifierException(e);
		}
		System.out.println("rating: "+sclass);
		System.out.println(ts.attribute(ts.classIndex()).value((int) inst.classValue()));
		//System.out.println(ts.distributionForInstance(ts.firstInstance()));
		return Sentiment.NEUTRAL;
	}

	@Override
	public Map<Status, Sentiment> classify(Set<Status> testSet) throws IllegalStateException {
		// TODO
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
