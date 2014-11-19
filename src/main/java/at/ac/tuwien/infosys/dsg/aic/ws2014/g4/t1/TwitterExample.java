package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.AbbreviationDictionary;
import cmu.arktweetnlp.Twokenize;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import com.twitter.hbc.core.processor.StringDelimitedProcessor;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Status;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.TwitterObjectFactory;

/**
 * Class that shows how to gather tweets from a file / Twitter Search API.
 */
public class TwitterExample {

	public static void main(String[] args) {

		/**
		 * EXAMPLE CODE for using the Twitter Search API --
		 * https://dev.twitter.com/rest/public/search
		 */
		// set up twitter instance
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query("@twitter :)");
		
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
			FileInputStream fin = new FileInputStream("../tweets.txt.bz2");
			BufferedInputStream in = new BufferedInputStream(fin);
			bzIn = new BZip2CompressorInputStream(in);

			// set up hbc's StringDelimitedProcessor -- see
			// https://github.com/twitter/hbc
			BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
			StringDelimitedProcessor sdp = new StringDelimitedProcessor(queue);
			sdp.setup(bzIn);

			// extract some JSON objects from the file
			int i = 0, limit = 5;
			while (sdp.process() && i < limit) {
				Status status = TwitterObjectFactory.createStatus(queue.poll());

				System.out.println("@" + status.getUser().getScreenName() + ":"
						+ status.getText());
				
				i++;
			}
		} catch (IOException | InterruptedException | TwitterException e) {
			e.printStackTrace();
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
