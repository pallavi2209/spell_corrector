package edu.stanford.cs276.util;

import java.io.Serializable;
import java.util.HashMap;

public class BigramDictionary implements Serializable {

	private int termCount;
	private HashMap<Long, Integer> map;

	public int termCount() {
		return termCount;
	}

	public BigramDictionary() {
		termCount = 0;
		map = new HashMap<Long, Integer>();
	}

	public void add(long bigram) {

		termCount++;
		if (map.containsKey(bigram)) {
			map.put(bigram, map.get(bigram) + 1);
		} else {
			map.put(bigram, 1);
		}
	}

	public int count(String bigram, WordDictionary wordToId) {
		String[] bigramTerms  = bigram.split(" ");
		String term1 = bigramTerms[0];
		String term2 = bigramTerms[1];
		int tId1 = wordToId.getId(term1);
		int tId2 = wordToId.getId(term2);
		int vocabSize = wordToId.getMap().size();
		long bId;
		if ( tId1 < 0 || tId1 > vocabSize || tId2 < 0 || tId2 > vocabSize){ return 0;}
			bId = (long)tId1+ (long)wordToId.getMap().size()*(long)tId2;
		if (map.containsKey(bId)) {
			return map.get(bId);
		} else {
			return 0;
		}
	}


	public HashMap<Long, Integer> getMap(){
		return map;
	}
}
