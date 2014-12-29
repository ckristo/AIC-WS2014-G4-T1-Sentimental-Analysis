
package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A spell dictionary based on Jazzy.
 */
public class SpellDictionary {
	
	/**
	 * The dictionary file for the spell dictionary.
	 */
	private static final String DICT_FILE = "/spell.txt";
	
	/**
	 * The threshold (edit distance) for the spell correction.
	 */
	private static final int SUGGESTION_THRESHOLD = 0;
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(SpellDictionary.class);
	
	/**
	 * The singleton instance.
	 */
	private static SpellDictionary instance = null;
	
	/**
	 * The Jazzy spell dictionary implementation.
	 */
	private SpellDictionaryHashMap dictionary = null;
	
	/**
	 * Matrix used to generate distance values for spell correction suggestions.
	 */
	private int[][] distMatrix = null;
	
	/**
	 * Constructor.
	 */
	private SpellDictionary() {}
	
	/**
	 * Returns the spell dictionary instance.
	 * @return the spell dictionary instance.
	 */
	public static SpellDictionary getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}
	
	/**
	 * Performs the initialization of the spell dictionary.
	 */
	private static void init() {
		try {
			instance = new SpellDictionary();
			instance.dictionary = new SpellDictionaryHashMap();
			instance.loadDictionary();
		} catch (IOException ex) {
			logger.error("Couldn't load spell dictionary file", ex);
		}
	}
	
	/**
	 * Loads the dictionary file.
	 * @throws 
	 *   - FileNotFoundException if the spell dictionary file doesn't exist
	 *   - IOException if the spell dictionary file couldn't be read
	 */
	private void loadDictionary() throws IOException {
		// load dictionary file
		InputStream is = SpellDictionary.class.getResourceAsStream(DICT_FILE);
		if (is == null) {
			throw new FileNotFoundException("Spell dictionary resource '"+DICT_FILE+"' couldn't be found!");
		}
		dictionary.addDictionary(new InputStreamReader(is));
	}
	
	/**
	 * Check if the dictionary contains a given word.
	 * @param word the word to check.
	 * @return whether the word is present in the dictionary, false otherwise
	 */
	public boolean containsWord(String word) {
		//TODO: convert all words to lower case in dictionary
		return dictionary.isCorrect(word.toLowerCase());
 	}
	
	/**
	 * Gets a suggestion for a misspelled word.
	 * @param word the misspelled word to get a replacement suggestion.
	 * @return the replacement suggestion or null if no suggestions was found.
	 */
	public String getSuggestion(String word) {
		List<Word> suggestions = dictionary.getSuggestions(word, SUGGESTION_THRESHOLD, distMatrix);
		if (suggestions.size() > 0) {
			return suggestions.get(0).getWord();
		} else {
			return null;
		}
	}
}
