package edu.stanford.cs276;


public class BuildModels {

	public static double MU = .05;
	public static LanguageModel languageModel;
	public static NoisyChannelModel noisyChannelModel;

	public static void main(String[] args) throws Exception {

		String trainingCorpus = null;
		String editsFile = null;
		String extra = null;
		if (args.length == 2 || args.length == 3) {
			trainingCorpus = args[0];
			editsFile = args[1];
			if (args.length == 3) extra = args[2];
		} else {
			System.err.println(
					"Invalid arguments.  Argument count must 2 or 3" + 
							"./buildmodels <training corpus dir> <training edit1s file> \n" + 
							"./buildmodels <training corpus dir> <training edit1s file> <extra> \n" + 
							"SAMPLE: ./buildmodels data/corpus data/edit1s.txt \n" +
							"SAMPLE: ./buildmodels data/corpus data/edit1s.txt extra \n"
					);
			return;
		}
		System.out.println("training corpus: " + args[0]);
		boolean considerExtra = false;
		if ("extra".equals(extra)) {
			considerExtra = true;
		}
		
		languageModel =  LanguageModel.create(trainingCorpus, considerExtra);
		noisyChannelModel = NoisyChannelModel.create(editsFile);

		// Save the models to disk
		noisyChannelModel.save();
		languageModel.save();
	}
}
