package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.ClassifierModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper class for wrapping the application configuration.
 */
public class ApplicationConfig {

	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(ApplicationConfig.class);

	/**
	 * The key for the classifier name property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_NAME = "classifier.name";

	/**
	 * The key for the classifier type property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_MODEL = "classifier.model";

	/**
	 * The key for the classifier output directory property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_EXPORT_OUTPUT_DIRECTORY = "classifier.export_output_directory";

	/**
	 * The key for the classifier export trained data to file property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_EXPORT_TRAINED_CLASSIFIER = "classifier.export_trained_classifier";

	/**
	 * The key for the classifier import trained data to file property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_IMPORT_TRAINED_CLASSIFIER = "classifier.import_trained_classifier";

	/**
	 * The properties object for the configuration file.
	 */
	private final Properties properties;

	/**
	 * Constructor.
	 *
	 * @param configFile the configuration properties file to load
	 * @throws IOException
	 */
	public ApplicationConfig(File configFile) throws IOException {
		this(new FileInputStream(configFile));
	}

	/**
	 * Constructor.
	 *
	 * @param inputStream the configuration properties file as input stream to
	 * load
	 * @throws IOException
	 */
	public ApplicationConfig(InputStream inputStream) throws IOException {
		properties = new Properties();
		properties.load(inputStream);
	}

	/**
	 * Checks if a property with a given key is present in the application
	 * configuration.
	 *
	 * @param key the property key to check
	 * @return true if the key is present, false otherwise
	 */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}

	/**
	 * Returns the value of a property with a given key as string.
	 *
	 * @param key the property key
	 * @return the set property value or null if the property key isn't present
	 */
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Returns the value of a property with a given key as string or a provided
	 * default value if the property key is not present.
	 *
	 * @param key the property key
	 * @param defaultValue the default value to return if the key is not present
	 * @return the set property value or the provided default value if the
	 * property key isn't present
	 */
	public String getProperty(String key, String defaultValue) {
		String val = getProperty(key);
		return (val != null) ? val : defaultValue;
	}

	/**
	 * Returns the value of a property with a given key as boolean.
	 *
	 * @param key the property key
	 * @return the set property value or null if the property key isn't present
	 * @throws IllegalArgumentException if the value is not a valid boolean
	 * value ({0,1}, {t,f}, {true, false})
	 */
	public Boolean getPropertyAsBoolean(String key) {
		String val = properties.getProperty(key);
		if (val != null) {
			switch (val) {
				case "1":
				case "t":
				case "true":
					return true;
				case "0":
				case "f":
				case "false":
					return false;
				default:
					throw new IllegalArgumentException("Property value ('" + val + "') for key '" + key + "' is not a valid boolean value.");
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns the value of a property with a given key as boolean or a provided
	 * default value if the property key is not present.
	 *
	 * @param key the property key
	 * @param defaultValue the default value to return if the key is not present
	 * @return the set property value or the provided default value if the
	 * property key isn't present
	 * @throws IllegalArgumentException if the value is not a valid boolean
	 * value ({0,1}, {t,f}, {true, false})
	 */
	public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
		Boolean val = getPropertyAsBoolean(key);
		return (val != null) ? val : defaultValue;
	}

	/**
	 * Returns the classifier name specified in the configuration file.
	 *
	 * @return the classifier name specified in the configuration file.
	 */
	public String getClassifierName() {
		return getProperty(CONFIG_KEY_CLASSIFIER_NAME);
	}

	/**
	 * Returns the configuration option that indicates whether to export the
	 * trained classifier to file (defaults to false).
	 *
	 * @return the configuration option that indicates whether to export the
	 * trained classifier to file (defaults to false).
	 */
	public boolean getExportTrainedClassifierToFile() {
		return getPropertyAsBoolean(CONFIG_KEY_CLASSIFIER_EXPORT_TRAINED_CLASSIFIER, false);
	}

	/**
	 * Returns the configuration option that indicates whether to import a
	 * trained classifier from file (defaults to false).
	 *
	 * @return the configuration option that indicates whether to import a
	 * trained classifier from file (defaults to false).
	 */
	public boolean getImportTrainedClassifierToFile() {
		return getPropertyAsBoolean(CONFIG_KEY_CLASSIFIER_IMPORT_TRAINED_CLASSIFIER, false);
	}

	/**
	 * Returns the output directory for exporting the trained classifier data
	 * (defaults to the current working directory '.')
	 *
	 * @return the output directory for exporting the trained classifier data
	 * (defaults to the current working directory '.')
	 */
	public File getClassifierExportDirectory() {
		return new File(getProperty(CONFIG_KEY_CLASSIFIER_EXPORT_OUTPUT_DIRECTORY, "."));
	}

	/**
	 * Returns the class of the classifier type to use according to the
	 * configuration.
	 *
	 * @return the class of the classifier type to use according to the
	 * configuration or null if configuration option is not set or is set to an
	 * invalid class.
	 */
	public ClassifierModel getClassifierModel() {
		String modelName = getProperty(CONFIG_KEY_CLASSIFIER_MODEL);
		if (modelName != null) {
			try {
				return ClassifierModel.valueOf(modelName);
			} catch (IllegalArgumentException ex) {
				logger.warn("Illegal classifier model supplied in application config", ex);
				return null;
			}
		}
		return null;
	}
}
