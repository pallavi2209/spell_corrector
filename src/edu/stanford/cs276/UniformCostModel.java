package edu.stanford.cs276;

import edu.stanford.cs276.EditCostModel;

public class UniformCostModel implements EditCostModel {
	static double assumedProb = 0.05;
	
	@Override
	public double editProbability(String original, String R, int distance) {
		double probabilityEdit = 0;
		
		if(distance == 0){
		    probabilityEdit = Math.log10(0.925);
		}else if(distance == 1){
		    probabilityEdit = Math.log10(assumedProb);
		}else if(distance == 2){
		    probabilityEdit = 2 * Math.log10(assumedProb);
		}
		
		return probabilityEdit;
	}
}
