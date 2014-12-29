package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple stopwords dictionary.
 */
public class StopwordsDictionary {

	/**
	 * The file to load the stopwords from.
	 */
	private static final String DICT_FILE = "/stopwords.txt";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(StopwordsDictionary.class);
	
	/**
	 * The set instance containing all (loaded) stopwords.
	 */
	private final HashSet<String> stopwords = new HashSet<>();
	
	/**
	 * The singleton instance.
	 */
	private static StopwordsDictionary instance = null;
	
	/**
	 * Constructor.
	 */
	private StopwordsDictionary() {}
	
	/**
	 * Returns the stopwords dictionary instance.
	 * @return the stopwords dictionary instance.
	 */
	public static StopwordsDictionary getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}
	
	/**
	 * Performs initialization of the stopwords dictionary.
	 */
	private static void init() {
		instance = new StopwordsDictionary();
		try {
			instance.loadFile();
		} catch (IOException ex) {
			logger.error("Couldn't load stopwords dictionary file", ex);
		}
	}
	
	/**
	 * Loads the stopwords dictionary file into the stopwords set.
	 * @throws 
	 *   - FileNotFoundException if the stopwords dictionary file doesn't exist
	 *   - IOException if the stopwords dictionary file couldn't be read
	 */
	private void loadFile() throws IOException {
		// create stream for resource file
		InputStream is = PreprocessorImpl.class.getResourceAsStream(DICT_FILE);
		if (is == null) {
			throw new FileNotFoundException("Stopwords dictionary file doesn't exist.");
		}
		// read file line by line
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = reader.readLine()) != null) {
			stopwords.add(line.toLowerCase());
		}
	}
	
	/**
	 * Checks whether a given string is a stopword.
	 * @param str the string to check
	 * @return true if the string is a stopword (exact match) or false otherwise.
	 */
	public boolean containsWord(String str) {
		return stopwords.contains(str.toLowerCase());
	}
}
