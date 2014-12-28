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
	public static final String CONFIG_FILE = "/config.properties";
	
	/**
	 * The properties object for the configuration file.
	 */
	private static Properties properties = null;
	
	/**
	 * Load configuration file as properties object.
	 */
	static {
		InputStream is = Config.class.getResourceAsStream(CONFIG_FILE);
		try {
			if (is == null) {
				throw new FileNotFoundException("configuration file resource '"+CONFIG_FILE+"' couldn't be found!");
			}
			
			Properties prop = new Properties();
			prop.load(is);
			
			properties = prop;
		} catch (IOException ex) {
			logger.error("Couldn't read configuration file!", ex);
		}
	}
	
	/**
	 * Checks if the application configuration properties file was loaded successfully.
	 * @throws IllegalStateException -- if the application configuration properties file wasn't loaded successfully.
	 */
	private static void checkState() {
		if (properties == null) {
			throw new IllegalStateException("Configuration properties file wasn't loaded successfully");
		}
	}
	
	/**
	 * Checks if a property with a given key is present in the application config.
	 * @param key the property key to check
	 * @return true if the key is present, false otherwise
	 */
	public static boolean hasProperty(String key) {
		checkState();
		return properties.containsKey(key);
	}
	
	/**
	 * Returns the value of a property with a given key as string.
	 * @param key the property key
	 * @return the set property value or null if the property key isn't present
	 */
	public static String getProperty(String key) {
		checkState();
		return properties.getProperty(key);
	}
	
	/**
	 * Returns the value of a property with a given key as string or a provided
	 * default value if the property key is not present.
	 * @param key the property key
	 * @param defaultValue the default value to return if the key is not present
	 * @return the set property value or the provided default value if the property key isn't present
	 */
	public static String getProperty(String key, String defaultValue) {
		String val = getProperty(key);
		return (val != null) ? val : defaultValue;
	}
	
	/**
	 * Returns the value of a property with a given key as boolean.
	 * @param key the property key
	 * @return the set property value or null if the property key isn't present
	 * @throws IllegalArgumentException if the value is not a valid boolean value ({0,1}, {t,f}, {true, false})
	 */
	public static Boolean getPropertyAsBoolean(String key) {
		checkState();
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
	public static Boolean getPropertyAsBoolean(String key, boolean defaultValue) {
		Boolean val = getPropertyAsBoolean(key);
		return (val != null) ? val : defaultValue;
	}
}
