package ch.ralena.natibo.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Schedule extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private int numSentences;
	private int sentenceIndex;		// which sentence we are at, e.g. sentence 1, sentence 51, sentence 2,041, etc.
	private String order;			// order in which to play sentences (e.g. base, target, target)
	private RealmList<Integer> reviewPattern = new RealmList<>();

	public int getNumSentences() {
		return numSentences;
	}

	public void setNumSentences(int numSentences) {
		this.numSentences = numSentences;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

	public RealmList<Integer> getReviewPattern() {
		return reviewPattern;
	}

	public void setReviewPattern(RealmList<Integer> reviewPattern) {
		this.reviewPattern = reviewPattern;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
}
