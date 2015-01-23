package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A dictionary for getting the mood of smileys.
 */
public class SmileyDictionary implements IDictionary {

	/**
	 * Mood enumeration.
	 */
	public enum Mood {
		HAPPY {
			@Override
			public String toString() {
				return "happy";
			}
		},
		NEUTRAL {
			@Override
			public String toString() {
				return "neutral";
			}
		},
		SAD {
			@Override
			public String toString() {
				return "sad";
			}
		},
	}

	/**
	 * The name of the resource file to load the dictionary from.
	 */
	private static final String DICT_FILE_RESOURCE = "/smileys.txt";

	/**
	 * The string used to delimit the two elements of an abbreviation.
	 */
	private static final String DELIM_STR = "\\t";

	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(SmileyDictionary.class);

	/**
	 * The set instance containing all (loaded) dictionary entries.
	 */
	private final HashMap<String, Mood> dictionary = new HashMap<>();

	/**
	 * The singleton instance.
	 */
	private static SmileyDictionary instance = null;

	/**
	 * Constructor.
	 */
	private SmileyDictionary() {
	}

	/**
	 * Returns the abbreviations dictionary instance.
	 *
	 * @return the abbreviations dictionary instance.
	 */
	public static SmileyDictionary getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}

	/**
	 * Performs init of the abbreviations dictionary.
	 */
	private static void init() {
		instance = new SmileyDictionary();
		try {
			instance.loadDictionaryResource(DICT_FILE_RESOURCE);
		} catch (IOException ex) {
			logger.error("Couldn't load smiley dictionary file", ex);
		}
	}

	/**
	 * Loads the dictionary file from a resource.
	 *
	 * @param resourceName the dictionary file resource
	 * @throws - FileNotFoundException if the dictionary file doesn't exist -
	 * IOException if the dictionary file couldn't be read
	 */
	private void loadDictionaryResource(String resourceName) throws IOException {
		InputStream is = PreprocessorImpl.class.getResourceAsStream(resourceName);
		if (is == null) {
			throw new FileNotFoundException("Smiley dictionary resource '" + resourceName + "' doesn't exist.");
		} else {
			loadDictionary(is);
		}
	}

	/**
	 * Loads the dictionary from an input stream.
	 *
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
				// convert string to mood enum value
				Mood m;
				try {
					m = Mood.valueOf(tmp[1]);
				} catch (IllegalArgumentException ex) {
					throw new IllegalArgumentException("Invalid dictionary entry -- mood value unknown, line: " + lineNr);
				}

				dictionary.put(tmp[0], m);
			} else {
				throw new IllegalArgumentException("Invalid dictionary entry, line: " + lineNr);
			}
		}
	}

	/**
	 * Checks whether a given string is a known smiley.
	 *
	 * @param smiley the string to check
	 * @return true if the string is known smiley (exact match) or false
	 * otherwise.
	 */
	@Override
	public boolean contains(String smiley) {
		return dictionary.containsKey(smiley);
	}

	/**
	 * Returns the mood for a smiley.
	 *
	 * @param smiley the smiley
	 * @return the mood value, or null if the smiley is not known
	 */
	public Mood getMood(String smiley) {
		return dictionary.get(smiley);
	}
}
