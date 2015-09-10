package edu.stanford.cs276.util;

import java.io.Serializable;
import java.util.HashMap;

public class UnigramDictionary implements Serializable {

	private int termCount;
	private int vocabSize;
	private int[] map;

	public int termCount() {
		return termCount;
	}

	public int vocabSize() {
		return vocabSize;
	}

	public UnigramDictionary(int numToken) {
		termCount = 0;
		map = new int[numToken];
		for (int i = 0; i < numToken; i++){ map[i] = 0;}
	}

	public void add(int tokenId) {
		termCount++;
		if (map[tokenId]  ==  0) {vocabSize++;}
		map[tokenId] = map[tokenId] + 1;
	}

	public int count(String term, WordDictionary wordToid) {
		int id = wordToid.getMap().get(term);
		if ( id < 0 || id > vocabSize){ return 0;}
		return map[id];
	}
	
}
