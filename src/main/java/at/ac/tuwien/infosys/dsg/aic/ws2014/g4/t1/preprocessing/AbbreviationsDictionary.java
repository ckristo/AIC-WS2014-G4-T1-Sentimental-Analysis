package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AbbreviationsDictionary {

	/**
	 * The file to load the abbreviations from.
	 */
	private static final String DICT_FILE = "/abbreviations.txt";
	
	/**
	 * The string used to delimit the two elements of an abbreviation.
	 */
	private static final String DELIM_STR = "\\|";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(AbbreviationsDictionary.class);
	
	/**
	 * The set instance containing all (loaded) abbreviations.
	 */
	private final HashMap<String, String> abbreviations = new HashMap<>();
	
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
			instance.loadFile();
		} catch (IOException ex) {
			logger.error("Couldn't load abbreviations dictionary file", ex);
		}
	}
	
	/**
	 * Loads the abbreviation dictionary file into the abbreviation set.
	 * @throws 
	 *   - FileNotFoundException if the abbreviation dictionary file doesn't exist
	 *   - IOException if the abbreviation dictionary file couldn't be read
	 */
	private void loadFile() throws IOException {
		// create stream for resource file
		InputStream is = PreprocessorImpl.class.getResourceAsStream(DICT_FILE);
		if (is == null) {
			throw new FileNotFoundException("Abbreviation dictionary file doesn't exist.");
		}
		// read file line by line
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] tmp = line.split(DELIM_STR);
			if (tmp.length >= 2) {
				abbreviations.put(tmp[0].toLowerCase(), tmp[1].toLowerCase());
			}
		}
	}
	
	/**
	 * Checks whether a given string is a known abbreviation.
	 * @param str the string to check
	 * @return true if the string is known abbreviation (exact match) or false otherwise.
	 */
	public boolean containsWord(String str) {
		return abbreviations.containsKey(str.toLowerCase());
	}
	
	/**
	 * Returns the long form for a known abbreviation.
	 * @param str the string to check
	 * @return the long form for a known abbreviation, or null otherwise.
	 */
	public String getLongForm(String str) {
		return abbreviations.get(str.toLowerCase());
	}
}
