package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A spell dictionary based on Jazzy.
 */
public class SpellDictionary implements IDictionary {

	/**
	 * The name of the resource file to load the dictionary from.
	 */
	private static final String DICT_FILE_RESOURCE = "/spell.txt";

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
	 * Allows to specify spell suggestions that should be ignored.
	 */
	private final List<String> SUGGESTIONS_TO_IGNORE = new ArrayList<>();

	{
		SUGGESTIONS_TO_IGNORE.add("h");
	}

	/**
	 * Matrix used to generate distance values for spell correction suggestions.
	 */
	private int[][] distMatrix = null;

	/**
	 * Constructor.
	 */
	private SpellDictionary() {
	}

	/**
	 * Returns the spell dictionary instance.
	 *
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
			instance.loadDictionaryResource(DICT_FILE_RESOURCE);
		} catch (IOException ex) {
			logger.error("Couldn't load spell dictionary file", ex);
		}
	}

	/**
	 * Loads the dictionary from a resource.
	 *
	 * @param resourceName the dictionary file resource
	 * @throws - FileNotFoundException if the dictionary file doesn't exist -
	 * IOException if the dictionary file couldn't be read
	 */
	private void loadDictionaryResource(String resourceName) throws IOException {
		InputStream is = SpellDictionary.class.getResourceAsStream(resourceName);
		if (is == null) {
			throw new FileNotFoundException("Spell dictionary resource '" + resourceName + "' couldn't be found!");
		} else {
			dictionary.addDictionary(new InputStreamReader(is));
		}
	}

	/**
	 * Check if the dictionary contains a given word.
	 *
	 * @param word the word to check.
	 * @return whether the word is present in the dictionary, false otherwise
	 */
	@Override
	public boolean contains(String word) {
		return dictionary.isCorrect(word);
	}

	/**
	 * Gets a suggestion for a misspelled word.
	 *
	 * @param word the misspelled word to get a replacement suggestion.
	 * @return the replacement suggestion or null if no suggestions was found.
	 */
	public String getSuggestion(String word) {
		List<Word> suggestions = dictionary.getSuggestions(word, 0, distMatrix); // threshold has no effect!
		for (Word suggestion : suggestions) {
			// check if spell suggestion should be ignored:
			//   Jazzy sometimes suggests strange corrections (e.g. "*1" => "h") 
			//   if it doesn't come up with suggestions based on edit distance 
			//   (see SpellDictionaryASpell.addBestGuess)
			if (!SUGGESTIONS_TO_IGNORE.contains(suggestion.getWord())) {
				return suggestion.getWord();
			}
		}
		return null;
	}
}
