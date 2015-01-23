package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.List;

/**
 * Preprocessor interface.
 */
public interface IPreprocessor {

	/**
	 * Token for a username.
	 */
	public static final String USERNAME_TOKEN = "__++USERNAME++__";
	/**
	 * Token for a URL.
	 */
	public static final String URL_TOKEN = "__++URL++__";
	/**
	 * Token for a happy smiley.
	 */
	public static final String SMILEY_HAPPY_TOKEN = "__++SMILEY_HAPPY++__";
	/**
	 * Token for a neutral smiley.
	 */
	public static final String SMILEY_NEUTRAL_TOKEN = "__++SMILEY_NEUTRAL++__";
	/**
	 * Token for a sad smiley.
	 */
	public static final String SMILEY_SAD_TOKEN = "__++SMILEY_SAD++__";

	/**
	 * Performs the preprocessing step.
	 *
	 * @param tokens the list of raw tokens that will be manipulated by the
	 * preprocessor.
	 */
	public void preprocess(List<String> tokens);

}
