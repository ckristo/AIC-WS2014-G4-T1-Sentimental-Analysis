package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

/**
 * An enumeration for the sentiment classes.
 */
public enum Sentiment {
	POSITIVE {
		@Override
		public String toString() {
			return "positive";
		}
	},
	NEGATIVE {
		@Override
		public String toString() {
			return "negative";
		}
	},
	NEUTRAL {
		@Override
		public String toString() {
			return "neutral";
		}
	},
}
