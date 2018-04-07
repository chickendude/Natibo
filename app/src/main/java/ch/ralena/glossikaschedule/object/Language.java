package ch.ralena.glossikaschedule.object;

import ch.ralena.glossikaschedule.data.LanguageData;
import ch.ralena.glossikaschedule.data.LanguageType;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Language extends RealmObject {
	@PrimaryKey
	@Index
	private String languageId;

	private String longName;

	RealmList<Pack> packs;

	public String getLanguageId() {
		return languageId;
	}

	public RealmList<Pack> getPacks() {
		return packs;
	}

	public Pack getPack(String book) {
		for (Pack pack : packs) {
			if (pack.getBook().equals(book)) {
				return pack;
			}
		}
		return null;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public LanguageType getLanguageType() {
		LanguageType languageType = null;
		for (LanguageType language : LanguageData.languages) {
			if (this.longName.equals(language.getName())) {
				return language;
			}
		}
		return languageType;
	}

	public int getSentenceCount() {
		int numSentences = 0;
		for (Pack pack : packs) {
			numSentences += pack.getSentences().size();
		}
		return numSentences;
	}
}
