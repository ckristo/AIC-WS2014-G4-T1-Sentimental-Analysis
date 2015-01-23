package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

/**
 * Abstract class for common Twitter Sentiment classifier functionality.
 */
public abstract class AbstractTwitterSentimentClassifier implements ITwitterSentimentClassifier {

	/**
	 * Tokenizer used for Tweet processing.
	 */
	protected final ITokenizer tokenizer = new TokenizerImpl();
	
	/**
	 * Preprocessor used for Tweet processing.
	 */
	protected final IPreprocessor preprocessor = new PreprocessorImpl();

	@Override
	public Sentiment getSentiment(double[] probabilities) {
		if (probabilities[Sentiment.NEGATIVE.ordinal()] > 0.5) {
			return Sentiment.NEGATIVE;
		} else if (probabilities[Sentiment.POSITIVE.ordinal()] > 0.5) {
			return Sentiment.POSITIVE;
		} else {
			return Sentiment.NEUTRAL;
		}
	}

}
