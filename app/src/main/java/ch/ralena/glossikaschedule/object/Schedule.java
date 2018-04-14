package ch.ralena.glossikaschedule.object;

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
	private RealmList<Integer> reviewPattern = new RealmList<>();

	public int getNumSentences() {
		return numSentences;
	}

	public void setNumSentences(int numSentences) {
		this.numSentences = numSentences;
	}

	public RealmList<Integer> getReviewPattern() {
		return reviewPattern;
	}

	public void setReviewPattern(RealmList<Integer> reviewPattern) {
		this.reviewPattern = reviewPattern;
	}
}
