package edu.stanford.cs276.util;

import java.io.Serializable;
import java.util.HashMap;

public class WordDictionary implements Serializable {

	private HashMap<String, Integer> map;

	public WordDictionary() {
		map = new HashMap<String, Integer>();
	}

	public void add(String term) {
		if (map.containsKey(term)) {
			map.put(term, map.get(term) + 1);
		} else {
			map.put(term, 1);
		}
	}

	public int getId(String term) {

		if (map.containsKey(term)) {
			return map.get(term);
		} else {
			return -1;
		}
	}


	public HashMap<String, Integer> getMap(){
		return map;
	}
}
