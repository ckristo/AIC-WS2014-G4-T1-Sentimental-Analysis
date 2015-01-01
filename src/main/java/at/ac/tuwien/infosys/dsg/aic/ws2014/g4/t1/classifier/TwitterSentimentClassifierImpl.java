package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.io.File;
import java.io.IOException;
import java.util.*;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.IPreprocessor;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.ITokenizer;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.PreprocessorImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.TokenizerImpl;
import twitter4j.Status;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.core.*;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 * Class implementing our custom Twitter sentiment detection classifier.
 */
public class TwitterSentimentClassifierImpl implements ITwitterSentimentClassifier {
	
	// general TODOs:
	// * ( add other features )
	// ...

	private Classifier cls = null;
	private Attribute attrSentiment;
	private Instances ts;

	private List<SentiData> traindata = new ArrayList<>();

	public void addTrainingData(Map<Status, Double> trainingSet) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		ITokenizer tokenizer = new TokenizerImpl();
		IPreprocessor preproc = new PreprocessorImpl();

		for(Map.Entry<Status, Double> entry : trainingSet.entrySet()) {
			String text = entry.getKey().getText();
			List<String> twtoks = tokenizer.tokenize(text);

			preproc.preprocess(twtoks);
			List<String> filtered = new ArrayList<>();
			for(String t : twtoks) {
				if(t != null && !t.equals("__++USERNAME++__") && !t.equals("H")) filtered.add(t.toLowerCase());
			}
			traindata.add(new SentiData(filtered, entry.getValue()));
		}
	}

	public void addTrainingWords(Map<String, Double> trainingSet) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		//TODO
		throw new IllegalStateException("not yet implemented");
	}

	@Override
	public void addSentiData(List<SentiData> trainingData) throws IllegalStateException {
		if(cls != null)
			throw new IllegalStateException("classifier has already been trained");

		traindata.addAll(trainingData);
		System.out.printf("added %d SentiData entries\n", trainingData.size());
	}

	@Override
	public void train() throws ClassifierException {
		FastVector sentiments = new FastVector();
		for(Integer i = 0; i <= 10; i++) {
			sentiments.addElement("s"+i.toString());
			System.out.println("sentiment class " + i.toString());
		}
		attrSentiment = new Attribute("__sentiment__", sentiments);

		Set<String> tokens = new HashSet<>();
		for(SentiData sd : traindata) {
			tokens.addAll(sd.getTokens());
		}

		// build our own hash map with the attribute indices (WEKA performs a
		// linear search otherwise...)
		Map<String, Integer> attrmap = new HashMap<>();

		FastVector attrs = new FastVector(tokens.size()+1);
		attrs.addElement(attrSentiment);

		int i = 1;
		for(String t : tokens) {
			if(t == null) continue;
			Attribute attr = new Attribute(t);
			attrs.addElement(attr);
			attrmap.put(t, i);
			i++;
		}


		ts = new Instances("twitter-sentiments", attrs, traindata.size());
		ts.setClassIndex(0);

		System.out.printf("training using sparse instances with %d attributes\n", ts.numAttributes());

		double[] defaults = new double[ts.numAttributes()];
		Arrays.fill(defaults, 0.0);

		for(SentiData sd : traindata) {
			List<String> twtoks = sd.getTokens();

			SparseInstance inst = new SparseInstance(ts.numAttributes());
			inst.setDataset(ts);

			Integer snt = new Integer((int)Math.round(sd.getSentiment() * 10.0));
			inst.setValue(attrSentiment, "s"+snt.toString());
			inst.setDataset(ts);
			//System.out.println("adding tokens "+StringUtils.join(twtoks, ", ")+" with sentiment "+snt);

			// set contained tokens to 1
			for(String t : twtoks) {
				if(t == null) continue;
				inst.setValue(attrmap.get(t), 1.0);
			}
			// set all other tokens to 0
			inst.replaceMissingValues(defaults);

			//System.out.println("instance: "+inst);

			//inst.setWeight(10);	//TODO: custom weight, e.g. for manually classified training data
			ts.add(inst);
		}

		ts.setClassIndex(0);	// class is the sentiment

		System.out.println("building classifier...");

		cls = new IBk();
		try {
			cls.buildClassifier(ts);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}

		/*System.out.println("evaluating classifier...");

		int split = (int)(ts.numInstances()*0.7);
		Instances train = new Instances(ts, 0, split);
		train.setClassIndex(0);
		Instances test = new Instances(ts, split+1, ts.numInstances()-split-1);
		test.setClassIndex(0);

		Evaluation eval = null;
		try {
			eval = new Evaluation(train);
			eval.evaluateModel(cls, test);
		} catch (Exception e) {
			throw new ClassifierException(e);
		}

		System.out.println(eval.toSummaryString());*/
	}

	@Override
	public void save(String path) throws IllegalStateException, IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(ts);
		saver.setFile(new File(path));
		saver.writeBatch();
	}

	@Override
	public void load(String path) throws IOException, ClassifierException {
		ArffLoader loader = new ArffLoader();
		loader.setFile(new File(path));
		ts = loader.getDataSet();
		ts.setClassIndex(0);

		attrSentiment = ts.attribute(0);

		cls = new IBk();
		try {
			cls.buildClassifier(ts);
		} catch(Exception e) {
			throw new ClassifierException(e);
		}
	}

	@Override
	public double classify(Status tweet) throws IllegalStateException, ClassifierException {
		if(cls == null)
			throw new IllegalStateException("classifier has not been trained");

		ITokenizer tokenizer = new TokenizerImpl();
		String text = tweet.getText();
		List<String> twtoks = tokenizer.tokenize(text);
		IPreprocessor preproc = new PreprocessorImpl();
		preproc.preprocess(twtoks);

		Instances testset = new Instances(ts, 1);

		Instance inst = new SparseInstance(testset.numAttributes());
		inst.setDataset(testset);

		System.out.println("tweet: "+tweet.getText());
		System.out.print("classify: ");
		for(String t : twtoks) {
			if(t == null) continue;
			System.out.print(t.toLowerCase());
			Attribute attr = testset.attribute(t.toLowerCase());
			if(attr != null) {
				inst.setValue(attr, 1.0);
				System.out.print(" [exists]");
			}
			System.out.print(", ");
		}
		System.out.println("");
		double[] defaults = new double[inst.numAttributes()];
		Arrays.fill(defaults, 0.0);
		inst.replaceMissingValues(defaults);

		System.out.println("instance: "+inst);
		testset.add(inst);
		testset.setClassIndex(0);

		try {
			double sclass = cls.classifyInstance(testset.instance(0));
			System.out.println("class: "+testset.classAttribute().value((int)sclass));
			return sclass/(attrSentiment.numValues()-1);
		} catch (Exception e) {
			throw new ClassifierException(e);
		}
	}

	@Override
	public double classify(Set<Status> testSet) throws IllegalStateException, ClassifierException {
		double sum = 0.0, wsum = 0.0;
		for(Status s : testSet) {
			//TODO: improve this primitive weighting
			double weight = 1+s.getUser().getFollowersCount()*0.0001;
			sum += classify(s)*weight;
			wsum += weight;
		}
		return wsum > 0 ? sum/wsum : Double.NaN;
	}

}
