package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class SentenceSet extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private RealmList<Sentence> baseSentences;
	private RealmList<Sentence> targetSentences;
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

	public RealmList<Integer> getReviews() {
		return reviews;
	}

	public void setReviews(RealmList<Integer> reviews) {
		this.reviews = reviews;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
}
