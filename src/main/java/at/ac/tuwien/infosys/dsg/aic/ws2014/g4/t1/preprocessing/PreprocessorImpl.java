package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.List;
import java.util.ListIterator;

public class PreprocessorImpl implements IPreprocessor {

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

			// (4) replace abbreviations and emoticons
			iterator.set(AbbreviationDictionary.getInstance().getAbbreviation(word));
			
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
		return (str.matches("^[a-zA-Z0-9\\-\\.]+\\.(com|org|net|mil|edu|COM|ORG|NET|MIL|EDU)$") ||
				str.matches("((mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)"));
	}
}
