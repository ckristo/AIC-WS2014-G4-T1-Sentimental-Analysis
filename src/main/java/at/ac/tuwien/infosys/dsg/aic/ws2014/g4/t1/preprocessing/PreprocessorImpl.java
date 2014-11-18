package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class PreprocessorImpl implements Preprocessor {
	
	/**
	 * Emoticons token map.
	 */
	private static final HashMap<String, String> EMOTICONS_TOKEN_MAP = new HashMap<>();
	
	static {
		EMOTICONS_TOKEN_MAP.put(":-)", "__++SMILE++__");
		EMOTICONS_TOKEN_MAP.put(":)",  "__++SMILE++__");
		EMOTICONS_TOKEN_MAP.put(":D",  "__++SMILE++__");
		//TODO: complete emoticon map -- differnet smiles expressing the
		// same emotions should map to the same tokens
	}
	
	/**
	 * Username token.
	 */
	private static final String USERNAME_TOKEN = "__++USERNAME++__";
	
	/**
	 * URL token.
	 */
	private static final String URL_TOKEN = "__++URL++__";
	
	/**
	 * Performs the preprocessing step.
	 * @param tokens the list of raw tokens that will be manipulated by the preprocessor.
	 */
	@Override
	public void preprocess(List<String> tokens) {
		
		for (ListIterator<String> iterator = tokens.listIterator(); iterator.hasNext();) {
			String word = iterator.next();
			
			// (1) remove token if it's a stopword
			if (StopwordsDictionary.getInstance().isStopword(word)) {
				iterator.remove();
				continue;
			} 
			
			// (2) replace URLs
			if (isURL(word)) {
				iterator.set(URL_TOKEN);
				continue;
			}
			
			// (3) replace emoticons
			if (isEmoticon(word)) {
				iterator.set(getEmoticonToken(word));
				continue;
			}
			
			// (4) replace abbreviations
			if (false /*TODO*/) {
				// TODO: create an abbreviation dictionary and replace abbrev. terms
				// with their fully-written equivalent (e.g. "wtf" => "what the fuck")
				// * create a singleton class similar to StopwordsDictionary
				// * look for a list with abbreviations (check paper 'Twitter Sentiment Analysis: The Good the Bad and the OMG!')
				continue;
			}
			
			// (5) replace misspelled words
			if (false /*TODO*/) {
				// TODO: use an external spellchecker library for Java (e.g. https://www.languagetool.org), 
				// detect misspelled words and replace them with their corrected versions
				continue;
			}
		}
	}
	
	/**
	 * Checks if a given string is an URL.
	 * @param str the string to check
	 * @return true if the string is an URL, false otherwise.
	 */
	private boolean isURL(String str) {
		//TODO
		return false;
	}
	
	/**
	 * Checks if a given string is an emoticon.
	 * @param str the string to check.
	 * @return true if the string is an emoticon, false otherwise.
	 */
	private boolean isEmoticon(String str) {
		return EMOTICONS_TOKEN_MAP.containsKey(str);
	}
	
	/**
	 * Returns the token for an emoticon.
	 * @param str the string to check
	 * @return the emoticon token or null if str is no known emoticon.
	 */
	private String getEmoticonToken(String str) {
		return EMOTICONS_TOKEN_MAP.get(str);
	}
	
}
