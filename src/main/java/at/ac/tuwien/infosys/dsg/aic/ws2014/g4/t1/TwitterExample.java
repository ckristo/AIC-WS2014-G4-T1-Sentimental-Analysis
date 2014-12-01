package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.AbbreviationDictionary;
import cmu.arktweetnlp.Twokenize;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ClassifierException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class that shows how to gather tweets from a file / Twitter Search API.
 */
public class TwitterExample {

	private static final Logger logger = LogManager.getLogger("SentimentClassifier");

	public static void main(String[] args) {

		/**
		 * EXAMPLE CODE for using the Twitter Search API --
		 * https://dev.twitter.com/rest/public/search
		 */
		
		//test such
		String testsuche = "telekom austria";
		// set up twitter instance
		Twitter twitter = new TwitterFactory().getInstance();
		
		//input for 1 word-filter argument in command line
		//mvn exec:java -Dexec.args="telekom"
		Query query = new Query(args[0]);
		
		try {
			// obtain oauth2 bearer token
			twitter.getOAuth2Token();

			// execute query and print results
			QueryResult result = twitter.search(query);
			for (Status status : result.getTweets()) {
				System.out.println("@" + status.getUser().getScreenName() + ":"
						+ status.getText());
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		System.out.println("----------------------------------------");

		/**
		 * EXAMPLE CODE for loading a Twitter Stream API dump --
		 * https://dev.twitter.com/streaming/overview/processing
		 */
		InputStream bzIn = null;
		try {
			// uses the Apache Commons Compress library
			// to directly read the bzipped file
			FileInputStream fin = new FileInputStream("/Users/alexanderwurl/Documents/Uni/Master/ws_14/AIC/tweets.txt.bz2");
			BufferedInputStream in = new BufferedInputStream(fin);
			bzIn = new BZip2CompressorInputStream(in);

			// set up hbc's StringDelimitedProcessor -- see
			// https://github.com/twitter/hbc
			BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
			StringDelimitedProcessor sdp = new StringDelimitedProcessor(queue);
			sdp.setup(bzIn);

			Map<Status, Sentiment> sentiments = new HashMap<Status, Sentiment>();

			// extract some JSON objects from the file
			int i = 0, limit = 100;
			while (sdp.process() && i < limit) {
				Status status = TwitterObjectFactory.createStatus(queue.poll());

				// simple filters to remove high-volume noise
				//TODO: do this properly
				if (status.getSource().startsWith("<a href=\"http://foursquare.com")
				 || status.getSource().startsWith("<a href=\"http://trendsmap.com")
				 || status.getText().contains("#teamfollowback"))
					continue;

				Sentiment sentiment;
				// highly sophisticated training set partitioning :)
				if (status.getText().contains(":)") && !status.getText().contains(":("))
					sentiment = Sentiment.POSITIVE;
				else if (!status.getText().contains(":)") && status.getText().contains(":("))
					sentiment = Sentiment.NEGATIVE;
				else continue;

				System.out.println(i+": @" + status.getUser().getScreenName() + ":"
						+ status.getText());

				sentiments.put(status, sentiment);

				i++;
			}
			System.out.printf("------ selected %d tweets for training\n", i);

			ITwitterSentimentClassifier cls = new TwitterSentimentClassifierImpl();
			cls.train(sentiments);
			
			

		} catch (IOException | InterruptedException | TwitterException e) {
			e.printStackTrace();
		} catch(ClassifierException e) {
			logger.error("exception while building classifier", e.getCause());
		} finally {
			// close input stream
			try {
				if (bzIn != null)
					bzIn.close();
			} catch (IOException e) {
			}
		}
		
	}
}
