package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessor;

import java.util.List;

import cmu.arktweetnlp.Twokenize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenizerImpl implements ITokenizer {

	/**
	 * The logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(TokenizerImpl.class);
	
	/**
	 * Performs tokenization of an input string using the Twokenize tokenizer.
	 * @param string the input string.
	 * @return a list of tokens strings.
	 */
	@Override
	public List<String> tokenize(String string) {
		List<String> tokens = Twokenize.tokenize(string);
		
		logger.debug("* Tokenize tweet:");
		logger.debug("  - input: '"+string+"'");
		logger.debug("  - tokens: '"+tokens+"'");
		
		return tokens;
	}
	
}
