package ch.ralena.natibo.data.room.object;

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
	private RealmList<SentenceGroup> sentences = new RealmList<>(); // the shuffled set of sentences
	private RealmList<SentenceGroup> sentenceSet; // the set of unique sentences to be learned
	private RealmList<Integer> reviews;    // number of reviews per sentence per day, eg. [6, 4, 3, 2]
	private String order;          // order to play sentences, eg. base + target + target, base + target, etc.

	public String getId() {
		return id;
	}

	public RealmList<SentenceGroup> getSentenceGroups() {
		return sentences;
	}

	public RealmList<SentenceGroup> getSentenceSet() {
		return sentenceSet;
	}

	public void setSentences(RealmList<SentenceGroup> sentences) {
		this.sentences = sentences;
	}

	public void setSentenceSet(RealmList<SentenceGroup> sentenceSet) {
		this.sentenceSet = sentenceSet;
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

		realm.executeTransaction((Realm r) -> {
			sentences.clear();

			// get the number of reviews and remove it from the list
			int numReviews = reviews.get(0);
			reviews.remove(0);

			// don't shuffle first repetition if it is the first time seeing sentences
			if (isFirstDay) {
				numReviews--;
				sentences.addAll(sentenceSet);
				isFirstDay = false;
			}

			// shuffle sentences
			ArrayList<SentenceGroup> sentenceGroups = new ArrayList<>();
			for (int i = 0; i < numReviews; i++) {
				for (int j = 0; j < sentenceSet.size(); j++) {
					SentenceGroup sentenceGroup = sentenceSet.get(j);
					sentenceGroups.add(sentenceGroup);
				}
			}
			Collections.shuffle(sentenceGroups);

			// if there is more than one sentence, try to avoid duplicates being placed consecutively (so you don't get the same sentence twice in a row)
			// todo freezes here
			if (sentenceSet.size() > 1) {
				for (int i = 0; i < sentenceGroups.size(); i++) {
					int size = sentenceGroups.size();
					Random rand = new Random();
					if (i + 1 < size && sentenceGroups.get(i).getSentences().get(0).getIndex() == sentenceGroups.get(i + 1).getSentences().get(0).getIndex()) {
						int swap = rand.nextInt(size);
						Collections.swap(sentenceGroups, i, swap);
						i = -1;
					}
				}
			}

			// combine all the sentences, first day reviews (if any) and shuffled reviews
			sentences.addAll(sentenceGroups);
		});
		return true;
	}

	@Override
	public String toString() {
		return "SentenceSet{" +
				"id='" + id + '\'' +
				", isFirstDay=" + isFirstDay +
				", sentences=" + sentences +
				", sentenceSet=" + sentenceSet +
				", reviews=" + reviews +
				", order='" + order + '\'' +
				'}';
	}
}
