package ch.ralena.natibo.object;

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

	private RealmList<SentenceSet> sentenceSets = new RealmList<>();
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

	public SentenceGroup getCurrentSentenceGroup() {
		if (curSentenceSetId >= sentenceSets.size())
			return null;
		SentenceSet sentenceSet = sentenceSets.get(curSentenceSetId);
		return sentenceSet.getSentenceGroups().get(curSentenceId);
	}

	public Sentence getCurrentSentence() {
		if (curSentenceSetId >= sentenceSets.size())
			return null;
		SentenceSet sentenceSet = sentenceSets.get(curSentenceSetId);
		SentenceGroup sentenceGroup = sentenceSet.getSentenceGroups().get(curSentenceId);
		Sentence sentence;
		if (sentenceSet.getOrder().charAt(patternIndex) == 'B')
			sentence = sentenceGroup.getSentences().first();
		else
			sentence = sentenceGroup.getSentences().last();
		return sentence;
	}

	/**
	 * Moves to the next sentence.
	 *
	 * @param realm Realm object in case objects need to be updated.
	 * @return True if there is a next sentence, false if there are no more sentences.
	 */
	public boolean nextSentence(Realm realm) {
		if (curSentenceSetId >= sentenceSets.size())
			return false;
		realm.executeTransaction(r -> {
			patternIndex++;
			patternIndex %= sentenceSets.get(curSentenceSetId).getOrder().length();
		});
		if (patternIndex == 0) {
			goToNextSentencePair(realm);
		}
		return true;
	}

	public void goToNextSentencePair(Realm realm) {
		realm.executeTransaction(r -> {
			patternIndex = 0;
			curSentenceId++;
			curSentenceId %= sentenceSets.get(curSentenceSetId).getSentenceGroups().size();
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
					curSentenceId = sentenceSets.get(--curSentenceSetId).getSentenceGroups().size();
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

			List<SentenceGroup> tempSentenceGroups = new ArrayList<>();
			RealmList<SentenceGroup> setSentenceGroups = sentenceSet.getSentenceGroups();

			String order = sentenceSet.getOrder();

			// only get subsection of SentenceGroups if it's the first sentence set
			if (i == curSentenceSetId) {
				tempSentenceGroups.addAll(setSentenceGroups.subList(curSentenceId, setSentenceGroups.size()));

				// first sentence pair will possible have fewer sentences depending on whether the base sentence has been played or not
				for (int j = patternIndex; j < order.length(); j++) {
					if (order.charAt(j) == 'B')
						sentences.add(tempSentenceGroups.get(0).getSentences().first());
					else if (order.charAt(j) == 'T')
						sentences.add(tempSentenceGroups.get(0).getSentences().last());
				}
				tempSentenceGroups.remove(0);
			} else {
				tempSentenceGroups.addAll(setSentenceGroups);
			}

			// add all sentences according to the order to the list of sentences
			for (SentenceGroup sentencePair : tempSentenceGroups) {
				for (char c : order.toCharArray()) {
					if (c == 'B')
						sentences.add(sentencePair.getSentences().first());
					else if (c == 'T')
						sentences.add(sentencePair.getSentences().last());
				}
			}
		}
		return sentences;
	}

	public int getNumReviewsLeft() {
		int numReviews = 0;
		for (int i = curSentenceSetId; i < sentenceSets.size(); i++) {
			numReviews += sentenceSets.get(i).getSentenceGroups().size();
			if (i == curSentenceSetId)
				numReviews -= curSentenceId;
		}
		return numReviews;
	}
	public int getTotalReviews() {
		int totalReviews = 0;
		for (SentenceSet sentenceSet : sentenceSets) {
			totalReviews += sentenceSet.getSentenceGroups().size();
		}
		return totalReviews;
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
