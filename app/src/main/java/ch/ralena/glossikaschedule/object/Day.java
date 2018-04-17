package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Day extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private RealmList<SentenceSet> sentenceSets;
	private boolean isCompleted;

	// internal fields
	private int curSentenceSetId;
	private int curSentenceId;

	public String getId() {
		return id;
	}

	public RealmList<SentenceSet> getSentenceSets() {
		return sentenceSets;
	}

	public void setSentenceSets(RealmList<SentenceSet> sentenceSets) {
		this.sentenceSets = sentenceSets;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean completed) {
		isCompleted = completed;
	}

	public void resetReviews(Realm realm) {
		realm.executeTransaction(r -> {
			curSentenceId = 0;
			curSentenceSetId = 0;
		});
	}

	public SentencePair getNextSentencePair(Realm realm) {
		if (curSentenceSetId >= sentenceSets.size())
			return null;
		SentenceSet sentenceSet = sentenceSets.get(curSentenceSetId);
		SentencePair sentencePair = sentenceSet.getSentences().get(curSentenceId);
		realm.executeTransaction(r -> {
			curSentenceId++;
			curSentenceId %= sentenceSet.getSentences().size() - 1;
			if (curSentenceId == 0) {
				curSentenceSetId++;
			}
		});
		return sentencePair;
	}
}
