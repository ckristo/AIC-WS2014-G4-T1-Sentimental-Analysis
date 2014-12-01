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
import weka.filters.unsupervised.attribute.StringToWordVector;

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
	Attribute attrSentiment, attrText;
	private Evaluation eval;
	Instances ts;

	@Override
	public void train(Map<Status, Sentiment> trainingSet) throws ClassifierException {
		FastVector sentiments = new FastVector();
		sentiments.addElement(Sentiment.NEUTRAL.toString());
		sentiments.addElement(Sentiment.POSITIVE.toString());
		sentiments.addElement(Sentiment.NEGATIVE.toString());
		attrSentiment = new Attribute("__sentiment__", sentiments);

		FastVector attrs = new FastVector();
		attrs.addElement(attrSentiment);

		Set<String> tokens = new HashSet<String>();
		Map<Status, List<String>> twtokens = new HashMap<>();

		for(Map.Entry<Status, Sentiment> entry : trainingSet.entrySet()) {
			String text = entry.getKey().getText();
			ITokenizer tok = new TokenizerImpl();
			List<String> twtoks = tok.tokenize(text);

			IPreprocessor preproc = new PreprocessorImpl();
			preproc.preprocess(twtoks);

			tokens.addAll(twtoks);
			twtokens.put(entry.getKey(), twtoks);
		}

		for(String t : tokens) {
			Attribute ta = new Attribute(t);
			attrs.addElement(ta);
		}

		ts = new Instances("twitter-sentiments", attrs, trainingSet.size());

		for(Map.Entry<Status, Sentiment> entry : trainingSet.entrySet()) {
			List<String> twtoks = twtokens.get(entry.getKey());
			System.out.println("tokens: " + StringUtils.join(twtoks, ','));

			SparseInstance inst = new SparseInstance(ts.numAttributes());
			inst.setValue(attrSentiment, entry.getValue().toString() /*attrSentiment.indexOfValue(entry.getValue().toString())*/);

			// set contained tokens to 1
			for(String t : twtoks) {
				inst.setValue(ts.attribute(t), 1.0);
			}
			// set all other tokens to 0
			double[] defaults = new double[inst.numAttributes()];
			Arrays.fill(defaults, 0.0);
			inst.replaceMissingValues(defaults);

			ts.add(inst);
			System.out.println("sentiment: "+entry.getValue().toString()+", instance: " + inst+", msg: "+entry.getKey().getText());
		}

		System.out.println("\n- ----------------------- AFTER FILTERING --------------------");

		for(Object instobj : Collections.list(ts.enumerateInstances())) {
			Instance inst = (Instance)instobj;
			System.out.println("instance: " + inst);
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

		//System.out.println("classifier: "+ts);

		Evaluation eval = null;
		try {
			eval = new Evaluation(train);
			eval.evaluateModel(cls, test);
		} catch (Exception e) {
			throw new ClassifierException(e);
		}

		System.out.println(eval.toSummaryString());

		try {
			Instance ti = ts.instance(1);
			System.out.println("instance: "+ti);
			System.out.println(cls.classifyInstance(ti));
			System.out.println(ts.attribute(ts.classIndex()).value((int) ts.firstInstance().classValue()));
			System.out.println(cls.distributionForInstance(ts.firstInstance()));
		} catch (Exception e) {
			throw new ClassifierException(e);
		}
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
