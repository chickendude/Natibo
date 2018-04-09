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

	private Sentence base;
	private Sentence target;
	private RealmList<Integer> reviews;    // number of reviews per sentence per day, eg. [6, 4, 3, 2]
	private String order;          // order to play sentences, eg. base + target + target, base + target, etc.

	public String getId() {
		return id;
	}

	public Sentence getBase() {
		return base;
	}

	public void setBase(Sentence base) {
		this.base = base;
	}

	public Sentence getTarget() {
		return target;
	}

	public void setTarget(Sentence target) {
		this.target = target;
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
