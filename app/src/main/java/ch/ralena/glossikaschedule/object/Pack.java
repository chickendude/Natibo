package ch.ralena.glossikaschedule.object;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Pack extends RealmObject {
	@PrimaryKey
	@Index
	private String id;

	private String book;

	RealmList<Sentence> sentences;

	public String getId() {
		return id;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public RealmList<Sentence> getSentences() {
		return sentences;
	}

	public Sentence getSentenceWithIndex(int index) {
		for (Sentence sentence : sentences) {
			if (sentence.getIndex() == index)
				return sentence;
		}
		return null;
	}
}
