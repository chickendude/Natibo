package ch.ralena.glossikaschedule.object;

import android.media.MediaMetadataRetriever;

import java.util.ArrayList;
import java.util.List;
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
	private int pauseMillis;

	// internal fields
	private int curSentenceSetId;
	private int curSentenceId;
	private int patternIndex;

	// --- getters and setters ---

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

	public int getPauseMillis() {
		return pauseMillis;
	}

	public void setPauseMillis(int pauseMillis) {
		this.pauseMillis = pauseMillis;
	}

	// --- helper methods ---

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
		SentencePair sentencePair = sentenceSet.getSentencePairs().get(curSentenceId);
		return sentencePair;
	}

	public Sentence getCurrentSentence() {
		if (curSentenceSetId >= sentenceSets.size())
			return null;
		SentenceSet sentenceSet = sentenceSets.get(curSentenceSetId);
		SentencePair sentencePair = sentenceSet.getSentencePairs().get(curSentenceId);
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
			curSentenceId %= sentenceSets.get(curSentenceSetId).getSentencePairs().size();
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
					curSentenceId = sentenceSets.get(--curSentenceSetId).getSentencePairs().size();
				} else {
					curSentenceId = 0;
				}
			}
		});
	}

	private List<Sentence> getRemainingSentences() {
		List<Sentence> sentences = new ArrayList<>();
		for (int i = curSentenceSetId; i < sentenceSets.size(); i++) {
			SentenceSet sentenceSet = sentenceSets.get(i);

			List<SentencePair> tempSentencePairs = new ArrayList<>();
			RealmList<SentencePair> setSentencePairs = sentenceSet.getSentencePairs();

			String order = sentenceSet.getOrder();

			// only get subsection of SentencePairs if it's the first sentence set
			if (i == curSentenceSetId) {
				tempSentencePairs.addAll(setSentencePairs.subList(curSentenceId, setSentencePairs.size()));

				// first sentence pair will possible have fewer sentences depending on whether the base sentence has been played or not
				for (int j = patternIndex; j < order.length(); j++) {
					if (order.charAt(j) == 'B')
						sentences.add(tempSentencePairs.get(0).getBaseSentence());
					else if (order.charAt(j) == 'T')
						sentences.add(tempSentencePairs.get(0).getTargetSentence());
				}
				tempSentencePairs.remove(0);
			} else {
				tempSentencePairs.addAll(setSentencePairs);
			}

			// add all sentences according to the order to the list of sentences
			for (SentencePair sentencePair : tempSentencePairs) {
				for (char c : order.toCharArray()) {
					if (c == 'B')
						sentences.add(sentencePair.getBaseSentence());
					else if (c == 'T')
						sentences.add(sentencePair.getTargetSentence());
				}
			}
		}
		return sentences;
	}

	public int getNumReviewsLeft() {
		int numReviews = 0;
		for (int i = curSentenceSetId; i < sentenceSets.size(); i++) {
			numReviews += sentenceSets.get(i).getSentencePairs().size();
			if (i == curSentenceSetId)
				numReviews -= curSentenceId;
		}
		return numReviews;
	}

	public int getTimeLeft() {
		MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

		int millisecondsLeft = 0;
		for (Sentence sentence : getRemainingSentences()) {
			metadataRetriever.setDataSource(sentence.getUri());
			millisecondsLeft += Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) + pauseMillis;
		}

		return millisecondsLeft;
	}
}
