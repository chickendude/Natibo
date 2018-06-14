package ch.ralena.natibo.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class SentenceSet extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private boolean isFirstDay;
	private RealmList<Sentence> baseSentences;
	private RealmList<Sentence> targetSentences;
	private RealmList<SentenceGroup> sentences;
	private RealmList<Integer> reviews;    // number of reviews per sentence per day, eg. [6, 4, 3, 2]
	private String order;          // order to play sentences, eg. base + target + target, base + target, etc.

	public String getId() {
		return id;
	}

	public RealmList<Sentence> getBaseSentences() {
		return baseSentences;
	}

	public void setBaseSentences(RealmList<Sentence> baseSentences) {
		this.baseSentences = baseSentences;
	}

	public RealmList<Sentence> getTargetSentences() {
		return targetSentences;
	}

	public void setTargetSentences(RealmList<Sentence> targetSentences) {
		this.targetSentences = targetSentences;
	}

	public RealmList<SentenceGroup> getSentenceGroups() {
		return sentences;
	}

	public void setSentences(RealmList<SentenceGroup> sentences) {
		this.sentences = sentences;
	}

	public RealmList<Integer> getReviews() {
		return reviews;
	}

	public void setReviews(RealmList<Integer> reviews) {
		this.reviews = reviews;
	}

	public boolean isFirstDay() {
		return isFirstDay;
	}

	public void setFirstDay(boolean firstDay) {
		isFirstDay = firstDay;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public boolean buildSentences(Realm realm) {
		// check if there are any reviews left, if not return false
		if (reviews.size() == 0)
			return false;

		realm.executeTransaction(r -> {
			sentences.clear();

			// get the number of reviews and remove it from the list
			int numReviews = reviews.get(0);
			reviews.remove(0);

			// don't shuffle first repetition if it is the first time seeing sentences
			if (isFirstDay) {
				numReviews--;
				for (int i = 0; i < targetSentences.size(); i++) {
					SentenceGroup sentenceGroup = new SentenceGroup();
					sentenceGroup.setBaseSentence(baseSentences.get(i));
					sentenceGroup.setTargetSentence(targetSentences.get(i));
					sentences.add(sentenceGroup);
				}
				isFirstDay = false;
			}

			// shuffle sentences
			ArrayList<SentenceGroup> sentencePairs = new ArrayList<>();
			for (int i = 0; i < numReviews; i++) {
				for (int j = 0; j < targetSentences.size(); j++) {
					SentenceGroup sentenceGroup = new SentenceGroup();
					sentenceGroup.setBaseSentence(baseSentences.get(j));
					sentenceGroup.setTargetSentence(targetSentences.get(j));
					sentencePairs.add(sentenceGroup);
				}
			}
			Collections.shuffle(sentencePairs);

			// if there is more than one sentence, try to avoid duplicates being placed consecutively (so you don't get the same sentence twice in a row)
			if (baseSentences.size() > 1) {
				for (int i = 0; i < sentencePairs.size(); i++) {
					int size = sentencePairs.size();
					Random rand = new Random();
					if (i + 1 < size && sentencePairs.get(i).getBaseSentence().getIndex() == sentencePairs.get(i + 1).getBaseSentence().getIndex()) {
						int swap = rand.nextInt(size);
						Collections.swap(sentencePairs, i, swap);
						i = -1;
					}
				}
			}

			// combine all the sentences, first day reviews (if any) and shuffled reviews
			sentences.addAll(sentencePairs);
		});
		return true;
	}
}
