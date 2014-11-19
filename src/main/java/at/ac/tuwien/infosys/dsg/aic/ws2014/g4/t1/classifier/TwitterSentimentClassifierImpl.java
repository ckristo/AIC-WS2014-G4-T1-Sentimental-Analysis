package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.Map;
import java.util.Set;
import twitter4j.Status;

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
	
	@Override
	public void train(Map<Status, Sentiment> trainingSet) {
		// TODO
		throw new UnsupportedOperationException("Not supported yet.");
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
