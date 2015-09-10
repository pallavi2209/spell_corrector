package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import edu.stanford.cs276.util.Dictionary;

public class EmpiricalCostModel implements EditCostModel{
	
    private  Dictionary substitutions;
    private  Dictionary insertions;
    private  Dictionary transpositions;
    private  Dictionary deletions;
    private  Dictionary unigramChars;
    
    String findEdits(String noisy, String clean, int distance, String edits, char prev) {
	if (noisy.equals(clean)) return edits;
	if ((noisy.length() > 0) && (clean.length() > 0) && (noisy.charAt(0) == clean.charAt(0))) 
	    return findEdits(noisy.substring(1), clean.substring(1), distance, edits, noisy.charAt(0));
	if (distance == 0) return null;
	String insert = null;
	if (noisy.length() > 0) 
	    insert = findEdits(noisy.substring(1), clean, distance - 1, edits + "insert:" + prev + noisy.charAt(0) + ",", prev);
	String delete = null;
	if (clean.length() > 0)
	    delete = findEdits(noisy,  clean.substring(1), distance - 1, edits + "delete:" + prev + clean.charAt(0) + ",", clean.charAt(0));
	String sub = null;
	if (noisy.length() > 0 && clean.length() > 0)
	    sub = findEdits(noisy.substring(1), clean.substring(1), distance - 1, edits + "sub:" + clean.charAt(0) + noisy.charAt(0) + ",", clean.charAt(0));
	String trans = null;
	if ((noisy.length() > 1) && (clean.length() > 1))
	    trans = findEdits(noisy.substring(1, 2) + noisy.substring(0, 1) + noisy.substring(2), clean, distance - 1, edits + "trans:" + noisy.substring(0, 2) + ",", prev);
	int numPaths = 0;
	if (insert != null) numPaths++;
	if (delete != null) numPaths++;
	if (sub != null) numPaths++;
	if (trans != null) numPaths++;
	if (numPaths > 1) {
	    String best = insert;
	    double bestProb = getProb(insert);
	    double prob = getProb(delete);
	    if (prob > bestProb) {
		bestProb = prob;
		best = delete;
	    }
	    prob = getProb(sub);
	    if (prob > bestProb) {
		bestProb = prob;
		best = sub;
	    }
	    prob = getProb(trans);
	    if (prob > bestProb) {
		bestProb = prob;
		best = trans;
	    }
	    return best;
	} else if (numPaths == 1) {
	    if (insert != null) return insert;
	    if (delete != null) return delete;
	    if (sub != null) return sub;
	    if (trans != null) return trans;
	}
	return null;
    }

    private void updateDictionaries(String noisy, String edits) {
	for (int i = 0; i < noisy.length(); i++)
	    unigramChars.add(noisy.substring(i, i + 1));
	for (String edit : edits.split(",")) {
	    String[] parsed = edit.split(":");
	    if ("insert".equals(parsed[0])) insertions.add(parsed[1]);
	    if ("delete".equals(parsed[0])) deletions.add(parsed[1]);
	    if ("sub".equals(parsed[0])) substitutions.add(parsed[1]);
	    if ("trans".equals(parsed[0])) transpositions.add(parsed[1]);
	}
    }

    private double getProb(String edits) {
	double prob = 0.0;
	if (edits == null) return Double.NEGATIVE_INFINITY;
	for (String edit: edits.split(",")) {
	    String[] parsed = edit.split(":");
	    if ("insert".equals(parsed[0]))
		prob += Math.log10(insertions.count(parsed[1]) + 1) - Math.log10(unigramChars.count(parsed[1].substring(0, 1)) + unigramChars.vocabSize());
	    if ("delete".equals(parsed[0]))
		prob += Math.log10(deletions.count(parsed[1]) + 1) - Math.log10(unigramChars.count(parsed[1].substring(0, 1)) + unigramChars.vocabSize());
	    if ("sub".equals(parsed[0]))
		prob += Math.log10(substitutions.count(parsed[1]) + 1) - Math.log10(unigramChars.count(parsed[1].substring(0, 1)) + unigramChars.vocabSize());
	    if ("trans".equals(parsed[0]))
		prob += Math.log10(transpositions.count(parsed[1]) + 1) - Math.log10(unigramChars.count(parsed[1].substring(0, 1)) + unigramChars.vocabSize());
	}
	return prob;
    } 

    
    public EmpiricalCostModel(String editsFile) throws IOException {

	substitutions = new Dictionary();
	insertions = new Dictionary();
	transpositions = new Dictionary();
	deletions = new Dictionary();
	unigramChars = new Dictionary();

	BufferedReader input = new BufferedReader(new FileReader(editsFile));
	System.out.println("Constructing edit distance map...");
	String line = null;
	while ((line = input.readLine()) != null) {
	    Scanner lineSc = new Scanner(line);
	    lineSc.useDelimiter("\t");
	    String noisy = lineSc.next();
	    String clean = lineSc.next();
	    if (noisy.equals(clean)) continue;
	    String edits = findEdits(noisy, clean, 1, "", '$');
	    if (edits == null) continue;
	    updateDictionaries(noisy, edits);
	}
	input.close();
	System.out.println("Done.");
    }
    
    // You need to update this to calculate the proper empirical cost
    @Override
    public double editProbability(String original, String R, int distance) {
	if (distance == 0) return 0.925;
	String edits = findEdits(original, R, distance, "", '$');
	return getProb(edits);
    }

}
