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
public class StopwordsDictionary implements IDictionary {

	/**
	 * The name of the resource file to load the dictionary from.
	 */
	private static final String DICT_FILE_RESOURCE = "/stopwords.txt";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(StopwordsDictionary.class);
	
	/**
	 * The set instance containing all (loaded) dictionary entries.
	 */
	private final HashSet<String> dictionary = new HashSet<>();
	
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
			instance.loadDictionaryResource(DICT_FILE_RESOURCE);
		} catch (IOException ex) {
			logger.error("Couldn't load stopwords dictionary file", ex);
		}
	}
	
	/**
	 * Loads the dictionary file from a resource.
	 * @throws 
	 *   - FileNotFoundException if the dictionary file doesn't exist
	 *   - IOException if the dictionary file couldn't be read
	 */
	private void loadDictionaryResource(String resourceName) throws IOException {
		InputStream is = PreprocessorImpl.class.getResourceAsStream(resourceName);
		if (is == null) {
			throw new FileNotFoundException("Stopwords dictionary file '"+resourceName+"' doesn't exist.");
		} else {
			loadDictionary(is);
		}
	}
	
	/**
	 * Loads the dictionary from an input stream.
	 * @param inputStream the input stream to load
	 * @throws IOException
	 */
	private void loadDictionary(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = reader.readLine()) != null) {
			dictionary.add(line);
		}
	}
	
	/**
	 * Checks whether a given string is a stopword.
	 * @param str the string to check
	 * @return true if the string is a stopword (exact match) or false otherwise.
	 */
	@Override
	public boolean contains(String str) {
		return dictionary.contains(str);
	}
}
