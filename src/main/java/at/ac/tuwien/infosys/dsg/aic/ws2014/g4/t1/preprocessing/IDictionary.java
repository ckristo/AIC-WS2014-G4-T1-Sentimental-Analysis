package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

/**
 * Dictionary interface.
 */
public interface IDictionary {
	
	/**
	 * Check whether the dictionary contains a word.
	 * @param word the word to check
	 * @return true if the dictionary contains the word, false otherwise
	 */
	public boolean contains(String word);
	
}
