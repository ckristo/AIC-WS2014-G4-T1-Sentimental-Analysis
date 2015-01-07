package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SentiWordNet {
	private static final Logger logger = LogManager.getLogger("SentimentClassifier");

	public static List<SentiData> readFile(String path) {
		ClassLoader classLoader = SentiWordNet.class.getClassLoader();
		File file = new File(classLoader.getResource(path).getFile());

		List<SentiData> ds = new ArrayList<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			int i = 0;
			while((line = in.readLine()) != null && i < 30000) {
				line = line.trim();
				if(line.startsWith("#")) continue;	// skip comments
				String[] parts = line.split("\t");
				double posScore = Double.parseDouble(parts[2]),
						negScore = Double.parseDouble(parts[3]);
				String[] terms = parts[4].split(" ");
				//System.out.printf("%.3f %.3f ", posScore, negScore);
				for(String t : terms) {
					double score = ((posScore-negScore)+1.0)*0.5;
					//System.out.print(t.split("#")[0]+" "+score+" ");
					ArrayList<String> tl = new ArrayList<>();
					tl.add(t.split("#")[0]);
					ds.add(new SentiData(tl, score));
				}
				//System.out.println("");
				i++;
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		logger.info("successfully loaded SentiWordNet dataset "+path);
		return ds;
	}
}
