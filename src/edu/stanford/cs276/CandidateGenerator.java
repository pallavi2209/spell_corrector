package edu.stanford.cs276;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CandidateGenerator implements Serializable {

//	Map<String, Integer> unigramDictionary = LanguageModel.getLanguageModel().unigramDict
//			.getMap();
	Map<String, Integer> wordDict = LanguageModel.getLanguageModel().wordToId.getMap();
		
	private static CandidateGenerator cg_;

	// Don't use the constructor since this is a Singleton instance
	private CandidateGenerator() {
	}

	public static CandidateGenerator get() throws Exception {
		if (cg_ == null) {
			cg_ = new CandidateGenerator();
		}
		return cg_;
	}

	public static final Character[] alphabet = { 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', ' ', ',', '\'' };

	public Set<String> getOneEditCandidates(Set<String> allOneEdits,
			Set<String> candidates) {
		for (String string : allOneEdits) {
			if (checkUnigramsInDict(string)) {
				candidates.add(string);
			}
		}
		return candidates;
	}

	public Set<String> getTwoEditCandidates(Set<String> oneEdits,
			Set<String> candidates) throws Exception {
		for (String string : oneEdits) {
			candidates.addAll(computeQuickOneEdits(string));
		}
		return candidates;
	}

	public void computeExhaustiveOneEdits(String query,
			Set<String> allCandidates) {
		// Deletion
		for (int idx = 0; idx < query.length(); ++idx)
			allCandidates.add(query.substring(0, idx)
					+ query.substring(idx + 1));

		// adjacent
		for (int idx = 0; idx < query.length() - 1; ++idx)
			allCandidates.add(query.substring(0, idx)
					+ query.substring(idx + 1, idx + 2)
					+ query.substring(idx, idx + 1) + query.substring(idx + 2));

		// replacements
		for (int idx = 0; idx < query.length(); ++idx) {
			for (int iAlpha = 0; iAlpha < alphabet.length; iAlpha++) {
				char character = alphabet[iAlpha].charValue();
				allCandidates.add(query.substring(0, idx)
						+ String.valueOf(character) + query.substring(idx + 1));
			}
		}

		// insertions
		for (int idx = 0; idx <= query.length(); ++idx) {
			for (int iAlpha = 0; iAlpha < alphabet.length; iAlpha++) {
				char character = alphabet[iAlpha].charValue();
				allCandidates.add(query.substring(0, idx)
						+ String.valueOf(character) + query.substring(idx));
			}
		}
	}

	public Set<String> computeQuickOneEdits(String query) {
		Set<String> candidates = new HashSet<String>();
		List<String> termsMisspelled = new ArrayList<String>();
		List<Integer> indexToConsiderInQuery = new ArrayList<Integer>();

		int beginIdx = 0, endIdx = query.indexOf(' ');
		if (endIdx == -1) {
			// handle single term query
			String term = query;
			if (!wordDict.containsKey(term)) {
				termsMisspelled.add(term);
				for (int idx = beginIdx; idx < term.length(); idx++) {
					indexToConsiderInQuery.add(idx);
				}
			}
		} else {
			int lastEndIndex = -1;
			while ((endIdx > -1) && (termsMisspelled.size() < 2)) {
				String term = query.substring(beginIdx, endIdx);
				if (!wordDict.containsKey(term)) {
					termsMisspelled.add(term);
					if (termsMisspelled.size() == 1) {
						if (!(beginIdx == 0)) {
							indexToConsiderInQuery.add(beginIdx - 1);
						}
						for (int i = beginIdx; i <= (beginIdx + term.length()); i++) {
							indexToConsiderInQuery.add(i);
						}
					} else if ((termsMisspelled.size() == 2)
							&& (beginIdx == (lastEndIndex + 1))) {
						List<Integer> indexToConsiderInQuery2 = new ArrayList<Integer>();
						indexToConsiderInQuery2.add(lastEndIndex);
						indexToConsiderInQuery = indexToConsiderInQuery2;
					}
				}
				lastEndIndex = endIdx;
				beginIdx = endIdx + 1;
				endIdx = query.indexOf(' ', beginIdx);

				if (endIdx == -1) { // no more spaces --> meaning last word
					term = query.substring(beginIdx);
					if (termsMisspelled.size() < 2) {
						if (!wordDict.containsKey(term)) {
							termsMisspelled.add(term);
							if (termsMisspelled.size() == 1) {
								if (!(beginIdx == 0)) {
									indexToConsiderInQuery.add(beginIdx - 1);
								}
								for (int i = beginIdx; i < (beginIdx + term
										.length()); i++) {
									indexToConsiderInQuery.add(i);
								}
							} else if ((termsMisspelled.size() == 2)
									&& (beginIdx == (lastEndIndex + 1))) {
								List<Integer> indexToConsiderInQuery2 = new ArrayList<Integer>();
								indexToConsiderInQuery2.add(lastEndIndex);
								indexToConsiderInQuery = indexToConsiderInQuery2;
							}
						}
					}
				}
			}
		}

		if (termsMisspelled.size() == 0) {
			Set<String> oneEdits = new HashSet<String>();
			computeExhaustiveOneEdits(query, oneEdits);
			for (String string : oneEdits) {
				if (checkUnigramsInDict(string)) {
					candidates.add(string);
				}
			}
		} else {
			checkEditsGetCandidates(query, candidates, indexToConsiderInQuery);
		}
		return candidates;

	}

	private void checkEditsGetCandidates(String query, Set<String> candidates,
			List<Integer> indexInQuery) {
		for (Integer iQuery : indexInQuery) {
			// check transposition for previous and next character
			String strStartDel = query.substring(0, iQuery);
			String strEndDel = "";

			String strStartIns = query.substring(0, iQuery);
			String strEndIns = query.substring(iQuery);

			String strStartSubs = query.substring(0, iQuery);
			String strEndSubs = "";

			String strStartTransPrev = "";
			String strMidTransPrev = "";
			String strEndTransPrev = "";

			String strStartTransNext = query.substring(0, iQuery);
			String strMidTransNext = "";
			String strEndTransNext = "";

			if (iQuery == 0) {
				strEndDel = query.substring(iQuery + 1);
				strEndSubs = query.substring(iQuery + 1);
				strEndTransPrev = query.substring(iQuery + 1);
				strMidTransNext = String.valueOf(query.charAt(iQuery + 1));
				if (!(iQuery == query.length() - 2)) {
					strEndTransNext = query.substring(iQuery + 2);
				}
			} else if (iQuery == query.length() - 1) {
				strStartTransPrev = query.substring(0, iQuery - 1);
				strMidTransPrev = String.valueOf(query.charAt(iQuery - 1));
			} else {
				strEndDel = query.substring(iQuery + 1);
				strEndSubs = query.substring(iQuery + 1);

				strStartTransPrev = query.substring(0, iQuery - 1);
				strMidTransPrev = String.valueOf(query.charAt(iQuery - 1));
				strEndTransPrev = query.substring(iQuery + 1);

				strMidTransNext = String.valueOf(query.charAt(iQuery + 1));
				if (!(iQuery == query.length() - 2)) {
					strEndTransNext = query.substring(iQuery + 2);
				}
			}

			// check deletion
			String delCandidate = strStartDel + strEndDel;
			if (checkUnigramsInDict(delCandidate)) {
				candidates.add(delCandidate);
			}

			// check insertion
			for (int iAlpha = 0; iAlpha < alphabet.length; iAlpha++) {
				char character = alphabet[iAlpha].charValue();
				String insertCandidate = strStartIns
						+ String.valueOf(character) + strEndIns;

				if (checkUnigramsInDict(insertCandidate)) {
					candidates.add(insertCandidate);
				}
			}

			// check substitution
			for (int iAlpha = 0; iAlpha < alphabet.length; iAlpha++) {
				char character = alphabet[iAlpha].charValue();
				String insertCandidate = strStartSubs
						+ String.valueOf(character) + strEndSubs;

				if (checkUnigramsInDict(insertCandidate)) {
					candidates.add(insertCandidate);
				}
			}

			// transposition
			String transCandidatePrev = strStartTransPrev
					+ String.valueOf(query.charAt(iQuery)) + strMidTransPrev
					+ strEndTransPrev;
			if (checkUnigramsInDict(transCandidatePrev)) {
				candidates.add(transCandidatePrev);
			}
			String transCandidateNext = strStartTransNext + strMidTransNext
					+ String.valueOf(query.charAt(iQuery)) + strEndTransNext;

			if (checkUnigramsInDict(transCandidateNext)) {
				candidates.add(transCandidateNext);
			}
		}
	}

	boolean checkUnigramsInDict(String candidate) {
		boolean candidatePresentInDict = false;
		String[] termsDelCandidate = candidate.split(" ");

		// check if all unigrams of this candidate are present in our unigram
		// dictionary, then add to candidates
		for (int j = 0; j < termsDelCandidate.length; j++) {
			String currentTermOfQuery = termsDelCandidate[j];
			if (!wordDict.containsKey(currentTermOfQuery)) {
				break;
			}
			if(j == termsDelCandidate.length-1){
				candidatePresentInDict = true;
			}
		}
		return candidatePresentInDict;
	}

	public Set<String> getPrunedOneEditCandidates(Set<String> allOneEdits,
			Set<String> candidates) {
		for (String string : allOneEdits) {
			String[] strTerms = string.split(" ");
			List<Integer> idxMisspelledTerms = new ArrayList<Integer>();
			for (int idxCurrentTerm = 0; idxCurrentTerm < strTerms.length; idxCurrentTerm++) {
				String term = strTerms[idxCurrentTerm];
				if (!(wordDict.containsKey(term))) {
					idxMisspelledTerms.add(idxCurrentTerm);
				}
			}
			if (idxMisspelledTerms.size() <= 1) {
				candidates.add(string);
			} else if ((idxMisspelledTerms.size() == 2)) {
				int idxFirstMisTerm = idxMisspelledTerms.get(0);
				int idxSecondMisTerm = idxMisspelledTerms.get(1);
				if (idxSecondMisTerm == (idxFirstMisTerm + 1)) {
					candidates.add(string);
				}
			}
		}
		return candidates;
	}

}
