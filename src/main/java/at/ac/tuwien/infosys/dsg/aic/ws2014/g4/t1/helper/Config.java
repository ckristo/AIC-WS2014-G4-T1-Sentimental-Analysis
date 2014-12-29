package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper class for wrapping the application configuration.
 */
public class Config {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(Config.class);
	
	/**
	 * The name of the application configuration file resource.
	 */
	private static final String CONFIG_FILE = "/config.properties";
	
	/**
	 * The key for the classifier name property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_NAME = "classifier.name";
	
	/**
	 * The key for the classifier export trained data to file property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_EXPORT_TRAINED_DATA_TO_FILE = "classifier.export_trained_data_to_file";
	
	/**
	 * The key for the classifier output directory property.
	 */
	private static final String CONFIG_KEY_CLASSIFIER_OUTPUT_DIRECTORY = "classifier.output_directory";
	
	/**
	 * The singleton instance.
	 */
	private static Config instance = null;
	
	/**
	 * The properties object for the configuration file.
	 */
	private Properties properties = null;

	/**
	 * Constructor.
	 */
	private Config() {
		properties = new Properties();
	}
	
	/**
	 * Returns the configuration singleton instance.
	 * @return the configuration singleton instance.
	 */
	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
			try {
				instance.loadProperties();
			} catch (IOException ex) {
				logger.error("Couldn't load configuration properties file", ex);
			}
		}
		return instance;
	}
	
	/**
	 * Loads the configuration properties file.
	 * @throws IOException if the configuration file wasn't found or couldn't be read.
	 */
	private void loadProperties() throws IOException {
		InputStream is = Config.class.getResourceAsStream(CONFIG_FILE);
		if (is == null) {
			throw new FileNotFoundException("configuration file resource '"+CONFIG_FILE+"' couldn't be found!");
		}
		properties.load(is);
	}
	
	/**
	 * Checks if a property with a given key is present in the application configuration.
	 * @param key the property key to check
	 * @return true if the key is present, false otherwise
	 */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * Returns the value of a property with a given key as string.
	 * @param key the property key
	 * @return the set property value or null if the property key isn't present
	 */
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * Returns the value of a property with a given key as string or a provided
	 * default value if the property key is not present.
	 * @param key the property key
	 * @param defaultValue the default value to return if the key is not present
	 * @return the set property value or the provided default value if the property key isn't present
	 */
	public String getProperty(String key, String defaultValue) {
		String val = getProperty(key);
		return (val != null) ? val : defaultValue;
	}
	
	/**
	 * Returns the value of a property with a given key as boolean.
	 * @param key the property key
	 * @return the set property value or null if the property key isn't present
	 * @throws IllegalArgumentException if the value is not a valid boolean value ({0,1}, {t,f}, {true, false})
	 */
	public Boolean getPropertyAsBoolean(String key) {
		String val = properties.getProperty(key);
		if (val != null) {
			switch (val) {
				case "1" :
				case "t" :
				case "true" :
					return true;
				case "0" :
				case "f" :
				case "false" :
					return false;
				default :
					throw new IllegalArgumentException("Property value ('"+val+"') for key '"+key+"' is not a valid boolean value.");
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the value of a property with a given key as boolean or a provided
	 * default value if the property key is not present.
	 * @param key the property key
	 * @param defaultValue the default value to return if the key is not present
	 * @return the set property value or the provided default value if the property key isn't present
	 * @throws IllegalArgumentException if the value is not a valid boolean value ({0,1}, {t,f}, {true, false})
	 */
	public Boolean getPropertyAsBoolean(String key, boolean defaultValue) {
		Boolean val = getPropertyAsBoolean(key);
		return (val != null) ? val : defaultValue;
	}
	
	/**
	 * Returns the classifier name specified in the configuration file.
	 * @return the classifier name specified in the configuration file.
	 */
	public String getClassifierName() {
		return getProperty(CONFIG_KEY_CLASSIFIER_NAME);
	}
	
	/**
	 * Returns the configuration option that indicates whether to export a trained classifier to file (defaults to false).
	 * @return the configuration option that indicates whether to export a trained classifier to file (defaults to false).
	 */
	public Boolean getExportTrainedClassifierToFile() {
		return getPropertyAsBoolean(CONFIG_KEY_CLASSIFIER_EXPORT_TRAINED_DATA_TO_FILE, false);
	}
	
	/**
	 * Returns the output directory for exporting the trained classifier data (defaults to the current working directory '.')
	 * @return the output directory for exporting the trained classifier data (defaults to the current working directory '.')
	 */
	public String getClassifierOutputDirectory() {
		return getProperty(CONFIG_KEY_CLASSIFIER_OUTPUT_DIRECTORY, ".");
	}
}
