package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreprocessorImpl implements IPreprocessor {
	
	/**
	 * RegEx pattern for a URL.
	 * @see http://blog.mattheworiordan.com/post/13174566389/url-regular-expression-for-links-with-or-without
	 */
	private static final Pattern URL_PATTERN = Pattern.compile("^((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)$");
	
	/**
	 * Dictionary used for stopword removal.
	 */
	private StopwordsDictionary stopwordsDict = StopwordsDictionary.getInstance();
	
	/**
	 * Dictionary used to expand abbreviations.
	 */
	private AbbreviationDictionary abbrevDict = AbbreviationDictionary.getInstance();
	
	/**
	 * Tokenizer used to tokenize an expanded abbreviation.
	 */
	private ITokenizer tokenizer = new TokenizerImpl();
	
	/**
	 * Dictionary used for spell checking & correction.
	 */
	private SpellDictionary spellDict = SpellDictionary.getInstance();
	
	/**
	 * Performs the preprocessing step.
	 * @param tokens the list of raw tokens that will be manipulated by the preprocessor.
	 */
	@Override
	public void preprocess(List<String> tokens) {
		ListIterator<String> iterator = tokens.listIterator();
		String word;
		while (iterator.hasNext()) {
			word = iterator.next();
			
			// (1) remove token if it's a stopword
			if (stopwordsDict.containsWord(word)) {
				iterator.remove();
				continue;
			}
			
			// (2) replace URLs
			if (isURL(word)) {
				iterator.set(URL_TOKEN);
				continue;
			}
			
			// (3) replace usernames
			if (isUsername(word)) {
				iterator.set(USERNAME_TOKEN);
				continue;
			}
			
			// (4) replace abbreviations and emoticons
			if (abbrevDict.containsWord(word)) {
				iterator.remove();
				// add tokens for the words in long form
				for (String ad : tokenizer.tokenize(abbrevDict.getLongForm(word))) {
					iterator.add(ad);
				}
				continue;
			}
			
			// (5) replace misspelled words
			if (!spellDict.containsWord(word)) {
				iterator.set(spellDict.getSuggestion(word));
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
		Matcher m = URL_PATTERN.matcher(str);
		return m.matches();
	}
	
	/**
	 * Checks if a given string is a mentioned Twitter username (starting with @).
	 * @param str the string to check
	 * @return true if the string is a mentioned Twitter username, false otherwise.
	 */
	private boolean isUsername(String str) {
		return str.startsWith("@");
	}
}
