package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AbbreviationDictionary {

	/**
	 * The file to load the abbreviations from.
	 */
	private static final String ABBREVIATION_DICT_FILE = "/abbreviations.txt";
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger("AbbreviationsDictionary");
	
	/**
	 * The set instance containing all (loaded) abbreviations.
	 */
	private final HashMap<String, String> abbreviations = new HashMap<String, String>();
	
	/**
	 * The singleton instance.
	 */
	private static AbbreviationDictionary instance = null;
	
	/**
	 * Constructor.
	 */
	private AbbreviationDictionary() {
	}
	
	/**
	 * Returns the abbreviations dictionary instance.
	 * @return the abbreviations dictionary instance.
	 */
	public static AbbreviationDictionary getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}
	
	
	/**
	 * Checks whether a given string is a abbreviation, if so the full word is returned.
	 * @param str the string to check
	 * @return full word if the string is a abbreviation (exact match) or str otherwise.
	 */
	public String getAbbreviation(String str) {
		if(abbreviations.containsKey(str.toUpperCase()))
			return abbreviations.get(str.toUpperCase());
		else
			return str;
	}
	
	/**
	 * Performs init of the abbreviations dictionary.
	 */
	private static void init() {
		instance = new AbbreviationDictionary();
		try {
			instance.loadFile();
		} catch (IOException ex) {
			logger.error("Couldn't load abbreviations dictionary file", ex);
		}
	}
	
	/**
	 * Loads the abbreviation dictionary file into the abbreviation set.
	 * @throws 
	 *   - FileNotFoundException if the abbreviation dictionary file doesn't exist
	 *   - IOException if the abbreviation dictionary file couldn't be read
	 */
	private void loadFile() throws IOException {
		// create stream for resource file
		InputStream is = PreprocessorImpl.class.getResourceAsStream(ABBREVIATION_DICT_FILE);
		if (is == null) {
			throw new FileNotFoundException("Abbreviation dictionary file doesn't exist.");
		}
		// read file line by line
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line ="";
		try{
		while ((line = reader.readLine()) != null) {
			if(!line.matches("\\s*"))
				abbreviations.put(line.split("---")[0], line.split("---")[1]);
		}
		} 
		catch(Exception e) 
		{
			System.out.println(line);
			}
	}
}
