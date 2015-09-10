package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RunCorrector {

	public static LanguageModel languageModel;
	public static NoisyChannelModel nsm;
	

	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		// Parse input arguments
		String uniformOrEmpirical = null;
		String queryFilePath = null;
		String goldFilePath = null;
		String extra = null;
		BufferedReader goldFileReader = null;
		if (args.length == 2) {
			// Run without extra and comparing to gold
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
		}
		else if (args.length == 3) {
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
			if (args[2].equals("extra")) {
				extra = args[2];
			} else {
				goldFilePath = args[2];
			}
		} 
		else if (args.length == 4) {
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
			extra = args[2];
			goldFilePath = args[3];
		}
		else {
			System.err.println(
					"Invalid arguments.  Argument count must be 2, 3 or 4" +
					"./runcorrector <uniform | empirical> <query file> \n" + 
					"./runcorrector <uniform | empirical> <query file> <gold file> \n" +
					"./runcorrector <uniform | empirical> <query file> <extra> \n" +
					"./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt extra \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
			return;
		}
		
		
		
		if (goldFilePath != null ){
			goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
		}
		
		// Load models from disk
		languageModel = LanguageModel.load(); 
		nsm = NoisyChannelModel.load();
		BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
		nsm.setProbabilityType(uniformOrEmpirical);
		
		int totalCount = 0;
		int yourCorrectCount = 0;
		String query = null;
		boolean considerExtra = false;
		if ("extra".equals(extra)) {
			considerExtra = true;
		}
		
		/*
		 * Each line in the file represents one query.  We loop over each query and find
		 * the most likely correction
		 */
		while ((query = queriesFileReader.readLine()) != null) {
			
			String correctedQuery = query;
			Map<String, Double> mapCandProb = new HashMap<String, Double>();
			
			CandidateGenerator cg = CandidateGenerator.get();
			
			if(cg.checkUnigramsInDict(query)){
				double score = calcScore(query, query, 0, considerExtra);
				mapCandProb.put(query, score);
			}
			
			Set<String> allOneEdits = new HashSet<String>();
			cg.computeExhaustiveOneEdits(query, allOneEdits);
			
			Set<String> oneEditCandidates  = new HashSet<String>();
			oneEditCandidates = cg.getOneEditCandidates(allOneEdits, oneEditCandidates);
			for (String string : oneEditCandidates) {
				double score = calcScore(query, string, 1, considerExtra);
				if (mapCandProb.containsKey(string)) {
					double s = mapCandProb.get(string);
					if (Double.compare(score, s) > 0) {
						mapCandProb.put(string, score);
					}
				} else {
					mapCandProb.put(string, score);
				}
			
			}
			// prune allOneEdits before doing 1-more edit to find 2-edits.
			// send prunedAllOneEdit instead of allOneEdits
			Set<String> prunedOneEditCandidates = new HashSet<String>();
			prunedOneEditCandidates = cg.getPrunedOneEditCandidates(allOneEdits, prunedOneEditCandidates);
			
			Set<String> twoEditCandidates = new HashSet<String>();
			twoEditCandidates = cg.getTwoEditCandidates(prunedOneEditCandidates, twoEditCandidates);
			for (String string : twoEditCandidates) {
				double score = calcScore(query, string, 2, considerExtra);

				if (mapCandProb.containsKey(string)) {
					double s = mapCandProb.get(string);
					if (Double.compare(score, s) > 0) {
						mapCandProb.put(string, score);
					}
				} else {
					mapCandProb.put(string, score);
				}
			}

			double maxValueInMap = Double.NEGATIVE_INFINITY;
			String keyForMaxValue = null;
			for (java.util.Map.Entry<String, Double> entry: mapCandProb.entrySet() ) {
				int comp = Double.compare(entry.getValue(), maxValueInMap);
				if ( comp > 0){
					maxValueInMap = entry.getValue();
					keyForMaxValue = entry.getKey();
				}
			}
			if(keyForMaxValue!= null){
			correctedQuery = keyForMaxValue.replaceAll("\\s+", " ").trim();
			}
			
			String goldQuery = null;

			// If a gold file was provided, compare our correction to the gold correction
			// and output the running accuracy
			if (goldFileReader != null) {
				goldQuery = goldFileReader.readLine();
				if (goldQuery.equals(correctedQuery)) {
					yourCorrectCount++;
				} 
				totalCount++;
			}
			
			System.out.println(correctedQuery);

		}
		queriesFileReader.close();
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;

		//System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");
		//System.out.println(((float) yourCorrectCount*100.0 / (float) totalCount));

	}

    private static double calcScore(String query, String candidate, int editDistance, boolean considerExtra) {
	double nsmProb = nsm.editProbability(query, candidate, editDistance);
	double lmProb;
	if(considerExtra){
	    lmProb = languageModel.getSmoothedProbability(candidate);
	}else{
	    lmProb = languageModel.getCandProbability(candidate);
	}
	double mu  = 1.0;
	double score = nsmProb+mu*lmProb;
	return score;
    }
}
