package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.utils;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterSentimentDetectionCLI {

	public static void main(String[] args) {
		String line = "";
		String cmd;

		TwitterSentimentClassifierImpl classifier = new TwitterSentimentClassifierImpl();

		ArrayList<Status> tweets = new ArrayList<>();
		Collection<double[]> sentiments = new HashSet<>();
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

			cmd = line.split(" ")[0];

			if (cmd.equals("train")) {
				if (line.split(" ").length < 2) {
					error("Please add path to file!");
					printCmds();
				} else {
					System.out.println("*:> Training Data...");
					String[] arg = {line.split(" ")[1]};
					Sentiment140DataLoader.main(arg);
				}
			} else if (cmd.equals("gettweets")) {

				if (line.split(" ").length < 3) {
					error("Please add number of tweets and searchWord!");
					printCmds();
				} else {
					System.out.println("*:> Downloading Tweets...");
					searchTerm = line.split(" ")[2];
					tweetCount = Integer.valueOf(line.split(" ")[1]);
					tweets = getTweets(tweetCount, searchTerm);
				}
			} else if (cmd.equals("classify")) {
				if (tweets.size() < 1) {
					error("Please get tweets first!");
					printCmds();
				} else {
					System.out.println("*:> Classifying Tweets...");
				}
				try {
					sentiments = classifier.classifyWithProbabilities(tweets).values();
				} catch (Exception e) {
					error("Classifier stopped!");
					e.printStackTrace();
				}

				Double[] sentSum = new Double[]{0.0d, 0.0d, 0.0d};
				for (double[] s : sentiments) {
					sentSum[0] += s[0];
					sentSum[1] += s[1];
					sentSum[2] += s[2];
				}

				sentSum[0] /= tweetCount;
				sentSum[1] /= tweetCount;
				sentSum[2] /= tweetCount;

				System.out.format("*:> successfully classified %d tweets for searchterm '%s'%n", tweetCount, searchTerm);
				System.out.format("- negative: %.2f%n", sentSum[0]);
				System.out.format("- positive: %.2f%n", sentSum[2]);
			} else if (cmd.equals("exit")) {
				break;
			} else {
				error(" Unknownd command: " + cmd);
				printCmds();
			}
		}
	}

	public static ArrayList<Status> getTweets(int count, String searchWord) {
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query(searchWord);
		ArrayList<Status> tweets = new ArrayList<>();
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
		//System.out.println("* train [file]                     *");
		System.out.println("* gettweets [number] [searchWord]  *");
		System.out.println("* classify                         *");
		System.out.println("* exit                             *");
		System.out.println("************************************");
	}
}
