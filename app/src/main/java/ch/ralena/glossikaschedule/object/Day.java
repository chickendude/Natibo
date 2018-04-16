package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Day extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	RealmList<SentenceSet> sentenceSets;
	boolean isCompleted;

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
}
