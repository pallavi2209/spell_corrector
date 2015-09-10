package edu.stanford.cs276.util;

import java.io.Serializable;
import java.util.HashMap;

public class TrigramDictionary implements Serializable {

	private int termCount;
	private HashMap<Integer,HashMap<Long, Integer>> map;

	public int termCount() {
		return termCount;
	}

	public TrigramDictionary() {
		termCount = 0;
		map = new HashMap<Integer,HashMap<Long, Integer>>();
	}

	public void add(int term1, long term23) {

		termCount++;
		if (map.containsKey(term1)) {
			HashMap<Long,Integer> tmap = map.get(term1);
			if ( tmap.containsKey(term23) ){
				int tval = tmap.get(term23);
				tmap.put(term23, tval + 1 );
			}else{
				tmap.put(term23, 1 );
			}
			map.put(term1, tmap);
		} else {
			HashMap<Long,Integer> tmap = new HashMap<Long, Integer>();
			tmap.put(term23, 1);
			map.put(term1, tmap);
		}
	}

	public int count(String term1, String term23, WordDictionary wordToId) {
		int t1Id = wordToId.getId(term1);
		if (t1Id>-1 && map.containsKey(t1Id)) {
			HashMap<Long, Integer> bigramMap = map.get(t1Id);
			
			String[] bigramTerms  = term23.split(" ");
			String t1 = bigramTerms[0];
			String t2 = bigramTerms[1];
			int tId1 = wordToId.getId(t1);
			int tId2 = wordToId.getId(t2);
			int vocabSize = wordToId.getMap().size();
			long bId;
			if ( tId1 < 0 || tId1 > vocabSize || tId2 < 0 || tId2 > vocabSize){ return 0;}
				bId = (long)tId1+ (long)wordToId.getMap().size()*(long)tId2;
			
			if (bigramMap.containsKey(bId)) {
				return map.get(t1Id).get(bId);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}


	public HashMap<Integer,HashMap<Long, Integer>> getMap(){
		return map;
	}
}
