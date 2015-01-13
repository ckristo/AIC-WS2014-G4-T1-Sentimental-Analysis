package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Constants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 * Reads the Sentiment140 training dataset file and exports the processed 
 * training data as ARFF file.
 * @see http://help.sentiment140.com/for-students/
 */
public class Sentiment140DataLoader {
	
	/**
	 * Exit value in case of error.
	 */
	private static final int EXIT_ERROR = 1;
	/**
	 * Exit value in case of a successful run.
	 */
	private static final int EXIT_SUCCESS = 0;
	
	/**
	 * Sentiment value for positive sentiment.
	 */
	private static final int POLARITY_POSITIVE = 4;
	/**
	 * Sentiment value for negative sentiment.
	 */
	private static final int POLARITY_NEGATIVE = 0;
	/**
	 * Sentiment value for neutral sentiment.
	 */
	private static final int POLARITY_NEUTRAL = 2;
	
	/**
	 * The CSV row that contains the polarity value.
	 */
	private static final int ROW_POLARITY = 0;
	/**
	 * The CSV row that contains the Tweet's ID.
	 */
	private static final int ROW_ID = 1;
	/**
	 * The CSV row that contains the Tweet's text.
	 */
	private static final int ROW_TEXT = 5;
	
	/**
	 * The max. number of elements to put into the training set per class.
	 */
	private static int classLimit = 5000;
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LogManager.getLogger("Sentiment140TrainClassifier");
	
	/**
	 * Prints usage message to stdout.
	 */
	public static void usage() {
		System.out.println("Sentiment140TrainClassifier <bzipped-csv-file> [class-limit]");
	}
	
	/**
	 * main()
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// check arguments
		if (args.length == 0) {
			System.err.println("Missing argument <bzipped-csv-file>");
			usage();
			System.exit(EXIT_ERROR);
		} else if (args.length > 2) {
			System.err.println("Too much arguments");
			usage();
			System.exit(EXIT_ERROR);
		}
		
		// read class limit argument
		if (args.length == 2) {
			try {
				classLimit = Integer.parseInt(args[1]) * 1000;
			} catch (NumberFormatException ex) {
				System.err.println("Argument [class-limit] must be a number");
				usage();
				System.exit(EXIT_ERROR);
			}
		}
		
		// check if file exists
		File inFile = new File(args[0]);
		if (!inFile.exists()) {
			System.err.println("Given <bzipped-csv-file> doesn't exist");
			usage();
			System.exit(EXIT_ERROR);
		}
		
		// read training set data from given file
		Map<Status, Sentiment> trainingSet = null;
		try {
			// read training set from file
			trainingSet = readTrainingSet(inFile);
		} catch (IOException ex) {
			System.err.println("Couldn't read given <bzipped-csv-file>");
			usage();
			System.exit(EXIT_ERROR);
		}
		
		ApplicationConfig config = null;
		try {
			InputStream is = Sentiment140DataLoader.class.getResourceAsStream(Constants.DEFAULT_CONFIG_FILE_RESOURCE);
			config = new ApplicationConfig(is);
		} catch (IOException ex) {
			logger.error("Couldn't load application configuration file", ex);
			System.err.println("Couldn't create classifier -- configuration file couldn't be read");
			System.exit(EXIT_ERROR);
		}
		
		// create classifier
		TwitterSentimentClassifierImpl classifier = new TwitterSentimentClassifierImpl(config);
		
		// process and export training data
		classifier.processTrainingSet(trainingSet);
		
		// export processed training data
		try {
			classifier.exportProcessedTrainingDataToArffFile("Sentiment140TrainingData-"+(classLimit/1000)+"k.arff");
		} catch (IOException ex) {
			logger.error("Couldn't export processed training data to ARFF file", ex);
			System.err.println("Couldn't export processed training data to ARFF file");
			System.exit(EXIT_ERROR);
		}
		
		System.exit(EXIT_SUCCESS);
	}
	
	/**
	 * Read training set from a BZIP2-compressed file.
	 * @param file the file to read from
	 * @return the training set
	 * @throws IOException if the file couldn't be read successfully.
	 */
	private static Map<Status, Sentiment> readTrainingSet(File file) throws IOException {
		// create bzipped input stream
		InputStream bzIn = null;
		try {
			FileInputStream fin = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fin);
			bzIn = new BZip2CompressorInputStream(in);
		} catch (IOException ex) {
			logger.error("Couldn't create input stream for bzipped CSV file.", ex);
			throw ex;
		}
		
		// parse the CSV file
		CSVParser csvParser = null;
		try {
			csvParser = new CSVParser(new InputStreamReader(bzIn), CSVFormat.DEFAULT);
		} catch (IOException ex) {
			logger.error("Couldn't create CSV parser.", ex);
			throw ex;
		}
		
		Map<Status, Sentiment> trainingSet = new HashMap<>();
		
		int numNegative = 0, numNeutral = 0, numPositive = 0;
		
		for(CSVRecord record : csvParser) {
			int polarity;
			try {
				polarity = Integer.parseInt(record.get(ROW_POLARITY));
			} catch (NumberFormatException ex) {
				logger.warn("Read polarity is not a valid number for record #"+record.getRecordNumber(), ex);
				continue;
			}
			
			// map polarity of input to a sentiment class
			Sentiment sent = mapPolarityToSentiment(polarity);
			if (sent == null) {
				logger.warn("Read polarity is not a known polarity value for record #"+record.getRecordNumber());
				continue;
			}
			
			// FIXME: refactor code to be independant to the number of classes
			
			// check if we reached the limit for at least one class
			boolean classLimitReached = false;
			// - negative
			if (sent.equals(Sentiment.NEGATIVE)) {
				if (numNegative < classLimit) {
					numNegative++;
				} else {
					classLimitReached = true;
				}
			}
			// - neutral
			if (sent.equals(Sentiment.NEUTRAL)) {
				if (numNeutral < classLimit) {
					numNeutral++;
				} else {
					classLimitReached = true;
				}
			}
			// - positive
			if (sent.equals(Sentiment.POSITIVE)) {
				if (numPositive < classLimit) {
					numPositive++;
				} else {
					classLimitReached = true;
				}
			}
			
			// stop after limits for each class is reached
			if (numNegative == classLimit 
					&& numNeutral == classLimit 
					&& numPositive == classLimit) {
				logger.info("Limit for all classes reached, stop collecting training data");
				break;
			}
			
			// do not add element if limit for the class is reached
			if (classLimitReached) {
				//logger.debug("Limit for class "+sent+" reached");
				continue;
			}
			
			// create JSON string and let Twitter4J generate a Status object
			Status status = createStatusObject(record);
			if (status == null) {
				logger.warn("Coudln't create status object for record #"+record.getRecordNumber());
				continue;
			}
			
			logger.debug("Put new "+sent+" entry into training set");
			
			trainingSet.put(status, sent);
		}
		
		csvParser.close();
		
		return trainingSet;
	}
	
	/**
	 * Returns the sentiment for a given polarity value.
	 * @param polarity the polarity value to map
	 * @return the mapped sentiment, or null if polarity is an unknown value
	 */
	private static Sentiment mapPolarityToSentiment(int polarity) {
		switch (polarity) {
			case POLARITY_NEGATIVE :
				return Sentiment.NEGATIVE;
			case POLARITY_NEUTRAL :
				return Sentiment.NEUTRAL;
			case POLARITY_POSITIVE :
				return Sentiment.POSITIVE;
			default :
				logger.warn("Unknown polarity value "+polarity);
				return null;
		}
	}

	/**
	 * Creates a status object based on a CSV entry.
	 * @param record the CSV entry to create a status object from.
	 * @return the created status object or null if (based on the input) no status object could be created
	 */
	private static Status createStatusObject(CSVRecord record) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("id", record.get(ROW_ID));
			jo.put("text", record.get(ROW_TEXT));
			return TwitterObjectFactory.createStatus(jo.toString());
		} catch (JSONException | TwitterException ex) {
			logger.warn("Couldn't create status object", ex);
			return null;
		}
	}
}
