package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dictionary for SentiWordNet data (http://sentiwordnet.isti.cnr.it).
 * Heavily based on http://sentiwordnet.isti.cnr.it/code/SentiWordNetDemoCode.java
 */
public class SentiWordNetDictionary {
	
	public enum WordNetPosition {
		A {
			@Override
			public String toString() {
				return "a";
			}
		},
		N {
			@Override
			public String toString() {
				return "n";
			}
		},
		R {
			@Override
			public String toString() {
				return "r";
			}
		},
		V {
			@Override
			public String toString() {
				return "v";
			}
		}
	}
	
	/**
	 * The SentiWordNet file.
	 */
	private static final String DICT_FILE_RESOURCE = "/sentiwordnet.txt";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(SentiWordNetDictionary.class);
	
	/**
	 * The map instance containing all (loaded) SentiWordNet entries.
	 */
	private final HashMap<String, Double> dictionary = new HashMap<>();
	
	/**
	 * The singleton instance.
	 */
	private static SentiWordNetDictionary instance = null;
	
	/**
	 * Constructor.
	 */
	private SentiWordNetDictionary() {}
	
	/**
	 * Returns the stopwords dictionary instance.
	 * @return the stopwords dictionary instance.
	 */
	public static SentiWordNetDictionary getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}
	
	/**
	 * Performs initialization of the stopwords dictionary.
	 */
	private static void init() {
		instance = new SentiWordNetDictionary();
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
	 * @param is the input stream to load
	 * @throws IOException
	 */
	private void loadDictionary(InputStream is) throws IOException {
		HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<>();
		try (BufferedReader csv = new BufferedReader(new InputStreamReader(is))) {
			int lineNr = 0;
			String line;
			while ((line = csv.readLine()) != null) {
				lineNr++;

				// If it's a comment, skip this line.
				if (line.trim().startsWith("#")) {
					continue;
				}
				
				// We use tab separation
				String[] data = line.split("\t");
				String wordTypeMarker = data[0];

				// Example line:
				// POS ID PosS NegS SynsetTerm#sensenumber Desc
				// a 00009618 0.5 0.25 spartan#4 austere#3 ascetical#2
				// ascetic#2 practicing great self-denial;...etc

				// Is it a valid line? Otherwise, through exception.
				if (data.length != 6) {
					throw new IllegalArgumentException("Incorrect tabulation format in file, line: " + lineNr);
				}

				// Calculate synset score as score = PosS - NegS
				Double synsetScore;
				try {
					synsetScore = Double.parseDouble(data[2]) - Double.parseDouble(data[3]);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("Score values couldn't be parsed to double, line: " + lineNr, ex);
				}

				// Get all Synset terms
				String[] synTermsSplit = data[4].split(" ");

				// Go through all terms of current synset.
				for (String synTermSplit : synTermsSplit) {
					// Get synterm and synterm rank
					String[] synTermAndRank = synTermSplit.split("#");
					String synTerm = synTermAndRank[0] + "#" + wordTypeMarker;

					int synTermRank = Integer.parseInt(synTermAndRank[1]);
					// What we get here is a map of the type:
					// term -> {score of synset#1, score of synset#2...}

					// Add map to term if it doesn't have one
					if (!tempDictionary.containsKey(synTerm)) {
						tempDictionary.put(synTerm, new HashMap<Integer, Double>());
					}

					// Add synset link to synterm
					tempDictionary.get(synTerm).put(synTermRank, synsetScore);
				}
			}
			
			// Go through all the terms.
			for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary.entrySet()) {
				String word = entry.getKey();
				Map<Integer, Double> synSetScoreMap = entry.getValue();

				// Calculate weighted average. Weigh the synsets according to
				// their rank.
				// Score= 1/2*first + 1/3*second + 1/4*third ..... etc.
				// Sum = 1/1 + 1/2 + 1/3 ...
				double score = 0.0;
				double sum = 0.0;
				for (Map.Entry<Integer, Double> setScore : synSetScoreMap.entrySet()) {
					score += setScore.getValue() / (double) setScore.getKey();
					sum += 1.0 / (double) setScore.getKey();
				}
				score /= sum;

				dictionary.put(word, score);
			}
		}
	}
	
	/**
	 * Generates the key for a word and a wordnet position.
	 * @param word the word
	 * @param position the position
	 * @return the key for the word and position
	 */
	private String createKey(String word, WordNetPosition position) {
		return word + "#" + position.toString();
	}
	
	/**
	 * Checks if a given word/position is present in the dictionary.
	 * @param word the word
	 * @param position the position
	 * @return true if the word/position is present in the dictionary, false otherwise
	 */
	public boolean containsWord(String word, WordNetPosition position) {
		return dictionary.containsKey(createKey(word, position));
	}
	
	/**
	 * Returns the sentiment value for a word/position.
	 * @param word the word
	 * @param position the position
	 * @return the sentiment value for word/position or null if word/position is not present in the dictionary
	 */
	public double getSentimentValue(String word, WordNetPosition position) {
		return dictionary.get(createKey(word, position));
	}
}
