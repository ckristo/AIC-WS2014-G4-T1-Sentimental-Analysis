package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Map;
import java.util.Set;

import twitter4j.Status;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
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

	private Classifier cls = new LibSVM();
	private Evaluation eval;

	@Override
	public void train(Map<Status, Sentiment> trainingSet) throws ClassifierException {
		FastVector sentiments = new FastVector();
		sentiments.addElement(Sentiment.POSITIVE.toString());
		sentiments.addElement(Sentiment.NEGATIVE.toString());
		sentiments.addElement(Sentiment.NEUTRAL.toString());

		Attribute attrSentiment = new Attribute("sentiment", sentiments),
				  attrText = new Attribute("text", (FastVector)null);

		FastVector attrs = new FastVector(2);
		attrs.addElement(attrSentiment);
		attrs.addElement(attrText);

		Instances ts = new Instances("twitter-sentiments", attrs, trainingSet.size());

		for(Map.Entry<Status, Sentiment> entry : trainingSet.entrySet()) {
			Instance inst = new Instance(2);
			inst.setValue(attrSentiment, attrSentiment.indexOfValue(entry.getValue().toString()));
			inst.setValue(attrText, entry.getKey().getText());
			ts.add(inst);
		}

		//TODO: we'll have to roll our own
		StringToWordVector filter = new StringToWordVector();
		try {
			filter.setInputFormat(ts);
			ts = filter.useFilter(ts, filter);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}

		try {
			ts.setClassIndex(0);
			cls.buildClassifier(ts);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}
	}
	
	@Override
	public Sentiment classify(Status tweet) throws IllegalStateException {
		// TODO
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Map<Status, Sentiment> classify(Set<Status> testSet) throws IllegalStateException {
		// TODO
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
