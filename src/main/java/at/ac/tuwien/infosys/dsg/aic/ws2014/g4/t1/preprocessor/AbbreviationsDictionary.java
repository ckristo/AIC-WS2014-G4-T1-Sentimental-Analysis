package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AbbreviationsDictionary implements IDictionary {

	/**
	 * The name of the resource file to load the dictionary from.
	 */
	private static final String DICT_FILE_RESOURCE = "/abbreviations.txt";
	
	/**
	 * The string used to delimit the two elements of an abbreviation.
	 */
	private static final String DELIM_STR = "\\t";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(AbbreviationsDictionary.class);
	
	/**
	 * The set instance containing all (loaded) dictionary entries.
	 */
	private final HashMap<String, String> dictionary = new HashMap<>();
	
	/**
	 * The singleton instance.
	 */
	private static AbbreviationsDictionary instance = null;
	
	/**
	 * Constructor.
	 */
	private AbbreviationsDictionary() {}
	
	/**
	 * Returns the abbreviations dictionary instance.
	 * @return the abbreviations dictionary instance.
	 */
	public static AbbreviationsDictionary getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}
	
	/**
	 * Performs init of the abbreviations dictionary.
	 */
	private static void init() {
		instance = new AbbreviationsDictionary();
		try {
			instance.loadDictionaryResource(DICT_FILE_RESOURCE);
		} catch (IOException ex) {
			logger.error("Couldn't load abbreviations dictionary file", ex);
		}
	}
	
	/**
	 * Loads the dictionary file from a resource.
	 * @param resourceName the dictionary file resource
	 * @throws 
	 *   - FileNotFoundException if the dictionary file doesn't exist
	 *   - IOException if the dictionary file couldn't be read
	 */
	private void loadDictionaryResource(String resourceName) throws IOException {
		InputStream is = PreprocessorImpl.class.getResourceAsStream(resourceName);
		if (is == null) {
			throw new FileNotFoundException("Abbreviation dictionary resource '"+resourceName+"' doesn't exist.");
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
		int lineNr = 0;
		while ((line = reader.readLine()) != null) {
			lineNr++;
			
			String[] tmp = line.split(DELIM_STR);
			if (tmp.length == 2) {
				dictionary.put(tmp[0].toLowerCase(), tmp[1]);
			} else {
				throw new IllegalArgumentException("Invalid dictionary entry, line: "+lineNr);
			}
		}
	}
	
	/**
	 * Checks whether a given string is a known abbreviation.
	 * @param str the string to check
	 * @return true if the string is known abbreviation (exact match) or false otherwise.
	 */
	@Override
	public boolean contains(String str) {
		return dictionary.containsKey(str.toLowerCase());
	}
	
	/**
	 * Returns the long form for a known abbreviation.
	 * @param str the string to check
	 * @return the long form for a known abbreviation, or null otherwise.
	 */
	public String getLongForm(String str) {
		return dictionary.get(str.toLowerCase());
	}
}
