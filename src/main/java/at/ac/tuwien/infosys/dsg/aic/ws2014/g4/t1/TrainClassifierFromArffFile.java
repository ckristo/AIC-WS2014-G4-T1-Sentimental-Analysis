package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ClassifierException;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Constants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 */
public class TrainClassifierFromArffFile {
	
	/**
	 * Exit value in case of error.
	 */
	private static final int EXIT_ERROR = 1;
	/**
	 * Exit value in case of a successful run.
	 */
	private static final int EXIT_SUCCESS = 0;
	
	/**
	 * Prints usage message to stdout.
	 */
	public static void usage() {
		System.out.println(TrainClassifierFromArffFile.class.getSimpleName()+" <arff-file>");
	}
	
	/**
	 * main()
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// check arguments
		if (args.length < 1) {
			System.err.println("Not enough arguments");
			usage();
			System.exit(EXIT_ERROR);
		} else if (args.length > 1) {
			System.err.println("Too much arguments");
			usage();
			System.exit(EXIT_ERROR);
		}
		
		// check ARFF file argument
		File arffFile = new File(args[0]);
		if (!arffFile.exists()) {
			System.err.println("Given <arff-file> doesn't exist");
			usage();
			System.exit(EXIT_ERROR);
		} else if (!arffFile.isFile()) {
			System.err.println("Given <arff-file> is not a file");
			usage();
			System.exit(EXIT_ERROR);
		} else if (!arffFile.canRead()) {
			System.err.println("Given <arff-file> is not readable");
			usage();
			System.exit(EXIT_ERROR);
		}
		
		// create classifier instance
		ApplicationConfig config = null;
		try {
			InputStream is = Sentiment140DataLoader.class.getResourceAsStream(Constants.DEFAULT_CONFIG_FILE_RESOURCE);
			config = new ApplicationConfig(is);
		} catch (IOException ex) {
			System.err.println("Couldn't create classifier -- configuration file couldn't be read");
			System.exit(EXIT_ERROR);
		}
		TwitterSentimentClassifierImpl classifier = new TwitterSentimentClassifierImpl(config);
		
		// alter classifier settings -- force export and alter export file name
		classifier.setExportTrainedClassifier(true);
		
		String arffFileName = arffFile.getName().substring(0, arffFile.getName().lastIndexOf("."));
		String oldName;
		oldName = classifier.getAttributesOutputFileName();
		classifier.setAttributesOutputFileName(oldName.substring(0, oldName.indexOf(".attributes")) + "-" + arffFileName + ".attributes");
		oldName = classifier.getClassifierOuptutFileName();
		classifier.setClassifierOutputFileName(oldName.substring(0, oldName.indexOf(".classifier")) + "-" + arffFileName + ".classifier");
		
		// load ARFF file with
		try {
			classifier.loadProcessedTrainingDataFromArffFile(arffFile);
		} catch (IOException ex) {
			System.err.println("Couldn't load ARFF file: " + ex.getMessage());
			System.exit(EXIT_ERROR);
		}
		
		// execute training
		try {
			classifier.train();
		} catch (ClassifierException ex) {
			System.err.println("Couldn't train classifier: " + ex.getMessage());
			System.exit(EXIT_ERROR);
		}
		
		System.exit(EXIT_SUCCESS);
	}
	
}
