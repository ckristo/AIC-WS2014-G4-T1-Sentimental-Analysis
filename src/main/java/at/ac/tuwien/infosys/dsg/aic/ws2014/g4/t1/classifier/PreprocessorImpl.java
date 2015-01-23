package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Preprocessor implementation.
 */
public class PreprocessorImpl implements IPreprocessor {

	/**
	 * The logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(PreprocessorImpl.class);

	/**
	 * String that delimits alternatives.
	 */
	private static final String ALTERNATIVE_DELIMITER = "/";

	/**
	 * Pattern for detecting URLs.
	 */
	private static final Pattern URL_PATTERN = Pattern.compile("^((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w_-]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w/?:@_-]*))?)$");
	/**
	 * Pattern which matches words consisting of alpha chars only.
	 */
	private static final Pattern ALPHA_CHARS_PATTERN = Pattern.compile("^[A-Za-z]+$");
	/**
	 * Pattern for a character being repeated more than 3 times.
	 */
	private static final Pattern REPETITIVE_CHAR_PATTERN = Pattern.compile("(\\w)\\1{2,}");
	/**
	 * Pattern which matches a string containing only of special chars.
	 */
	private static final Pattern NONWORD_CHARS_PATTERN = Pattern.compile("^\\W+$");
	/**
	 * Pattern matching for alternatives (e.g. "lunch/dinner").
	 */
	private static final Pattern ALTERNATIVE_PATTERN = Pattern.compile("\\w+(" + ALTERNATIVE_DELIMITER + "\\w+)+");

	/**
	 * Dictionary used for stopword removal.
	 */
	private final StopwordsDictionary stopwordsDictionary = StopwordsDictionary.getInstance();
	/**
	 * Dictionary used to expand abbreviations.
	 */
	private final AbbreviationsDictionary abbreviationsDictionary = AbbreviationsDictionary.getInstance();
	/**
	 * Dictionary used for smiley detection.
	 */
	private final SmileyDictionary smileyDictionary = SmileyDictionary.getInstance();
	/**
	 * Dictionary used for spell checking & correction.
	 */
	private final SpellDictionary spellDictionary = SpellDictionary.getInstance();

	/**
	 * Tokenizer used to tokenize an expanded abbreviation.
	 */
	private final ITokenizer tokenizer = new TokenizerImpl();

	/**
	 * Performs the preprocessing step.
	 *
	 * @param tokens the list of raw tokens that will be manipulated by the
	 * preprocessor.
	 */
	@Override
	public void preprocess(List<String> tokens) {
		logger.debug("* Preprocessing tweet:");

		ListIterator<String> iterator = tokens.listIterator();
		String word, normalizedWord;

		while (iterator.hasNext()) {
			word = iterator.next();
			normalizedWord = normalize(word);

			logger.debug("  - preprocess token '" + word + "'");

			// (1) remove token if it's a stopword
			if (stopwordsDictionary.contains(normalizedWord)) {
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
				logger.debug("     --> username detected, replace it with username-token.");

				iterator.set(USERNAME_TOKEN);
				continue;
			}

			// (4) keep hashtags
			if (isHashtag(word)) {
				continue;
			}

			// (5) replace smileys with tokens
			if (smileyDictionary.contains(word)) {
				SmileyDictionary.Mood mood = smileyDictionary.getMood(word);
				String token;
				switch (mood) {
					case HAPPY:
						token = SMILEY_HAPPY_TOKEN;
						break;
					case NEUTRAL:
						token = SMILEY_NEUTRAL_TOKEN;
						break;
					case SAD:
						token = SMILEY_SAD_TOKEN;
						break;
					default:
						/* NOTREACHED */
						assert false;
						continue;
				}

				logger.debug("     --> smiley detected, replace it with " + mood + " smiley token");

				iterator.set(token);

				continue;
			}

			// (6) replace abbreviations
			if (abbreviationsDictionary.contains(word)) {
				String longForm = abbreviationsDictionary.getLongForm(word);

				logger.debug("     --> abbreviation detected, replace it with tokens for '" + longForm + "'.");

				iterator.remove();

				// add tokens for the words in long form
				for (String ad : tokenizer.tokenize(longForm)) {
					iterator.add(ad);
				}
				continue;
			}

			// (7) remove token if it consists of special chars only
			if (containsNonWordCharsOnly(word)) {
				logger.debug("     --> token consisting of non-word characters only, remove it.");
				iterator.remove();
				continue;
			}

			// (8) replace alternatives (e.g. lunch/dinner) with single words
			if (isAlternative(word)) {
				logger.debug("     --> alternative string detected, split it up.");
				iterator.remove();

				String[] alternatives = word.split((ALTERNATIVE_DELIMITER));
				for (String alternative : alternatives) {
					iterator.add(alternative);
				}
				continue;
			}

			// (9) spell correction
			if (word.length() >= 3 // ignore words that consist of less than 3 chars
					&& containsLettersOnly(word) // ignore words that don't consist of letters only
					&& !containsUpperCharsOnly(word) // ignore words that consist of upper-case letters only
					&& !spellDictionary.contains(word)) {
				String correction;

				// try to find a correction for word by condense multiple characters
				correction = findRepetitiveCharCorrection(word);
				if (correction != null) {
					String replacement = normalize(correction);
					logger.debug("     --> word with more than 3 repetitive chars detected, replace it with '" + replacement + "'");

					iterator.set(replacement);
					continue;
				}

				// try to get a spell correction suggestion
				correction = spellDictionary.getSuggestion(word);
				if (correction != null) {
					String replacement = normalize(correction);
					logger.debug("     --> misspelled word detected, replace it with '" + replacement + "'");

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
	 *
	 * @param str the string to check
	 * @return true if the string is an URL, false otherwise.
	 */
	private boolean isURL(String str) {
		Matcher m = URL_PATTERN.matcher(str);
		return m.matches();
	}

	/**
	 * Checks if a given string is a mentioned Twitter username (starting with @).
	 *
	 * @param str the string to check
	 * @return true if the string is a mentioned Twitter username, false
	 * otherwise.
	 */
	private boolean isUsername(String str) {
		return str.length() > 1 && str.startsWith("@");
	}

	/**
	 * Checks if a given string is a Twitter hash tag (starting with #).
	 *
	 * @param str the string to check
	 * @return true if the string is a Twitter hashtag, false otherwise
	 */
	private boolean isHashtag(String str) {
		return str.length() > 1 && str.startsWith("#");
	}

	/**
	 * Checks if a given string consists of alpha chars only.
	 *
	 * @param str the string to check
	 * @return true if the string consists of alpha chars only, false otherwise
	 */
	private boolean containsLettersOnly(String str) {
		Matcher m = ALPHA_CHARS_PATTERN.matcher(str);
		return m.matches();
	}

	/**
	 * Checks if a given string consists of upper-case chars only.
	 *
	 * @param str the string to check
	 * @return true if the string consists of upper-case chars only, false
	 * otherwise
	 */
	private boolean containsUpperCharsOnly(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a given string contains of non-word characters only.
	 *
	 * @param str the string to check
	 * @return true if the string consists of non-word chars only, false
	 * otherwise
	 */
	private boolean containsNonWordCharsOnly(String str) {
		Matcher m = NONWORD_CHARS_PATTERN.matcher(str);
		return m.matches();
	}

	/**
	 * Checks if a string expresses an alternative (e.g. lunch/dinner)
	 *
	 * @param str the string to check
	 * @return
	 */
	private boolean isAlternative(String str) {
		Matcher m = ALTERNATIVE_PATTERN.matcher(str);
		return m.matches();
	}

	/**
	 * Tries to find a correction by replacing repetitive characters.
	 *
	 * @param str the string to find corrections for
	 * @return the correction or null if no correction found
	 */
	private String findRepetitiveCharCorrection(String str) {
		String correctedStr;

		// TODO: try all combinations for one/two chars (e.g. looooserrrrr => loser, looserr, loserr, looser)
		
		correctedStr = REPETITIVE_CHAR_PATTERN.matcher(str).replaceAll("$1");
		if (spellDictionary.contains(correctedStr)) {
			return correctedStr;
		}

		correctedStr = REPETITIVE_CHAR_PATTERN.matcher(str).replaceAll("$1$1");
		if (spellDictionary.contains(correctedStr)) {
			return correctedStr;
		}

		return null;
	}

	/**
	 * Performs normalization of a token.
	 *
	 * @param str the string to normalize
	 * @return the normalized string.
	 */
	private String normalize(String str) {
		return str.toLowerCase();
	}
}
