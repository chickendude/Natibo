package ch.ralena.natibo.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class SentenceGroup extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private RealmList<Sentence> sentences;
	private RealmList<Language> languages;

	public String getId() {
		return id;
	}

	public RealmList<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(RealmList<Sentence> sentences) {
		this.sentences = sentences;
	}

	public RealmList<Language> getLanguages() {
		return languages;
	}

	public void setLanguages(RealmList<Language> languages) {
		this.languages = languages;
	}
}
