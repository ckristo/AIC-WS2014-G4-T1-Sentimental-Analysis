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
		public Double toDouble() {
			return 1.0;
		}
	},
	NEGATIVE {
		@Override
		public String toString() {
			return "negative";
		}
		public Double toDouble() {
			return 0.0;
		}
	},
	NEUTRAL {
		@Override
		public String toString() {
			return "neutral";
		}
		public Double toDouble() {
			return 0.5;
		}
	};

	abstract public Double toDouble();
}
