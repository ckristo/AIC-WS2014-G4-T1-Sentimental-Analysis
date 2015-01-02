package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.Sentiment140TrainClassifier;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.TwitterSentimentDetection;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ClassifierException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier;
import twitter4j.Status;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SentimentAnalyzer {

	private static ITwitterSentimentClassifier classifier;

	/**
	 * Starts the classifier.
	 * @return true if the classifier was created successfully, false otherwise
	 */
	public static boolean start() {
		classifier = Sentiment140TrainClassifier.main(new String[]{"training.1600000.processed.noemoticon.csv.bz2"});
		return classifier != null;
	}

	public static Double analyze(String term, Date from, Date to) throws ClassifierException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String query = term;
		if(from != null) query += " since:"+sdf.format(from);
		if(to != null) query += " until:"+sdf.format(to);

		int count = 10;
		//TODO: check for exceptions
		List<Status> tweets = TwitterSentimentDetection.getTweets(count, query);

		Collection<Double> sentiments = classifier.classify(tweets).values();

		double sentSum = 0.0;
		for (Double s : sentiments) {
			sentSum += s;
		}
		return sentSum /= count;
	}
}
