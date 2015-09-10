//package edu.stanford.cs276;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//
//public class RunCorrectorChangeParameter {
//
//	public static LanguageModel languageModel;
//	public static NoisyChannelModel nsm;
//	
//
//	public static void main(String[] args) throws Exception {
//		
//		long startTime = System.currentTimeMillis();
//		
//		// Parse input arguments
//		String uniformOrEmpirical = null;
//		String queryFilePath = null;
//		String goldFilePath = null;
//		String extra = null;
//		Double par = null;
//		BufferedReader goldFileReader = null;
//		if (args.length == 2) {
//			// Run without extra and comparing to gold
//			uniformOrEmpirical = args[0];
//			queryFilePath = args[1];
//		}
//		else if (args.length == 3) {
//			uniformOrEmpirical = args[0];
//			queryFilePath = args[1];
//			if (args[2].equals("extra")) {
//				extra = args[2];
//			} else {
//				goldFilePath = args[2];
//			}
//		} 
//		else if (args.length == 4) {
//			uniformOrEmpirical = args[0];
//			queryFilePath = args[1];
//			if (args[2].equals("extra")) {
//				extra = args[2];
//				goldFilePath = args[3];
//			} else {
//				goldFilePath = args[2];
//				par = Double.valueOf(args[3]);
//			}
//			
//		}
//		else {
//			System.err.println(
//					"Invalid arguments.  Argument count must be 2, 3 or 4" +
//					"./runcorrector <uniform | empirical> <query file> \n" + 
//					"./runcorrector <uniform | empirical> <query file> <gold file> \n" +
//					"./runcorrector <uniform | empirical> <query file> <extra> \n" +
//					"./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n" +
//					"SAMPLE: ./runcorrector empirical data/queries.txt \n" +
//					"SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n" +
//					"SAMPLE: ./runcorrector empirical data/queries.txt extra \n" +
//					"SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
//			return;
//		}
//		
//		
//		
//		if (goldFilePath != null ){
//			goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
//		}
//		
//		// Load models from disk
//		languageModel = LanguageModel.load(); 
//		nsm = NoisyChannelModel.load();
//		BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
//		nsm.setProbabilityType(uniformOrEmpirical);
//		
//		if( par != null)
//			languageModel.lamda = par;
//		
//		int totalCount = 0;
//		int yourCorrectCount = 0;
//		String query = null;
//		
//		/*
//		 * Each line in the file represents one query.  We loop over each query and find
//		 * the most likely correction
//		 */
//		while ((query = queriesFileReader.readLine()) != null) {
//			
//			String correctedQuery = query;
//			Map<String, Double> mapCandProb = new HashMap<String, Double>();
//			
//			CandidateGenerator cg = CandidateGenerator.get();
//			
//			if(cg.checkUnigramsInDict(query)){
//				double score = calcScore(query, query, 0);
//				mapCandProb.put(query, score);
//			}
//			Set<String> oneEditCandidates = cg.computeQuickOneEdits(query);
//			for (String string : oneEditCandidates) {
//				double score = calcScore(query, string, 1);
//				if (mapCandProb.containsKey(string)) {
//					double s = mapCandProb.get(string);
//					if (Double.compare(score, s) > 0) {
//						mapCandProb.put(string, score);
//					}
//				} else {
//					mapCandProb.put(string, score);
//				}
//			}
//			
//			Set<String> twoEditCandidates = cg.getTwoEditCandidates(query);
//			for (String string : twoEditCandidates) {
//				double score = calcScore(query, string, 2);
//				if (mapCandProb.containsKey(string)) {
//					double s = mapCandProb.get(string);
//					if (Double.compare(score, s) > 0) {
//						mapCandProb.put(string, score);
//					}
//				} else {
//					mapCandProb.put(string, score);
//				}
//			}
//
//			double maxValueInMap = Double.NEGATIVE_INFINITY;
//			String keyForMaxValue = null;
//			for (java.util.Map.Entry<String, Double> entry: mapCandProb.entrySet() ) {
//				int comp = Double.compare(entry.getValue(), maxValueInMap);
//				if ( comp > 0){
//					maxValueInMap = entry.getValue();
//					keyForMaxValue = entry.getKey();
//				}
//			}
//			
//			correctedQuery = keyForMaxValue.replaceAll("\\s+", " ").trim();
//			
//			if ("extra".equals(extra)) {
//				/*
//				 * If you are going to implement something regarding to running the corrector, 
//				 * you can add code here. Feel free to move this code block to wherever 
//				 * you think is appropriate. But make sure if you add "extra" parameter, 
//				 * it will run code for your extra credit and it will run you basic 
//				 * implementations without the "extra" parameter.
//				 */	
//			}
//			
//
//			// If a gold file was provided, compare our correction to the gold correction
//			// and output the running accuracy
//			if (goldFileReader != null) {
//				String goldQuery = goldFileReader.readLine();
//				if (goldQuery.equals(correctedQuery)) {
//					yourCorrectCount++;
//				}
//				totalCount++;
//			}
////			System.out.println(correctedQuery 
////					+ ", yourCorrectCount=" + yourCorrectCount 
////					+ ", totalCount=" + totalCount 
////					+ " [" + ((float) yourCorrectCount*100.0 / (float) totalCount) + "%]");
//		}
//		queriesFileReader.close();
//		long endTime   = System.currentTimeMillis();
//		long totalTime = endTime - startTime;
//		// System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");
//		System.out.println( ((float) yourCorrectCount*100.0 / (float) totalCount));
//
//	}
//
//
//	private static double calcScore(String query, String candidate, int editDistance) {
//		double nsmProb = nsm.uniformCostModel.editProbability(candidate, query,editDistance);
//		double lmProb = languageModel.getCandProbability(candidate);
//		int parameter = 1;
//		double score = Math.log10(nsmProb)+parameter*lmProb;
//		//System.out.println("Query=" + query + ", candidate=" + candidate + ", editDistance=" + editDistance + ", nsmProb=" + nsmProb + ", lmProb=" + lmProb + ", score=" + score);
//		return score;
//	}
//}
