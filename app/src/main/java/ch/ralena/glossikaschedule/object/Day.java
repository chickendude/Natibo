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
	private int patternIndex;

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
			patternIndex = 0;
		});
	}

	public SentencePair getCurrentSentencePair() {
		if (curSentenceSetId >= sentenceSets.size())
			return null;
		SentenceSet sentenceSet = sentenceSets.get(curSentenceSetId);
		SentencePair sentencePair = sentenceSet.getSentences().get(curSentenceId);
		return sentencePair;
	}

	public Sentence getCurrentSentence() {
		if (curSentenceSetId >= sentenceSets.size())
			return null;
		SentenceSet sentenceSet = sentenceSets.get(curSentenceSetId);
		SentencePair sentencePair = sentenceSet.getSentences().get(curSentenceId);
		Sentence sentence;
		if (sentenceSet.getOrder().charAt(patternIndex) == 'B')
			sentence = sentencePair.getBaseSentence();
		else
			sentence = sentencePair.getTargetSentence();
		return sentence;
	}

	public void nextSentence(Realm realm) {
		realm.executeTransaction(r -> {
			patternIndex++;
			patternIndex %= sentenceSets.get(curSentenceSetId).getOrder().length();
		});
		if (patternIndex == 0) {
			goToNextSentencePair(realm);
		}
	}

	public void goToNextSentencePair(Realm realm) {
		realm.executeTransaction(r -> {
			patternIndex = 0;
			curSentenceId++;
			curSentenceId %= sentenceSets.get(curSentenceSetId).getSentences().size();
			if (curSentenceId == 0) {
				curSentenceSetId++;
			}
		});
	}

	public void goToPreviousSentencePair(Realm realm) {
		realm.executeTransaction(r -> {
			patternIndex = 0;
			curSentenceId--;
			if (curSentenceId < 0) {
				if (curSentenceSetId > 0) {
					curSentenceId = sentenceSets.get(--curSentenceSetId).getSentences().size();
				} else {
					curSentenceId = 0;
				}
			}
		});
	}
}
