package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class SentencePair extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private Sentence baseSentence;
	private Sentence targetSentence;

	public String getId() {
		return id;
	}

	public Sentence getBaseSentence() {
		return baseSentence;
	}

	public void setBaseSentence(Sentence baseSentences) {
		this.baseSentence = baseSentences;
	}

	public Sentence getTargetSentence() {
		return targetSentence;
	}

	public void setTargetSentence(Sentence targetSentence) {
		this.targetSentence = targetSentence;
	}
}
