##Spell correction for a query


If a user types in a (possibly corrupted) query R, we want to find the query Q that
the user intended to type in. To do this, we used probabilities to find the
most likely query that the user meant to enter.
The problem becomes finding the query that maximizes the conditional probability
P(Q|R), the probability that the user meant to search for Q when enterring R.

To spell correct an input query R, we choose a correction Q, such that we maximize P(Q | R) ∽ P(R | Q) * P(Q)^μ. The P(R | Q)  term is determined by the noisy channel model, while the P(Q) comes from the language model. 

###System Design:
The program is split into two phases: model building and correction. Building the models and then using them to rank candidate queries is expensive in both time and space, so we have some techniques to improve efficiency.

1. Language Model: In LM, we read data corpus and create the following dictionaries:
  1. Word Dictionary: Mapping of token -> tokenID
  2. Unigram Dictionary: Integer array of (Size of Word Dictionary).
  arrayIndex = tokenID, arrayValue = count of token.
  3. Bigram Dictionary: BigramID = idTerm1 + (Size of Word Dictionary)* idTerm2.
  		BigramMap = Mapping of BigramID -> Bigram count 
  4. Trigram Dictionary (used for extra credit):
  		BigramID = idTerm2 + (Size of Word Dictionary)* idTerm3.
		BigramMap = Mapping of BigramID -> Bigram count
		TrigramMap = idTerm1 -> BigramMap.
Storing our dictionary in this way, as opposed to with Strings as the keys of each dictionary allows us to conserve memory. This is crucial when building the trigram dictionary, which would not be possible to do with storing dictionaries of strings. 


2. Noisy Channel Models: Uniform / Empirical
  1. Uniform: Assigns same probability to each individual edit. No significant design features to discuss.
  2. Empirical: Learns probabilities based on counts of edits seen in training data. Counts stored in dictionaries. With these dictionaries, we can quickly calculate probabilities once we determine the edits that occurred. To determine these edits, we utilize the standard recursive approach to minimum edit distance calculation with one modification: the algorithm is provided the edit distance so it will not explore edit paths longer than this distance. This allows for faster training and scoring of queries.


###Candidate Generation:

We are generating candidates with for each query to be spell corrected with two level of optimizations:
1. Pruning All-one-edits (explained below at 2.)
2. b) Generating Quick-1 Edits (explained below at 3.)

Explained below is the sequence of candidate generation:

1. Exhaustive 1-Edits → 1-Edit-Candidates:
We perform edit operations on each character of input query and create a set of All-one-edits. For each candidate in All-one-edits: If all words of the candidate is present in dictionary, add to the set of valid 1-Edit-Candidates.

2. Pruning Exhaustive 1-Edits:
Next, we create a set of Pruned-1Edits from All-one-edits such that the number of misspelled words in the candidates should be either 1 or 2(the misspelled words should be adjacent in this case). This strategy avoids the candidates with more than 2 edit distance away from the query and hence, reduces our search space for generation of 2-Edit-Candidates.

3. Quick-1-Edits → 2-Edit-Candidates:
For each candidate in Pruned-1Edits, we find the misspelled words and store the index of characters where 1-edit have to be performed for possible candidates. Example:

Query: We will meet agan later		Stored indices: [ agan ] -> 12, 13,14, 15, 16, 17
Query: We will meeta gain later		Stored indices: [ ] -> 12

So, we will perform 2nd edit operations only on the stored indices of the candidates instead of all characters to create a set of possible 2-Edit-Candidates. Next, for each candidate in Quick-1-Edits: If all words of the candidate is present in dictionary, add to the set of valid 2-Edit-Candidates.
