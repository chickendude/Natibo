package ch.ralena.natibo.object;

import java.util.UUID;

import io.realm.Realm;
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

	public void createSentenceOrUpdate(Realm realm, int index, String sentence, String ipa, String romanization, String uri) {
		realm.executeTransaction(r -> {
			Sentence newSentence = getSentenceWithIndex(index);
			if (newSentence == null) {
				newSentence = r.createObject(Sentence.class, UUID.randomUUID().toString());
				sentences.add(newSentence);
			}
			newSentence.setIndex(index);
			if (sentence != null)
				newSentence.setText(sentence);
			if (ipa != null)
				newSentence.setIpa(ipa);
			if (romanization != null)
				newSentence.setRomanization(romanization);
			if (uri != null)
				newSentence.setUri(uri);
		});
	}
}

