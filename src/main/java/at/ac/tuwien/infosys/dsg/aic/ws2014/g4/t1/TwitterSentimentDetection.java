package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import java.util.Collection;
import java.util.HashSet;

public class TwitterSentimentDetection {

	public static void main(String[] args) {
		String line = "";
		String cmd = "";
		ITwitterSentimentClassifier classifier = new TwitterSentimentClassifierImpl();
		ArrayList<Status> tweets = new ArrayList<Status>();
		String searchTerm = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("************************************");
		System.out.println("* Twitter Sentiment Detection v1.0 *");
		printCmds();

		int tweetCount = 0;

		while (true) {
			System.out.print("*:> ");
			try {
				line = in.readLine();
			} catch (IOException e) {
				error("Couldn't read line!");
				printCmds();
			}

			if(line == null) {
				break;
			}

			cmd = line.split(" ")[0];

			if (cmd.equals("train")) {
				if (line.split(" ").length < 2) {
					error("Please add path to file!");
					printCmds();
				} else {
					System.out.println("*:> Training Data...");
					String[] arg = { line.split(" ")[1] };
					classifier = Sentiment140TrainClassifier.main(arg);
					if(classifier == null) {
						break;
					}
				}
			}

			else if (cmd.equals("gettweets")) {

				if (line.split(" ").length < 3) {
					error("Please add number of tweets and searchWord!");
					printCmds();
				} else {
					System.out.println("*:> Downloading Tweets...");
					searchTerm = line.split(" ")[2];
					tweetCount = Integer.valueOf(line.split(" ")[1]);
					tweets = getTweets(tweetCount, searchTerm);
				}
			}

			else if (cmd.equals("classify")) {
				if (tweets.size() < 1) {
					error("Please get tweets first!");
					printCmds();
				} else
					System.out.println("*:> Classifying Tweets...");
				try {
					Collection<Double> sentiments = classifier.classify(tweets).values();

					double sentSum = 0.0;
					for (Double s : sentiments) {
						sentSum += s;
					}
					sentSum /= tweetCount;

					System.out.format("*:> successfully classified %d tweets for searchterm '%s'%n", tweetCount, searchTerm);
					System.out.format("- sentiment: %.2f%n", sentSum);
				} catch (Exception e) {
					error("Classifier stopped!");
					e.printStackTrace();
				}
			}

			else if (cmd.equals("exit")) {
				break;
			}

			else {
				error(" Unknown command: " + cmd);
				printCmds();
			}
		}
	}

	public static ArrayList<Status> getTweets(int count, String searchWord) {
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query(searchWord);
		ArrayList<Status> tweets = new ArrayList<Status>();
		query.setCount(count);
		query.setLang("en");
		try {
			// obtain oauth2 bearer token
			twitter.getOAuth2Token();

			// execute query and print results
			QueryResult result = twitter.search(query);
			tweets = (ArrayList<Status>) result.getTweets();
			for (Status status : tweets) {
				System.out.println("*:> @" + status.getUser().getScreenName()
						+ ":" + status.getText());
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return tweets;
	}

	private static void error(String msg) {
		System.err.println("ERROR! " + msg);
		System.out.println("");
	}

	private static void printCmds() {
		System.out.println("************************************");
		System.out.println("* Commands                         *");
		System.out.println("*                                  *");
		System.out.println("* train [file]                     *");
		System.out.println("* gettweets [number] [searchWord]  *");
		System.out.println("* classify                         *");
		System.out.println("* exit                             *");
		System.out.println("************************************");
	}
}
