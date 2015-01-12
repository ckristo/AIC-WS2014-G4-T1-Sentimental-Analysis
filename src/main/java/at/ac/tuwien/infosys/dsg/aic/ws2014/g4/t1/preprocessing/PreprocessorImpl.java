package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreprocessorImpl implements IPreprocessor {

	/**
	 * RegEx pattern for a URL.
	 * @see http://blog.mattheworiordan.com/post/13174566389/url-regular-expression-for-links-with-or-without
	 */
	private static final Pattern URL_PATTERN = Pattern.compile("^((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)$");

	/**
	 * Pattern for a character being repeated more than 3 times ("hellooooo")
	 */
	//private static final Pattern REP_CHAR_PATTERN = Pattern.compile("(\\w)\\1{3,}");

	/**
	 * Pattern which matches a string containing only of special chars
	 */
	private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("^[,.;:?!'\"+\\-*/=_#(){}\\[\\]&%$§]+$");

	/**
	 * String that delimits alternatives -- may not contain special regular expression characters!.
	 */
	private static final String ALTERNATIVE_DELIMITER = "/";
	
	/**
	 * Pattern matching for alternative (e.g. "lunch/dinner").
	 */
	private static final Pattern ALTERNATIVE_PATTERN = Pattern.compile("\\w+("+ALTERNATIVE_DELIMITER+"\\w+)+");

	/**
	 * Dictionary used for stopword removal.
	 */
	private final StopwordsDictionary stopwordsDict = StopwordsDictionary.getInstance();

	/**
	 * Dictionary used to expand abbreviations.
	 */
	private final AbbreviationsDictionary abbrevDict = AbbreviationsDictionary.getInstance();

	/**
	 * Tokenizer used to tokenize an expanded abbreviation.
	 */
	private final ITokenizer tokenizer = new TokenizerImpl();

	/**
	 * Dictionary used for spell checking & correction.
	 */
	private final SpellDictionary spellDict = SpellDictionary.getInstance();

	/**
	 * The logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(PreprocessorImpl.class);

	/**
	 * Performs the preprocessing step.
	 * @param tokens the list of raw tokens that will be manipulated by the preprocessor.
	 */
	@Override
	public void preprocess(List<String> tokens) {
		logger.debug("* Preprocessing tweet:");

		ListIterator<String> iterator = tokens.listIterator();
		String word, normalizedWord;

		while (iterator.hasNext()) {
			word = iterator.next();
			normalizedWord = normalize(word);

			logger.debug("  - preprocess normalized token '"+normalizedWord+"'");

			// (1) remove token if it's a stopword
			if (stopwordsDict.containsWord(normalizedWord)) {
				logger.debug("     --> stopword detected, remove it.");

				iterator.remove();
				continue;
			}

			// (2) replace URLs
			if (isURL(word)) {
				logger.debug("     --> URL detected, replace it with URL-token.");

				iterator.set(URL_TOKEN);
				continue;
			}

			// (3) replace usernames
			if (isUsername(word)) {
				logger.debug("     --> username detected, replace it with Username-token.");

				iterator.set(USERNAME_TOKEN);
				continue;
			}

			// (4) replace abbreviations and emoticons
			if (abbrevDict.containsWord(normalizedWord)) {
				String longForm = abbrevDict.getLongForm(normalizedWord);

				logger.debug("     --> abbreviation detected, replace it with tokens for '"+longForm+"'.");

				iterator.remove();

				// add tokens for the words in long form
				for (String ad : tokenizer.tokenize(longForm)) {
					iterator.add(ad);
				}
				continue;
			}

			// (5) remove token if it does not seem to be a useful word (e.g. just punctuation)
			if (containsOnlySpecialChars(word)) {
				logger.debug("     --> non-word token detected, remove it.");
				iterator.remove();
				continue;
			}
			
			// (6) replace alternatives (e.g. lunch/dinner) with single words
			if (isAlternative(word)) {
				logger.debug("     --> alternative string detected, split it up.");
				iterator.remove();
				
				String[] alternatives = word.split((ALTERNATIVE_DELIMITER));
				for (String alternative : alternatives) {
					iterator.add(alternative);
				}
				continue;
			}
			
			// TODO: (7) repeated chars
				// - try to condense each string consisting of repeated chars to 2 equal chars -- check if in dictionary and replace it p.r.n.
				// - if not: try to condense each string consisting of repeated chars to 1 char -- check if in dictionary and replace it p.r.n.

			// (8) replace misspelled words
			if (!spellDict.containsWord(normalizedWord)) {
				String correction = spellDict.getSuggestion(normalizedWord);
				if (correction != null) {
					String replacement = normalize(correction);
					logger.debug("     --> misspelled word detected, replace it with '"+replacement+"'");

					iterator.set(replacement);
					continue;
				}
			}
			
			// normalize the token
			iterator.set(normalizedWord);
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
	
	/**
	 * Checks if a given string contains at least one letter.
	 * @param str the string to check
	 * @return true if the string contains at least one letter, false otherwise.
	 */
	private boolean containsOnlySpecialChars(String str) {
		Matcher m = SPECIAL_CHARS_PATTERN.matcher(str);
		return m.matches();
	}
	
	/**
	 * Checks if a string expresses an alternative (e.g. lunch/dinner)
	 * @param str the string to check
	 * @return 
	 */
	private boolean isAlternative(String str) {
		Matcher m = ALTERNATIVE_PATTERN.matcher(str);
		return m.matches();
	}

	/**
	 * Performs normalization of a token.
	 * @param str the string to normalize
	 * @return the normalized string.
	 */
	private String normalize(String str) {
		return str.toLowerCase();
	}
}
